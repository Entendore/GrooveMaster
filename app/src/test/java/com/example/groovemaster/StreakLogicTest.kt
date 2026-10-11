package com.example.groovemaster

import com.example.groovemaster.data.AnswerEvent
import com.example.groovemaster.data.StreakLogic
import org.junit.Assert.*
import org.junit.Test

class StreakLogicTest {

    private val DAY = 86_400_000L
    private val dayOf = { ts: Long -> ts / DAY }
    private val prevDay = { k: Long -> k - DAY }

    private fun events(day: Long, n: Int) =
        (1..n).map { AnswerEvent(mode = "m", correct = true, ts = day * DAY + it) }

    /** Build a qualified set through the REAL threshold logic. */
    private fun qualifiedOf(vararg days: Long): Set<Long> =
        StreakLogic.qualify(days.flatMap { events(it, StreakLogic.MIN_ANSWERS) }, dayOf)

    @Test fun `five answers qualify a day, four do not`() {
        val q = StreakLogic.qualify(events(3, 5) + events(4, 4), dayOf)
        assertTrue(3L in q); assertFalse(4L in q)
    }

    @Test fun `consecutive days chain and today extends`() {
        val q = qualifiedOf(2, 3, 4, 5, 6)
        val s = StreakLogic.compute(6 * DAY, prevDay, q, emptySet(), 1, 0)
        assertEquals(5, s.current)
        assertTrue(s.todayQualified)
    }

    @Test fun `yesterday still counts until today ends`() {
        val q = qualifiedOf(3, 4, 5)
        val s = StreakLogic.compute(6 * DAY, prevDay, q, emptySet(), 1, 0)
        assertEquals(3, s.current)
        assertFalse(s.todayQualified)
        assertEquals(StreakLogic.MIN_ANSWERS, s.answersNeededToday)
    }

    @Test fun `miss older than yesterday is gone - no restore offer`() {
        val q = qualifiedOf(1, 2, 3)          // last active day 3; today 6 (missed 4 AND 5)
        val s = StreakLogic.compute(6 * DAY, prevDay, q, emptySet(), 2, 0)
        assertEquals(0, s.current)
        assertFalse(s.canRestore)
    }

    @Test fun `missed exactly yesterday with a token offers restore`() {
        val q = qualifiedOf(1, 2, 3, 4)       // missed 5 only, today 6
        val s = StreakLogic.compute(6 * DAY, prevDay, q, emptySet(), 1, 0)
        assertEquals(0, s.current)
        assertTrue(s.canRestore)
    }

    @Test fun `restore offer needs a token`() {
        val q = qualifiedOf(1, 2, 3, 4)
        val s = StreakLogic.compute(6 * DAY, prevDay, q, emptySet(), 0, 0)
        assertFalse(s.canRestore)
    }

    @Test fun `frozen day keeps the chain alive`() {
        val q = qualifiedOf(1, 2, 4, 5)       // day 3 was missed → frozen
        val s = StreakLogic.compute(6 * DAY, prevDay, q, setOf(3), 0, 0)
        assertEquals(5, s.current)            // 5,4,3❄,2,1
    }

    @Test fun `auto-freeze consumes a token for a live streak`() {
        val q = qualifiedOf(1, 2)             // day 3 missed, today 4
        val r = StreakLogic.reconcile(4 * DAY, prevDay, q, emptySet(), 1, 0, 0)
        assertEquals(setOf(3L), r.frozen)
        assertEquals(0, r.freezes)
        assertEquals(3, r.state.current)      // chain 3❄,2,1 — corrected expectation
    }

    @Test fun `no token - streak just breaks`() {
        val q = qualifiedOf(1, 2)
        val r = StreakLogic.reconcile(4 * DAY, prevDay, q, emptySet(), 0, 0, 0)
        assertTrue(r.frozen.isEmpty())
        assertEquals(0, r.state.current)
    }

    @Test fun `auto-freeze never fires for an empty streak`() {
        val q = qualifiedOf(1)                // streak died long ago
        val r = StreakLogic.reconcile(6 * DAY, prevDay, q, emptySet(), 2, 0, 0)
        assertTrue(r.frozen.isEmpty())
        assertEquals(2, r.freezes)
    }

    @Test fun `earning a token at 7 days`() {
        val q = qualifiedOf(1, 2, 3, 4, 5, 6, 7)
        val r = StreakLogic.reconcile(7 * DAY, prevDay, q, emptySet(), 0, 0, 0)
        assertEquals(1, r.freezes)
        assertEquals(7 * DAY, r.lastEarn)
        assertEquals(7, r.best)
    }

    @Test fun `earning respects the cap`() {
        val q = qualifiedOf(1, 2, 3, 4, 5, 6, 7)
        val r = StreakLogic.reconcile(7 * DAY, prevDay, q, emptySet(),
            StreakLogic.FREEZE_CAP, 0, 0)
        assertEquals(StreakLogic.FREEZE_CAP, r.freezes)
    }

    @Test fun `earning fires once per day`() {
        val q = qualifiedOf(1, 2, 3, 4, 5, 6, 7)
        val again = StreakLogic.reconcile(7 * DAY, prevDay, q, emptySet(), 1, 0, 7 * DAY)
        assertEquals(1, again.freezes)
    }

    @Test fun `best never decreases`() {
        val q = qualifiedOf(5, 6)
        val r = StreakLogic.reconcile(6 * DAY, prevDay, q, emptySet(), 1, 9, 0)
        assertEquals(9, r.best)
    }
}
