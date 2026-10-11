package com.example.groovemaster.data

import android.content.Context
import org.json.JSONArray

data class StreakState(
    val current: Int,
    val best: Int,
    val freezes: Int,
    val todayQualified: Boolean,
    val todayCount: Int = 0,
    val canRestore: Boolean = false
) {
    val answersNeededToday: Int
        get() = (StreakLogic.MIN_ANSWERS - todayCount).coerceAtLeast(0)
}

object StreakLogic {
    const val MIN_ANSWERS = 5      // a day qualifies with ≥5 logged answers (half a round)
    const val FREEZE_CAP = 2
    const val FREEZE_EVERY = 7     // +1 ❄️ each time the streak climbs to a new multiple of 7

    class Reconciled(val state: StreakState, val frozen: Set<Long>,
                     val freezes: Int, val lastEarn: Long, val best: Int)

    /** Days (as day-keys) with at least MIN_ANSWERS logged answers. */
    fun qualify(events: List<AnswerEvent>, dayOf: (Long) -> Long): Set<Long> {
        val counts = HashMap<Long, Int>()
        events.forEach { counts[dayOf(it.ts)] = (counts[dayOf(it.ts)] ?: 0) + 1 }
        return counts.filterValues { it >= MIN_ANSWERS }.keys
    }

    /** Read-only snapshot for display. Frozen days count as active. */
    fun compute(today: Long, prevDay: (Long) -> Long, qualified: Set<Long>,
                frozen: Set<Long>, freezes: Int, bestStored: Int): StreakState {
        val active = qualified + frozen
        fun chain(start: Long): Int {
            var k = start; var n = 0
            while (k in active) { n++; k = prevDay(k) }
            return n
        }
        val current = when {
            today in active -> chain(today)
            prevDay(today) in active -> chain(prevDay(today))   // yesterday counts until today ends
            else -> 0
        }
        val lastActive = active.maxOrNull()
        // RESTORE grace: streak is 0, we hold a token, and the only missed day was yesterday
        val canRestore = current == 0 && freezes > 0 &&
                lastActive != null && lastActive == prevDay(prevDay(today))
        return StreakState(current, bestStored, freezes, today in qualified, 0, canRestore)
    }

    /** Full daily reconciliation — pure so it's testable:
     *  1. AUTO-FREEZE: live streak missed yesterday → consume a token, mark yesterday frozen.
     *  2. COMPUTE current/best/restore-offer.
     *  3. EARN: streak hit a new multiple of FREEZE_EVERY today → +1 token (once per day, capped). */
    fun reconcile(today: Long, prevDay: (Long) -> Long, qualified: Set<Long>,
                  frozenIn: Set<Long>, freezesIn: Int, bestIn: Int,
                  lastEarnIn: Long): Reconciled {
        val yesterday = prevDay(today)
        var frozen = frozenIn
        var freezes = freezesIn

        val aliveEnteringYesterday = prevDay(yesterday) in qualified || prevDay(yesterday) in frozen
        if (yesterday !in qualified && yesterday !in frozen &&
            aliveEnteringYesterday && freezes > 0) {
            frozen = frozen + yesterday
            freezes -= 1
        }

        val base = compute(today, prevDay, qualified, frozen, freezes, bestIn)

        var lastEarn = lastEarnIn
        var fr = freezes
        if (base.current > 0 && base.current % FREEZE_EVERY == 0 && lastEarn != today) {
            fr = (freezes + 1).coerceAtMost(FREEZE_CAP)
            lastEarn = today
        }
        val best = maxOf(bestIn, base.current)
        return Reconciled(base.copy(freezes = fr, best = best), frozen, fr, lastEarn, best)
    }
}

object StreakStore {
    private const val FILE = "groove_streak"
    private fun p(c: Context) = c.getSharedPreferences(FILE, Context.MODE_PRIVATE)

    fun reconcile(context: Context, events: List<AnswerEvent>,
                  dayOf: (Long) -> Long, today: Long, prevDay: (Long) -> Long): StreakState {
        val qualified = StreakLogic.qualify(events, dayOf)
        val frozen = mutableSetOf<Long>()
        p(context).getString("frozen", null)?.let { raw ->
            val a = JSONArray(raw)
            for (i in 0 until a.length()) frozen.add(a.getLong(i))
        }
        val r = StreakLogic.reconcile(today, prevDay, qualified, frozen,
            p(context).getInt("freezes", StreakLogic.FREEZE_CAP),      // start stocked with 2
            p(context).getInt("best", 0),
            p(context).getLong("lastEarn", 0L))
        val arr = JSONArray(); r.frozen.forEach { arr.put(it) }
        p(context).edit().putString("frozen", arr.toString())
            .putInt("freezes", r.freezes).putInt("best", r.best)
            .putLong("lastEarn", r.lastEarn).apply()
        return r.state
    }

    /** Spend a token to retro-freeze yesterday (48-hour grace restore). */
    fun restore(context: Context, missedDay: Long) {
        val arr = JSONArray()
        p(context).getString("frozen", null)?.let { raw ->
            val a = JSONArray(raw)
            for (i in 0 until a.length()) arr.put(a.getLong(i))
        }
        arr.put(missedDay)
        p(context).edit().putString("frozen", arr.toString())
            .putInt("freezes", (p(context).getInt("freezes", 0) - 1).coerceAtLeast(0)).apply()
    }

    suspend fun homeLine(context: Context): String {
        val now = System.currentTimeMillis()
        val events = AppDb.get(context).answers().since(now - 365L * 86_400_000L)
        val s = reconcile(context, events, Stats::dayKeyOf, Stats.dayKeyOf(now), Stats::prevDayKey)
        return when {
            s.canRestore -> "🔥 Streak broke — restore it in 📊 Stats!"
            s.current > 0 && s.todayQualified -> "🔥 ${s.current}-day streak · ❄️×${s.freezes} · ✅ today locked in"
            s.current > 0 -> "🔥 ${s.current}-day streak · ${s.answersNeededToday} more answers today!"
            else -> "🔥 Answer ${StreakLogic.MIN_ANSWERS} today to start a streak!"
        }
    }
}
