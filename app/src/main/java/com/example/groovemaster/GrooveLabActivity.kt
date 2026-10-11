package com.example.groovemaster

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.GridLayout
import android.widget.SeekBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.groovemaster.audio.SynthEngine
import com.example.groovemaster.data.GrooveCode
import com.example.groovemaster.data.MusicTheoryData

class GrooveLabActivity : AppCompatActivity() {

    private val rows = listOf(
        Triple("Kick", "kick", 0), Triple("Snare", "snare", 1), Triple("Hat", "hat", 2),
        Triple("Clap", "clap", 3), Triple("Tom", "tom", 4), Triple("Wood", "wood", 5))

    private val gridState = Array(GrooveCode.INSTS.size) { CharArray(GrooveCode.STEPS) { '.' } }
    private val cells = ArrayList<List<View>>()
    private var tempo = 100
    private var looping = false
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_groovelab)
        title = "🛠️ Groove Lab"
        val gl = findViewById<GridLayout>(R.id.labGrid)
        gl.columnCount = GrooveCode.STEPS + 1
        for (r in rows) {
            val lbl = TextView(this).apply {
                text = r.first; textSize = 11f; gravity = Gravity.CENTER_VERTICAL
                setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
                layoutParams = GridLayout.LayoutParams().apply {
                    width = dp(46); height = dp(22); setMargins(0, 2, 4, 2) }
            }
            gl.addView(lbl)
            val row = mutableListOf<View>()
            for (c in 0 until GrooveCode.STEPS) {
                val v = View(this).apply {
                    layoutParams = GridLayout.LayoutParams().apply {
                        width = dp(16); height = dp(22); setMargins(1, 2, 1, 2) }
                    setOnClickListener { toggle(r.third, c) }
                    setOnLongClickListener { accent(r.third, c); true }
                }
                gl.addView(v); row.add(v)
            }
            cells.add(row)
        }
        paint()
        val seek = findViewById<SeekBar>(R.id.labTempo)
        seek.min = 60; seek.max = 160; seek.progress = tempo
        val tvT = findViewById<TextView>(R.id.labTvTempo); tvT.text = "Tempo $tempo BPM"
        seek.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(sb: SeekBar?, p: Int, u: Boolean) { tempo = p; tvT.text = "Tempo $p BPM" }
            override fun onStartTrackingTouch(sb: SeekBar?) {}
            override fun onStopTrackingTouch(sb: SeekBar?) {}
        })
        findViewById<Button>(R.id.btnLabPlay).setOnClickListener {
            looping = !looping
            findViewById<Button>(R.id.btnLabPlay).text = if (looping) "⏹ Stop" else "▶ Play loop"
            if (looping) playLoop() else SynthEngine.stopAll()
        }
        findViewById<Button>(R.id.btnLabClear).setOnClickListener {
            for (r in gridState.indices) gridState[r] = CharArray(GrooveCode.STEPS) { '.' }
            paint()
        }
        findViewById<Button>(R.id.btnLabSave).setOnClickListener { saveDialog() }
        findViewById<Button>(R.id.btnLabShare).setOnClickListener { share() }
        findViewById<Button>(R.id.btnLabImport).setOnClickListener { importDialog() }
        refreshSpinner()
        findViewById<Spinner>(R.id.labSaved).onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {
                override fun onItemSelected(p: android.widget.AdapterView<*>?, v: View?, pos: Int, id: Long) {
                    val names = savedNames()
                    if (pos in names.indices) {
                        val code = prefs().getString("gml_" + names[pos], "") ?: ""
                        GrooveCode.fromCode(code)?.let { (t, grid) ->
                            tempo = t
                            for (r in gridState.indices) gridState[r] = grid[r]
                            seek.progress = tempo; tvT.text = "Tempo $tempo BPM"
                            paint()
                        }
                    }
                }
                override fun onNothingSelected(p: android.widget.AdapterView<*>?) {}
            }
    }

    private fun prefs() = getSharedPreferences("groovelab", MODE_PRIVATE)
    private fun savedNames() = (prefs().getString("gml_names", "") ?: "").split("\n").filter { it.isNotBlank() }
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()

    private fun toggle(r: Int, c: Int) {
        gridState[r][c] = if (gridState[r][c] == '.') 'x' else '.'
        if (gridState[r][c] == 'x') SynthEngine.playInst(rows[r].second, 0.8f)
        paint()
    }
    private fun accent(r: Int, c: Int) {
        gridState[r][c] = if (gridState[r][c] == 'X') 'x' else 'X'
        if (gridState[r][c] == 'X') SynthEngine.playInst(rows[r].second, 1f)
        paint()
    }

    private fun paint() {
        val on = ContextCompat.getColor(this, R.color.stepOn)
        val acc = ContextCompat.getColor(this, R.color.stepOnAccent)
        val off = ContextCompat.getColor(this, R.color.stepOff)
        for (r in gridState.indices) for (c in 0 until GrooveCode.STEPS)
            cells[r][c].setBackgroundColor(when (gridState[r][c]) { 'X' -> acc; 'x' -> on; else -> off })
    }

    private fun buildGroove(): MusicTheoryData.Groove {
        val layers = rows.mapNotNull { (label, inst, r) ->
            val p = String(gridState[r])
            if (p.any { it != '.' }) MusicTheoryData.Layer(label, inst, p) else null }
        return MusicTheoryData.Groove("Lab", "You", "Custom", tempo, "4/4", 0f, "Your pattern", layers)
    }

    private fun playLoop() {
        if (!looping) return
        SynthEngine.playGroove(buildGroove(), 100, loops = 1,
            onEnd = { handler.postDelayed({ playLoop() }, 120) })
    }

    private fun share() {
        val code = GrooveCode.encode(tempo, gridState)
        (getSystemService(ClipboardManager::class.java)).setPrimaryClip(ClipData.newPlainText("groove", code))
        val send = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"; putExtra(Intent.EXTRA_TEXT, "My GrooveMaster groove:\n$code") }
        startActivity(Intent.createChooser(send, "Share groove"))
        Toast.makeText(this, "Copied to clipboard ✔", Toast.LENGTH_SHORT).show()
    }

    private fun saveDialog() {
        val et = EditText(this); et.hint = "Pattern name"
        AlertDialog.Builder(this).setTitle("Save pattern").setView(et)
            .setPositiveButton("Save") { _, _ ->
                val name = et.text.toString().trim().replace(";", "").replace("=", "").ifBlank { "Untitled" }
                val names = savedNames().toMutableList()
                if (name !in names) names.add(name)
                prefs().edit().putString("gml_names", names.joinToString("\n"))
                    .putString("gml_$name", GrooveCode.encode(tempo, gridState)).apply()
                refreshSpinner(); Toast.makeText(this, "Saved ✔", Toast.LENGTH_SHORT).show()
            }.setNegativeButton("Cancel", null).show()
    }

    private fun refreshSpinner() {
        findViewById<Spinner>(R.id.labSaved).adapter = ArrayAdapter(this,
            android.R.layout.simple_spinner_dropdown_item, listOf("Load saved…") + savedNames())
    }

    private fun importDialog() {
        val et = EditText(this); et.hint = "GML1;100;kick=x..x..."
        AlertDialog.Builder(this).setTitle("Import groove code").setView(et)
            .setPositiveButton("Import") { _, _ ->
                val r = GrooveCode.fromCode(et.text.toString().trim())
                if (r == null) Toast.makeText(this, "Bad code", Toast.LENGTH_SHORT).show()
                else { tempo = r.first
                    for (i in gridState.indices) gridState[i] = r.second[i]
                    paint() }
            }.setNegativeButton("Cancel", null).show()
    }

    override fun onDestroy() { super.onDestroy(); looping = false; SynthEngine.stopAll() }
}
