package com.example.groovemaster

import com.example.groovemaster.data.GrooveCode
import org.junit.Assert.*
import org.junit.Test

class GrooveCodeTest {
    @Test fun `groove code round trips`() {
        val g = Array(GrooveCode.INSTS.size) { CharArray(GrooveCode.STEPS) { '.' } }
        g[0][0] = 'x'; g[1][4] = 'X'; g[2][15] = 'o'; g[5][7] = 'x'
        val r = GrooveCode.fromCode(GrooveCode.encode(112, g))!!
        assertEquals(112, r.first)
        assertEquals('x', r.second[0][0])
        assertEquals('X', r.second[1][4])
        assertEquals('o', r.second[2][15])
        assertEquals('x', r.second[5][7])
        // untouched cells stay empty
        assertEquals('.', r.second[3][3])
    }
    @Test fun `garbage code returns null not crash`() {
        assertNull(GrooveCode.fromCode("hello"))
        assertNull(GrooveCode.fromCode(""))
        assertNull(GrooveCode.fromCode("GML2;100;kick=xxxx"))
        assertNull(GrooveCode.fromCode("GML1;banana;kick=x"))
    }
    @Test fun `tempo is clamped`() {
        val g = Array(GrooveCode.INSTS.size) { CharArray(GrooveCode.STEPS) { '.' } }
        assertEquals(60, GrooveCode.fromCode(GrooveCode.encode(10, g))!!.first)
        assertEquals(160, GrooveCode.fromCode(GrooveCode.encode(999, g))!!.first)
    }
    @Test fun `empty grid still round trips`() {
        val g = Array(GrooveCode.INSTS.size) { CharArray(GrooveCode.STEPS) { '.' } }
        val r = GrooveCode.fromCode(GrooveCode.encode(100, g))!!
        assertEquals(100, r.first)
        assertTrue(r.second.all { row -> row.all { it == '.' } })
    }
    @Test fun `truncated patterns import safely`() {
        val r = GrooveCode.fromCode("GML1;100;kick=x.x")!!
        assertEquals('x', r.second[0][0]); assertEquals('x', r.second[0][2])
        assertEquals('.', r.second[0][15])
    }
}
