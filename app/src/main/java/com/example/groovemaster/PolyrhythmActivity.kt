package com.example.groovemaster

import android.os.Bundle
import android.os.SystemClock
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.groovemaster.audio.SynthEngine

class PolyrhythmActivity : AppCompatActivity() {

    private lateinit var view: PolyrhythmView
    private lateinit var tvHelp: TextView
    private lateinit var btnGo: Button
    private var running = false
    private var t0 = 0L
    private var lastCycle = -1L
    private val period = 2400L

    private val frame = object : Runnable {
        override fun run() {
            if (!running) return
            val el = SystemClock.elapsedRealtime() - t0
            val cycle = el / period
            if (cycle != lastCycle) { lastCycle = cycle; scheduleClicks(cycle) }
            view.phase = (el % period) / period.toFloat()
            view.invalidate()
            view.postDelayed(this, 16)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_poly)
        title = "🌀 Polyrhythm Trainer"
        view = findViewById(R.id.polyView); tvHelp = findViewById(R.id.tvPolyHelp); btnGo = findViewById(R.id.btnPolyGo)
        val spinner = findViewById<Spinner>(R.id.spinnerRatio)
        val ratios = listOf("3 : 2 (hemiola)", "4 : 3", "5 : 4")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, ratios)
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                stop()
                val rs = listOf(3 to 2, 4 to 3, 5 to 4)[pos]
                view.a = rs.first; view.b = rs.second
                tvHelp.text = when (pos) {
                    0 -> "3 against 2 — the hemiola. Big circle = 3 clicks (amber), small = 2 (pink). They meet only at the top."
                    1 -> "4 against 3 — the 'second hemiola'. Count both pulses until they fuse."
                    else -> "5 against 4 — expert mode. Feel each wheel separately, then merge."
                }
                view.invalidate()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        btnGo.setOnClickListener {
            if (running) stop()
            else {
                running = true; btnGo.text = "⏹ Stop"
                SynthEngine.stopAll()
                t0 = SystemClock.elapsedRealtime(); lastCycle = -1
                frame.run()
            }
        }
    }

    /** Sample-accurate clicks for the NEXT cycle (guard ensures no clipping). */
    private fun scheduleClicks(cycle: Long) {
        val items = mutableListOf<Triple<Double, ShortArray, Float>>()
        for (i in 0 until view.a) items.add(Triple(cycle * period + i * period / view.a + StreamPlayer.GUARD_MS,
            SynthEngine.clickBuffer(880.0), 0.8f))
        for (j in 0 until view.b) items.add(Triple(cycle * period + j * period / view.b + StreamPlayer.GUARD_MS,
            SynthEngine.clickBuffer(524.0), 0.8f))
        SynthEngine.playScheduleBuffers(items)
    }

    private fun stop() {
        running = false
        SynthEngine.stopAll()
        btnGo.text = "▶ Start"
    }

    override fun onDestroy() { super.onDestroy(); running = false; SynthEngine.stopAll() }
}
