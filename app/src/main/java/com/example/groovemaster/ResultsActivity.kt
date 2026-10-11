package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.groovemaster.data.GameProgress

class ResultsActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_results)
        val score = intent.getIntExtra("score", 0)
        val type = intent.getStringExtra("type") ?: "theory"
        val stars = GameProgress.starsFor(score)
        val best = GameProgress.best(this, type)
        findViewById<TextView>(R.id.tvScore).text = "$score"
        findViewById<TextView>(R.id.tvStars).text = "★".repeat(stars) + "☆".repeat(3 - stars)
        findViewById<TextView>(R.id.tvBest).text = "Personal best: $best"
        findViewById<TextView>(R.id.tvMsg).text = when (stars) {
            3 -> "Gold groove! You're locked in! 🏆"
            2 -> "Nice pocket! 🎶"
            1 -> "Keep practicing! 🎧"
            else -> "Every master started somewhere 🌱"
        }
        findViewById<Button>(R.id.btnAgain).setOnClickListener {
            val intent = when (type) {
                "grooves" -> Intent(this, RhythmActivity::class.java)
                "staff" -> Intent(this, StaffActivity::class.java)
                "tapback" -> Intent(this, TapBackActivity::class.java)
                "sing" -> Intent(this, SingBackActivity::class.java)
                "piano" -> Intent(this, PianoActivity::class.java)
                "fret" -> Intent(this, FretboardActivity::class.java)
                "rhythmread" -> Intent(this, RhythmReadingActivity::class.java)
                "compas_tap" -> Intent(this, CompasTapActivity::class.java)
                else -> Intent(this, QuizActivity::class.java).putExtra("type", type)
            }
            startActivity(intent); finish()
        }
        findViewById<Button>(R.id.btnHome).setOnClickListener {
            startActivity(Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        }
    }
}
