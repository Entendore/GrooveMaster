package com.example.groovemaster.data

/** Pure codec for Groove Lab share codes: "GML1;112;kick=x..x...;snare=....x..." */
object GrooveCode {
    val INSTS = listOf("kick","snare","hat","clap","tom","wood")
    const val STEPS = 16

    fun encode(tempo: Int, grid: Array<CharArray>): String {
        val parts = INSTS.mapIndexedNotNull { i, inst ->
            if (i >= grid.size) null
            else {
                val p = String(grid[i])
                if (p.any { it == 'x' || it == 'X' || it == 'o' }) "$inst=$p" else null
            }
        }
        return "GML1;${tempo.coerceIn(60, 160)};" + parts.joinToString(";")
    }

    /** Returns null on any malformed input instead of throwing. */
    fun fromCode(code: String): Pair<Int, Array<CharArray>>? {
        return try {
            val parts = code.trim().split(";")
            if (parts.size < 2 || parts[0] != "GML1") return null
            val tempo = parts[1].toInt().coerceIn(60, 160)
            val grid = Array(INSTS.size) { CharArray(STEPS) { '.' } }
            parts.drop(2).forEach { p ->
                val kv = p.split("=", limit = 2)
                if (kv.size == 2) {
                    val row = INSTS.indexOf(kv[0])
                    if (row >= 0) for (c in 0 until STEPS) grid[row][c] = kv[1].getOrNull(c) ?: '.'
                }
            }
            tempo to grid
        } catch (_: Exception) { null }
    }
}
