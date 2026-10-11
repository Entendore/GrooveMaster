package com.example.groovemaster.audio
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTrack
import android.os.Handler
import android.os.Looper
import com.example.groovemaster.data.MusicTheoryData.Groove
import kotlin.math.PI
import kotlin.math.exp
import kotlin.math.pow
import kotlin.math.sin
import kotlin.random.Random

@Suppress("DEPRECATION")
object SynthEngine {
    const val SR = 44100
    private val handler = Handler(Looper.getMainLooper())
    private val pending = mutableListOf<Runnable>()
    fun midiToFreq(midi: Int): Double = 440.0 * 2.0.pow((midi - 69) / 12.0)
    fun centsToFreq(c: Double): Double = midiToFreq(60) * 2.0.pow(c / 1200.0)
    fun schedule(ms: Long, r: () -> Unit) {
        val task = Runnable { pending.remove(task); r() }
        handler.postDelayed(task, ms); pending.add(task)
    }
    fun stopAll() { pending.forEach { handler.removeCallbacks(it) }; pending.clear(); droneRunning = false; StreamPlayer.clearFuture() }
    fun stopGroove() = stopAll()

    // ---- Karplus-Strong ----
    private fun ks(freq: Double, durMs: Int, vol: Float, damp: Double): ShortArray {
        val n = maxOf(SR * durMs / 1000, 1); val buf = ShortArray(n)
        val N = maxOf(2, (SR / freq).toInt())
        val line = DoubleArray(N) { Random.nextDouble() * 2 - 1 }
        for (i in N - 1 downTo 1) line[i] = 0.5 * (line[i] + line[i - 1])
        var body = 0.0
        for (i in 0 until n) {
            val v = line[i % N]
            line[i % N] = damp * 0.5 * (v + line[(i + 1) % N])
            body = 0.85 * v + 0.15 * body
            buf[i] = (body * vol * 12000).toInt().toShort()
        }
        return buf
    }
    private fun render(freq: Double, durMs: Int, vol: Float, timbre: String): ShortArray {
        val n = maxOf(SR * durMs / 1000, 1)
        val buf = when (timbre) {
            "pluck" -> ks(freq, durMs, vol, 0.996)
            "nylon" -> ks(freq, durMs, vol, 0.9935)
            "metal" -> ShortArray(n) { i -> val t = i / SR.toDouble()
                val env = vol * exp(-2.0 * t / (durMs / 1000.0)) * (1 - exp(-t * 300))
                val w = sin(2*PI*freq*t) + 0.55*sin(2*PI*2.76*freq*t) + 0.30*sin(2*PI*5.40*freq*t) + 0.15*sin(2*PI*8.93*freq*t)
                (w * env * 8000).toInt().toShort() }
            "sine" -> ShortArray(n) { i -> val t = i / SR.toDouble()
                ((sin(2*PI*freq*t) * vol * exp(-3.0*t/(durMs/1000.0)) * (1-exp(-t*300)) * 12000)).toInt().toShort() }
            "click" -> ShortArray(n) { i -> val t = i / SR.toDouble()
                ((sin(2*PI*freq*t) * exp(-t*60) * vol * 12000)).toInt().toShort() }
            else -> ShortArray(n) { i -> val t = i / SR.toDouble()
                val env = vol * exp(-4.0*t/(durMs/1000.0)) * (1-exp(-t*300))
                val w = sin(2*PI*freq*t) + 0.4*sin(4*PI*freq*t) + 0.2*sin(6*PI*freq*t)
                (w * env * 8000).toInt().toShort() }
        }
        addReverb(buf); return buf
    }
    private fun addReverb(buf: ShortArray, mix: Double = 0.26) {
        val d = SR * 88 / 1000
        if (buf.size <= d + 16) return
        val fb = DoubleArray(buf.size)
        for (i in d until buf.size) {
            val x = buf[i - d] / 32768.0; val prev = fb[i - d]
            fb[i] = (0.62 * x + 0.38 * prev) * 0.55
        }
        for (i in buf.indices) buf[i] = (buf[i] + fb[i] * 32768.0 * mix).toInt().coerceIn(-32767, 32767).toShort()
    }
    private val cache = HashMap<String, ShortArray>()
    private fun cached(key: String, build: () -> ShortArray): ShortArray = cache.getOrPut(key) { build() }
    private fun play(buf: ShortArray) {
        val track = AudioTrack(AudioManager.STREAM_MUSIC, SR, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, buf.size * 2, AudioTrack.MODE_STATIC)
        track.write(buf, 0, buf.size); track.play()
        schedule(buf.size * 1000L / SR + 200) { try { track.stop(); track.release() } catch (_: Exception) {} }
    }
    fun playFreq(freq: Double, durMs: Int = 500, vol: Float = 0.7f, timbre: String = "pluck") { if (freq > 0) play(render(freq, durMs, vol, timbre)) }
    fun note(midi: Int, durMs: Int = 500, vol: Float = 0.7f) = playFreq(midiToFreq(midi), durMs, vol)
    fun click(freqHz: Double, vol: Float = 0.9f) = play(cached("click$freqHz") { render(freqHz, 150, vol, "click") })
    fun clickBuffer(freq: Double): ShortArray = cached("cb$freq") { render(freq, 150, 0.9f, "click") }
    fun playHarmonic(baseHz: Double, n: Int, durMs: Int = 800) = playFreq(baseHz * n, durMs, 0.8f, "sine")

