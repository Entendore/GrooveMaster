package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.Stats

class StaffActivity : AppCompatActivity() {

    private val LETTERS = listOf("C","D","E","F","G","A","B")
    private lateinit var staff: StaffView
    private lateinit var tvScore: TextView
    private lateinit var tvTimer: TextView
    private lateinit var tvInfo: TextView
    private lateinit var btnGo: Button
    private var score = 0
    private var timeLeft = 45
    private var running = false
    private var currentLetter = ""
    private var clefChoice = "All"
    private val handler = Handler(Looper.getMainLooper())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_staff)
        title = "📜 Staff Sprint"
        staff = findViewById(R.id.staffView)
        tvScore = findViewById(R.id.tvStaffScore)
        tvTimer = findViewById(R.id.tvTimer)
        tvInfo = findViewById(R.id.tvStaffInfo)
        btnGo = findViewById(R.id.btnGo)
        val spinner = findViewById<Spinner>(R.id.spinnerClef)
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item,
            listOf("All clefs","Treble","Bass","Alto","Tenor"))
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(p: AdapterView<*>?, v: android.view.View?, pos: Int, id: Long) {
                clefChoice = listOf("All","Treble","Bass","Alto","Tenor")[pos]
            }
            override fun onNothingSelected(p: AdapterView<*>?) {}
        }
        val ids = mapOf("C" to R.id.btnC, "D" to R.id.btnD, "E" to R.id.btnE, "F" to R.id.btnF,
                        "G" to R.id.btnG, "A" to R.id.btnA, "B" to R.id.btnB)
        ids.forEach { (letter, id) -> findViewById<Button>(id).setOnClickListener { onLetter(letter) } }
        btnGo.setOnClickListener {
            if (running) return@setOnClickListener
            score = 0; timeLeft = 45; running = true
            btnGo.isEnabled = false; tvInfo.text = ""
            tick(); nextNote()
        }
    }

    private fun tick() {
        tvTimer.text = "⏱️ ${timeLeft}s"
        if (timeLeft <= 0) { end(); return }
        timeLeft--
        handler.postDelayed({ if (running) tick() }, 1000)
    }

    private fun nextNote() {
        val clef = if (clefChoice == "All") listOf("Treble","Bass","Alto","Tenor").random() else clefChoice
        val hs = (0..8).random()
        currentLetter = staffNote(clef, hs)
        staff.clef = clef; staff.halfStep = hs.toFloat(); staff.invalidate()
        tvInfo.text = ""
    }

    private fun staffNote(clef: String, halfStep: Int): String {
        val (letterStart, octaveStart) = when (clef) {
            "Treble" -> 2 to 4   // bottom line = E4
            "Bass" -> 4 to 2     // bottom line = G2
            "Alto" -> 3 to 3     // bottom line = F3
            else -> 1 to 3       // Tenor: bottom line = D3
        }
        val abs = octaveStart * 7 + letterStart + halfStep
        return LETTERS[abs % 7]
    }

    private fun onLetter(letter: String) {
        if (!running) return
        val ok = letter == currentLetter
        Stats.log(this, "staff", ok)
        if (ok) { score += 2; tvInfo.text = "✅"; nextNote() }
        else { score = maxOf(0, score - 1); tvInfo.text = "❌ that was $currentLetter" }
        tvScore.text = "Score $score"
    }

    private fun end() {
        running = false; btnGo.isEnabled = true
        GameProgress.save(this, "staff", score)
        startActivity(Intent(this, ResultsActivity::class.java)
            .putExtra("score", score).putExtra("type", "staff"))
        finish()
    }
}
