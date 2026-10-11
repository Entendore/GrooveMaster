package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.ListView
import androidx.appcompat.app.AppCompatActivity
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.WorldTourData

class TourActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_tour)
        title = "🌍 Guided World Tour"
        val list = findViewById<ListView>(R.id.tourList)
        val done = GameProgress.tourDone(this)
        val rows = WorldTourData.lessons.mapIndexed { i, l ->
            val mark = when {
                done.contains(i.toString()) -> "✅"
                i == 0 || done.contains((i - 1).toString()) -> "▶"
                else -> "🔒"
            }
            "$mark  ${i + 1}. ${l.chapter} — ${l.title}"
        }
        list.adapter = ArrayAdapter(this, android.R.layout.simple_list_item_1, rows)
        list.setOnItemClickListener { _, _, pos, _ ->
            if (pos == 0 || GameProgress.tourDone(this).contains((pos - 1).toString()))
                startActivity(Intent(this, LessonActivity::class.java).putExtra("idx", pos))
        }
    }
}
