package com.example.groovemaster.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object SpacedRep {

    data class Item(val id: String, val mode: String, val prompt: String, val display: String?,
                    val options: List<String>, val answer: Int, val info: String, val audio: String?,
                    var box: Int, var due: Long, var wrong: Int)

    private val DAYS = longArrayOf(1, 2, 4, 8, 16, 32)
    private const val KEY = "sr_items"

    private fun prefs(c: Context) = c.getSharedPreferences("groove_sr", Context.MODE_PRIVATE)

    private fun all(c: Context): MutableList<Item> {
        val raw = prefs(c).getString(KEY, null) ?: return mutableListOf()
        val out = mutableListOf<Item>()
        try {
            val arr = JSONArray(raw)
            for (i in 0 until arr.length()) {
                val o = arr.getJSONObject(i)
                val opts = mutableListOf<String>()
                val oa = o.getJSONArray("options")
                for (j in 0 until oa.length()) opts.add(oa.getString(j))
                out.add(Item(o.getString("id"), o.getString("mode"), o.getString("prompt"),
                    if (o.isNull("display")) null else o.getString("display"),
                    opts, o.getInt("answer"), o.getString("info"),
                    if (o.isNull("audio")) null else o.getString("audio"),
                    o.getInt("box"), o.getLong("due"), o.getInt("wrong")))
            }
        } catch (_: Exception) {}
        return out
    }

    private fun save(c: Context, list: List<Item>) {
        val arr = JSONArray()
        list.forEach { it2 ->
            val o = JSONObject()
            o.put("id", it2.id); o.put("mode", it2.mode); o.put("prompt", it2.prompt)
            if (it2.display == null) o.put("display", JSONObject.NULL) else o.put("display", it2.display)
            o.put("options", JSONArray(it2.options)); o.put("answer", it2.answer)
            o.put("info", it2.info)
            if (it2.audio == null) o.put("audio", JSONObject.NULL) else o.put("audio", it2.audio)
            o.put("box", it2.box); o.put("due", it2.due); o.put("wrong", it2.wrong)
            arr.put(o)
        }
        prefs(c).edit().putString(KEY, arr.toString()).apply()
    }

    fun upsertWrong(c: Context, item: Item) {
        val list = all(c)
        val ex = list.firstOrNull { it.id == item.id }
        if (ex != null) { ex.box = 0; ex.due = System.currentTimeMillis(); ex.wrong++ }
        else list.add(item.copy(box = 0, due = System.currentTimeMillis()))
        save(c, list)
    }

    fun markCorrect(c: Context, id: String) {
        val list = all(c)
        val ex = list.firstOrNull { it.id == id } ?: return
        ex.box = (ex.box + 1).coerceAtMost(DAYS.size - 1)
        ex.due = System.currentTimeMillis() + DAYS[ex.box] * 86_400_000L
        save(c, list)
    }

    fun due(c: Context, limit: Int = 10): List<Item> {
        val now = System.currentTimeMillis()
        return all(c).filter { it.due <= now }.sortedBy { it.due }.take(limit)
    }

    fun dueCount(c: Context): Int {
        val now = System.currentTimeMillis()
        return all(c).count { it.due <= now }
    }

    fun totalTracked(c: Context): Int = all(c).size

    // ---- QA Lab helpers ----
    fun forceAllDue(c: Context) { val l = all(c); l.forEach { it.due = 0 }; save(c, l) }
    fun wipe(c: Context) = prefs(c).edit().remove(KEY).apply()
}
