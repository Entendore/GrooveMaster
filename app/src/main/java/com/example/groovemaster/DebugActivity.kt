package com.example.groovemaster

import android.os.Bundle
import android.os.SystemClock
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.groovemaster.audio.PitchDetector
import com.example.groovemaster.audio.SynthEngine
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.MusicTheoryData
import com.example.groovemaster.data.SpacedRep
import com.example.groovemaster.data.WesternTheoryData
import kotlin.math.abs
import kotlin.math.ln
import kotlin.math.sin

class DebugActivity : AppCompatActivity() {

    private lateinit var tvLog: TextView
    private lateinit var scroll: ScrollView
    private fun log(s: String) { tvLog.append("$s\n"); scroll.post { scroll.fullScroll(View.FOCUS_DOWN) } }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)
        title = "🧪 QA Lab"
        tvLog = findViewById(R.id.tvDebugLog); scroll = findViewById(R.id.debugScroll)

        findViewById<Button>(R.id.btnTInstrument).setOnClickListener {
            listOf("kick","snare","hat","clap","tom","wood","bell","metal","wave")
                .forEachIndexed { i, it -> SynthEngine.schedule(i * 350L) { SynthEngine.playInst(it, 1f) } }
            log("▶ Playing all 9 instruments…") }

        findViewById<Button>(R.id.btnTTimbre).setOnClickListener {
            listOf("pluck","nylon","metal","sine","click").forEachIndexed { i, it ->
                SynthEngine.schedule(i * 500L) { SynthEngine.playFreq(220.0, 400, 0.8f, it) } }
            log("▶ KS pluck / nylon / metal / sine / click at A3…") }

        findViewById<Button>(R.id.btnTIntegrity).setOnClickListener { integrity() }

        findViewById<Button>(R.id.btnTPitch).setOnClickListener {
            val sr = 44100
            listOf(82.41, 110.0, 220.0, 440.0).forEach { f ->
                val x = FloatArray(sr / 2) { i -> (0.3 * sin(2 * Math.PI * f * i / sr)).toFloat() }
                val d = PitchDetector.detect(x, sr)
                val err = d?.let { 1200 * ln(it / f) / ln(2.0) }
                log(if (err != null) "target $f Hz → ${"%.1f".format(d)} Hz (${"%.0f".format(err)}¢) ${if (abs(err) < 5) "✅" else "❌"}"
                    else "target $f Hz → NOT DETECTED ❌") } }

        findViewById<Button>(R.id.btnTLatency).setOnClickListener {
            val t1 = SystemClock.elapsedRealtime(); SynthEngine.playInst("kick", 1f)
            val cold = SystemClock.elapsedRealtime() - t1
            val t2 = SystemClock.elapsedRealtime(); SynthEngine.playInst("kick", 1f)
            val warm = SystemClock.elapsedRealtime() - t2
            log("kick first hit (render+cache): ${cold}ms · cached: ${warm}ms") }

        findViewById<Button>(R.id.btnTAllGrooves).setOnClickListener {
            val all = MusicTheoryData.grooves; var i = 0
            fun next() { if (i >= all.size) { log("✅ played all ${all.size} grooves"); return }
                val g = all[i++]; log("▶ [$i/${all.size}] ${g.name} (${g.pulses} pulses)")
                SynthEngine.playGroove(g, 200, 1,
                    onEnd = { android.os.Handler(mainLooper).postDelayed({ next() }, 100) }) }
            next() }

        findViewById<Button>(R.id.btnTSRDue).setOnClickListener {
            SpacedRep.forceAllDue(this); log("📅 ${SpacedRep.totalTracked(this)} SR cards forced due") }
        findViewById<Button>(R.id.btnTWipe).setOnClickListener {
            SpacedRep.wipe(this); log("🧹 SR wiped")
            GameProgress.GAMES.forEach { GameProgress.save(this, it, 0) }
            Toast.makeText(this, "Progress reset", Toast.LENGTH_SHORT).show() }
    }

    /** Static validator for ALL game content — run after every data edit. */
    private fun integrity() {
        val problems = mutableListOf<String>()
        val insts = setOf("kick","snare","hat","clap","tom","wood","bell","metal","wave")
        MusicTheoryData.grooves.forEach { g ->
            if (g.pulses !in 5..16) problems += "${g.name}: odd pulse count ${g.pulses}"
            g.layers.forEach { l ->
                if (l.pattern.length > g.pulses) problems += "${g.name}/${l.label}: pattern longer than pulses"
                if (l.pattern.any { it !in "Xxo." }) problems += "${g.name}/${l.label}: bad char"
                if (l.inst !in insts) problems += "${g.name}: unknown inst ${l.inst}" }
        }
        MusicTheoryData.ragas.forEach { r ->
            if (r.semitones != r.semitones.sorted()) problems += "${r.name}: unsorted semitones"
            if (MusicTheoryData.pakadCents(r.pakad).isEmpty()) problems += "${r.name}: pakad won't parse" }
        MusicTheoryData.scales.forEach { s ->
            if (s.steps.any { it < 0 || it > 1200 }) problems += "${s.name}: step out of octave" }
        WesternTheoryData.keySigs.forEach { k ->
            if (WesternTheoryData.relMinor(k.major) != k.minor) problems += "key sig mismatch ${k.major}" }
        MusicTheoryData.theoryQuestions.forEach { q ->
            if (q.answer !in q.options.indices) problems += "quiz: bad answer index: ${q.q}" }
        if (problems.isEmpty()) log("✅ INTEGRITY OK — ${MusicTheoryData.grooves.size} grooves, " +
            "${MusicTheoryData.scales.size} scales, ${MusicTheoryData.ragas.size} ragas, " +
            "${MusicTheoryData.theoryQuestions.size} questions, all valid")
        else { log("❌ ${problems.size} PROBLEMS:"); problems.take(30).forEach { log("· $it") } }
    }

    override fun onDestroy() { super.onDestroy(); SynthEngine.stopAll() }
}
