package com.example.groovemaster

import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ActivitySmokeTest {
    private fun launch(cls: Class<*>, extra: Pair<String, String>? = null) {
        val i = Intent(ApplicationProvider.getApplicationContext(), cls)
        extra?.let { i.putExtra(it.first, it.second) }
        ActivityScenario.launch<android.app.Activity>(i).close()
    }
    @Test fun allScreensOpen() {
        launch(MainActivity::class.java)
        QuizActivity.QUIZ_TYPES.forEach { launch(QuizActivity::class.java, "type" to it) }
        launch(RhythmActivity::class.java)
        launch(StaffActivity::class.java)
        launch(TapBackActivity::class.java)
        launch(PolyrhythmActivity::class.java)
        launch(TourActivity::class.java)
        launch(LessonActivity::class.java, "idx" to "0")
        launch(PianoActivity::class.java)
        launch(FretboardActivity::class.java)
        launch(RhythmReadingActivity::class.java)
        launch(GrooveLabActivity::class.java)
        launch(CompasTapActivity::class.java)
        launch(StatsActivity::class.java)
        launch(LibraryActivity::class.java)
        launch(SingBackActivity::class.java)
        launch(ResultsActivity::class.java, "score" to "77")
        launch(DebugActivity::class.java)
    }
}
