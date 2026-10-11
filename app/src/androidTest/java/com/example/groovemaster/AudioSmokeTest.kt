package com.example.groovemaster

import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.groovemaster.audio.SynthEngine
import com.example.groovemaster.audio.StreamPlayer
import com.example.groovemaster.data.MusicTheoryData
import org.junit.Assert.assertNotEquals
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AudioSmokeTest {
    @Test fun allInstrumentsPlayWithoutCrash() {
        listOf("kick","snare","hat","clap","tom","wood","bell","metal","wave")
            .forEach { SynthEngine.playInst(it, 0.8f) }
        Thread.sleep(400)
    }
    @Test fun allTimbresPlayWithoutCrash() {
        listOf("pluck","nylon","metal","sine","click").forEach {
            SynthEngine.playFreq(220.0, 200, 0.5f, it) }
        Thread.sleep(400)
    }
    @Test fun droneStartsAndStops() {
        SynthEngine.setDrone(true, 60); Thread.sleep(1500); SynthEngine.stopAll()
    }
    @Test fun everyGrooveSchedulesAndStops() {
        MusicTheoryData.grooves.forEach { g ->
            SynthEngine.playGroove(g, 300, 1)   // 3x speed, headless
            Thread.sleep(60)
            SynthEngine.stopAll()
        }
    }
    @Test fun streamPlayerPatternSchedulesWithoutCrash() {
        val items = listOf(
            Triple(0.0, SynthEngine.drumBuffer("hat"), 0.6f),
            Triple(250.0, SynthEngine.drumBuffer("kick"), 1f),
            Triple(500.0, SynthEngine.drumBuffer("wood"), 0.9f))
        SynthEngine.playScheduleBuffers(items)
        Thread.sleep(1200)
        assertNotEquals(0L, StreamPlayer.nowSample())
    }
    @Test fun everyAudioCodecSpecPlaysWithoutCrash() {
        com.example.groovemaster.audio.AudioCodec.play("s:0:0,2,4,5,7,9,11")
        com.example.groovemaster.audio.AudioCodec.play("ab:0,200,400|0,203,386|230|pluck")
        com.example.groovemaster.audio.AudioCodec.play("o4:0,2,4|0,1,4|0,3,7|0,2,3")
        com.example.groovemaster.audio.AudioCodec.play("c:60,64,67")
        com.example.groovemaster.audio.AudioCodec.play("i:60:7")
        com.example.groovemaster.audio.AudioCodec.play("p:60:0,4,5,3")
        com.example.groovemaster.audio.AudioCodec.play("satb:60:0,1,4,0")
        com.example.groovemaster.audio.AudioCodec.play("m:60,62,64:200")
        com.example.groovemaster.audio.AudioCodec.play("d:0,4,7")
        com.example.groovemaster.audio.AudioCodec.play("g:Maqsoum:0")
        com.example.groovemaster.audio.AudioCodec.play("n:110:3")
        com.example.groovemaster.audio.AudioCodec.play("pk:Yaman")
        com.example.groovemaster.audio.AudioCodec.play("cg:4:2")
        com.example.groovemaster.audio.AudioCodec.play("sw:1")
        com.example.groovemaster.audio.AudioCodec.play("seq:g:Maqsoum:100|g:Malfouf:105")
        Thread.sleep(600)
        SynthEngine.stopAll()
    }
}
