package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.view.View
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

class PianoActivity : AppCompatActivity() {

    private var mode = 0 // 0 free, 1 find, 2 chord, 3 scale
    private var score = 0
    private var target = 60
    private var chordTarget: Set<Int> = emptySet()
    private var chordName = ""
    private var taskActive = false
    private lateinit var piano: PianoView
    private lateinit var tv: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_piano)
        title = "🎹 Piano & Chord Builder"
        piano = findViewById(R.id.pianoView); tv = findViewById(R.id.tvPiano)
        val spinner = findViewById<Spinner>(R.id.spinnerPiano)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("Free play","Find the note","Build a chord (live audio)","Build a scale"))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                mode = pos; nextTask()
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        piano.onKey = { midi ->
            SynthEngine.note(midi, 700, 0.8f)
            when (mode) {
                1 -> if (taskActive) {
                    if (midi == target) { score += 5; Stats.log(this, "piano", true)
                        tv.text = "✅ ${piano.nameOf(midi)}!  Score $score"; nextTask() }
                    else { Stats.log(this, "piano", false)
                        tv.text = "That was ${piano.nameOf(midi)} — find ${piano.nameOf(target)}" }
                }
                2, 3 -> if (taskActive) {
                    if (piano.selected.contains(midi)) piano.selected.remove(midi) else piano.selected.add(midi)
                    piano.invalidate()
                    val pcs = piano.selected.map { it % 12 }.toSet()
                    tv.text = "Selected: " + piano.selected.sorted().joinToString(" ") { piano.nameOf(it) } +
                            "\nTarget: $chordName"
                    if (pcs == chordTarget) {
                        score += 10; Stats.log(this, "piano", true)
                        tv.text = "✅ $chordName built!  +10  Score $score"
                        nextTask()
                    } else if (piano.selected.size > chordTarget.size)
                        tv.text = tv.text.toString() + "\n⚠️ too many notes"
                }
                else -> tv.text = piano.nameOf(midi)
            }
        }
        findViewById<Button>(R.id.btnPianoEnd).setOnClickListener {
            GameProgress.save(this, "piano", score)
            startActivity(Intent(this, ResultsActivity::class.java)
                .putExtra("score", score).putExtra("type", "piano")); finish()
        }
        tv.text = "Tap the keys 🎵"
    }

    private fun nextTask() {
        piano.highlightMidi = null
        piano.selected.clear(); piano.invalidate()
        taskActive = true
        when (mode) {
            1 -> { target = 60 + (0..24).random(); tv.text = "Tap ${piano.nameOf(target)}" }
            2 -> { val c = MusicTheoryData.chordTypes.random(); val root = (0..11).random()
                   chordTarget = c.steps.map { (root + it) % 12 }.toSet()
                   chordName = MusicTheoryData.NOTE_NAMES[root] + " " + c.name
                   tv.text = "Build: $chordName — toggle notes (they play as you toggle!)" }
            3 -> { val s = listOf(
                       MusicTheoryData.Scale("Major","x",listOf(0.0,2.0,4.0,5.0,7.0,9.0,11.0),""),
                       MusicTheoryData.Scale("Natural Minor","x",listOf(0.0,2.0,3.0,5.0,7.0,8.0,10.0),""),
                       MusicTheoryData.Scale("Major Pentatonic","x",listOf(0.0,2.0,4.0,7.0,9.0),""),
                       MusicTheoryData.Scale("Harmonic Minor","x",listOf(0.0,2.0,3.0,5.0,7.0,8.0,11.0),""),
                       MusicTheoryData.Scale("Blues","x",listOf(0.0,3.0,5.0,6.0,7.0,10.0),"")).random()
                   val root = (0..11).random()
                   chordTarget = s.steps.map { (root + it.toInt()) % 12 }.toSet()
                   chordName = MusicTheoryData.NOTE_NAMES[root] + " " + s.name
                   tv.text = "Build: $chordName (one octave, pitch classes)" }
        }
    }
}
