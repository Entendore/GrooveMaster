package com.example.groovemaster

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class PianoView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    val selected = mutableSetOf<Int>()
    var highlightMidi: Int? = null
    var onKey: ((Int) -> Unit)? = null

    private val whiteSemis = intArrayOf(0, 2, 4, 5, 7, 9, 11)
    private val blackAfter = setOf(0, 1, 3, 4, 5)
    private val pWhite = Paint().apply { color = Color.WHITE }
    private val pBlack = Paint().apply { color = Color.parseColor("#141432") }
    private val pSelW = Paint().apply { color = Color.parseColor("#FFB300") }
    private val pSelB = Paint().apply { color = Color.parseColor("#FF80AB") }
    private val pEdge = Paint().apply { color = Color.parseColor("#3A3A66"); style = Paint.Style.STROKE; strokeWidth = 2f }
    private val pTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#3A3A66"); textAlign = Paint.Align.CENTER }
    private val pHi = Paint().apply {
        color = Color.parseColor("#6EE7FF"); style = Paint.Style.STROKE; strokeWidth = 6f }

    fun midiOfWhite(k: Int) = 60 + (k / 7) * 12 + whiteSemis[k % 7]
    fun nameOf(midi: Int): String {
        val names = listOf("C","C♯","D","D♯","E","F","F♯","G","G♯","A","A♯","B")
        return names[((midi % 12) + 12) % 12] + (midi / 12 - 1)
    }

    override fun onDraw(c: Canvas) {
        val n = 14; val wW = width.toFloat() / n
        for (k in 0 until n) {
            val l = k * wW; val m = midiOfWhite(k)
            c.drawRect(l, 0f, l + wW, height.toFloat(), if (selected.contains(m)) pSelW else pWhite)
            c.drawRect(l, 0f, l + wW, height.toFloat(), pEdge)
            if (highlightMidi == m) c.drawRect(l + 3, 3f, l + wW - 3, height - 3f, pHi)
            pTxt.textSize = wW * 0.28f
            c.drawText(nameOf(m), l + wW / 2, height - 8f, pTxt)
        }
        val bW = wW * 0.62f; val bH = height * 0.6f
        for (k in 0 until n) {
            if (k % 7 in blackAfter) {
                val x = (k + 1) * wW - bW / 2; val m = midiOfWhite(k) + 1
                c.drawRect(x, 0f, x + bW, bH, if (selected.contains(m)) pSelB else pBlack)
                if (highlightMidi == m) c.drawRect(x + 3, 3f, x + bW - 3, bH - 3, pHi)
            }
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_DOWN) return true
        val n = 14; val wW = width.toFloat() / n
        val bW = wW * 0.62f; val bH = height * 0.6f
        for (k in 0 until n) {
            if (k % 7 in blackAfter) {
                val x = (k + 1) * wW - bW / 2
                if (e.x in x..(x + bW) && e.y <= bH) { onKey?.invoke(midiOfWhite(k) + 1); return true }
            }
        }
        val k = (e.x / wW).toInt().coerceIn(0, n - 1)
        onKey?.invoke(midiOfWhite(k))
        return true
    }
}
