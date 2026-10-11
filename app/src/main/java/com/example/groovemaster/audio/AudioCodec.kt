package com.example.groovemaster.audio
import com.example.groovemaster.data.MusicTheoryData

/** Compact replay specs: s: · ab: · o4: · c: · i: · p: · satb: · m: · d: · g: · n: · pk: · cg: · sw: · seq: */
object AudioCodec {
    fun clapGroove(beats: Int, sub: Int): MusicTheoryData.Groove {
        val pulses = beats * sub
        val acc = (0 until pulses).map { if (it % sub == 0) 'X' else '.' }.joinToString("")
        return MusicTheoryData.Groove("cg","","",100,"",0f,"",listOf(
            MusicTheoryData.Layer("Beat","clap",acc), MusicTheoryData.Layer("Sub","hat","x".repeat(pulses))))
    }
    fun swingGroove(swung: Boolean): MusicTheoryData.Groove =
        MusicTheoryData.Groove("sw","","",120,"4/4",if (swung) 1f else 0f,"",listOf(
            MusicTheoryData.Layer("Hat","hat","x.x.x.x.x.x.x.x."),
            MusicTheoryData.Layer("Kick","kick","x.......x.......")))
    fun play(spec: String?) {
        if (spec.isNullOrEmpty()) return
        val ci = spec.indexOf(':')
        val kind = if (ci < 0) spec else spec.substring(0, ci)
        val rest = if (ci < 0) "" else spec.substring(ci + 1)
        when (kind) {
            "s" -> { val a = rest.split(":"); val r = a[0].toDouble()
                SynthEngine.playCents(a[1].split(",").map { r + it.toDouble() } + r + 1200.0) }
            "ab" -> { val a = rest.split("|")
                SynthEngine.playAB(a[0].split(",").map { it.toDouble() }, a[1].split(",").map { it.toDouble() }, a[2].toLong(), a[3]) }
            "o4" -> rest.split("|").map { it.split(",").map { c -> c.toDouble() } }
                .forEachIndexed { k, l -> SynthEngine.schedule(k * 2100L) { SynthEngine.playCents(l + 1200.0, 190) } }
            "c" -> SynthEngine.playChord(rest.split(",").map { it.toInt() })
            "i" -> { val a = rest.split(":"); SynthEngine.playInterval(a[0].toInt(), a[0].toInt() + a[1].toInt()) }
            "p" -> { val a = rest.split(":"); SynthEngine.playDegreeProgression(a[0].toInt(), a[1].split(",").map { it.toInt() }) }
            "satb" -> { val a = rest.split(":"); SynthEngine.playSATB(a[0].toInt(), a[1].split(",").map { it.toInt() }) }
            "m" -> { val a = rest.split(":"); SynthEngine.playMelody(a[0].split(",").map { it.toInt() }, a[1].toLong()) }
            "d" -> { val mel = rest.split(",").map { it.toInt() }
                SynthEngine.note(60, 600); SynthEngine.schedule(750) { SynthEngine.playMelody(mel.map { 60 + it }, 450) } }
            "g" -> { val a = rest.split(":"); val bpm = a[1].toInt()
                MusicTheoryData.grooves.firstOrNull { it.name == a[0] }?.let { SynthEngine.playGroove(if (bpm > 0) it.copy(bpm = bpm) else it) } }
            "n" -> { val a = rest.split(":"); SynthEngine.playHarmonic(a[0].toDouble(), a[1].toInt()) }
            "pk" -> MusicTheoryData.ragas.firstOrNull { it.name == rest }?.let {
                SynthEngine.playCents(MusicTheoryData.pakadCents(it.pakad) + 1200.0, 240) }
            "cg" -> { val a = rest.split(":"); SynthEngine.playGroove(clapGroove(a[0].toInt(), a[1].toInt())) }
            "sw" -> SynthEngine.playGroove(swingGroove(rest == "1"))
            "seq" -> rest.split("|").forEachIndexed { k, p -> SynthEngine.schedule(k * 4300L) { play(p) } }
        }
    }
}
