package com.example.groovemaster

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class StaffView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    var clef = "Treble"
    var halfStep = 4f   // 0 = bottom line … 8 = top line (odd = space)

    private val line = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#F2F2F7"); strokeWidth = 3f }
    private val glyph = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFD54F"); textAlign = Paint.Align.CENTER }
    private val head = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFB300") }
    private val stem = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFB300"); strokeWidth = 6f }

    override fun onDraw(c: Canvas) {
        val w = width.toFloat()
        val gap = height / 7f
        val left = w * 0.20f
        val right = w * 0.94f
        val top = height / 2f - 2 * gap
        for (i in 0..4) c.drawLine(left, top + i * gap, right, top + i * gap, line)
        glyph.textSize = gap * 3.0f
        val g = when (clef) {
            "Treble" -> "\uD834\uDD1E"; "Bass" -> "\uD834\uDD22"; else -> "\uD834\uDD21"
        }
        c.drawText(g, left + gap, top + 4 * gap, glyph)
        val y = top + (8 - halfStep) * gap
        var p = -2
        while (p <= 10) {
            if (p < 0 || p > 8) {
                val ly = top + (8 - p) * gap
                c.drawLine(left - gap * 0.3f, ly, left + gap * 0.7f, ly, line)
            }
            p += 2
        }
        val hx = w * 0.60f
        c.drawOval(hx - gap * 0.55f, y - gap * 0.38f, hx + gap * 0.55f, y + gap * 0.38f, head)
        if (halfStep < 4) c.drawLine(hx + gap * 0.5f, y, hx + gap * 0.5f, y - gap * 3.2f, stem)
        else c.drawLine(hx - gap * 0.5f, y, hx - gap * 0.5f, y + gap * 3.2f, stem)
    }
}
