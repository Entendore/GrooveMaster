package com.example.groovemaster

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.floor

class FretboardView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) :
    View(context, attrs) {

    var tuning = listOf(40, 45, 50, 55, 59, 64)
    var marks: Set<Pair<Int, Int>> = emptySet()   // (stringIndex, fret)
    var onFret: ((Int, Int) -> Unit)? = null
    val FRETS = 12

    private val pString = Paint().apply { color = Color.parseColor("#F2F2F7"); strokeWidth = 3f }
    private val pFret = Paint().apply { color = Color.parseColor("#6b6f8a"); strokeWidth = 3f }
    private val pNut = Paint().apply { color = Color.WHITE; strokeWidth = 6f }
    private val pDot = Paint().apply { color = Color.parseColor("#3A3A66") }
    private val pMark = Paint().apply { color = Color.parseColor("#FFB300") }
    private val pTxt = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#9AA0B4"); textSize = 26f }

    override fun onDraw(c: Canvas) {
        val top = 46f; val x0 = width * 0.12f
        val fw = (width * 0.86f) / FRETS
        val sh = (height - top - 20f) / tuning.size
        for (f in 0..FRETS) {
            val x = x0 + f * fw
            c.drawLine(x, top, x, top + sh * tuning.size, if (f == 0) pNut else pFret)
        }
        for (s in tuning.indices) c.drawLine(x0, top + s * sh + sh / 2, x0 + FRETS * fw, top + s * sh + sh / 2, pString)
        for (s in tuning.indices) c.drawText("${s + 1}", 8f, top + s * sh + sh / 2 + 8f, pTxt)
        listOf(3, 5, 7, 9).forEach { f -> c.drawCircle(x0 + (f - 0.5f) * fw, top + sh * tuning.size / 2, 9f, pDot) }
        c.drawCircle(x0 + (FRETS - 0.5f) * fw, top + sh * tuning.size / 2, 9f, pDot)
        marks.forEach { (s, f) ->
            val cx = if (f == 0) x0 + fw * 0.35f else x0 + (f - 0.5f) * fw
            c.drawCircle(cx, top + s * sh + sh / 2, 17f, pMark)
        }
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if (e.action != MotionEvent.ACTION_DOWN) return true
        val top = 46f; val x0 = width * 0.12f
        val fw = (width * 0.86f) / FRETS
        val sh = (height - top - 20f) / tuning.size
        val s = (((e.y - top) / sh).toInt()).coerceIn(0, tuning.size - 1)
        val f = (floor((e.x - x0) / fw).toInt() + 1).coerceIn(0, FRETS)  // floor: left of nut = open
        onFret?.invoke(s, f)
        return true
    }
}
