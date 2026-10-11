package com.example.groovemaster.audio
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioTimestamp
import android.media.AudioTrack
import android.os.SystemClock
import java.util.concurrent.CopyOnWriteArrayList
import kotlin.math.min

/** Persistent streaming AudioTrack + high-priority mixing thread. Sample-accurate events. */
@Suppress("DEPRECATION")
object StreamPlayer {
    const val SR = 44100
    const val GUARD_MS = 220
    private const val CHUNK = 4410
    private class Event(val pos: Long, val buf: ShortArray, val gain: Float)
    private val events = CopyOnWriteArrayList<Event>()
    private var track: AudioTrack? = null
    @Volatile private var running = false
    @Volatile private var writePos = 0L
    @Volatile private var epochReal = 0L

    fun ensureStarted() {
        if (running) return
        val minBuf = AudioTrack.getMinBufferSize(SR, AudioFormat.CHANNEL_OUT_MONO, AudioFormat.ENCODING_PCM_16BIT)
        val t = AudioTrack(AudioManager.STREAM_MUSIC, SR, AudioFormat.CHANNEL_OUT_MONO,
            AudioFormat.ENCODING_PCM_16BIT, maxOf(minBuf, CHUNK * 4), AudioTrack.MODE_STREAM)
        if (t.state != AudioTrack.STATE_INITIALIZED) return
        track = t; running = true; t.play()
        Thread {
            android.os.Process.setThreadPriority(android.os.Process.THREAD_PRIORITY_URGENT_AUDIO)
            val chunk = ShortArray(CHUNK)
            while (running) {
                java.util.Arrays.fill(chunk, 0)
                val it = events.iterator()
                while (it.hasNext()) {
                    val e = it.next(); val rel = e.pos - writePos
                    if (rel < CHUNK) { if (rel >= 0) mixAt(chunk, rel.toInt(), e.buf, e.gain); events.remove(e) }
                }
                t.write(chunk, 0, CHUNK)
                if (writePos == 0L) epochReal = SystemClock.elapsedRealtime() - 60
                writePos += CHUNK
            }
            try { t.stop(); t.release() } catch (_: Exception) {}
        }.start()
    }
    private fun mixAt(chunk: ShortArray, off: Int, buf: ShortArray, gain: Float) {
        val n = min(buf.size, chunk.size - off)
        for (i in 0 until n) {
            val v = chunk[off + i] + (buf[i] * gain).toInt()
            chunk[off + i] = v.coerceIn(-32767, 32767).toShort()
        }
    }
    fun nowSample(): Long {
        val at = AudioTimestamp(); val tr = track
        if (tr != null && tr.getTimestamp(at)) {
            val deltaNs = System.nanoTime() - at.nanoTime
            return at.framePosition + (deltaNs * SR) / 1_000_000_000L
        }
        return ((SystemClock.elapsedRealtime() - epochReal) * SR) / 1000
    }
    fun clearFuture() { events.clear() }
    fun writeAnchor(): Long = writePos + GUARD_MS.toLong() * SR / 1000
    fun schedulePattern(items: List<Triple<Double, ShortArray, Float>>): Long {
        ensureStarted()
        val base = writeAnchor()
        items.forEach { (ms, buf, gain) -> events.add(Event(base + (ms / 1000.0 * SR).toLong(), buf, gain)) }
        return base
    }
}
