package com.example.groovemaster

import com.example.groovemaster.audio.PitchDetector
import org.junit.Assert.*
import org.junit.Test
import kotlin.math.PI
import kotlin.math.ln
import kotlin.math.sin

class PitchDetectorTest {
    private val TOL = 5.0   // parabolic interpolation → sub-sample lag accuracy

    private fun sine(f: Double, sr: Int = 44100, ms: Int = 900): FloatArray {
        val n = sr * ms / 1000
        return FloatArray(n) { i ->
            val t = i.toDouble() / sr
            (0.30 * sin(2 * PI * f * t) + 0.10 * sin(4 * PI * f * t)).toFloat()  // + 2nd harmonic
        }
    }
    private fun cents(meas: Double, exp: Double) = 1200 * ln(meas / exp) / ln(2.0)

    @Test fun `detects guitar low E`()  { assertEquals(0.0, cents(PitchDetector.detect(sine(82.41), 44100)!!, 82.41), TOL) }
    @Test fun `detects A2`()            { assertEquals(0.0, cents(PitchDetector.detect(sine(110.0), 44100)!!, 110.0), TOL) }
    @Test fun `detects A3 and A4`()     {
        assertEquals(0.0, cents(PitchDetector.detect(sine(220.0), 44100)!!, 220.0), TOL)
        assertEquals(0.0, cents(PitchDetector.detect(sine(440.0), 44100)!!, 440.0), TOL)
    }
    @Test fun `detects a singer-ish G3 with vibrato-ish noise`() {
        val sr = 44100; val f = 196.0; val n = sr / 2
        val x = FloatArray(n) { i ->
            val t = i.toDouble() / sr
            val wob = 1.0 + 0.003 * sin(2 * PI * 5.0 * t)   // ±5¢ vibrato
            (0.3 * sin(2 * PI * f * wob * t) + 0.08 * sin(2 * PI * 2 * f * t)).toFloat()
        }
        assertEquals(0.0, cents(PitchDetector.detect(x, sr)!!, f), 15.0)
    }
    @Test fun `silence returns null`() { assertNull(PitchDetector.detect(FloatArray(44100), 44100)) }
}
