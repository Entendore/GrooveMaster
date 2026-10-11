package com.example.groovemaster

import com.example.groovemaster.audio.Tuning
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.abs

class TuningTest {
    @Test fun `all tunings span exactly one octave`() {
        val all = listOf(Tuning.ET_MAJOR, Tuning.JI_MAJOR, Tuning.PYTHAGOREAN_MAJOR,
            Tuning.MEANTONE_MAJOR, Tuning.WERCKMEISTER_MAJOR, Tuning.RAST,
            Tuning.NAHAWAND, Tuning.SLENDRO, Tuning.PELOG)
        all.forEach { assertEquals(0.0, it.first(), 1e-9); assertEquals(1200.0, it.last(), 1e-9) }
    }
    @Test fun `ET is 100-cent steps`() {
        Tuning.ET_MAJOR.forEachIndexed { i, c -> assertEquals(i * 100.0, c, 1e-9) }
    }
    @Test fun `JI third is pure at 386c, meantone matches, pythagorean is wide`() {
        assertEquals(386.31, Tuning.JI_MAJOR[2], 0.5)
        assertEquals(386.31, Tuning.MEANTONE_MAJOR[2], 0.5)
        assertEquals(407.82, Tuning.PYTHAGOREAN_MAJOR[2], 0.5)
    }
    @Test fun `freq doubles over an octave`() {
        assertTrue(abs(Tuning.freq(1200.0) / Tuning.freq(0.0) - 2.0) < 1e-6)
    }
    @Test fun `exactly 22 ascending shrutis in range`() {
        assertEquals(22, Tuning.SHRUTIS.size)
        Tuning.SHRUTIS.zipWithNext().forEach { (a, b) ->
            assertTrue(b.second > a.second); assertTrue(b.second < 1200.0) }
    }
    @Test fun `gamelan sizes`() { assertEquals(6, Tuning.SLENDRO.size); assertEquals(8, Tuning.PELOG.size) }
}
