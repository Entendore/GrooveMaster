package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.groovemaster.audio.AudioCodec
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.Stats
import com.example.groovemaster.data.WorldTourData

class LessonActivity : AppCompatActivity() {

    private class FreshQ(val q: String, val options: List<String>, val ans: Int, val info: String)

    private var idx = 0
    private var qIdx = 0
    private var correct = 0
    private var finished = false
    private lateinit var lesson: WorldTourData.Lesson
    private lateinit var quiz: List<FreshQ>
    private lateinit var tvChapter: TextView; private lateinit var tvBody: TextView
    private lateinit var tvQ: TextView; private lateinit var tvFb: TextView
    private lateinit var btnDemo: Button; private lateinit var btnNext: Button
    private lateinit var opts: List<Button>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_lesson)
        idx = intent.getIntExtra("idx", 0)
        lesson = WorldTourData.lessons[idx]
        quiz = pickFresh()                       // fresh every entry/retry
        title = "Lesson ${idx + 1} · ${lesson.chapter}"
        tvChapter = findViewById(R.id.tvLessonTitle); tvBody = findViewById(R.id.tvLessonBody)
        tvQ = findViewById(R.id.tvLessonQ); tvFb = findViewById(R.id.tvLessonFb)
        btnDemo = findViewById(R.id.btnLessonDemo); btnNext = findViewById(R.id.btnLessonNext)
        opts = listOf(R.id.btnL1, R.id.btnL2, R.id.btnL3).map { findViewById<Button>(it) }
        tvChapter.text = lesson.title
        tvBody.text = lesson.body
        btnDemo.setOnClickListener { AudioCodec.play(lesson.audio) }
        opts.forEachIndexed { i, b -> b.setOnClickListener { answer(i) } }
        btnNext.setOnClickListener { onNext() }
        showQ()
    }

    /** 3 random questions from (authored 3 + 2 extras), options reshuffled. */
    private fun pickFresh(): List<FreshQ> {
        val pool = lesson.quiz + (WorldTourData.extraQuestions[idx] ?: emptyList())
        return pool.shuffled().take(3).map { q ->
            val o = q.options.shuffled()
            FreshQ(q.q, o, o.indexOf(q.options[q.ans]), q.info)
        }
    }

    private fun showQ() {
        val q = quiz[qIdx]
        tvQ.text = "Q${qIdx + 1}/${quiz.size}: ${q.q}"
        tvFb.text = ""
        opts.forEachIndexed { i, b ->
            b.text = q.options[i]; b.isEnabled = true
            b.setBackgroundColor(ContextCompat.getColor(this, R.color.optionBg))
        }
        btnNext.isEnabled = false
        btnNext.text = "Next →"
    }

    private fun answer(i: Int) {
        val q = quiz[qIdx]
        opts.forEach { it.isEnabled = false }
        val ok = i == q.ans
        Stats.log(this, "tour", ok)
        if (ok) { correct++; tvFb.setTextColor(ContextCompat.getColor(this, R.color.correct)) }
        else tvFb.setTextColor(ContextCompat.getColor(this, R.color.wrong))
        tvFb.text = (if (ok) "✅ " else "❌ ${q.options[q.ans]} — ") + q.info
        btnNext.isEnabled = true
    }

    private fun onNext() {
        if (!finished && qIdx < quiz.lastIndex) { qIdx++; showQ(); return }
        val passed = correct >= 2
        if (passed && !finished) {
            finished = true
            GameProgress.markTour(this, idx)
            tvQ.text = "🎓 Lesson complete! ($correct/3) — next unlocked."
            tvFb.text = ""
            btnNext.text = if (idx + 1 < WorldTourData.lessons.size) "Next lesson →" else "Back to tour"
            btnNext.setOnClickListener {
                if (idx + 1 < WorldTourData.lessons.size) {
                    startActivity(Intent(this, LessonActivity::class.java).putExtra("idx", idx + 1)); finish()
                } else { startActivity(Intent(this, TourActivity::class.java)); finish() }
            }
        } else if (!passed) {
            tvQ.text = "Almost — $correct/3. Retry with NEW questions (pool reshuffles every attempt)."
            tvFb.text = ""
            btnNext.text = "🔁 Retry (fresh questions)"
            btnNext.setOnClickListener {
                startActivity(Intent(this, LessonActivity::class.java).putExtra("idx", idx)); finish()
            }
        }
    }
}
