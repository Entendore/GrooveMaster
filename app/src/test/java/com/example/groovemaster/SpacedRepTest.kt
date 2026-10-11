package com.example.groovemaster

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.groovemaster.data.SpacedRep
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class SpacedRepTest {
    private lateinit var ctx: Context
    @Before fun setup() { ctx = ApplicationProvider.getApplicationContext() }

    private fun item(id: String) = SpacedRep.Item(id, "scales", "p?", null,
        listOf("a","b","c","d"), 1, "info", "s:0:0,2,4,5,7,9,11", 0, 0, 0)

    @Test fun `wrong answer is immediately due`() {
        SpacedRep.wipe(ctx)
        SpacedRep.upsertWrong(ctx, item("t1"))
        assertEquals(1, SpacedRep.dueCount(ctx))
        assertEquals("scales", SpacedRep.due(ctx)[0].mode)
    }
    @Test fun `correct answer promotes and schedules for later`() {
        SpacedRep.wipe(ctx)
        SpacedRep.upsertWrong(ctx, item("t2"))
        SpacedRep.markCorrect(ctx, "t2")
        assertEquals(0, SpacedRep.dueCount(ctx))       // not due today
        assertEquals(1, SpacedRep.totalTracked(ctx))   // still tracked
    }
    @Test fun `re-missing the same card updates it instead of duplicating`() {
        SpacedRep.wipe(ctx)
        SpacedRep.upsertWrong(ctx, item("t3"))
        SpacedRep.markCorrect(ctx, "t3")
        SpacedRep.upsertWrong(ctx, item("t3"))
        assertEquals(1, SpacedRep.totalTracked(ctx))
        assertEquals(1, SpacedRep.dueCount(ctx))       // back in box 0
    }
}
