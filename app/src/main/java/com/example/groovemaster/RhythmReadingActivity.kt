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

class RhythmReadingActivity : AppCompatActivity() {

    private val cellPatterns = listOf(listOf(4), listOf(2, 2), listOf(2, 1, 1), listOf(3, 1))
    private val kinds = mapOf(4 to "Q", 2 to "E", 1 to "S", 3 to "ED")

    private var events = mutableListOf<Triple<Int, String, Boolean?>>()
    private var stepMs = 0.0
    private var t0Samples = 0L
    private var countInSteps = 16
    private var running = false
    private var score = 0
    private val taps = mutableListOf<Double>()   // ms since metronome start (sample clock)
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var view: RhythmView
    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rhythmread)
        title = "📖 Rhythm Reading"
        view = findViewById(R.id.rhythmView); tv = findViewById(R.id.tvRR)
        buildRhythm()
        findViewById<Button>(R.id.btnRRHear).setOnClickListener {
            val items = events.mapIndexed { i, (pos, _, _) ->
                Triple(300.0 + (countInSteps + pos) * stepMs,
                       if (i == 0) "wood" else "hat", if (i == 0) 1f else 0.55f) }
            SynthEngine.playSchedule(items)
        }
        findViewById<Button>(R.id.btnRRStart).setOnClickListener { start() }
        findViewById<Button>(R.id.btnRRTap).setOnClickListener {
            if (running) taps.add((StreamPlayer.nowSample() - t0Samples) * 1000.0 / StreamPlayer.SR)
        }
        findViewById<Button>(R.id.btnRREnd).setOnClickListener {
            SynthEngine.stopAll(); GameProgress.save(this, "rhythmread", score)
            startActivity(Intent(this, ResultsActivity::class.java)
                .putExtra("score", score).putExtra("type", "rhythmread")); finish()
        }
    }

    private fun buildRhythm() {
        val bpm = when (GameProgress.tier(this, "rhythmread")) { 0 -> 70; 1 -> 84; else -> 96 }
        stepMs = 60000.0 / bpm / 4
        events.clear()
        var pos = 0
        repeat(8) { events.addAll(cellPatterns.random().map { v ->
            Triple(pos, kinds.getValue(v), null as Boolean?)); pos += 4 } }
        view.events = events.toList(); view.invalidate()
        tv.text = "Read the rhythm (2 bars). 'Hear it' to check, then Start: 4 clicks, then TAP every note."
    }

    private fun start() {
        SynthEngine.stopAll(); taps.clear(); running = true
        val items = mutableListOf<Triple<Double, ShortArray, Float>>()
        for (q in 0 until (countInSteps + 32 + 8) / 4)
            items.add(Triple(q * 4.0 * stepMs, SynthEngine.clickBuffer(880.0), if (q < 4) 1f else 0.6f))
        t0Samples = SynthEngine.playScheduleBuffers(items)
        handler.postDelayed({ evaluate() }, ((countInSteps + 32) * stepMs).toLong() + StreamPlayer.GUARD_MS)
        tv.text = "🎧 Count-in…"
    }

    private fun evaluate() {
        running = false
        val expected = events.map { (countInSteps + it.first) * stepMs }
        val used = BooleanArray(taps.size)
        var gained = 0
        val out = events.toMutableList()
        for (i in events.indices) {
            var best = -1; var err = Double.MAX_VALUE
            taps.forEachIndexed { k, t -> if (!used[k] && abs(t - expected[i]) < err) { err = abs(t - expected[i]); best = k } }
            val hit = best >= 0 && err <= 150
            if (hit) { used[best] = true; gained += if (err < 70) 15 else 10 }
            out[i] = Triple(out[i].first, out[i].second, hit)
        }
        score += gained
        Stats.log(this, "rhythmread", gained >= events.size * 10)
        view.events = out; view.invalidate()
        tv.text = "✅ $gained / ${events.size * 15} this rhythm · Score $score — Start for a new one"
        handler.postDelayed({ buildRhythm() }, 1400)
    }

    override fun onDestroy() { super.onDestroy(); SynthEngine.stopAll(); handler.removeCallbacksAndMessages(null) }
}
