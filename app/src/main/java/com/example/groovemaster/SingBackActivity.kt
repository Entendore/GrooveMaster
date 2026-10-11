package com.example.groovemaster

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.groovemaster.audio.PitchDetector
import com.example.groovemaster.audio.SynthEngine
import com.example.groovemaster.data.GameProgress
import com.example.groovemaster.data.MusicTheoryData
import com.example.groovemaster.data.Stats
import kotlin.math.abs
import kotlin.math.log2
import kotlin.math.roundToInt

class SingBackActivity : AppCompatActivity() {

    private var rootMidi = 0
    private var interval = MusicTheoryData.intervals.random()
    private var score = 0

    private lateinit var tvPrompt: TextView
    private lateinit var tvResult: TextView
    private lateinit var tvScore: TextView

    private val perm = registerForActivityResult(ActivityResultContracts.RequestPermission()) {
        if (it) startRecording()
        else Toast.makeText(this, "Microphone permission is needed", Toast.LENGTH_SHORT).show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_sing)
        title = "🎤 Sing-Back"
        tvPrompt = findViewById(R.id.tvSingPrompt)
        tvResult = findViewById(R.id.tvSingResult)
        tvScore = findViewById(R.id.tvSingScore)
        newRound()
        findViewById<Button>(R.id.btnSingRoot).setOnClickListener { SynthEngine.note(rootMidi, 900) }
        findViewById<Button>(R.id.btnSingDemo).setOnClickListener {
            SynthEngine.note(rootMidi, 700)
            SynthEngine.schedule(800) { SynthEngine.note(rootMidi + interval.semitones, 900) }
        }
        findViewById<Button>(R.id.btnSingRec).setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
                == PackageManager.PERMISSION_GRANTED) startRecording()
            else perm.launch(Manifest.permission.RECORD_AUDIO)
        }
        findViewById<Button>(R.id.btnSingNext).setOnClickListener { newRound(); tvResult.text = "" }
        findViewById<Button>(R.id.btnSingEnd).setOnClickListener {
            GameProgress.save(this, "sing", score)
            startActivity(Intent(this, ResultsActivity::class.java)
                .putExtra("score", score).putExtra("type", "sing"))
            finish()
        }
    }

    private fun newRound() {
        rootMidi = 48 + (0..11).random()
        interval = MusicTheoryData.intervals.random()
        tvPrompt.text = "Play the root, then SING a ${interval.name} above it and HOLD.\nRoot: ${noteName(rootMidi)}"
        tvScore.text = "Score $score"
    }

    private fun noteName(m: Int) = MusicTheoryData.NOTE_NAMES[((m % 12) + 12) % 12]

    @SuppressLint("MissingPermission")
    private fun startRecording() {
        tvResult.text = "🎙️ Listening… sing and HOLD the note!"
        Thread {
            try {
                val sr = 44100
                val n = (sr * 3.5).toInt()
                val minBuf = AudioRecord.getMinBufferSize(sr, AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT)
                val rec = AudioRecord(MediaRecorder.AudioSource.MIC, sr,
                    AudioFormat.CHANNEL_IN_MONO, AudioFormat.ENCODING_PCM_16BIT,
                    maxOf(minBuf, n * 2) * 2)
                if (rec.state != AudioRecord.STATE_INITIALIZED) {
                    runOnUiThread { tvResult.text = "Mic failed to initialize" }; return@Thread
                }
                val buf = ShortArray(n)
                rec.startRecording()
                var read = 0
                while (read < n) {
                    val r = rec.read(buf, read, n - read)
                    if (r <= 0) break
                    read += r
                }
                rec.stop(); rec.release()

                val frame = 4096; val hop = 2048
                val pitches = mutableListOf<Double>()
                var i = 0
                while (i + frame <= read) {
                    val f = FloatArray(frame) { buf[i + it] / 32768f }
                    PitchDetector.detect(f, sr)?.let { pitches.add(it) }
                    i += hop
                }
                if (pitches.size < 4) {
                    runOnUiThread { tvResult.text = "Couldn't hear you — sing louder & hold the note 🎵" }
                    return@Thread
                }
                pitches.sort()
                val user = pitches[pitches.size / 2]
                val expected = SynthEngine.midiToFreq(rootMidi + interval.semitones)
                var cents = 1200 * log2(user / expected)
                cents = ((cents + 600) % 1200 + 1200) % 1200 - 600   // any octave counts
                val ok = abs(cents) <= 60
                val sungMidi = (69 + 12 * log2(user / 440.0)).roundToInt()
                runOnUiThread {
                    Stats.log(this@SingBackActivity, "sing", ok)
                    if (ok) score += 10
                    tvResult.text = buildString {
                        append("Detected: ${noteName(sungMidi)}  (${format(user)} Hz)\n")
                        if (ok) append("✅ That's a ${interval.name}!  off by ${format(cents)} cents")
                        else append("❌ Off by ${format(cents)} cents — target: ${noteName(rootMidi + interval.semitones)}")
                        append("\n${interval.name} = ${interval.semitones} semitones")
                    }
                    tvScore.text = "Score $score"
                }
            } catch (e: Exception) {
                runOnUiThread { tvResult.text = "Recording error: ${e.message}" }
            }
        }.start()
    }

    private fun format(v: Double) = "%.0f".format(v)
}
