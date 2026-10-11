package com.example.groovemaster

import com.example.groovemaster.data.WesternTheoryData as W
import org.junit.Assert.*
import org.junit.Test

class WesternTheoryDataTest {
    @Test fun `circle has 12 unique keys`() { assertEquals(12, W.CIRCLE.distinct().size) }
    @Test fun `fifth above C is G, and F wraps to C`() {
        assertEquals("G", W.fifthAbove("C")); assertEquals("C", W.fifthAbove("F"))
    }
    @Test fun `relative minors round-trip`() {
        W.keySigs.forEach { assertEquals(it.minor, W.relMinor(W.relMajor(it.minor))) }
    }
    @Test fun `key signatures match semitone roots`() {
        assertEquals(0, W.sigOfMajor("C").accidentals)
        assertTrue(W.sigOfMajor("D").sharp && W.sigOfMajor("D").accidentals == 2)
        assertEquals(3, W.keyRootSemitone("E♭"))
    }
    @Test fun `note values sum correctly`() {
        assertEquals(16, W.NOTE_VALUES["Whole"]); assertEquals(6, W.NOTE_VALUES["Dotted quarter"])
        assertEquals(8, W.NOTE_VALUES.getValue("Dotted quarter") + W.NOTE_VALUES.getValue("Eighth"))
    }
}