    // ---- tanpura drone ----
    var droneOn = false
    private var droneRunning = false
    private var droneRoot = 60
    fun setDrone(on: Boolean, rootMidi: Int = 60) { droneOn = on; droneRoot = rootMidi; if (on) maybeDrone() else stopDrone() }
    fun stopDrone() { droneOn = false; droneRunning = false }
    private fun maybeDrone() { if (droneOn && !droneRunning) { droneRunning = true; droneTick() } }
    private fun droneTick() {
        if (!droneOn) { droneRunning = false; return }
        playFreq(midiToFreq(droneRoot - 12), 1500, 0.42f, "nylon")
        playFreq(midiToFreq(droneRoot - 5), 1300, 0.22f, "nylon")
        schedule(1250) { droneTick() }
    }
    fun playCents(cents: List<Double>, stepMs: Long = 260, timbre: String = "pluck", startDelayMs: Long = 0) {
        maybeDrone()
        cents.forEachIndexed { i, c -> schedule(startDelayMs + i * stepMs) {
            playFreq(centsToFreq(c), (stepMs * 1.7).toInt().coerceIn(150, 900), 0.7f, timbre) } }
    }
    fun playAB(a: List<Double>, b: List<Double>, stepMs: Long = 230, timbre: String = "pluck") {
        maybeDrone(); playCents(a, stepMs, timbre, 0); playCents(b, stepMs, timbre, a.size * stepMs + 900)
    }
    fun playChord(midis: List<Int>) { midis.forEachIndexed { i, m -> schedule(i * 120L) { note(m, 1000) } } }
    fun playChords(progression: List<List<Int>>, chordMs: Long = 750) {
        progression.forEachIndexed { k, ch -> ch.forEach { m -> schedule(k * chordMs) { note(m, chordMs.toInt()) } } }
    }
    fun playMelody(midis: List<Int>, noteMs: Long = 420) {
        midis.forEachIndexed { i, m -> schedule(i * noteMs) { note(m, (noteMs * 0.95).toInt()) } }
    }
    fun playScale(midis: List<Int>, stepMs: Long = 280) = playMelody(midis + (midis.first() + 12), stepMs)
    fun playInterval(rootMidi: Int, secondMidi: Int) {
        maybeDrone(); note(rootMidi, 500); schedule(600) { note(secondMidi, 700) }
        schedule(1600) { note(rootMidi, 450, 0.35f) }; schedule(2200) { note(secondMidi, 700, 0.35f) }
    }
    private val MAJ = intArrayOf(0,4,7); private val MIN = intArrayOf(0,3,7)
    private val MAJ7 = intArrayOf(0,4,7,11); private val MIN7 = intArrayOf(0,3,7,10)
    private val DOM7 = intArrayOf(0,4,7,10); private val DIM = intArrayOf(0,3,6)
    fun degreeChord(deg: Int, seventh: Boolean): IntArray = when (deg) {
        0 -> if (seventh) MAJ7 else MAJ; 1 -> if (seventh) MIN7 else MIN; 2 -> MIN
        3 -> if (seventh) MAJ7 else MAJ; 4 -> if (seventh) DOM7 else MAJ
        5 -> if (seventh) MIN7 else MIN; else -> DIM }
    fun playDegreeProgression(root: Int, degs: List<Int>, chordMs: Long = 700) {
        degs.forEachIndexed { k, d -> degreeChord(d, false).forEach { iv -> schedule(k * chordMs) { note(root + iv - 12, chordMs.toInt(), 0.55f) } } }
    }
    fun playSATB(root: Int, degs: List<Int>, chordMs: Long = 950) {
        degs.forEachIndexed { k, d ->
            val iv = when (d) { 0 -> MAJ; 1 -> MIN7; 3 -> MAJ; 4 -> DOM7; 5 -> MIN7; else -> DIM }
            val bass = root - 12 + iv[0]; val tenor = root + iv[1]; val alto = root + iv[2]; val sop = root + (if (iv.size > 3) iv[3] else 12)
            schedule(k * chordMs) { note(bass, chordMs.toInt(), 0.55f); note(tenor, chordMs.toInt(), 0.4f); note(alto, chordMs.toInt(), 0.4f); note(sop, chordMs.toInt(), 0.45f) } }
    }
    // ---- percussion builders (cached) ----
    private fun buildKick(): ShortArray { val n = SR*280/1000; val b = ShortArray(n); var ph = 0.0
        for (i in 0 until n) { val t = i/SR.toDouble(); val f = 45 + 110*exp(-t*28); ph += 2*PI*f/SR
            b[i] = (sin(ph)*exp(-t*10)*26000).toInt().toShort() }; return b }
    private fun buildSnare(): ShortArray { val n = SR*200/1000; val b = ShortArray(n); var lp = 0.0
        for (i in 0 until n) { val t = i/SR.toDouble(); val nz = Random.nextDouble()*2-1; lp += 0.25*(nz-lp)
            val env = exp(-t*20); val body = 0.35*sin(2*PI*185*t)*exp(-t*30)
            b[i] = ((0.7*(nz-lp)+body)*env*16000).toInt().toShort() }; return b }
    private fun buildHat(): ShortArray { val n = SR*80/1000; val b = ShortArray(n); var prev = 0.0
        for (i in 0 until n) { val t = i/SR.toDouble(); val nz = Random.nextDouble()*2-1; val hp = nz-prev; prev = nz
            b[i] = (hp*exp(-t*50)*9000).toInt().toShort() }; return b }
    private fun buildClap(): ShortArray { val n = SR*180/1000; val b = ShortArray(n)
        for (i in 0 until n) { val t = i/SR.toDouble(); val nz = Random.nextDouble()*2-1
            val env = (exp(-t*90)+0.7*exp(-((t-0.012).coerceAtLeast(0.0))*90)+0.5*exp(-((t-0.024).coerceAtLeast(0.0))*90))*exp(-t*12)
            b[i] = (nz*env*12000).toInt().toShort() }; return b }
    private fun buildTom(): ShortArray { val n = SR*260/1000; val b = ShortArray(n); var ph = 0.0
        for (i in 0 until n) { val t = i/SR.toDouble(); val f = 160 - 65*(t/0.26); ph += 2*PI*f/SR
            b[i] = (sin(ph)*exp(-t*12)*22000).toInt().toShort() }; return b }
    private fun buildWood(): ShortArray { val n = SR*90/1000; val b = ShortArray(n)
        for (i in 0 until n) { val t = i/SR.toDouble(); val tone = sin(2*PI*820*t)+0.5*sin(2*PI*1640*t)
            b[i] = (tone*exp(-t*55)*15000).toInt().toShort() }; return b }
    private fun buildBell(): ShortArray = render(830.0, 380, 1f, "metal")
    private fun buildMetal(): ShortArray = render(622.0, 400, 1f, "metal")
    private fun buildWave(): ShortArray = render(3200.0, 120, 0.35f, "click")
    private fun rawOf(inst: String): ShortArray = when (inst) {
        "kick" -> buildKick(); "snare" -> buildSnare(); "hat" -> buildHat(); "clap" -> buildClap()
        "tom" -> buildTom(); "wood" -> buildWood(); "bell" -> buildBell(); "metal" -> buildMetal()
        "wave" -> buildWave(); else -> buildHat() }
    fun playInst(inst: String, vol: Float) {
        val gain = (vol * 32767 / 26000.0).toFloat()
        val buf = cached(inst) { rawOf(inst) }
        play(ShortArray(buf.size) { (buf[it] * gain).toInt().coerceIn(-32767, 32767).toShort() })
    }
    /** Full-scale cached buffer for the streaming mixer. */
    fun drumBuffer(inst: String): ShortArray = cached("norm_$inst") {
        val raw = rawOf(inst)
        ShortArray(raw.size) { (raw[it] * 1.2585f).toInt().coerceIn(-32767, 32767).toShort() } }
    fun playSchedule(items: List<Triple<Double, String, Float>>): Long =
        StreamPlayer.schedulePattern(items.map { Triple(it.first, drumBuffer(it.second), it.third) })
    fun playScheduleBuffers(items: List<Triple<Double, ShortArray, Float>>): Long = StreamPlayer.schedulePattern(items)

