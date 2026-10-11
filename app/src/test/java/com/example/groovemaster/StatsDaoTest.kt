package com.example.groovemaster

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.groovemaster.data.AnswerEvent
import com.example.groovemaster.data.AppDb
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class StatsDaoTest {
    private lateinit var dao: com.example.groovemaster.data.AnswerDao
    @Before fun setup() {
        val ctx = ApplicationProvider.getApplicationContext<Context>()
        dao = AppDb.get(ctx).answers()
    }
    @Test fun `perMode aggregates correctly`() = runBlocking {
        dao.clear()
        listOf(true, true, false).forEach { dao.add(AnswerEvent(mode = "scales", correct = it, ts = 0)) }
        dao.add(AnswerEvent(mode = "grooves", correct = false, ts = 0))
        val stats = dao.perMode()
        assertEquals(2, stats.size)
        val scales = stats.first { it.mode == "scales" }
        assertEquals(3, scales.total); assertEquals(2, scales.correct)
        assertTrue(stats.first().total >= stats.last().total)
    }
    @Test fun `since returns only recent events`() = runBlocking {
        dao.clear()
        val now = System.currentTimeMillis()
        dao.add(AnswerEvent(mode = "scales", correct = true, ts = now - 1000))
        dao.add(AnswerEvent(mode = "scales", correct = false, ts = now - 20L * 86_400_000))
        val recent = dao.since(now - 7L * 86_400_000)
        assertEquals(1, recent.size)
        assertTrue(recent[0].correct)
    }
    @Test fun `clear wipes everything`() = runBlocking {
        dao.clear()
        dao.add(AnswerEvent(mode = "x", correct = true, ts = 0))
        assertEquals(1, dao.total())
        dao.clear()
        assertEquals(0, dao.total())
    }
}
