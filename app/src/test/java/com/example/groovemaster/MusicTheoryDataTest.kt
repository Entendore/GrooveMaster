package com.example.groovemaster

import com.example.groovemaster.data.MusicTheoryData
import org.junit.Assert.*
import org.junit.Test

class MusicTheoryDataTest {
    @Test fun `pakad parser basics`() {
        assertEquals(listOf(0.0, 200.0, 400.0), MusicTheoryData.pakadCents("S R G"))
        assertEquals(500.0, MusicTheoryData.pakadCents("m")[0], 1e-9)   // komal ma
        assertEquals(600.0, MusicTheoryData.pakadCents("M")[0], 1e-9)   // tivra Ma
        assertEquals(1200.0, MusicTheoryData.pakadCents("S'")[0], 1e-9) // octave Sa
    }
    @Test fun `yaman pakad parses to expected pitches`() {
        val y = MusicTheoryData.ragas.first { it.name == "Yaman" }
        assertEquals(listOf(1100.0, 200.0, 400.0, 600.0, 700.0, 600.0, 400.0),
            MusicTheoryData.pakadCents(y.pakad))
    }
    @Test fun `every raga has valid data and a parseable pakad`() {
        assertTrue("expected 20+ ragas", MusicTheoryData.ragas.size >= 20)
        MusicTheoryData.ragas.forEach { r ->
            assertTrue("${r.name} semitone range", r.semitones.all { it in 0..11 })
            assertEquals("${r.name} sorted", r.semitones, r.semitones.distinct().sorted())
            assertTrue("${r.name} pakad non-blank", r.pakad.isNotBlank())
            assertTrue("${r.name} pakad parses", MusicTheoryData.pakadCents(r.pakad).isNotEmpty())
        }
    }
    @Test fun `every scale is ascending inside one octave`() {
        MusicTheoryData.scales.forEach { s ->
            assertTrue("${s.name} step range", s.steps.all { it in 0.0..1200.0 })
            assertEquals("${s.name} ascending", s.steps, s.steps.sorted())
        }
    }
    @Test fun `quarter-flat Rast actually has quarter tones`() {
        val rast = MusicTheoryData.scales.first { it.name == "Maqam Rast" }
        assertTrue(rast.steps.any { it % 1.0 != 0.0 })
    }
    @Test fun `noteName quarter-flat rendering`() {
        assertEquals("C", MusicTheoryData.noteName(0.0))
        assertTrue(MusicTheoryData.noteName(350.0).endsWith("↓"))
    }
    @Test fun `grooves are structurally sound`() {
        val insts = setOf("kick","snare","hat","clap","tom","wood","bell","metal","wave")
        MusicTheoryData.grooves.forEach { g ->
            assertTrue("${g.name} pulses", g.pulses in 5..16)
            assertTrue("${g.name} needs hits", g.layers.any { l -> l.pattern.any { it != '.' } })
            g.layers.forEach { l ->
                assertTrue("${g.name}/${l.label} pattern fits pulses", l.pattern.length <= g.pulses)
                assertTrue("${g.name}/${l.label} bad chars", l.pattern.all { it in "Xxo." })
                assertTrue("${g.name} unknown inst ${l.inst}", l.inst in insts)
            }
            g.markers.forEach { m -> assertTrue("${g.name} marker chars", m.pattern.all { it in "x." }) }
        }
        assertEquals("duplicate groove names", MusicTheoryData.grooves.size,
            MusicTheoryData.grooves.map { it.name }.distinct().size)
    }
    @Test fun `key grooves have expected pulse counts`() {
        assertEquals(12, MusicTheoryData.grooves.first { it.name == "Soleá" }.pulses)
        assertEquals(10, MusicTheoryData.grooves.first { it.name == "Samai Thaqil" }.pulses)
    }
    @Test fun `every trivia question is answerable`() {
        MusicTheoryData.theoryQuestions.forEach { q ->
            assertEquals(q.q, 4, q.options.size)
            assertTrue("${q.q} answer index", q.answer in q.options.indices)
            assertTrue(q.info.isNotBlank())
        }
    }
}
