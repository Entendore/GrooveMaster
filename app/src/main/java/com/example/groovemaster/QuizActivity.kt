package com.example.groovemaster

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.groovemaster.audio.AudioCodec
import com.example.groovemaster.audio.SynthEngine
import com.example.groovemaster.audio.Tuning
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.MusicTheoryData
import com.example.groovemaster.data.SpacedRep
import com.example.groovemaster.data.Stats
import com.example.groovemaster.data.WesternTheoryData
import kotlin.random.Random

class QuizActivity : AppCompatActivity() {

    private class Question(val prompt: String, val display: String?, val options: List<String>,
                           val answer: Int, val info: String, val audioSpec: String? = null,
                           val id: String? = null, val fromSR: Boolean = false,
                           val play: (() -> Unit)? = null)

    companion object {
        val QUIZ_TYPES = listOf("scales","chords","intervals","modes_hard","microtune","gamelan",
            "raga","compas","keysig","circle5","notemath","timesig","roman","cadence","inversion",
            "function","dictation","forms","swing","updown","overtone","odd1","ntune","temperament",
            "bpm","harmdict","theory","daily")
    }

    private lateinit var type: String
    private lateinit var questions: List<Question>
    private var theoryBank: List<MusicTheoryData.QuizQ> = emptyList()
    private var bankPos = 0
    private var qIndex = 0
    private var score = 0
    private var streak = 0
    private var answered = false
    private var hintUsed = false
    private val misses = mutableListOf<ReviewActivity.Miss>()

    private lateinit var tvPrompt: TextView; private lateinit var tvDisplay: TextView
    private lateinit var tvFeedback: TextView; private lateinit var tvScore: TextView
    private lateinit var tvStreak: TextView; private lateinit var tvProgress: TextView
    private lateinit var btnPlay: Button; private lateinit var btnNext: Button
    private lateinit var btnTier: Button; private lateinit var btnHint: Button; private lateinit var btnDrone: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var options: List<Button>

