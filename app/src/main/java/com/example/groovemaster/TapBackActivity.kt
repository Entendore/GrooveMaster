package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.groovemaster.audio.SynthEngine
import com.example.groovemaster.audio.StreamPlayer
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.Stats
import kotlin.math.abs

class TapBackActivity : AppCompatActivity() {

    private class Target(val step: Int, val pad: String)

    private val pads = listOf("kick", "snare", "hat", "clap")
    private val padIds = mapOf("kick" to R.id.btnPadKick, "snare" to R.id.btnPadSnare,
                               "hat" to R.id.btnPadHat, "clap" to R.id.btnPadClap)

    private var targets: List<Target> = emptyList()
    private val taps = mutableListOf<Double>()          // ms into the pattern (sample clock)
    private var listening = false
    private var stepMs = 0.0
    private var countInMs = 0.0
    private var baseSample = 0L
    private var countInSamples = 0L
    private var bpm = 88
    private var round = 0
    private var score = 0
    private val totalRounds = 12
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var tvStatus: TextView
    private lateinit var tvScore: TextView
    private lateinit var tvResults: TextView
    private lateinit var btnStart: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tapback)
        title = "🪘 Tap-Back (ladder)"
        tvStatus = findViewById(R.id.tvTapStatus); tvScore = findViewById(R.id.tvTapScore)
        tvResults = findViewById(R.id.tvTapResults); btnStart = findViewById(R.id.btnTapStart)
        padIds.forEach { (pad, id) ->
            findViewById<Button>(id).setOnClickListener {
                SynthEngine.playInst(pad, 0.9f)   // cached buffer → near-instant
                if (listening) taps.add((StreamPlayer.nowSample() - baseSample - countInSamples) * 1000.0 / StreamPlayer.SR)
            }
        }
        findViewById<Button>(R.id.btnTapEnd).setOnClickListener { finishToResults() }
        btnStart.setOnClickListener { if (round >= totalRounds) finishToResults() else newPattern() }
    }

    override fun onDestroy() { super.onDestroy(); SynthEngine.stopAll(); handler.removeCallbacksAndMessages(null) }

    private fun newPattern() {
        round++
        SynthEngine.stopAll(); handler.removeCallbacksAndMessages(null)
        val steps = (0..14 step 2).shuffled().take(6).sorted()
        targets = steps.map { Target(it, pads.random()) }
        taps.clear(); listening = false
        stepMs = 60000.0 / bpm / 4
        countInMs = 4.0 * 60000.0 / bpm
        countInSamples = (countInMs * StreamPlayer.SR / 1000).toLong()
        tvResults.text = "Round $round / $totalRounds · $bpm BPM"
        btnStart.text = "…"; btnStart.isEnabled = false

        val items = mutableListOf<Triple<Double, String, Float>>()
        for (b in 0..3) items.add(Triple(b * 60000.0 / bpm, "hat", 0.5f))
        targets.forEach { items.add(Triple(countInMs + it.step * stepMs, it.pad, 1f)) }
        baseSample = SynthEngine.playSchedule(items)               // sample-accurate

        handler.postDelayed({ listening = true
            tvStatus.text = " YOUR TURN — $bpm BPM!" }, countInMs.toLong() + StreamPlayer.GUARD_MS)
        val patternMs = (targets.maxOf { it.step } + 4) * stepMs
        handler.postDelayed({ evaluate() }, (countInMs + patternMs).toLong() + StreamPlayer.GUARD_MS)
    }

    private fun evaluate() {
        listening = false
        val used = BooleanArray(taps.size)
        var gained = 0
        val lines = mutableListOf<String>()
        for (t in targets) {
            val expect = t.step * stepMs
            var best = -1; var err = Double.MAX_VALUE
            taps.forEachIndexed { i, ms -> if (!used[i] && abs(ms - expect) < err) { err = abs(ms - expect); best = i } }
            val tSec = "%.2f".format(expect / 1000.0)
            if (best >= 0 && err <= 300) {
                used[best] = true
                val pts = when { err < 80 -> 15; err < 160 -> 10; else -> 5 }
                gained += pts
                lines.add("${t.pad.padEnd(5)} @ ${tSec}s — ${err.toInt()}ms  +$pts")
            } else lines.add("${t.pad.padEnd(5)} @ ${tSec}s — MISSED")
        }
        val extras = used.count { !it }
        if (extras > 0) { gained = maxOf(0, gained - extras * 2); lines.add("Extra taps: $extras (−${extras * 2})") }
        score += gained
        Stats.log(this, "tapback", gained >= targets.size * 10)
        tvScore.text = "Score $score"
        tvResults.text = lines.joinToString("\n")
        bpm = (bpm + 6).coerceAtMost(168)
        tvStatus.text = if (round >= totalRounds) "Done! Top of the ladder 🎉" else "Next: ${bpm} BPM"
        btnStart.text = if (round >= totalRounds) "🏁 See Results" else "▶ Next (${bpm} BPM)"
        btnStart.isEnabled = true
    }

    private fun finishToResults() {
        GameProgress.save(this, "tapback", score)
        startActivity(Intent(this, ResultsActivity::class.java)
            .putExtra("score", score).putExtra("type", "tapback"))
        finish()
    }
}
