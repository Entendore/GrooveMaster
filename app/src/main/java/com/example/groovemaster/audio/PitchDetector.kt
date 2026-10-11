package com.example.groovemaster.audio
import kotlin.math.abs
import kotlin.math.sqrt

object PitchDetector {
    private fun normCorr(x: FloatArray, lag: Int, energy: Double): Double {
        var corr = 0.0; var e2 = 0.0
        for (i in 0 until x.size - lag) { val b = x[i + lag]; corr += x[i] * b; e2 += b * b }
        return corr / (sqrt(energy * e2) + 1e-9)
    }
    /** Autocorrelation + octave guard + PARABOLIC PEAK FIT (sub-sample lag, ±5¢). */
    fun detect(x: FloatArray, sampleRate: Int): Double? {
        var energy = 0.0
        for (v in x) energy += v * v
        if (sqrt(energy / x.size) < 0.012) return null
        val minLag = sampleRate / 900
        val maxLag = minOf(sampleRate / 70, x.size / 2)
        var bestLag = -1; var best = 0.0
        for (lag in minLag..maxLag) { val c = normCorr(x, lag, energy); if (c > best) { best = c; bestLag = lag } }
        if (bestLag <= 0 || best < 0.55) return null
        var lag = bestLag
        for (l in minLag until bestLag) { if (normCorr(x, l, energy) >= best * 0.85) { lag = l; break } }
        val cm = normCorr(x, maxOf(1, lag - 1), energy)
        val c0 = normCorr(x, lag, energy)
        val cp = normCorr(x, lag + 1, energy)
        val denom = cm - 2 * c0 + cp
        var delta = if (abs(denom) > 1e-12) 0.5 * (cm - cp) / denom else 0.0
        delta = delta.coerceIn(-0.5, 0.5)
        return sampleRate / (lag + delta)
    }
}
