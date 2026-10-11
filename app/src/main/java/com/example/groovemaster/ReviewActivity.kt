package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.groovemaster.audio.AudioCodec
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.SpacedRep

object ReviewStore { var items: List<ReviewActivity.Miss> = emptyList() }

class ReviewActivity : AppCompatActivity() {

    data class Miss(val id: String, val mode: String, val prompt: String, val display: String?,
                    val options: List<String>, val answer: Int, val info: String, val audio: String?)

    private lateinit var items: List<Miss>
    private var idx = 0
    private var score = 0
    private var type = "theory"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_review)
        title = "🔍 Mistake Review"
        items = ReviewStore.items
        score = intent.getIntExtra("score", 0)
        type = intent.getStringExtra("type") ?: "theory"

        findViewById<Button>(R.id.btnRevReplay).setOnClickListener { AudioCodec.play(items[idx].audio) }
        findViewById<Button>(R.id.btnRevGot).setOnClickListener {
            if (items[idx].id.isNotEmpty()) SpacedRep.markCorrect(this, items[idx].id)
            advance()
        }
        findViewById<Button>(R.id.btnRevAgain).setOnClickListener {
            val m = items[idx]
            if (m.id.isNotEmpty()) SpacedRep.upsertWrong(this, SpacedRep.Item(
                m.id, m.mode, m.prompt, m.display, m.options, m.answer, m.info, m.audio,
                0, System.currentTimeMillis(), 1))
            advance()
        }
        show()
    }

    private fun show() {
        val m = items[idx]
        findViewById<TextView>(R.id.tvRevCount).text = "Missed ${idx + 1} / ${items.size}"
        findViewById<TextView>(R.id.tvRevPrompt).text = m.prompt
        val disp = findViewById<TextView>(R.id.tvRevDisplay)
        if (m.display == null) disp.visibility = View.GONE
        else { disp.visibility = View.VISIBLE; disp.text = m.display }
        findViewById<TextView>(R.id.tvRevAnswer).text =
            m.options.mapIndexed { i, o -> (if (i == m.answer) "✅ " else "　  ") + o }.joinToString("\n") +
                "\n\n${m.info}"
        findViewById<Button>(R.id.btnRevReplay).visibility =
            if (m.audio == null) View.GONE else View.VISIBLE
    }

    private fun advance() {
        idx++
        if (idx >= items.size) {
            GameProgress.save(this, type, score)
            startActivity(Intent(this, ResultsActivity::class.java)
                .putExtra("score", score).putExtra("type", type))
            finish()
        } else show()
    }
}
