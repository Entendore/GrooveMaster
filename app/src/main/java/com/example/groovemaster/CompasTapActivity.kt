package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.groovemaster.audio.SynthEngine
import com.example.groovemaster.audio.StreamPlayer
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.MusicTheoryData
import com.example.groovemaster.data.Stats
import kotlin.math.abs

class CompasTapActivity : AppCompatActivity() {

    private val palos = MusicTheoryData.grooves.filter { it.family == "Spain (Flamenco)" }
    private var paloIdx = 0
    private var tempoPct = 90
    private var baseSample = 0L
    private var expectedSamples = listOf<Long>()
    private val taps = mutableListOf<Long>()
    private var listening = false
    private var score = 0
    private val handler = Handler(Looper.getMainLooper())
    private val circleViews = mutableListOf<android.view.View>()

    private lateinit var tv: TextView
    private lateinit var tvResult: TextView
    private lateinit var grid: GridLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_compastap)
        title = "💃 Compás Tap"
        tv = findViewById(R.id.tvCtStatus); tvResult = findViewById(R.id.tvCtResult)
        grid = findViewById(R.id.ctGrid)
        val spinner = findViewById<Spinner>(R.id.spinnerPalo)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            palos.map { "${it.name} (${it.meter})" })
        spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: android.widget.AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                paloIdx = pos; buildCircles()
            }
            override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
        }
        val seek = findViewById<SeekBar>(R.id.ctTempo)
        seek.min = 60; seek.max = 110; seek.progress = tempoPct
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, u: Boolean) { tempoPct = p }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        findViewById<Button>(R.id.btnCtHear).setOnClickListener { hear() }
        findViewById<Button>(R.id.btnCtStart).setOnClickListener { start() }
        findViewById<Button>(R.id.btnCtTap).setOnClickListener { if (listening) taps.add(StreamPlayer.nowSample()) }
        findViewById<Button>(R.id.btnCtEnd).setOnClickListener {
            SynthEngine.stopAll(); GameProgress.save(this, "compas_tap", score)
            startActivity(Intent(this, ResultsActivity::class.java)
                .putExtra("score", score).putExtra("type", "compas_tap")); finish()
        }
        buildCircles()
    }

    private fun current() = palos[paloIdx]

    private fun accents(): List<Int> {
        val palmas = current().layers.firstOrNull { it.label.contains("Palmas") } ?: current().layers.first()
        return palmas.pattern.mapIndexedNotNull { i, c -> if (c == 'X') i else null }
    }

    private fun stepMs() = 60000.0 * 100 / tempoPct / current().bpm / 4

    private fun buildCircles() {
        grid.removeAllViews(); circleViews.clear()
        grid.columnCount = current().pulses
        val off = ContextCompat.getColor(this, R.color.stepOff)
        repeat(current().pulses) {
            val v = android.view.View(this).apply {
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dp(20); height = dp(20); setMargins(dp(2), dp(2), dp(2), dp(2)) }
                setBackgroundColor(off)
            }
            grid.addView(v); circleViews.add(v)
        }
        paintExpected()
    }

    private fun paintExpected() {
        val acc = accents()
        val hi = ContextCompat.getColor(this, R.color.stepOnAccent)
        val off = ContextCompat.getColor(this, R.color.stepOff)
        circleViews.forEachIndexed { i, v -> v.setBackgroundColor(if (i in acc) hi else off) }
        tv.text = "${current().name}: tap counts ${acc.joinToString(" · ") { "${it + 1}" }} — ${current().meter}"
    }

    private fun hear() {
        SynthEngine.stopAll()
        SynthEngine.playGroove(current(), tempoPct, loops = 2)
    }

    private fun start() {
        SynthEngine.stopAll(); handler.removeCallbacksAndMessages(null)
        taps.clear(); listening = false
        val g = current(); val sMs = stepMs(); val p = g.pulses
        val acc = accents()
        val items = mutableListOf<Triple<Double, String, Float>>()
        for (c in 0 until p)
            items.add(Triple(c * sMs, if (c in acc) "wood" else "hat", if (c in acc) 1f else 0.4f))
        for (cycle in 1..2) for (c in 0 until p)
            items.add(Triple((cycle * p + c) * sMs, "hat", 0.55f))
        baseSample = SynthEngine.playSchedule(items)
        expectedSamples = (1..2).flatMap { cycle ->
            acc.map { c -> baseSample + (((cycle * p + c) * sMs) * StreamPlayer.SR / 1000).toLong() }
        }
        val accIdx = acc
        for (cycle in 1..2) for (c in 0 until p) {
            handler.postDelayed({
                circleViews.forEachIndexed { i, v ->
                    v.setBackgroundColor(ContextCompat.getColor(this,
                        if (i == c) R.color.stepHi
                        else if (i in accIdx) R.color.stepOnAccent
                        else R.color.stepOff)) }
            }, ((cycle * p + c) * sMs).toLong() + StreamPlayer.GUARD_MS)
        }
        handler.postDelayed({ evaluate() },
            ((2L * p * sMs) + (p * sMs)).toLong() + StreamPlayer.GUARD_MS + 400)
        tvResult.text = "Count-in: listen to the accent shape… then TAP the accents!"
    }

    private fun evaluate() {
        listening = false
        val used = BooleanArray(taps.size)
        var gained = 0
        val lines = mutableListOf<String>()
        val tol = 150.0 * StreamPlayer.SR / 1000
        for (exp in expectedSamples) {
            var best = -1; var err = Long.MAX_VALUE
            taps.forEachIndexed { i, t -> if (!used[i] && abs(t - exp) < err) { err = abs(t - exp); best = i } }
            val ms = err * 1000.0 / StreamPlayer.SR
            if (best >= 0 && err <= tol) {
                used[best] = true
                val pts = if (ms <= 80) 15 else 8
                gained += pts
                lines.add("accent — ${ms.toInt()}ms  +$pts")
            } else lines.add("accent — MISSED")
        }
        val extras = used.count { !it }
        if (extras > 0) { gained = maxOf(0, gained - extras * 3); lines.add("Off-accent taps: $extras (−${extras * 3})") }
        score += gained
        Stats.log(this, "compas_tap", gained >= expectedSamples.size * 10)
        paintExpected()
        tvResult.text = "${current().name}: $gained pts\n" + lines.joinToString("\n") +
                "\nScore $score — switch palo or press Start again"
    }

    override fun onDestroy() { super.onDestroy(); SynthEngine.stopAll(); handler.removeCallbacksAndMessages(null) }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
