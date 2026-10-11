package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.SpacedRep
import com.example.groovemaster.data.StreakStore
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private data class Item(val emoji: String, val title: String, val sub: String, val route: String)
    private data class Section(val title: String, val items: List<Item>)
    private val bestViews = mutableMapOf<String, TextView>()
    private lateinit var tvTour: TextView
    private lateinit var tvDaily: TextView
    private lateinit var tvStreakHome: TextView
    private var debugTaps = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tvStreakHome = findViewById(R.id.tvStreak)
        buildMenu()
    }

    override fun onResume() {
        super.onResume()
        val stars = findViewById<TextView>(R.id.tvStars)
        stars.text = "⭐ ${GameProgress.totalStars(this)} / ${GameProgress.maxStars()} stars · 🧠 ${SpacedRep.totalTracked(this)} cards tracked"
        stars.setOnClickListener {
            if (++debugTaps >= 5) { debugTaps = 0; startActivity(Intent(this, DebugActivity::class.java)) }
            else if (debugTaps == 3) Toast.makeText(this, "2 more taps for the QA Lab…", Toast.LENGTH_SHORT).show()
        }
        bestViews.forEach { (key, tv) ->
            val b = GameProgress.best(this, key)
            tv.text = if (b > 0) "Best: $b  ${"★".repeat(GameProgress.starsFor(b))}" else "Not played yet"
        }
        tvTour.text = "${GameProgress.tourCount(this)} / ${GameProgress.TOUR_LESSONS} lessons complete"
        tvDaily.text = "${SpacedRep.dueCount(this)} cards due today"
        lifecycleScope.launch { tvStreakHome.text = StreakStore.homeLine(this@MainActivity) }
    }

    private fun buildMenu() {
        val container = findViewById<LinearLayout>(R.id.menuContainer)
        val tour = specialCard("🌍", "Guided World Tour", "20-lesson course: West Africa → Balkans → India → Bali", "tour")
        tvTour = tour.second; container.addView(tour.first)
        val daily = specialCard("📅", "Daily Challenge", "Your missed questions resurface here", "daily")
        tvDaily = daily.second; container.addView(daily.first)
        val stats = specialCard("📊", "Stats Dashboard", "Accuracy heatmap · 7/31-day activity · streaks", "stats")
        stats.second.text = ""
        container.addView(stats.first)

        val sections = listOf(
            Section("Melody & Harmony", listOf(
                Item("🎼","Scale Detective","Modes · Maqams · Ragas · Dastgāh · Echoi · Chant","quiz:scales"),
                Item("🎹","Chord Lab","Triads & 7th chords by ear","quiz:chords"),
                Item("👂","Interval Training","All 12 intervals","quiz:intervals"),
                Item("👆","Up or Down? Step or Leap?","Beginner-friendly entry ear training","quiz:updown"),
                Item("⚔️","Mode Duel — hard","Same tonic, two similar modes","quiz:modes_hard"),
                Item("🔊","Overtone Series","Which harmonic is sounding?","quiz:overtone"),
                Item("🎯","Odd One Out","4 scales, 3 same family — brutal","quiz:odd1"),
                Item("🧩","Chord Progressions","Which I–V–vi–IV did you hear?","quiz:roman"),
                Item("⛪","Harmonic Dictation","SATB 4-part, 7th chords","quiz:harmdict"),
                Item("🎯","Cadence Types","Authentic · Plagal · Deceptive · Half","quiz:cadence"),
                Item("🔀","Inversions","Root · 1st · 2nd","quiz:inversion"),
                Item("🏛️","Chord Function","Tonic? Subdominant? Dominant?","quiz:function"),
                Item("✍️","Melodic Dictation","Tonic + 3 notes","quiz:dictation"),
                Item("🎶","Name That Tune","Familiar melodies in random modes!","quiz:ntune"))),
            Section("Reading & Writing", listOf(
                Item("📜","Staff Sprint","Treble · Bass · Alto · Tenor — 45s speed round","staff"),
                Item("🔑","Key Signatures","How many ♯/♭? Which key?","quiz:keysig"),
                Item("⭕","Circle of Fifths","5th above D? Relative minors?","quiz:circle5"),
                Item("➕","Note Value Math","Dotted quarter + eighth = ?","quiz:notemath"),
                Item("⏱️","Time Signatures","Simple vs compound — eye & ear","quiz:timesig"))),
            Section("Rhythm Lab", listOf(
                Item("🥁","Groove Hunter","54 grooves · odd meters · tempo slider","rhythm"),
                Item("📖","Rhythm Reading","Read the notation, tap to the metronome","rread"),
                Item("🪘","Tap-Back — Ladder","Echo the groove; speeds up every round","tapback"),
                Item("💃","Compás Tap","Tap soleá's 3·6·8·10·12 — graded per hit","compas_tap"),
                Item("🌀","Polyrhythm Trainer","3:2 · 4:3 · 5:4 circles","poly"),
                Item("⏲️","BPM Guesser","Guess the tempo","quiz:bpm"),
                Item("🕳️","Swing or Straight?","Identify the subdivision","quiz:swing"))),
            Section("Instrument Labs", listOf(
                Item("🎹","Piano & Chord Builder","2 octaves — build Cm7♭5 with live audio","piano"),
                Item("🎸","Fretboard Trainer","Guitar · Oud · Bass · Sitar layouts","fret"),
                Item("🛠️","Groove Creator","16-step grid · accents · share as code","glab"),
                Item("🎤","Sing-Back","Mic checks your pitch","sing"))),
            Section("World Tunings & Forms", listOf(
                Item("🎛️","Microtonal Lab","Rast quarter-flats · 22 shrutis · JI vs ET","quiz:microtune"),
                Item("🏮","Gamelan","Slendro vs pelog","quiz:gamelan"),
                Item("🌅","Raga Room","Sargam · vadi/samvadi · pakads · time","quiz:raga"),
                Item("💃","Compás Quiz","Soleá · Bulerías · Alegrías","quiz:compas"),
                Item("🕰️","Temperaments A/B","Pythagorean · Meantone · Werckmeister","quiz:temperament"),
                Item("🏗️","Form & Blues","12-bar blues · AABA · rondo…","quiz:forms"),
                Item("📚","Theory Library","Browse & hear everything","library"),
                Item("🧠","Theory Trivia","All of the above, in words","quiz:theory")))
        )
        sections.forEach { sec ->
            container.addView(TextView(this).apply {
                text = sec.title; textSize = 15f
                setTextColor(ContextCompat.getColor(context, R.color.secondary))
                setTypeface(typeface, android.graphics.Typeface.BOLD)
                setPadding(dp(4), dp(20), 0, dp(4))
            })
            sec.items.forEach { container.addView(card(it)) }
        }
    }

    private fun specialCard(emoji: String, title: String, sub: String, route: String): Pair<MaterialCardView, TextView> {
        val c = MaterialCardView(this)
        c.setCardBackgroundColor(ContextCompat.getColor(this, R.color.cardBg))
        c.radius = dp(18).toFloat()
        c.strokeWidth = dp(2); c.strokeColor = ContextCompat.getColor(this, R.color.primary)
        c.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(12) }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(14), dp(16), dp(14)); gravity = Gravity.CENTER_VERTICAL }
        row.addView(TextView(this).apply { text = emoji; textSize = 27f })
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { marginStart = dp(14) } }
        col.addView(TextView(this).apply { text = title; textSize = 17f
            setTextColor(ContextCompat.getColor(context, R.color.primary))
            setTypeface(typeface, android.graphics.Typeface.BOLD) })
        col.addView(TextView(this).apply { text = sub; textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.textSecondary)) })
        val dyn = TextView(this).apply { textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.displayAmber)) }
        col.addView(dyn)
        row.addView(col); c.addView(row)
        c.setOnClickListener { launch(route) }
        return c to dyn
    }

    private fun card(item: Item): MaterialCardView {
        val c = MaterialCardView(this)
        c.setCardBackgroundColor(ContextCompat.getColor(this, R.color.cardBg))
        c.radius = dp(18).toFloat()
        c.layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT).apply { topMargin = dp(10) }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL
            setPadding(dp(16), dp(14), dp(16), dp(14)); gravity = Gravity.CENTER_VERTICAL }
        row.addView(TextView(this).apply { text = item.emoji; textSize = 27f })
        val col = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL
            layoutParams = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.WRAP_CONTENT, 1f)
                .apply { marginStart = dp(14) } }
        col.addView(TextView(this).apply { text = item.title; textSize = 17f
            setTextColor(ContextCompat.getColor(context, R.color.textPrimary))
            setTypeface(typeface, android.graphics.Typeface.BOLD) })
        col.addView(TextView(this).apply { text = item.sub; textSize = 12f
            setTextColor(ContextCompat.getColor(context, R.color.textSecondary)) })
        keyFor(item.route)?.let { key ->
            val best = TextView(this).apply { textSize = 11f
                setTextColor(ContextCompat.getColor(context, R.color.displayAmber)) }
            bestViews[key] = best; col.addView(best)
        }
        row.addView(col); c.addView(row)
        c.setOnClickListener { launch(item.route) }
        return c
    }

    private fun keyFor(route: String): String? = when {
        route.startsWith("quiz:") -> route.substringAfter("quiz:")
        route == "rhythm" -> "grooves"
        route == "staff" -> "staff"
        route == "tapback" -> "tapback"
        route == "sing" -> "sing"
        route == "piano" -> "piano"
        route == "fret" -> "fret"
        route == "rread" -> "rhythmread"
        route == "compas_tap" -> "compas_tap"
        else -> null
    }

    private fun launch(route: String) {
        when {
            route.startsWith("quiz:") -> startActivity(
                Intent(this, QuizActivity::class.java).putExtra("type", route.substringAfter("quiz:")))
            route == "rhythm" -> startActivity(Intent(this, RhythmActivity::class.java))
            route == "staff" -> startActivity(Intent(this, StaffActivity::class.java))
            route == "tapback" -> startActivity(Intent(this, TapBackActivity::class.java))
            route == "poly" -> startActivity(Intent(this, PolyrhythmActivity::class.java))
            route == "sing" -> startActivity(Intent(this, SingBackActivity::class.java))
            route == "tour" -> startActivity(Intent(this, TourActivity::class.java))
            route == "daily" -> startActivity(
                Intent(this, QuizActivity::class.java).putExtra("type", "daily"))
            route == "stats" -> startActivity(Intent(this, StatsActivity::class.java))
            route == "piano" -> startActivity(Intent(this, PianoActivity::class.java))
            route == "fret" -> startActivity(Intent(this, FretboardActivity::class.java))
            route == "rread" -> startActivity(Intent(this, RhythmReadingActivity::class.java))
            route == "glab" -> startActivity(Intent(this, GrooveLabActivity::class.java))
            route == "compas_tap" -> startActivity(Intent(this, CompasTapActivity::class.java))
            route == "library" -> startActivity(Intent(this, LibraryActivity::class.java))
        }
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
}
