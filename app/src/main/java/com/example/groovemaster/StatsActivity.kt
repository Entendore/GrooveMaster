package com.example.groovemaster

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.groovemaster.data.AnswerEvent
import com.example.groovemaster.data.AppDb
import com.example.groovemaster.data.GridRow
import com.example.groovemaster.data.Stats
import com.example.groovemaster.data.StreakLogic
import com.example.groovemaster.data.StreakState
import com.example.groovemaster.data.StreakStore
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class StatsActivity : AppCompatActivity() {

    private val LABELS = mapOf(
        "scales" to "🎼 Scales", "chords" to "🎹 Chords", "intervals" to "👂 Intervals",
        "modes_hard" to "⚔️ Mode Duel", "microtune" to "🎛️ Microtonal", "gamelan" to "🏮 Gamelan",
        "raga" to "🌅 Ragas", "compas" to "💃 Compás", "keysig" to "🔑 Key Sigs",
        "circle5" to "⭕ Circle of 5ths", "notemath" to "➕ Note Math", "timesig" to "⏱️ Time Sigs",
        "roman" to "🧩 Progressions", "cadence" to "🎯 Cadences", "inversion" to "🔀 Inversions",
        "function" to "🏛️ Function", "dictation" to "✍️ Dictation", "forms" to "🏗️ Forms",
        "swing" to "🕳️ Swing", "updown" to "👆 Up/Down", "overtone" to "🔊 Overtones",
        "odd1" to "🎯 Odd One Out", "ntune" to "🎶 Name That Tune", "temperament" to "🕰️ Temperaments",
        "bpm" to "⏲️ BPM", "harmdict" to "⛪ Harm. Dictation", "theory" to "🧠 Trivia",
        "daily" to "📅 Daily", "grooves" to "🥁 Grooves", "staff" to "📜 Staff",
        "tapback" to "🪘 Tap-Back", "rhythmread" to "📖 Rhythm Reading", "tour" to "🌍 World Tour",
        "compas_tap" to "🕺 Compás Tap", "piano" to "🎹 Piano", "fret" to "🎸 Fretboard",
        "sing" to "🎤 Sing-Back")

    private val HEAT = listOf("#3A3A66", "#5A4A1F", "#8A6D1F", "#C79A27", "#FFB300")
    private val FLAG_RED = "#EF5350"

    private var viewDays = 7
    private var nowMs = 0L

    private lateinit var container: LinearLayout
    private lateinit var streakContainer: LinearLayout
    private lateinit var gridContainer: LinearLayout
    private lateinit var gridScroll: HorizontalScrollView
    private lateinit var tvHead: TextView
    private lateinit var btn7: Button
    private lateinit var btn31: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_stats)
        title = "📊 Stats Dashboard"
        tvHead = findViewById(R.id.tvStatsHead)
        container = findViewById(R.id.statsContainer)
        streakContainer = findViewById(R.id.streakContainer)
        gridContainer = findViewById(R.id.gridContainer)
        gridScroll = findViewById(R.id.gridScroll)
        btn7 = findViewById(R.id.btnRange7); btn31 = findViewById(R.id.btnRange31)
        btn7.setOnClickListener { viewDays = 7; paintToggle(); refresh() }
        btn31.setOnClickListener { viewDays = 31; paintToggle(); refresh() }
        paintToggle()
        findViewById<Button>(R.id.btnStatsClear).setOnClickListener {
            lifecycleScope.launch { AppDb.get(this@StatsActivity).answers().clear(); refresh() }
        }
    }

    override fun onResume() { super.onResume(); refresh() }

    private fun paintToggle() {
        val on = ContextCompat.getColor(this, R.color.displayAmber)
        val off = ContextCompat.getColor(this, R.color.textSecondary)
        btn7.setTextColor(if (viewDays == 7) on else off)
        btn31.setTextColor(if (viewDays == 31) on else off)
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState); outState.putInt("viewDays", viewDays)
    }
    override fun onRestoreInstanceState(s: Bundle) {
        super.onRestoreInstanceState(s); viewDays = s.getInt("viewDays", 7); paintToggle()
    }

    private fun refresh() {
        lifecycleScope.launch {
            nowMs = System.currentTimeMillis()
            val dao = AppDb.get(this@StatsActivity).answers()
            val total = dao.total(); val ok = dao.totalCorrect()
            tvHead.text = if (total == 0) "Answer something first! 🎵"
                else "Overall: ${(100 * ok) / total}%  ·  $total answers logged"
            container.removeAllViews()
            dao.perMode().forEach { s ->
                val acc = if (s.total > 0) 100 * s.correct / s.total else 0
                container.addView(row(LABELS[s.mode] ?: s.mode, acc, s.total))
            }

            // streak (reconcile persists freezes/best)
            val year = dao.since(nowMs - 365L * 86_400_000L)
            val todayKey = Stats.dayKeyOf(nowMs)
            val st = StreakStore.reconcile(this@StatsActivity, year,
                Stats::dayKeyOf, todayKey, Stats::prevDayKey)
            val todayCount = year.count { Stats.dayKeyOf(it.ts) == todayKey }
            renderStreak(st.copy(todayCount = todayCount), todayKey)

            // grid + strip
            val period = dao.since(Stats.dayStart(viewDays - 1, nowMs))
            renderGrid(Stats.buildGrid(period, nowMs, viewDays),
                Stats.dayLabels(nowMs, viewDays), period.size, viewDays, period)
        }
    }

    private fun renderStreak(st: StreakState, todayKey: Long) {
        streakContainer.removeAllViews()
        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ContextCompat.getDrawable(context, R.drawable.bg_bar_back)
            setPadding(dp(14), dp(10), dp(14), dp(10))
        }
        card.addView(TextView(this).apply {
            text = "🔥 ${st.current}-day streak    🏆 Best: ${st.best}    " +
                    "❄️ ×${st.freezes} (max ${StreakLogic.FREEZE_CAP})"
            textSize = 16f; setTextColor(ContextCompat.getColor(context, R.color.primary))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
        })
        val todayName = SimpleDateFormat("EEE", Locale.getDefault()).format(Date(todayKey))
        when {
            st.canRestore -> {
                card.addView(TextView(this).apply {
                    text = "😱 You missed yesterday — spend a ❄️ to save the streak (today still counts!)"
                    textSize = 12f; setTextColor(ContextCompat.getColor(context, R.color.wrong))
                })
                card.addView(Button(this).apply {
                    text = "❄️ Restore yesterday's streak (−1 ❄️)"; textSize = 12f
                    setOnClickListener {
                        StreakStore.restore(context, Stats.prevDayKey(todayKey))
                        Toast.makeText(context, "Streak restored! 🔥", Toast.LENGTH_SHORT).show()
                        refresh()
                    }
                })
            }
            st.todayQualified -> card.addView(TextView(this).apply {
                text = "✅ $todayName is locked in — see you tomorrow!"
                textSize = 12f; setTextColor(ContextCompat.getColor(context, R.color.correct)) })
            st.current > 0 -> card.addView(TextView(this).apply {
                text = "⏳ ${st.answersNeededToday} more answer${if (st.answersNeededToday == 1) "" else "s"} today to keep the 🔥 alive"
                textSize = 12f; setTextColor(ContextCompat.getColor(context, R.color.displayAmber)) })
            else -> card.addView(TextView(this).apply {
                text = "Log ${StreakLogic.MIN_ANSWERS} answers in a day to start a streak (a 10-question round counts!)"
                textSize = 12f; setTextColor(ContextCompat.getColor(context, R.color.textSecondary)) })
        }
        streakContainer.addView(card)
    }

    private fun row(label: String, acc: Int, n: Int): LinearLayout {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL
            setPadding(0, dp(8), 0, dp(8))
        }
        row.addView(TextView(this).apply {
            text = label; textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
        })
        val barWrap = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            layoutParams = LinearLayout.LayoutParams(dp(120), dp(14)).apply { marginEnd = dp(8) }
            background = ContextCompat.getDrawable(context, R.drawable.bg_bar_back)
        }
        val color = when {
            n < 5 -> R.color.secondary
            acc >= 75 -> R.color.correct
            acc >= 50 -> R.color.displayAmber
            else -> R.color.wrong
        }
        barWrap.addView(LinearLayout(this).apply {
            setBackgroundColor(ContextCompat.getColor(context, color))
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, acc.coerceAtLeast(1).toFloat())
        })
        barWrap.addView(LinearLayout(this).apply {
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, (100 - acc).toFloat())
        })
        row.addView(barWrap)
        row.addView(TextView(this).apply {
            text = "$acc% · $n"; textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
        })
        return row
    }

    private fun renderGrid(rows: List<GridRow>, labels: List<String>,
                           periodTotal: Int, days: Int, period: List<AnswerEvent>) {
        gridContainer.removeAllViews()
        val title = if (days <= 7) "Last 7 days · $periodTotal answers"
            else "Last 31 days (${Stats.rangeLabel(nowMs, days)}) · $periodTotal answers"
        gridContainer.addView(TextView(this).apply {
            text = if (periodTotal == 0) "No activity in the last $days days 🌙" else title
            textSize = 15f; setTextColor(ContextCompat.getColor(context, R.color.primary))
            setTypeface(typeface, android.graphics.Typeface.BOLD)
            setPadding(0, dp(14), 0, dp(2))
        })
        if (rows.isEmpty()) return

        val cell = if (days <= 7) dp(16) else dp(13)
        val cellGap = if (days <= 7) dp(2) else dp(1)
        val labelSize = if (days <= 7) 10f else 8f

        val header = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        header.addView(spacer())
        labels.forEachIndexed { i, l ->
            header.addView(TextView(this).apply {
                text = if (i == labels.lastIndex) "$l▾" else l
                textSize = labelSize; gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context,
                    if (i == labels.lastIndex) R.color.displayAmber else R.color.textSecondary))
                layoutParams = LinearLayout.LayoutParams(cell + 2 * cellGap, dp(14))
            })
        }
        gridContainer.addView(header)

        // per-day totals strip
        val totals = Stats.dayTotals(period, nowMs, days)
        val maxT = totals.maxOfOrNull { it.count } ?: 0
        val stripH = dp(if (days <= 7) 46 else 38)
        val strip = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        strip.addView(spacer())
        totals.forEachIndexed { i, dt ->
            val col = FrameLayout(this).apply {
                layoutParams = LinearLayout.LayoutParams(cell + 2 * cellGap, stripH) }
            val barH = when {
                maxT == 0 || dt.count == 0 -> dp(3)
                else -> ((stripH - dp(4)) * dt.count / maxT).coerceIn(dp(6), stripH - dp(4))
            }
            col.addView(View(this).apply {
                setBackgroundColor(Color.parseColor(if (i == totals.lastIndex) "#FFB300" else "#C79A27"))
                layoutParams = FrameLayout.LayoutParams(cell, barH, Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL)
            })
            if (days <= 7 && dt.count > 0) col.addView(TextView(this).apply {
                text = "${dt.count}"; textSize = 8f; gravity = Gravity.CENTER
                setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
                layoutParams = FrameLayout.LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
                    FrameLayout.LayoutParams.WRAP_CONTENT, Gravity.TOP or Gravity.CENTER_HORIZONTAL) })
            val dayName = SimpleDateFormat(if (days <= 7) "EEE" else "EEE, MMM d",
                Locale.getDefault()).format(Date(Stats.dayStart(days - 1 - i, nowMs)))
            col.setOnClickListener { Toast.makeText(context,
                "$dayName: ${dt.count} answers across ${dt.modes} mode${if (dt.modes == 1) "" else "s"}",
                Toast.LENGTH_SHORT).show() }
            strip.addView(col)
        }
        gridContainer.addView(strip)

        // mode rows
        rows.forEach { r ->
            val line = LinearLayout(this).apply {
                orientation = LinearLayout.HORIZONTAL
                setPadding(0, dp(2), 0, dp(2)); gravity = Gravity.CENTER_VERTICAL }
            line.addView(TextView(this).apply {
                text = LABELS[r.mode] ?: r.mode
                textSize = 11f; maxLines = 1
                setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
                layoutParams = LinearLayout.LayoutParams(LABEL_W, LinearLayout.LayoutParams.WRAP_CONTENT) })
            r.cells.forEachIndexed { dayIdx, cellData ->
                val label = LABELS[r.mode] ?: r.mode
                val dayName = SimpleDateFormat(if (days <= 7) "EEE" else "EEE, MMM d",
                    Locale.getDefault()).format(Date(Stats.dayStart(days - 1 - dayIdx, nowMs)))
                val accTxt = if (cellData.count > 0) "${100 * cellData.correct / cellData.count}%" else "–"
                line.addView(View(this).apply {
                    setBackgroundColor(Color.parseColor(cellColor(cellData)))
                    layoutParams = LinearLayout.LayoutParams(cell, dp(16))
                        .apply { setMargins(cellGap, 0, cellGap, 0) }
                    setOnClickListener { Toast.makeText(context,
                        "$label · $dayName: ${cellData.count} answers, $accTxt",
                        Toast.LENGTH_SHORT).show() }
                })
            }
            line.addView(TextView(this).apply {
                text = "${r.periodTotal}"; textSize = labelSize; gravity = Gravity.END
                setTextColor(ContextCompat.getColor(context, R.color.textSecondary))
                layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                    .apply { marginStart = dp(6) }
            })
            gridContainer.addView(line)
        }
        gridScroll.post { gridScroll.fullScroll(View.FOCUS_RIGHT) }
    }

    private fun cellColor(cell: com.example.groovemaster.data.GridCell): String {
        if (cell.count >= 4 && cell.correct * 100 / cell.count < 50) return FLAG_RED
        return HEAT[Stats.intensityLevel(cell.count)]
    }

    private fun spacer(): View = View(this).apply {
        layoutParams = LinearLayout.LayoutParams(LABEL_W, 1)
    }

    private val LABEL_W get() = dp(118)
    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