    private val scaleByName by lazy { MusicTheoryData.scales.associateBy { it.name } }
    private val ROMAN = listOf("I","ii","iii","IV","V","vi","vii°")
    private val TIERS = arrayOf("Beginner","Intermediate","Expert")
    private fun tier() = GameProgress.tier(this, type)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_quiz)
        type = intent.getStringExtra("type") ?: "theory"
        title = when (type) {
            "scales" -> "🎼 Scale Detective"; "chords" -> "🎹 Chord Lab"
            "intervals" -> "👂 Interval Training"; "modes_hard" -> "⚔️ Mode Duel"
            "microtune" -> "🎛️ Microtonal Lab"; "gamelan" -> "🏮 Gamelan"
            "raga" -> "🌅 Raga Room"; "compas" -> "💃 Compás Quiz"
            "keysig" -> "🔑 Key Signatures"; "circle5" -> "⭕ Circle of Fifths"
            "notemath" -> "➕ Note Value Math"; "timesig" -> "⏱️ Time Signatures"
            "roman" -> "🧩 Chord Progressions"; "cadence" -> "🎯 Cadence Types"
            "inversion" -> "🔀 Inversions"; "function" -> "🏛️ Chord Function"
            "dictation" -> "✍️ Melodic Dictation"; "forms" -> "🏗️ Form & Blues"
            "swing" -> "🕳️ Swing or Straight?"; "updown" -> "👆 Up · Down · Step · Leap"
            "overtone" -> "🔊 Overtone Series"; "odd1" -> "🎯 Odd One Out"
            "ntune" -> "🎶 Name That Tune"; "temperament" -> "🕰️ Temperaments A/B"
            "bpm" -> "⏲️ BPM Guesser"; "harmdict" -> "⛪ Harmonic Dictation"
            else -> "📅 Daily Challenge"
        }
        theoryBank = MusicTheoryData.theoryQuestions.shuffled()
        questions = List(10) { makeQuestion() }

        tvPrompt = findViewById(R.id.tvPrompt); tvDisplay = findViewById(R.id.tvDisplay)
        tvFeedback = findViewById(R.id.tvFeedback); tvScore = findViewById(R.id.tvScore)
        tvStreak = findViewById(R.id.tvStreak); tvProgress = findViewById(R.id.tvProgress)
        btnPlay = findViewById(R.id.btnPlay); btnNext = findViewById(R.id.btnNext)
        btnTier = findViewById(R.id.btnTier); btnHint = findViewById(R.id.btnHint); btnDrone = findViewById(R.id.btnDrone)
        progressBar = findViewById(R.id.progressBar)
        options = listOf(R.id.btnOpt1, R.id.btnOpt2, R.id.btnOpt3, R.id.btnOpt4).map { findViewById<Button>(it) }

        options.forEachIndexed { i, b -> b.setOnClickListener { onAnswer(i) } }
        btnPlay.setOnClickListener { questions[qIndex].play?.invoke() }
        btnNext.setOnClickListener { next() }

        btnTier.text = "Tier: ${TIERS[tier()]}"
        btnTier.setOnClickListener {
            val t = (tier() + 1) % 3
            GameProgress.setTier(this, type, t)
            btnTier.text = "Tier: ${TIERS[t]}"
            questions = questions.subList(0, qIndex) + List(questions.size - qIndex) { makeQuestion() }
            showQuestion()
        }
        btnHint.text = "50/50 (−5)"
        btnHint.setOnClickListener {
            val q = questions[qIndex]
            if (hintUsed || answered) return@setOnClickListener
            hintUsed = true; btnHint.isEnabled = false
            val wrongIdx = q.options.indices.filter { it != q.answer }
                .filter { options[it].visibility == View.VISIBLE }.shuffled().take(2)
            wrongIdx.forEach { options[it].isEnabled = false; options[it].alpha = 0.3f }
            score = maxOf(0, score - 5); tvScore.text = "Score $score"
        }
        btnDrone.text = if (GameProgress.drone(this)) "🎻 On" else "🎻 Off"
        btnDrone.setOnClickListener {
            val on = !GameProgress.drone(this)
            GameProgress.setDrone(this, on)
            btnDrone.text = if (on) "🎻 On" else "🎻 Off"
            SynthEngine.setDrone(on, 60)
        }
        showQuestion()
    }

    override fun onDestroy() { super.onDestroy(); SynthEngine.stopDrone() }

    private fun Q(prompt: String, display: String?, opts: List<String>, ans: Int,
                  info: String, spec: String? = null, play: (() -> Unit)? = null): Question {
        val id = "$type::" + (prompt + opts.joinToString("|")).hashCode()
        return Question(prompt, display, opts, ans, info, spec, id, false, play ?: { AudioCodec.play(spec) })
    }

    private fun makeQuestion(): Question =
        if (type == "daily") dailyQuestion() else typed(type)

    private fun typed(t: String): Question = when (t) {
        "scales" -> scaleQuestion(); "chords" -> chordQuestion()
        "intervals" -> intervalQuestion(); "modes_hard" -> modeDuelQuestion()
        "microtune" -> microtuneQuestion(); "gamelan" -> gamelanQuestion()
        "raga" -> ragaQuestion(); "compas" -> compasQuestion()
        "keysig" -> keySigQuestion(); "circle5" -> circleQuestion()
        "notemath" -> noteMathQuestion(); "timesig" -> timeSigQuestion()
        "roman" -> romanQuestion(); "cadence" -> cadenceQuestion()
        "inversion" -> inversionQuestion(); "function" -> functionQuestion()
        "dictation" -> dictationQuestion(); "forms" -> formsQuestion()
        "swing" -> swingQuestion(); "updown" -> upDownQuestion()
        "overtone" -> overtoneQuestion(); "odd1" -> oddOneOutQuestion()
        "ntune" -> nameThatTuneQuestion(); "temperament" -> temperamentQuestion()
        "bpm" -> bpmQuestion(); "harmdict" -> harmDictQuestion()
        else -> theoryQuestion()
    }

    private fun dailyQuestion(): Question {
        val due = SpacedRep.due(this, 10)
        if (due.isNotEmpty() && Random.nextInt(100) < 70) {
            val it = due.random()
            return Question(it.prompt, it.display, it.options, it.answer,
                "🔁 Resurfaced from ${it.mode} (missed ${it.wrong}×).\n${it.info}",
                it.audio, it.id, true) { AudioCodec.play(it.audio) }
        }
        return typed(listOf("scales","chords","intervals","theory","circle5","notemath","timesig",
            "swing","forms","roman","cadence","inversion","function","dictation","keysig","updown").random())
    }

    private fun opt(pool: List<String>, correct: String, count: Int): Pair<List<String>, Int> {
        val o = (pool.filter { it != correct }.distinct().shuffled().take(count - 1) + correct).shuffled()
        return o to o.indexOf(correct)
    }

    // ================= factories =================
    private fun scaleQuestion(): Question {
        val pool = if (tier() == 0)
            MusicTheoryData.scales.filter { it.family == "Western" && it.steps.all { s -> s % 1.0 == 0.0 } }
        else MusicTheoryData.scales
        val scale = pool.random()
        val rootC = if (tier() == 0) 0.0 else Random.nextInt(12) * 100.0
        val (opts, ans) = opt(pool.map { it.name }.distinct(), scale.name, if (tier() == 0) 3 else 4)
        val display = scale.steps.map { MusicTheoryData.noteName(rootC + it) }.joinToString("  ")
        return Q("Which scale is this?  (root = ${MusicTheoryData.noteName(rootC)})", display, opts, ans,
            "${scale.name}  [${scale.family}]\n${scale.info}",
            "s:${rootC.toInt()}:${scale.steps.joinToString(",")}")
    }

    private fun chordQuestion(): Question {
        val pool = if (tier() == 0) MusicTheoryData.chordTypes.take(5) else MusicTheoryData.chordTypes
        val chord = pool.random()
        val root = if (tier() == 0) 0 else Random.nextInt(12)
        val display = chord.steps.map { MusicTheoryData.NOTE_NAMES[(root + it) % 12] }.joinToString("  ")
        val (opts, ans) = opt(pool.map { it.name }, chord.name, if (tier() == 0) 3 else 4)
        return Q("Which chord quality is this?  (root = ${MusicTheoryData.NOTE_NAMES[root]})", display, opts, ans,
            "${MusicTheoryData.NOTE_NAMES[root]} = ${chord.steps.joinToString(" + ")} semitones.\n${chord.info}",
            "c:${chord.steps.map { 60 + root + it }.joinToString(",")}")
    }

    private fun intervalQuestion(): Question {
        val pool = if (tier() == 0) MusicTheoryData.intervals.filter { it.semitones in listOf(3,4,5,7,12) }
                   else MusicTheoryData.intervals
        val iv = pool.random()
        val root = 55 + Random.nextInt(12)
        val (opts, ans) = opt(pool.map { it.name }, iv.name, if (tier() == 0) 3 else 4)
        return Q("Which interval do you hear?", null, opts, ans,
            "${iv.name} = ${iv.semitones} semitone(s).", "i:$root:${iv.semitones}")
    }

    private fun modeDuelQuestion(): Question {
        val pairs = listOf("Dorian Mode" to "Natural Minor (Aeolian)","Mixolydian Mode" to "Major (Ionian)",
            "Maqam Hijaz" to "Byzantine (Double Harmonic)","Phrygian Mode" to "Maqam Kurd",
            "Harmonic Minor" to "Melodic Minor (asc.)","Yo Scale (Japan)" to "Gong Scale (China)")
        val (na, nb) = pairs.random()
        val a = scaleByName.getValue(na); val b = scaleByName.getValue(nb)
        val rootC = Random.nextInt(12) * 100.0
        val aFirst = Random.nextBoolean()
        val target = if (aFirst) a else b
        val spec = "ab:" + (if (aFirst) a else b).steps.joinToString(",") + "|" +
                   (if (aFirst) b else a).steps.joinToString(",") + "|200|pluck"
        return Q("Two scales, SAME tonic (${MusicTheoryData.noteName(rootC)}). Which was ${target.name}?",
            null, listOf("The FIRST scale","The SECOND scale"), if (aFirst) 0 else 1,
            "1st = ${(if (aFirst) a else b).name} · 2nd = ${(if (aFirst) b else a).name}\n${target.info}", spec)
    }

    private fun microtuneQuestion(): Question = when (Random.nextInt(4)) {
        0 -> { val jf = Random.nextBoolean()
            Q("The SAME major scale, tuned two ways. Which was PURE just intonation? (Which sounded sweeter?)",
                null, listOf("The FIRST one","The SECOND one"), if (jf) 0 else 1,
                "Just: pure 5:4 third at 386¢ — no beating.\nET stretches it to 400¢ — the wavering you heard.",
                "ab:" + (if (jf) Tuning.JI_MAJOR else Tuning.ET_MAJOR).joinToString(",") + "|" +
                        (if (jf) Tuning.ET_MAJOR else Tuning.JI_MAJOR).joinToString(",") + "|230|pluck") }
        1 -> { val rf = Random.nextBoolean()
            Q("Which of the two is RAST (quarter-flat 3rd & 7th)?",
                null, listOf("The FIRST one","The SECOND one"), if (rf) 0 else 1,
                "Rast: C D E↓ F G A B↓ C (~350¢ / ~950¢).\nNahawand ≈ natural minor.",
                "ab:" + (if (rf) Tuning.RAST else Tuning.NAHAWAND).joinToString(",") + "|" +
                        (if (rf) Tuning.NAHAWAND else Tuning.RAST).joinToString(",") + "|230|pluck") }
        2 -> { val lowFirst = Random.nextBoolean()
            Q("Both are KOMAL Re — two different SHRUTIS (~90¢ vs ~112¢). Which was SHARPER?",
                null, listOf("The FIRST one","The SECOND one"), if (lowFirst) 1 else 0,
                "The 22 shrutis split each swara into two shades ~22¢ apart.",
                "ab:" + (if (lowFirst) "90" else "112") + "|" + (if (lowFirst) "112" else "90") + "|700|pluck") }
        else -> Q("The just (pure) fifth 'Pa' beats the ET fifth by how much?",
            null, listOf("2 cents","20 cents","50 cents","It's identical"), 0,
            "Just = 702¢ (3:2); ET = 700¢ — close enough that ET works.")
    }

    private fun gamelanQuestion(): Question = if (Random.nextBoolean()) {
        val sf = Random.nextBoolean()
        Q("Two gamelan tunings. Which was SLENDRO (5 nearly-equal tones)?",
            null, listOf("The FIRST one","The SECOND one"), if (sf) 0 else 1,
            "Slendro: 5 × ≈240¢ — no pure 3rds anywhere.\nPelog: uneven 7. Both vary per ensemble!",
            "ab:" + (if (sf) Tuning.SLENDRO else Tuning.PELOG).joinToString(",") + "|" +
                    (if (sf) Tuning.PELOG else Tuning.SLENDRO).joinToString(",") + "|260|metal")
    } else {
        val bank = listOf(
            Question("Pelog theoretically contains how many tones?", null, listOf("7","5","9","12"), 0, "Uneven 7; a pathet selects ~5."),
            Question("'Gamelan' properly refers to…", null, listOf("The ensemble itself","One gong","A scale","A dance"), 0, "The whole orchestra — and its paired tunings."),
            Question("Why do slendro melodies avoid Western 3rds?", null,
                listOf("Its tuning has no near-pure 3rds","3rds are forbidden","Instruments can't reach them","Only at funerals"), 0,
                "With ≈240¢ steps, no stack makes a 5:4."),
            Question("The two kotekan parts are called…", null, listOf("Polos & sangsih","Sa & Pa","Um & ya","Yin & yang"), 0, "One fast line split between two players."),
            Question("Gamelan pairs tuned slightly apart symbolize…", null,
                listOf("A deliberate shimmering aesthetic ('ombak')","A mistake","Colonial influence","Electronics"), 0, "The 'wave' is intentional beauty."))
        bank.random()
    }

    private fun ragaQuestion(): Question {
        val ragas = MusicTheoryData.ragas
        return when (Random.nextInt(6)) {
            0 -> { val r = ragas.random()
                val (opts, ans) = opt(ragas.map { it.name }, r.name, 4)
                Q("Which raga is this?", null, opts, ans,
                    "${r.name} — ${r.time}. Vadi ${r.vadi}, samvadi ${r.samvadi}.",
                    "s:0:${r.semitones.joinToString(",")}") }
            1 -> { val r = ragas.random()
                val (opts, ans) = opt(ragas.map { it.vadi }.distinct(), r.vadi, 4)
                Q("Which note is the VADI (king note) of ${r.name}?", null, opts, ans,
                    "Vadi = the melody's home note. ${r.name}: vadi ${r.vadi}, samvadi ${r.samvadi}.") }
            2 -> { val r = ragas.random()
                val (opts, ans) = opt(ragas.map { it.time }.distinct(), r.time, 4)
                Q("When is ${r.name} traditionally performed?", null, opts, ans,
                    "Ragas belong to times of day — ${r.name}: ${r.time}.") }
            3 -> { val r = ragas.random()
                val (opts, ans) = opt(ragas.map { it.pakad }, r.pakad, 4)
                Q("Which PAKAD (signature phrase) belongs to ${r.name}?", r.pakad, opts, ans,
                    "${r.name}: ${r.pakad}\n${r.info}", "pk:${r.name}") }
            4 -> { val r = ragas.random()
                val (opts, ans) = opt(ragas.map { it.name }, r.name, 4)
                Q("Hear the PAKAD — which raga sings it?", null, opts, ans,
                    "${r.name} · ${r.time} · vadi ${r.vadi}.", "pk:${r.name}") }
            else -> Q("The tanpura drone plays Sa & Pa because…", null,
                listOf("They anchor the tonic and its purest anchor-point","They are the loudest","It's tradition only","Pa hides the singer"), 0,
                "Sa–Pa (3:2) is the most stable frame every raga bends around.")
        }
    }

    private fun compasQuestion(): Question = if (Random.nextBoolean()) {
        val spain = MusicTheoryData.grooves.filter { it.family == "Spain (Flamenco)" }
        val g = spain.random()
        val (opts, ans) = opt(spain.map { it.name }, g.name, 4)
        Q("Which flamenco compás (palo) is this?", null, opts, ans,
            "${g.name} · ${g.meter}: ${g.info}", "g:${g.name}:0")
    } else {
        val bank = listOf(
            Triple("The compás of SOLEÁ has how many counts?", listOf("12","6","4","8"), "12 — accents on 3, 6, 8, 10 & 12."),
            Triple("SOLEÁ's accents fall on counts…", listOf("3, 6, 8, 10, 12","1, 2, 3, 4","2 and 4","1 and 5"), "Displaced stresses = the flamenco push."),
            Triple("BULERÍAS is usually felt as…", listOf("Fast 12 with the '1' on the last count","Slow 4","A waltz","5/8"), "Fast, playful; the downbeat rides count 12."),
            Triple("TANGO FLAMENCO (the palo) lives in…", listOf("A 4-count groove","12/8","7/8","Free rhythm"), "4/4 with sharp pick-up accents."))
        val (q, o, info) = bank.random()
        Q(q, null, o, 0, info)
    }

    private fun keySigQuestion(): Question {
        val ks = WesternTheoryData.keySigs.random()
        val accWord = if (ks.sharp) "sharps" else "flats"
        return when (Random.nextInt(3)) {
            0 -> {
                val correct = if (ks.accidentals == 0) "C" else ks.major
                val (o, a) = opt(WesternTheoryData.keySigs.map { it.major }, correct, 4)
                val prompt = if (ks.accidentals == 0) "Which MAJOR key has NO sharps or flats?"
                             else "Key signature: ${ks.accidentals} $accWord — which MAJOR key?"
                Q(prompt, null, o, a,
                    "$correct major: ${if (ks.accidentals == 0) "no accidentals" else "${ks.accidentals} $accWord"} · relative minor ${ks.minor}.")
            }
            1 -> { val o = ((listOf("0","1","2","3","4","5","6","7") - "${ks.accidentals}").shuffled().take(3) + "${ks.accidentals}").shuffled()
                Q("How many $accWord does ${ks.major} major have?", o, o, o.indexOf("${ks.accidentals}"),
                    "${ks.major} major = ${ks.accidentals} $accWord.") }
            else -> { val (o, a) = opt(WesternTheoryData.keySigs.map { it.minor }, ks.minor, 4)
                Q("Which MINOR key has ${ks.accidentals} $accWord?", o, o, a,
                    "${ks.minor} shares its signature with ${ks.major} major.") }
        }
    }

    private fun circleQuestion(): Question {
        val W = WesternTheoryData
        return when (Random.nextInt(4)) {
            0 -> { val k = W.CIRCLE.random(); val ans = W.fifthAbove(k)
                val (o, a) = opt(W.CIRCLE, ans, 4)
                Q("A perfect 5th ABOVE $k is…", null, o, a, "Clockwise on the circle: $k → $ans.") }
            1 -> { val k = W.CIRCLE.random(); val ans = W.relMinor(k)
                val (o, a) = opt(W.keySigs.map { it.minor }, ans, 4)
                Q("The RELATIVE MINOR of $k major is…", null, o, a, "Same signature, minor home: $k ↔ $ans.") }
            2 -> { val m = W.keySigs.random().minor; val ans = W.relMajor(m)
                val (o, a) = opt(W.CIRCLE, ans, 4)
                Q("The RELATIVE MAJOR of $m is…", null, o, a, "$m borrows its signature from $ans major.") }
            else -> Q("One step CLOCKWISE on the circle of 5ths…", null,
                listOf("Adds one ♯ (or removes one ♭)","Adds one ♭","Adds two ♯","Nothing changes"), 0,
                "Each clockwise step = up a fifth = one more ♯.")
        }
    }

    private fun noteMathQuestion(): Question {
        val NV = WesternTheoryData.NOTE_VALUES
        return if (Random.nextBoolean()) {
            val validSums = NV.values.toSet()
            val pairs = NV.keys.flatMap { a -> NV.keys.map { b -> a to b } }
                .filter { (a, b) -> (NV.getValue(a) + NV.getValue(b)) in validSums }   // FIXED: sum must be a real note value
            val (a, b) = pairs.random()
            val sum = NV.getValue(a) + NV.getValue(b)
            val ans = NV.entries.first { it.value == sum }.key
            val (o, idx) = opt(NV.keys.toList(), ans, 4)
            Q("$a  +  $b  = ?", null, o, idx,
                "$a = ${NV[a]} sixteenths · $b = ${NV[b]} → $sum sixteenths = $ans.")
        } else {
            val (name, v) = NV.entries.filter { it.key != "Sixteenth" }.random()
            val o = ((listOf("1","2","3","4","6","8","12","16") - "$v").shuffled().take(3) + "$v").shuffled()
            Q("How many SIXTEENTH notes fit in one $name?", o, o, o.indexOf("$v"), "$name = $v sixteenths.")
        }
    }

    private fun timeSigQuestion(): Question = when (Random.nextInt(3)) {
        0 -> { val beats = listOf(2,3,4).random(); val sub = listOf(2,3).random()
            Q("Listen to the accents. How many BEATS per bar?", null, listOf("2","3","4"), beats - 2,
                "$beats beats × $sub subdivision → ${if (sub == 2) "SIMPLE" else "COMPOUND"} meter.",
                "cg:$beats:$sub") }
        1 -> { val sub = listOf(2,3).random()
            Q("Listen. Is the meter SIMPLE or COMPOUND?", null,
                listOf("Simple — beat splits in 2","Compound — beat splits in 3"), sub - 2,
                "Each beat subdivides into $sub → ${if (sub == 2) "simple" else "compound"}.", "cg:4:$sub") }
        else -> { val ts = listOf("2/4" to (2 to 2),"3/4" to (3 to 2),"4/4" to (4 to 2),
                                  "6/8" to (2 to 3),"9/8" to (3 to 3),"12/8" to (4 to 3)).random()
            Q("${ts.first} — is it SIMPLE or COMPOUND time?", ts.first, listOf("Simple","Compound"),
                if (ts.second.second == 2) 0 else 1,
                "${ts.first} = ${ts.second.first} beats dividing in ${ts.second.second}.") }
    }

    private fun triadName(rootMidi: Int, deg: Int): String =
        MusicTheoryData.noteName(((rootMidi + listOf(0,2,4,5,7,9,11)[deg]) % 12) * 100.0) +
                listOf("","m","m","","","m","dim")[deg]

    private fun romanQuestion(): Question {
        val root = 60 + Random.nextInt(12)
        val pool = listOf(listOf(0,4,5,3), listOf(5,3,0,4), listOf(0,5,1,4),
                          listOf(1,4,0), listOf(0,3,4,0), listOf(0,4,3,0))
        val prog = pool.random()
        val correct = prog.joinToString("–") { ROMAN[it] }
        val (opts, ans) = opt(pool.map { p -> p.joinToString("–") { ROMAN[it] } }, correct, 4)
        return Q("In ${MusicTheoryData.noteName((root % 12) * 100.0)} major — which progression do you hear?",
            null, opts, ans, "$correct → ${prog.joinToString(" ") { triadName(root, it) }}",
            "p:$root:${prog.joinToString(",")}")
    }

    private fun cadenceQuestion(): Question {
        val root = 60 + Random.nextInt(12)
        val kinds = listOf(Triple("Authentic (V → I)", listOf(4,0), "Dominant → tonic: the full stop."),
            Triple("Plagal (IV → I)", listOf(3,0), "Subdominant → tonic: the 'Amen'."),
            Triple("Deceptive (V → vi)", listOf(4,5), "V 'lies' — lands up on vi."),
            Triple("Half (I → V)", listOf(0,4), "Ends ON the dominant — unfinished."))
        val (name, degs, info) = kinds.random()
        val (opts, ans) = opt(kinds.map { it.first }, name, 4)
        return Q("Which cadence do you hear?", null, opts, ans, "$name — $info", "p:$root:${degs.joinToString(",")}")
    }

    private fun inversionQuestion(): Question {
        val root = 60 + Random.nextInt(12)
        val s = if (Random.nextBoolean()) listOf(0,4,7) else listOf(0,3,7)
        val inv = Random.nextInt(3)
        val midis = when (inv) { 0 -> listOf(root, root+s[1], root+s[2])
            1 -> listOf(root+s[1], root+s[2], root+12)
            else -> listOf(root+s[2], root+12, root+12+s[1]) }
        val nm = MusicTheoryData.noteName((root % 12) * 100.0) + if (s[1] == 4) "" else "m"
        return Q("Which inversion of $nm is this?",
            "Bass note: ${MusicTheoryData.noteName((midis[0] % 12) * 100.0)}",
            listOf("Root position","1st inversion","2nd inversion"), inv,
            "Root position = root in bass · 1st = 3rd · 2nd = 5th.",
            "c:${midis.joinToString(",")}")
    }

    private fun functionQuestion(): Question {
        val root = 60 + Random.nextInt(12)
        val (deg, func) = listOf(0 to "Tonic", 1 to "Subdominant", 3 to "Subdominant",
                                 4 to "Dominant", 5 to "Mediant").random()
        val (opts, ans) = opt(listOf("Tonic","Subdominant","Dominant","Mediant"), func, 4)
        return Q("You hear I — then one more chord. Its FUNCTION is…?", null, opts, ans,
            "Degree ${ROMAN[deg]} = $func function.", "p:$root:0,${deg}")
    }

    private fun dictationQuestion(): Question {
        val steps = listOf(0,2,4,5,7,9)
        val mel = List(3) { steps.random() }
        val correct = mel.joinToString(" · ") { MusicTheoryData.NOTE_NAMES[it % 12] }
        fun mutate(): String {                       // FIXED: one index, one changed value
            val c = mel.toMutableList()
            val i = (0 until c.size).random()
            c[i] = steps.filter { it != c[i] }.random()
            return c.joinToString(" · ") { MusicTheoryData.NOTE_NAMES[it % 12] }
        }
        val (opts, ans) = opt(listOf(mutate(), mutate(), mutate()), correct, 4)
        return Q("Tonic C, then 3 notes. Which melody is it?", "C · ? · ? · ?", opts, ans,
            "It was: $correct — hum it back to lock it in!", "d:${mel.joinToString(",")}")
    }

    private fun formsQuestion(): Question {
        val isBlues = Random.nextBoolean()
        return if (isBlues) {
            val root = 60 + Random.nextInt(12)
            val (opts, ans) = opt(listOf("12-bar blues","AABA (32-bar form)","Rondo (ABACA)","Verse–chorus"), "12-bar blues", 4)
            Q("Which FORM do you hear?", null, opts, ans,
                "I×4 · IV×2 · I×2 · V–IV–I–V — the 12-bar blues.",
                "p:$root:0,0,0,0,5,5,0,0,7,5,0,7")
        } else {
            val A = listOf(0,4,7,4,9,7,4,2); val B = listOf(12,9,7,4,5,2,0,4)
            val midis = (A + A + B + A).map { 60 + it }
            val (opts, ans) = opt(listOf("AABA (32-bar form)","12-bar blues","Rondo (ABACA)","Verse–chorus"), "AABA (32-bar form)", 4)
            Q("Which FORM do you hear?", null, opts, ans,
                "A · A · contrasting bridge · A — the 32-bar standard.",
                "m:${midis.joinToString(",")}:170")
        }
    }

    private fun swingQuestion(): Question = if (Random.nextBoolean()) {
        val swung = Random.nextBoolean()
        Q("Listen to the 8th-notes. Straight or swung?", null, listOf("Straight eighths","Swung eighths"),
            if (swung) 1 else 0,
            if (swung) "Swung: first 8th ≈ 2/3 of the beat (triplet feel)."
            else "Straight: perfectly even 50/50.", "sw:${if (swung) 1 else 0}")
    } else Q("Swung eighths are closest in feel to…", null,
        listOf("Triplets (2/3 + 1/3)","Even 16ths","Quintuplets","Dotted 16ths"), 0,
        "The pair fills one triplet — long-short.")

    private fun upDownQuestion(): Question {
        val dir = if (Random.nextBoolean()) 1 else -1
        val semi = if (tier() == 0) listOf(1,2,3).random()
                   else (if (Random.nextBoolean()) listOf(1,2).random() else listOf(5,6,7,8).random())
        val root = 55 + Random.nextInt(12)
        val opts = listOf("Higher — by STEP","Higher — by LEAP","Lower — by STEP","Lower — by LEAP")
        val ans = when { dir == 1 && semi <= 2 -> 0; dir == 1 -> 1; semi <= 2 -> 2; else -> 3 }
        return Q("Two notes. Which way did it move — step or leap?", null, opts, ans,
            "It went ${if (dir == 1) "UP" else "DOWN"} by $semi semitones = ${if (semi <= 2) "a step" else "a leap"}.",
            "i:$root:${dir * semi}")
    }

    private fun overtoneQuestion(): Question {
        val names = mapOf(2 to "2nd — octave", 3 to "3rd — octave + fifth", 4 to "4th — 2 octaves",
            5 to "5th — major 3rd up high", 6 to "6th — fifth (2 oct)", 7 to "7th — flat 'blues' 7th",
            8 to "8th — 3 octaves")
        val maxN = if (tier() == 0) 5 else 8
        val n = (2..maxN).random()
        val (opts, ans) = opt((2..maxN).map { names[it]!! }, names[n]!!, 4)
        return Q("Which HARMONIC of the series is sounding? (base note A2)", null, opts, ans,
            "It was harmonic $n. Harmonics 2·4·8 = octaves; 3 = fifth; 5 = major third; 4+5+6 = a major triad — the origin of harmony!",
            "n:110:$n")
    }

    private fun oddOneOutQuestion(): Question {
        val S = MusicTheoryData.scales
        val fams = S.map { it.family }.distinct().filter { f -> S.count { it.family == f } >= 3 }
        val fam = fams.random()
        val trio = S.filter { it.family == fam }.shuffled().take(3)
        val outsider = S.filter { it.family != fam }.random()
        val four = (trio + outsider).shuffled()
        val spec = "o4:" + four.joinToString("|") { it.steps.joinToString(",") }
        return Q("FOUR scales. Three share a family — which one is the ODD ONE OUT?",
            "1 · 2 · 3 · 4", listOf("Scale 1","Scale 2","Scale 3","Scale 4"),
            four.indexOf(outsider),
            four.mapIndexed { i, s -> "${i + 1}. ${s.name} [${s.family}]" }.joinToString("\n"), spec)
    }

    private data class Tune(val name: String, val notes: List<Pair<Int, Int>>)
    private val tunes = listOf(
        Tune("Ode to Joy", listOf(3,3,4,5,5,4,3,2,1,1,2,3,3,2,2).map { it to 0 }),
        Tune("Twinkle Twinkle", listOf(1,1,5,5,6,6,5,4,4,3,3,2,2,1).map { it to 0 }),
        Tune("Frère Jacques", listOf(1,2,3,1,1,2,3,1,3,4,5,3,4,5).map { it to 0 }),
        Tune("London Bridge", listOf(5,6,5,4,3,4,5,2,3,4,3,2,1).map { it to 0 }),
        Tune("Mary Had a Little Lamb", listOf(3,2,1,2,3,3,3,2,2,2,3,5,5).map { it to 0 }),
        Tune("Amazing Grace", listOf(5 to -1,1 to 0,3 to 0,1 to 0,3 to 0,2 to 0,1 to 0,6 to -1,5 to -1)))

    private fun nameThatTuneQuestion(): Question {
        val modePool = if (tier() == 0) listOf("Major (Ionian)","Natural Minor (Aeolian)")
            else listOf("Major (Ionian)","Dorian Mode","Phrygian Mode","Mixolydian Mode",
                        "Natural Minor (Aeolian)","Harmonic Minor","Maqam Hijaz")
        val modeName = modePool.random()
        val mode = scaleByName.getValue(modeName)
        val tune = tunes.random()
        val root = if (tier() == 0) 60 else 60 + Random.nextInt(12)
        val midis = tune.notes.map { (d, o) ->
            root + mode.steps[((d - 1) % mode.steps.size)].toInt() + 12 * ((d - 1) / mode.steps.size) + 12 * o }
        return if (Random.nextInt(100) < 55) {
            val (opts, ans) = opt(tunes.map { it.name }, tune.name, 4)
            Q("Which tune is this — played in ${mode.name}?", "Mode: ${mode.name}", opts, ans,
                "'${tune.name}' rendered through ${mode.name} — hear how the mode recolors a familiar melody!",
                "m:${midis.joinToString(",")}:230")
        } else {
            val (opts, ans) = opt(modePool, modeName, if (tier() == 0) 3 else 4)
            Q("You know the tune — but which MODE is it wearing?", null, opts, ans,
                "It was ${tune.name} in ${mode.name}. Steps: ${mode.steps.joinToString(",")}.",
                "m:${midis.joinToString(",")}:230")
        }
    }

    private fun temperamentQuestion(): Question = when (Random.nextInt(4)) {
        0 -> { val mf = Random.nextBoolean()
            Q("Which tuning had the PURE, sweet 3rd? (the other is equal temperament)",
                null, listOf("The FIRST one","The SECOND one"), if (mf) 0 else 1,
                "Meantone's 3rd = 386¢ (pure 5:4); its fifths are squeezed to ~696.6¢.\nET: everything = compromise.",
                "ab:" + (if (mf) Tuning.MEANTONE_MAJOR else Tuning.ET_MAJOR).joinToString(",") + "|" +
                        (if (mf) Tuning.ET_MAJOR else Tuning.MEANTONE_MAJOR).joinToString(",") + "|280|pluck") }
        1 -> { val pf = Random.nextBoolean()
            Q("Which had the WIDE, bright Pythagorean 3rd (408¢)?",
                null, listOf("The FIRST one","The SECOND one"), if (pf) 0 else 1,
                "Pure 3:2 fifths stacked give a strained, glassy 3rd — medieval color.",
                "ab:" + (if (pf) Tuning.PYTHAGOREAN_MAJOR else Tuning.ET_MAJOR).joinToString(",") + "|" +
                        (if (pf) Tuning.ET_MAJOR else Tuning.PYTHAGOREAN_MAJOR).joinToString(",") + "|280|pluck") }
        2 -> { val wf = Random.nextBoolean()
            Q("One is Werckmeister III (1691) — Bach's world. Which?",
                null, listOf("The FIRST one","The SECOND one"), if (wf) 0 else 1,
                "Werckmeister III: all keys usable, each with its own shading — the 'well temperament' scandal.",
                "ab:" + (if (wf) Tuning.WERCKMEISTER_MAJOR else Tuning.ET_MAJOR).joinToString(",") + "|" +
                        (if (wf) Tuning.ET_MAJOR else Tuning.WERCKMEISTER_MAJOR).joinToString(",") + "|280|pluck") }
        else -> Q("Quarter-comma meantone shrinks each fifth by about…",
            null, listOf("5.4 cents (¼ of a syntonic comma)","50 cents","0 cents","21 cents"), 0,
            "¼ × 21.5¢ ≈ 5.4¢ off each fifth — the price of pure thirds.")
    }

    private fun bpmQuestion(): Question {
        val g = MusicTheoryData.grooves.filter { it.pulses == 16 }.random()
        val actual = 70 + 2 * Random.nextInt(36)
        val d = when (tier()) { 0 -> 14; 1 -> 9; else -> 5 }
        val opts = (listOf(actual, actual + d, actual - d, actual + 2 * d, actual - 2 * d)
            .filter { it in 60..180 }.distinct().take(4)).shuffled()
        return Q("Estimate the TEMPO (BPM).", "Tap your foot, count 15 seconds…",
            opts.map { "$it BPM" }, opts.indexOf(actual),
            "${g.name} at $actual BPM. Internal clock = trainable muscle!",
            "g:${g.name}:$actual")
    }

    private fun harmDictQuestion(): Question {
        val pats = listOf(listOf(0,1,4,0) to "I – ii7 – V7 – I", listOf(0,3,1,4) to "I – IV – ii7 – V7",
                          listOf(0,5,1,4) to "I – vi7 – ii7 – V7", listOf(1,4,0,0) to "ii7 – V7 – I – I",
                          listOf(0,3,4,0) to "I – IV – V7 – I")
        val root = if (tier() == 0) 60 else 55 + Random.nextInt(12)
        val (degs, correct) = pats.random()
        val (opts, ans) = opt(pats.map { it.second }, correct, 4)
        return Q("SATB progression — name ALL FOUR chords (7th chords included!).",
            null, opts, ans, "It was $correct — voiced bass/tenor/alto/soprano. Feel the ii7–V7 gravity!",
            "satb:$root:${degs.joinToString(",")}")
    }

    private fun theoryQuestion(): Question {
        val q = theoryBank[bankPos++ % theoryBank.size]
        return Question(q.q, null, q.options, q.answer, q.info)
    }

    // ================= round flow =================
    private fun showQuestion() {
        answered = false; hintUsed = false; btnHint.isEnabled = true
        val q = questions[qIndex]
        tvProgress.text = "Question ${qIndex + 1} / ${questions.size}"
        progressBar.progress = qIndex
        tvScore.text = "Score $score"
        tvStreak.text = "🔥 Streak $streak"
        tvPrompt.text = q.prompt
        tvFeedback.text = ""
        if (q.display == null) tvDisplay.visibility = View.GONE
        else { tvDisplay.visibility = View.VISIBLE; tvDisplay.text = q.display }
        btnPlay.visibility = if (q.play == null) View.GONE else View.VISIBLE
        options.forEachIndexed { i, b ->
            if (i >= q.options.size) { b.visibility = View.GONE; return@forEachIndexed }
            b.visibility = View.VISIBLE; b.alpha = 1f
            b.text = q.options[i]; b.isEnabled = true
            b.setBackgroundColor(ContextCompat.getColor(this, R.color.optionBg))
        }
        btnNext.isEnabled = false
        btnNext.text = if (qIndex == questions.lastIndex) "See Results 🏁" else "Next  →"
        q.play?.invoke()
    }

    private fun onAnswer(i: Int) {
        if (answered) return
        answered = true
        val q = questions[qIndex]
        options.forEach { it.isEnabled = false }
        val correct = i == q.answer
        Stats.log(this, type, correct)
        if (correct) {
            streak++
            val pts = 10 + (streak - 1) * 2
            score += pts
            if (q.fromSR && q.id != null) SpacedRep.markCorrect(this, q.id)
            options[i].setBackgroundColor(ContextCompat.getColor(this, R.color.correct))
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.correct))
            tvFeedback.text = "✅ Correct!  +$pts\n${q.info}"
        } else {
            streak = 0
            options[i].setBackgroundColor(ContextCompat.getColor(this, R.color.wrong))
            options[q.answer].setBackgroundColor(ContextCompat.getColor(this, R.color.correct))
            tvFeedback.setTextColor(ContextCompat.getColor(this, R.color.wrong))
            tvFeedback.text = "❌  Answer: ${q.options[q.answer]}\n${q.info}"
            if (q.id != null) SpacedRep.upsertWrong(this, SpacedRep.Item(
                q.id, type, q.prompt, q.display, q.options, q.answer, q.info, q.audioSpec,
                0, System.currentTimeMillis(), 0))
            misses.add(ReviewActivity.Miss(q.id ?: "", type, q.prompt, q.display,
                q.options, q.answer, q.info, q.audioSpec))
        }
        tvScore.text = "Score $score"
        tvStreak.text = "🔥 Streak $streak"
        btnNext.isEnabled = true
    }

    private fun next() {
        qIndex++
        if (qIndex >= questions.size) {
            GameProgress.save(this, type, score)
            if (misses.isNotEmpty()) {
                ReviewStore.items = misses.toList()
                startActivity(Intent(this, ReviewActivity::class.java)
                    .putExtra("score", score).putExtra("type", type))
            } else {
                startActivity(Intent(this, ResultsActivity::class.java)
                    .putExtra("score", score).putExtra("type", type))
            }
            finish()
        } else showQuestion()
    }
}
