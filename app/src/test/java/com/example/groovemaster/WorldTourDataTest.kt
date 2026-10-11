package com.example.groovemaster

import com.example.groovemaster.data.MusicTheoryData
import com.example.groovemaster.data.WorldTourData
import org.junit.Assert.*
import org.junit.Test

class WorldTourDataTest {
    @Test fun `20 lessons, each with 3 valid questions`() {
        assertEquals(20, WorldTourData.lessons.size)
        WorldTourData.lessons.forEach { l ->
            assertEquals("lesson '${l.title}' quiz size", 3, l.quiz.size)
            l.quiz.forEach { q -> assertTrue(q.ans in q.options.indices); assertTrue(q.info.isNotBlank()) }
            assertTrue(l.audio != null)
        }
    }
    @Test fun `every lesson has extra retry questions so pickFresh can draw 3`() {
        WorldTourData.lessons.indices.forEach { i ->
            assertTrue("lesson $i extras", (WorldTourData.extraQuestions[i]?.size ?: 0) >= 2)
        }
    }
    @Test fun `lesson audio references existing grooves and ragas`() {
        val grooveNames = MusicTheoryData.grooves.map { it.name }.toSet()
        val ragaNames = MusicTheoryData.ragas.map { it.name }.toSet()
        WorldTourData.lessons.forEach { l ->
            l.audio!!.split("|").forEach { raw ->
                val part = raw.removePrefix("seq:")
                when {
                    part.startsWith("g:") -> assertTrue(
                        "missing groove: $part", part.split(":")[1] in grooveNames)
                    part.startsWith("pk:") -> assertTrue(
                        "missing raga: $part", part.split(":")[1] in ragaNames)
                }
            }
        }
    }
}
