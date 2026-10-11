package com.example.groovemaster

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

class PolyrhythmView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    var a = 3
    var b = 2
    var phase = 0f   // 0..1 per cycle

    private val ring = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; strokeWidth = 5f; color = Color.parseColor("#6EE7FF") }
    private val spoke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        strokeWidth = 3f; color = Color.parseColor("#9AA0B4") }
    private val mark = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#3A3A66") }
    private val dotA = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FFB300") }
    private val dotB = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#FF80AB") }

    override fun onDraw(c: Canvas) {
        val r = min(width * 0.20f, height * 0.34f)
        drawWheel(c, width * 0.28f, height / 2f, r, a, phase * a, dotA)
        drawWheel(c, width * 0.72f, height / 2f, r, b, phase * b, dotB)
    }

    private fun drawWheel(c: Canvas, cx: Float, cy: Float, r: Float, n: Int, turns: Float, dot: Paint) {
        c.drawCircle(cx, cy, r, ring)
        for (i in 0 until n) {
            val ang = (i.toFloat() / n) * 2 * PI.toFloat() - PI.toFloat() / 2
            c.drawCircle(cx + r * cos(ang), cy + r * sin(ang), r * 0.10f, mark)
        }
        val ang = (turns % 1f) * 2 * PI.toFloat() - PI.toFloat() / 2
        val px = cx + r * cos(ang); val py = cy + r * sin(ang)
        c.drawLine(cx, cy, px, py, spoke)
        c.drawCircle(px, py, r * 0.18f, dot)
    }
}