    // ---- sequencer: sample-accurate audio, Handler visuals ----
    fun playGroove(g: Groove, tempoPct: Int = 100, loops: Int = 2,
                   onStep: ((Int) -> Unit)? = null, onEnd: (() -> Unit)? = null) {
        stopAll(); StreamPlayer.ensureStarted()
        val stepMs = 60000.0 / (g.bpm * tempoPct / 100.0) / 4.0
        val pulses = g.pulses; val total = pulses * loops
        val items = mutableListOf<Triple<Double, ShortArray, Float>>()
        val layers = g.layers.map { it to drumBuffer(it.inst) }
        val markers = g.markers.map { it to drumBuffer(it.inst) }
        for (s in 0 until total) {
            val idx = s % pulses
            var t = s * stepMs
            if (g.swing > 0f && idx % 2 == 1) t += stepMs / 3.0 * g.swing
            for ((l, b) in layers) when (l.pattern.getOrNull(idx)) {
                'X' -> items.add(Triple(t, b, 1f)); 'x' -> items.add(Triple(t, b, 0.62f))
                'o' -> items.add(Triple(t, b, 0.30f)); else -> {} }
            for ((m, b) in markers) if (m.pattern.getOrNull(idx) == 'x') items.add(Triple(t, b, 0.85f))
            onStep?.let { cb -> handler.postDelayed({ cb(idx) }, t.toLong() + StreamPlayer.GUARD_MS) }
        }
        StreamPlayer.schedulePattern(items)
        onEnd?.let { cb -> handler.postDelayed(cb, (total * stepMs).toLong() + StreamPlayer.GUARD_MS) }
    }
}
