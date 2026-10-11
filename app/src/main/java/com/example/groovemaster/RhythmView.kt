package com.example.groovemaster

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class RhythmView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    // (position in 16ths 0..32, kind "Q"/"E"/"S"/"ED", result null/pending, true/false)
    var events: List<Triple<Int, String, Boolean?>> = emptyList()

    private val pLine = Paint().apply { color = Color.parseColor("#9AA0B4"); strokeWidth = 3f }
    private val pNote = Paint().apply { color = Color.parseColor("#FFB300") }
    private val pOk = Paint().apply { color = Color.parseColor("#66BB6A") }
    private val pBad = Paint().apply { color = Color.parseColor("#EF5350") }
    private val pBar = Paint().apply { color = Color.parseColor("#F2F2F7"); strokeWidth = 5f }

    override fun onDraw(c: Canvas) {
        val y = height * 0.55f
        val x0 = width * 0.06f; val x1 = width * 0.94f
        c.drawLine(x0, y, x1, y, pLine)
        val total = 32f
        fun px(pos: Int) = x0 + (pos / total) * (x1 - x0)
        listOf(0, 16, 32).forEach { p -> c.drawLine(px(p), y - 60f, px(p), y + 60f, pBar) }
        events.forEach { (pos, kind, res) ->
            val paint = when (res) { null -> pNote; true -> pOk; else -> pBad }
            val x = px(pos)
            when (kind) {
                "Q" -> { c.drawOval(x - 14f, y - 10f, x + 14f, y + 10f, paint); c.drawLine(x + 12f, y, x + 12f, y - 62f, paint) }
                "ED" -> { c.drawOval(x - 14f, y - 10f, x + 14f, y + 10f, paint)
                          c.drawLine(x + 12f, y, x + 12f, y - 62f, paint)
                          c.drawCircle(x + 26f, y - 8f, 5f, paint) }
                "E" -> { c.drawOval(x - 14f, y - 10f, x + 14f, y + 10f, paint)
                         c.drawLine(x + 12f, y, x + 12f, y - 62f, paint)
                         c.drawLine(x + 12f, y - 62f, x + 34f, y - 44f, paint) }
                "S" -> { c.drawOval(x - 14f, y - 10f, x + 14f, y + 10f, paint)
                         c.drawLine(x + 12f, y, x + 12f, y - 62f, paint)
                         c.drawLine(x + 12f, y - 62f, x + 34f, y - 46f, paint)
                         c.drawLine(x + 12f, y - 52f, x + 34f, y - 34f, paint) }
            }
        }
    }
}
