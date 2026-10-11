package com.example.groovemaster.data

import android.content.Context
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.lifecycleScope
import androidx.room.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@Entity(tableName = "answers")
data class AnswerEvent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mode: String,
    val correct: Boolean,
    val ts: Long
)

data class ModeStat(val mode: String, val total: Int, val correct: Int)

/** One day-cell of the activity grid. */
data class GridCell(val count: Int, val correct: Int)
/** One mode-row: exactly `days` cells, oldest → today. */
data class GridRow(val mode: String, val cells: List<GridCell>) {
    val periodTotal: Int get() = cells.sumOf { it.count }
}
/** One bar of the per-day totals strip. */
data class DayTotal(val count: Int, val modes: Int)

@Dao
interface AnswerDao {
    @Insert suspend fun add(e: AnswerEvent)
    @Query("SELECT mode AS mode, COUNT(*) AS total, " +
           "SUM(CASE WHEN correct THEN 1 ELSE 0 END) AS correct " +
           "FROM answers GROUP BY mode ORDER BY total DESC")
    suspend fun perMode(): List<ModeStat>
    @Query("SELECT COUNT(*) FROM answers") suspend fun total(): Int
    @Query("SELECT COUNT(*) FROM answers WHERE correct = 1") suspend fun totalCorrect(): Int
    @Query("SELECT * FROM answers WHERE ts >= :since ORDER BY ts ASC")
    suspend fun since(since: Long): List<AnswerEvent>
    @Query("DELETE FROM answers") suspend fun clear()
}

@Database(entities = [AnswerEvent::class], version = 1, exportSchema = false)
abstract class AppDb : RoomDatabase() {
    abstract fun answers(): AnswerDao
    companion object {
        @Volatile private var inst: AppDb? = null
        fun get(ctx: Context): AppDb = inst ?: synchronized(this) {
            inst ?: Room.databaseBuilder(ctx.applicationContext, AppDb::class.java, "groovemaster.db")
                .fallbackToDestructiveMigration().build().also { inst = it }
        }
    }
}

object Stats {

    /** Fire-and-forget logging — safe from any Activity. */
    fun log(context: Context, mode: String, correct: Boolean) {
        (context as? LifecycleOwner)?.lifecycleScope?.launch {
            AppDb.get(context).answers().add(
                AnswerEvent(mode = mode, correct = correct, ts = System.currentTimeMillis()))
        }
    }

    // ================= pure, JVM-testable logic =================

    /** Local midnight N days before `now` (DST-safe, Calendar-based). */
    fun dayStart(daysAgo: Int, now: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = now
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        cal.add(Calendar.DAY_OF_YEAR, -daysAgo)
        return cal.timeInMillis
    }

    /** Local midnight of whatever day `ts` falls in (streak day-keys). */
    fun dayKeyOf(ts: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = ts
        cal.set(Calendar.HOUR_OF_DAY, 0); cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0); cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }

    /** The day-key one day before `key` (DST-safe). */
    fun prevDayKey(key: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = key
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return cal.timeInMillis
    }

    /** Column labels: weekday names for ≤7 days, day-of-month numbers beyond. */
    fun dayLabels(now: Long, days: Int = 7): List<String> {
        val fmt = SimpleDateFormat(if (days <= 7) "EEE" else "d", Locale.getDefault())
        return ((days - 1) downTo 0).map { fmt.format(Date(dayStart(it, now))) }
    }

    /** Human range for the header, e.g. "Feb 18 – Mar 19". */
    fun rangeLabel(now: Long, days: Int): String {
        val f = SimpleDateFormat("MMM d", Locale.getDefault())
        return "${f.format(Date(dayStart(days - 1, now)))} – ${f.format(Date(dayStart(0, now)))}"
    }

    /** Activity intensity bucket 0..4 (drives cell color). */
    fun intensityLevel(count: Int): Int = when {
        count <= 0 -> 0; count <= 2 -> 1; count <= 5 -> 2; count <= 9 -> 3; else -> 4
    }

    /** Bucket events into (mode × `days` columns), oldest → today, busiest rows first. */
    fun buildGrid(events: List<AnswerEvent>, now: Long, days: Int = 7): List<GridRow> {
        val horizon = dayStart(days - 1, now)
        val recent = events.filter { it.ts >= horizon }
        val starts = (0 until days).map { dayStart(days - 1 - it, now) }
        data class Acc(var c: Int = 0, var ok: Int = 0)
        val map = HashMap<String, Array<Acc>>()
        recent.forEach { e ->
            val idx = starts.indexOfLast { it <= e.ts }
            if (idx >= 0) {
                val arr = map.getOrPut(e.mode) { Array(days) { Acc() } }
                arr[idx].c++; if (e.correct) arr[idx].ok++
            }
        }
        return map.map { (m, arr) -> GridRow(m, arr.map { GridCell(it.c, it.ok) }) }
            .sortedByDescending { it.periodTotal }
    }

    /** Per-day totals across ALL modes — the strip's data. Oldest → today. */
    fun dayTotals(events: List<AnswerEvent>, now: Long, days: Int): List<DayTotal> {
        val starts = (0 until days).map { dayStart(days - 1 - it, now) }
        val horizon = starts.first()
        val counts = IntArray(days)
        val modeSets = Array(days) { HashSet<String>() }
        events.forEach { e ->
            if (e.ts >= horizon) {
                val idx = starts.indexOfLast { it <= e.ts }
                if (idx >= 0) { counts[idx]++; modeSets[idx].add(e.mode) }
            }
        }
        return (0 until days).map { DayTotal(counts[it], modeSets[it].size) }
    }
}
