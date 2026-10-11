package com.example.groovemaster

import com.example.groovemaster.data.AnswerEvent
import com.example.groovemaster.data.Stats
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

class StatsGridTest {

    private fun ev(mode: String, ts: Long, correct: Boolean) =
        AnswerEvent(mode = mode, correct = correct, ts = ts)

    @Test fun `dayStart zeroes the time-of-day`() {
        val now = System.currentTimeMillis()
        val cal = Calendar.getInstance()
        cal.timeInMillis = Stats.dayStart(0, now)
        assertEquals(0, cal.get(Calendar.HOUR_OF_DAY))
        assertEquals(0, cal.get(Calendar.MINUTE))
        assertEquals(0, cal.get(Calendar.SECOND))
        assertEquals(0, cal.get(Calendar.MILLISECOND))
    }

    @Test fun `stepping back one day is 23-25 hours (DST-safe)`() {
        val now = System.currentTimeMillis()
        val diff = Stats.dayStart(0, now) - Stats.dayStart(1, now)
        assertTrue(diff in 82_800_000L..90_000_000L)
    }

    @Test fun `events bucket into the correct day and mode (7-day)`() {
        val now = System.currentTimeMillis()
        val today = Stats.dayStart(0, now)
        val threeAgo = Stats.dayStart(3, now)
        val events = listOf(
            ev("scales", today + 3_600_000, true),
            ev("scales", today + 7_200_000, false),
            ev("grooves", threeAgo + 5_000, true))
        val grid = Stats.buildGrid(events, now)

        assertEquals(2, grid.size)                                   // busiest first
        val scales = grid.first { it.mode == "scales" }
        assertEquals(7, scales.cells.size)
        assertEquals(2, scales.cells[6].count)                       // today = last cell
        assertEquals(1, scales.cells[6].correct)
        assertEquals(0, scales.cells[5].count)                       // yesterday empty
        assertEquals(1, grid.first { it.mode == "grooves" }.cells[3].count)
        assertEquals(2, scales.periodTotal)
    }

    @Test fun `events older than 7 days are ignored`() {
        val now = System.currentTimeMillis()
        val grid = Stats.buildGrid(listOf(ev("chords", now - 9L * 86_400_000, true)), now)
        assertTrue(grid.isEmpty())
    }

    @Test fun `rows sort by weekly volume, busiest first`() {
        val now = System.currentTimeMillis()
        val today = Stats.dayStart(0, now)
        val grid = Stats.buildGrid(listOf(
            ev("raga", today, true),
            ev("scales", today, true), ev("scales", today + 1, false),
            ev("scales", today + 2, true)), now)
        assertEquals("scales", grid[0].mode)
        assertEquals(3, grid[0].periodTotal)
    }

    @Test fun `31-day grid buckets correctly and keeps column order`() {
        val now = System.currentTimeMillis()
        val tenAgo = Stats.dayStart(10, now)
        val events = listOf(ev("scales", tenAgo + 1000, true),
                            ev("scales", tenAgo + 5000, false),
                            ev("scales", now, true))
        val grid = Stats.buildGrid(events, now, 31)
        val row = grid.first { it.mode == "scales" }
        assertEquals(31, row.cells.size)
        assertEquals(2, row.cells[20].count)   // 10 days ago → index 30-10 = 20
        assertEquals(1, row.cells[20].correct)
        assertEquals(1, row.cells[30].count)   // today = last column
        assertEquals(0, row.cells[29].count)   // yesterday empty
        assertEquals(3, row.periodTotal)
    }

    @Test fun `event older than 31 days is ignored in month view`() {
        val now = System.currentTimeMillis()
        val grid = Stats.buildGrid(listOf(ev("chords", Stats.dayStart(32, now), true)), now, 31)
        assertTrue(grid.isEmpty())
    }

    @Test fun `event exactly at local midnight belongs to that day`() {
        val now = System.currentTimeMillis()
        val grid = Stats.buildGrid(
            listOf(ev("raga", Stats.dayStart(20, now), true)), now, 31)
        assertEquals(1, grid[0].cells[10].count)   // index 30-20 = 10
    }

    @Test fun `default view is 7 days`() {
        val now = System.currentTimeMillis()
        Stats.buildGrid(listOf(ev("x", now, true)), now).forEach { assertEquals(7, it.cells.size) }
    }

    @Test fun `intensity levels`() {
        assertEquals(0, Stats.intensityLevel(0))
        assertEquals(1, Stats.intensityLevel(1))
        assertEquals(1, Stats.intensityLevel(2))
        assertEquals(2, Stats.intensityLevel(5))
        assertEquals(3, Stats.intensityLevel(9))
        assertEquals(4, Stats.intensityLevel(10))
    }

    @Test fun `labels - weekday names for 7 days, day-of-month for 31, last is today`() {
        val now = System.currentTimeMillis()
        val fmt = java.text.SimpleDateFormat("EEE", java.util.Locale.getDefault())
        assertEquals(fmt.format(java.util.Date()), Stats.dayLabels(now, 7).last())
        val dFmt = java.text.SimpleDateFormat("d", java.util.Locale.getDefault())
        val labels31 = Stats.dayLabels(now, 31)
        assertEquals(31, labels31.size)
        assertEquals(dFmt.format(java.util.Date()).toInt(), labels31.last().toIntOrNull()!!)
    }

    @Test fun `range label spans oldest to today`() {
        assertTrue(Stats.rangeLabel(System.currentTimeMillis(), 31).contains("–"))
    }

    @Test fun `dayTotals sums all modes per day`() {
        val now = System.currentTimeMillis()
        val today = Stats.dayStart(0, now)
        val events = listOf(
            ev("scales", today + 1000, true), ev("grooves", today + 2000, false),
            ev("raga", today + 3000, true),
            ev("scales", Stats.dayStart(2, now) + 1000, true))
        val totals = Stats.dayTotals(events, now, 7)
        assertEquals(7, totals.size)
        assertEquals(3, totals[6].count)         // today
        assertEquals(3, totals[6].modes)         // three distinct modes
        assertEquals(1, totals[4].count)         // 2 days ago
        assertEquals(1, totals[4].modes)
        assertEquals(0, totals[5].count)         // yesterday empty
    }

    @Test fun `dayTotals ignores events older than the window`() {
        val now = System.currentTimeMillis()
        val totals = Stats.dayTotals(listOf(ev("x", Stats.dayStart(9, now), true)), now, 7)
        assertTrue(totals.all { it.count == 0 })
    }
}
