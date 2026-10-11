package com.example.groovemaster.data

object WesternTheoryData {

    data class KeySig(val accidentals: Int, val sharp: Boolean, val major: String, val minor: String)

    val keySigs = listOf(
        KeySig(0, true, "C", "A minor"),
        KeySig(1, true, "G", "E minor"), KeySig(2, true, "D", "B minor"),
        KeySig(3, true, "A", "F♯ minor"), KeySig(4, true, "E", "C♯ minor"),
        KeySig(5, true, "B", "G♯ minor"), KeySig(6, true, "F♯", "D♯ minor"),
        KeySig(7, true, "C♯", "A♯ minor"),
        KeySig(1, false, "F", "D minor"), KeySig(2, false, "B♭", "G minor"),
        KeySig(3, false, "E♭", "C minor"), KeySig(4, false, "A♭", "F minor"),
        KeySig(5, false, "D♭", "B♭ minor"), KeySig(6, false, "G♭", "E♭ minor"),
        KeySig(7, false, "C♭", "A♭ minor")
    )

    /** Clockwise circle of 5ths (major keys). */
    val CIRCLE = listOf("C","G","D","A","E","B","F♯","D♭","A♭","E♭","B♭","F")

    /** Duration values in sixteenth notes. */
    val NOTE_VALUES: Map<String, Int> = linkedMapOf(
        "Whole" to 16, "Dotted half" to 12, "Half" to 8, "Dotted quarter" to 6,
        "Quarter" to 4, "Dotted eighth" to 3, "Eighth" to 2, "Sixteenth" to 1
    )

    fun fifthAbove(k: String): String = CIRCLE[(CIRCLE.indexOf(k) + 1) % CIRCLE.size]
    fun relMinor(k: String): String = keySigs.first { it.major == k }.minor
    fun relMajor(m: String): String = keySigs.first { it.minor == m }.major
    fun sigOfMajor(k: String): KeySig = keySigs.first { it.major == k }

    fun keyRootSemitone(k: String): Int = when (k) {
        "C" -> 0; "G" -> 7; "D" -> 2; "A" -> 9; "E" -> 4; "B" -> 11
        "F♯" -> 6; "C♯" -> 1; "D♭" -> 1; "A♭" -> 8; "E♭" -> 3; "B♭" -> 10; "F" -> 5
        else -> 0
    }
}
