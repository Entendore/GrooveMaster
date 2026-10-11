package com.example.groovemaster.data

import android.content.Context

object GameProgress {
    val GAMES = listOf(
        "scales","chords","intervals","grooves","theory",
        "modes_hard","microtune","gamelan","raga","compas",
        "keysig","circle5","notemath","timesig","roman",
        "cadence","inversion","function","dictation","forms","swing",
        "staff","tapback","sing",
        "updown","overtone","odd1","ntune","temperament","bpm","harmdict",
        "daily","piano","fret","rhythmread","compas_tap")

    private fun p(c: Context) = c.getSharedPreferences("groove_progress", Context.MODE_PRIVATE)

    fun save(context: Context, game: String, score: Int) {
        if (score > p(context).getInt(game, 0)) p(context).edit().putInt(game, score).apply()
    }
    fun best(context: Context, game: String): Int = p(context).getInt(game, 0)

    fun starsFor(score: Int): Int = when { score >= 150 -> 3; score >= 100 -> 2; score >= 50 -> 1; else -> 0 }
    fun totalStars(context: Context): Int = GAMES.sumOf { starsFor(best(context, it)) }
    fun maxStars(): Int = GAMES.size * 3

    // ---- difficulty tiers (0 Beginner · 1 Intermediate · 2 Expert), per mode ----
    fun tier(context: Context, mode: String): Int = p(context).getInt("tier_$mode", 0)
    fun setTier(context: Context, mode: String, tier: Int) =
        p(context).edit().putInt("tier_$mode", tier).apply()

    // ---- tanpura drone preference ----
    fun drone(context: Context): Boolean = p(context).getBoolean("drone", false)
    fun setDrone(context: Context, on: Boolean) = p(context).edit().putBoolean("drone", on).apply()

    // ---- world tour ----
    fun tourDone(context: Context): Set<String> = p(context).getStringSet("tour_done", emptySet()) ?: emptySet()
    fun markTour(context: Context, index: Int) {
        val s = tourDone(context).toMutableSet(); s.add(index.toString())
        p(context).edit().putStringSet("tour_done", s).apply()
    }
    fun tourCount(context: Context): Int = tourDone(context).size
    const val TOUR_LESSONS = 20
}
