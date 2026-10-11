package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.GridLayout
import android.widget.ProgressBar
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.groovemaster.audio.SynthEngine
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.MusicTheoryData
import com.example.groovemaster.data.Stats

class RhythmActivity : AppCompatActivity() {

    private class Cell(val v: View, val ch: Char, val baseColor: Int)

    private var pool: List<MusicTheoryData.Groove> = emptyList()
    private lateinit var round: List<MusicTheoryData.Groove>
    private var qIndex = 0
    private var score = 0
    private var streak = 0
    private var answered = false
    private var tempoPct = 100

    private lateinit var tvPrompt: TextView; private lateinit var tvMeter: TextView
    private lateinit var tvFeedback: TextView; private lateinit var tvScore: TextView
    private lateinit var tvStreak: TextView; private lateinit var tvProgress: TextView
    private lateinit var tvTempo: TextView
    private lateinit var btnPlay: Button; private lateinit var btnNext: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var options: List<Button>
    private lateinit var grid: GridLayout
    private lateinit var seek: SeekBar
    private lateinit var spinner: Spinner
    private var cells = ArrayList<List<Cell>>()
    private lateinit var current: MusicTheoryData.Groove
    private lateinit var currentOptions: List<MusicTheoryData.Groove>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_rhythm)
        title = "🥁 Groove Hunter"
        tvPrompt = findViewById(R.id.tvPrompt); tvMeter = findViewById(R.id.tvMeter)
        tvFeedback = findViewById(R.id.tvFeedback); tvScore = findViewById(R.id.tvScore)
        tvStreak = findViewById(R.id.tvStreak); tvProgress = findViewById(R.id.tvProgress)
        tvTempo = findViewById(R.id.tvTempo); progressBar = findViewById(R.id.progressBar)
        btnPlay = findViewById(R.id.btnPlay); btnNext = findViewById(R.id.btnNext)
        grid = findViewById(R.id.stepGrid); seek = findViewById(R.id.seekTempo)
        spinner = findViewById(R.id.spinnerFamily)
        options = listOf(R.id.btnOpt1, R.id.btnOpt2, R.id.btnOpt3, R.id.btnOpt4).map { findViewById<Button>(it) }

        val families = listOf("All") + MusicTheoryData.grooves.map { it.family }.distinct()
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, families)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                pool = if (pos == 0) MusicTheoryData.grooves
                       else MusicTheoryData.grooves.filter { it.family == families[pos] }
                score = 0; streak = 0; qIndex = 0
                round = pool.shuffled().take(10)
                showQuestion()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        seek.min = 60; seek.max = 130; seek.progress = 100
        tvTempo.text = "Tempo 100%"
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, fromUser: Boolean) {
                tempoPct = p; tvTempo.text = "Tempo $p%"
            }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        options.forEachIndexed { i, b -> b.setOnClickListener { answer(i) } }
        btnPlay.setOnClickListener { play() }
        btnNext.setOnClickListener { next() }
    }

    override fun onDestroy() { super.onDestroy(); SynthEngine.stopAll() }

    private fun showQuestion() {
        if (round.isEmpty()) return
        answered = false
        current = round[qIndex]
        tvProgress.text = "Question ${qIndex + 1} / ${round.size}"
        progressBar.progress = qIndex
        tvScore.text = "Score $score"
        tvStreak.text = "🔥 Streak $streak"
        tvPrompt.text = "Which groove / cycle is playing?"
        tvMeter.text = "🔊 ${current.meter}"
        tvFeedback.text = ""
        btnNext.isEnabled = false
        btnNext.text = if (qIndex == round.lastIndex) "See Results 🏁" else "Next  →"
        buildGrid()
        val wrong = pool.filter { it.name != current.name }.shuffled().take(3)
        currentOptions = (wrong + current).shuffled()
        options.forEachIndexed { i, b ->
            b.text = currentOptions[i].name; b.isEnabled = true
            b.setBackgroundColor(ContextCompat.getColor(this, R.color.optionBg))
        }
        play()
    }

    private fun buildGrid() {
        grid.removeAllViews(); cells.clear()
        grid.columnCount = current.pulses + 1
        val cOn = ContextCompat.getColor(this, R.color.stepOn)
        val cAcc = ContextCompat.getColor(this, R.color.stepOnAccent)
        val cClap = ContextCompat.getColor(this, R.color.clapPink)
        val cWave = ContextCompat.getColor(this, R.color.waveCyan)
        val rows = current.layers.map { Triple(it.label, it.inst, it.pattern) } +
                current.markers.map { Triple("☆ ${it.label}", it.inst, it.pattern) }
        val markerInsts = current.markers.map { it.inst }.toSet()
        for ((label, inst, pattern) in rows) {
            val lbl = TextView(this).apply {
                text = label; textSize = 10f; gravity = Gravity.CENTER_VERTICAL
                setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dp(64); height = dp(16); setMargins(0, dp(2), dp(4), dp(2)) }
            }
            grid.addView(lbl)
            val row = mutableListOf<Cell>()
            for (c in 0 until current.pulses) {
                val ch = pattern.getOrNull(c) ?: '.'
                val base = when {
                    markerInsts.contains(inst) && inst == "clap" -> cClap
                    markerInsts.contains(inst) && inst == "wave" -> cWave
                    markerInsts.contains(inst) -> cOn
                    ch == 'X' -> cAcc
                    else -> cOn
                }
                val v = View(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = dp(13); height = dp(16); setMargins(dp(1), dp(2), dp(1), dp(2)) }
                }
                grid.addView(v); row.add(Cell(v, ch, base))
            }
            cells.add(row)
        }
        highlight(-1)
    }

    private fun highlight(col: Int) {
        val cOff = ContextCompat.getColor(this, R.color.stepOff)
        val cHi = ContextCompat.getColor(this, R.color.stepHi)
        for (row in cells) for (cell in row) {
            cell.v.setBackgroundColor(when {
                cell.ch == '.' -> cOff
                col >= 0 && cells.indexOf(row) >= 0 && row.indexOf(cell) == -1 -> cell.baseColor // fallback
                row.indexOf(cell) == col -> cHi
                else -> cell.baseColor
            })
        }
    }

    private fun play() {
        SynthEngine.playGroove(current, tempoPct, loops = 2,
            onStep = { highlight(it % current.pulses) },
            onEnd = { highlight(-1) })
    }

    private fun answer(i: Int) {
        if (answered) return
        answered = true
        SynthEngine.stopAll(); highlight(-1)
        options.forEach { it.isEnabled = false }
        val picked = currentOptions[i]
        val correctIdx = currentOptions.indexOfFirst { it.name == current.name }
        val correct = picked.name == current.name
        Stats.log(this, "grooves", correct)
        if (correct) {
            streak++
            val pts = 10 + (streak - 1) * 2
            score += pts
            options[i].setBackgroundColor(ContextCompat.getColor(this, R.color.correct))
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.correct))
            tvFeedback.text = "✅ Correct!  +$pts\n${current.name} · ${current.origin} · ${current.meter}\n${current.info}"
        } else {
            streak = 0
            options[i].setBackgroundColor(ContextCompat.getColor(this, R.color.wrong))
            options[correctIdx].setBackgroundColor(ContextCompat.getColor(this, R.color.correct))
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.wrong))
            tvFeedback.text = "❌ It was ${current.name} · ${current.origin} · ${current.meter}\n${current.info}"
        }
        tvScore.text = "Score $score"
        tvStreak.text = "🔥 Streak $streak"
        btnNext.isEnabled = true
    }

    private fun next() {
        qIndex++
        if (qIndex >= round.size) {
            GameProgress.save(this, "grooves", score)
            startActivity(Intent(this, ResultsActivity::class.java)
                .putExtra("score", score).putExtra("type", "grooves"))
            finish()
        } else showQuestion()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
