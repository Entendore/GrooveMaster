package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.groovemaster.audio.SynthEngine
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.MusicTheoryData
import com.example.groovemaster.data.Stats

class FretboardActivity : AppCompatActivity() {

    private val layouts = mapOf(
        "Guitar (E A D G B E)" to listOf(40, 45, 50, 55, 59, 64),
        "Oud (Arabic C F A D G C)" to listOf(36, 41, 45, 50, 55, 60),
        "Bass (E A D G)" to listOf(28, 33, 38, 43),
        "Sitar (Sa–Pa, simplified)" to listOf(48, 55, 60))

    private var findMode = true   // true = find exact note on exact string; false = find any fret with the pitch class
    private var score = 0
    private var targetS = 0; private var targetF = 0; private var targetMidi = 0
    private lateinit var fb: FretboardView
    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fretboard)
        title = "🎸 Fretboard Trainer"
        fb = findViewById(R.id.fretView); tv = findViewById(R.id.tvFret)
        val spinner = findViewById<Spinner>(R.id.spinnerStrings)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, layouts.keys.toList())
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                fb.tuning = layouts.values.toList()[pos]; fb.invalidate(); nextTask()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        fb.onFret = { s, f ->
            val midi = fb.tuning[s] + f
            SynthEngine.note(midi, 900, 0.8f)
            if (findMode) {
                if (s == targetS && midi == targetMidi) {
                    score += 5; Stats.log(this, "fret", true)
                    tv.text = "✅ ${MusicTheoryData.NOTE_NAMES[midi % 12]} on string ${s + 1}, fret $f!  Score $score"
                    nextTask()
                } else {
                    Stats.log(this, "fret", false)
                    tv.text = "That's ${MusicTheoryData.NOTE_NAMES[midi % 12]} (string ${s + 1}, fret $f) — find ${MusicTheoryData.NOTE_NAMES[targetMidi % 12]} on string ${targetS + 1}${if (targetF > 0) ", fret $targetF" else " (open)"}"
                }
            } else {
                if (midi % 12 == targetMidi % 12) {
                    score += 3; Stats.log(this, "fret", true)
                    tv.text = "✅ ${MusicTheoryData.NOTE_NAMES[midi % 12]} — string ${s + 1}, fret $f!  +3  Score $score"
                    nextTask()
                } else {
                    Stats.log(this, "fret", false)
                    tv.text = "${MusicTheoryData.NOTE_NAMES[midi % 12]} — keep hunting ${MusicTheoryData.NOTE_NAMES[targetMidi % 12]} (amber dot shows one spot)"
                }
            }
            fb.invalidate()
        }
        findViewById<Button>(R.id.btnFretMode).setOnClickListener { findMode = !findMode; nextTask() }
        findViewById<Button>(R.id.btnFretEnd).setOnClickListener {
            GameProgress.save(this, "fret", score)
            startActivity(Intent(this, ResultsActivity::class.java)
                .putExtra("score", score).putExtra("type", "fret")); finish()
        }
        nextTask()
    }

    private fun nextTask() {
        fb.marks = emptySet(); fb.invalidate()
        targetS = (0 until fb.tuning.size).random()
        targetF = (0..12).random()
        targetMidi = fb.tuning[targetS] + targetF
        if (findMode) {
            tv.text = "Tap ${MusicTheoryData.NOTE_NAMES[targetMidi % 12]} on STRING ${targetS + 1}" +
                    if (targetF > 0) ", FRET $targetF" else " (OPEN string)"
        } else {
            fb.marks = setOf(targetS to targetF); fb.invalidate()
            tv.text = "The amber dot is one ${MusicTheoryData.NOTE_NAMES[targetMidi % 12]}. Find ANY OTHER fret with the same note!"
        }
    }
}
