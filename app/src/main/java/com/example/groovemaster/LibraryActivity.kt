package com.example.groovemaster

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.Spinner
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.groovemaster.audio.SynthEngine
import com.example.groovemaster.data.MusicTheoryData
import com.example.groovemaster.data.WesternTheoryData

class LibraryActivity : AppCompatActivity() {

    private var category = 0
    private lateinit var list: ListView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_library)
        title = "📚 Theory Library"
        val spinner = findViewById<Spinner>(R.id.spinnerCat)
        list = findViewById(R.id.listView)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf(
            "🌏 Scales · Maqams · Ragas · Dastgāh", "🎹 Chords",
            "🥁 Grooves · Compás · Tala · Timelines", "🌅 Ragas", "🔑 Key Signatures"))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: View?, pos: Int, id: Long) {
                category = pos
                list.adapter = ArrayAdapter(this@LibraryActivity,
                    android.R.layout.simple_list_item_1, titles())
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        list.setOnItemClickListener { _, _, pos, _ -> showDetail(pos) }
    }

    private fun titles(): List<String> = when (category) {
        0 -> MusicTheoryData.scales.map { "${it.family} · ${it.name}" }
        1 -> MusicTheoryData.chordTypes.map { it.name }
        2 -> MusicTheoryData.grooves.map { "${it.family} · ${it.name} (${it.meter})" }
        3 -> MusicTheoryData.ragas.map { "${it.name} — ${it.time}" }
        else -> WesternTheoryData.keySigs.map { "${it.major} / ${it.minor}" }
    }

    private fun showDetail(pos: Int) {
        when (category) {
            0 -> { val s = MusicTheoryData.scales[pos]
                AlertDialog.Builder(this).setTitle(s.name)
                    .setMessage("Family: ${s.family}\nSemitones: ${s.steps.joinToString(" – ")}\n" +
                        "In C: ${s.steps.map { MusicTheoryData.noteName(it) }.joinToString(" ")}\n\n${s.info}")
                    .setPositiveButton("▶ Hear it") { _, _ -> SynthEngine.playCents(s.steps + 1200.0) }
                    .setNegativeButton("Close", null).show() }
            1 -> { val c = MusicTheoryData.chordTypes[pos]
                AlertDialog.Builder(this).setTitle(c.name)
                    .setMessage("Semitones from root: ${c.steps.joinToString(" – ")}\n\n${c.info}")
                    .setPositiveButton("▶ Hear it") { _, _ -> SynthEngine.playChord(c.steps.map { 60 + it }) }
                    .setNegativeButton("Close", null).show() }
            2 -> { val g = MusicTheoryData.grooves[pos]
                AlertDialog.Builder(this).setTitle("${g.name} — ${g.origin}")
                    .setMessage("${g.meter} · ${g.bpm} BPM\n\n${g.info}\n\n" +
                        g.layers.joinToString("\n") { "${it.label}: ${it.pattern}" } +
                        (if (g.markers.isNotEmpty()) "\n" + g.markers.joinToString("\n") { "☆ ${it.label}: ${it.pattern}" } else ""))
                    .setPositiveButton("▶ Hear it") { _, _ -> SynthEngine.playGroove(g) }
                    .setNegativeButton("Close", null).show() }
            3 -> { val r = MusicTheoryData.ragas[pos]
                AlertDialog.Builder(this).setTitle(r.name)
                    .setMessage("Sargam: ${r.sargam}\nVadi: ${r.vadi} · Samvadi: ${r.samvadi}\n" +
                        "Time: ${r.time}\nPakad: ${r.pakad}\n\n${r.info}")
                    .setPositiveButton("▶ Hear scale") { _, _ ->
                        SynthEngine.playCents(r.semitones.map { it * 100.0 } + 1200.0) }
                    .setNeutralButton("▶ Hear pakad") { _, _ ->
                        SynthEngine.playCents(MusicTheoryData.pakadCents(r.pakad) + 1200.0, 240) }
                    .setNegativeButton("Close", null).show() }
            else -> { val k = WesternTheoryData.keySigs[pos]
                AlertDialog.Builder(this).setTitle(k.major)
                    .setMessage("${k.accidentals} ${if (k.sharp) "sharps" else "flats"}\n" +
                        "Major: ${k.major} · Relative minor: ${k.minor}")
                    .setPositiveButton("▶ Hear scale") { _, _ ->
                        val root = WesternTheoryData.keyRootSemitone(k.major)
                        SynthEngine.playCents(listOf(0.0,200.0,400.0,500.0,700.0,900.0,1100.0,1200.0)
                            .map { root * 100.0 + it }) }
                    .setNegativeButton("Close", null).show() }
        }
    }
}
