package fr.claynum.fluflu.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class EntryRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun load(): List<JournalEntry> = runCatching {
        val array = JSONArray(preferences.getString(KEY_ENTRIES, "[]"))
        buildList {
            for (index in 0 until array.length()) add(array.getJSONObject(index).toEntry())
        }
    }.getOrDefault(emptyList())

    fun save(entries: List<JournalEntry>) {
        val array = JSONArray()
        entries.forEach { array.put(it.toJson()) }
        preferences.edit().putString(KEY_ENTRIES, array.toString()).apply()
    }

    fun clear() {
        preferences.edit().remove(KEY_ENTRIES).apply()
    }

    private fun JournalEntry.toJson() = JSONObject().apply {
        put("id", id)
        put("kind", kind.name)
        put("at", at)
        put("name", name)
        put("mealType", mealType)
        put("quantity", quantity)
        put("note", note)
        put("intensity", intensity)
        put("context", JSONObject().apply {
            context.forEach { (factor, value) -> put(factor.name, value.name) }
        })
    }

    private fun JSONObject.toEntry(): JournalEntry {
        val contextObject = optJSONObject("context") ?: JSONObject()
        val context = ContextFactor.entries.associateWith { factor ->
            runCatching { ContextValue.valueOf(contextObject.optString(factor.name, "UNKNOWN")) }
                .getOrDefault(ContextValue.UNKNOWN)
        }
        return JournalEntry(
            id = getString("id"),
            kind = EntryKind.valueOf(getString("kind")),
            at = getLong("at"),
            name = optString("name"),
            mealType = optString("mealType", "Non précisé"),
            quantity = optString("quantity"),
            note = optString("note"),
            intensity = optInt("intensity"),
            context = context
        )
    }

    private companion object {
        const val PREFERENCES = "fluflu-journal"
        const val KEY_ENTRIES = "entries-v1"
    }
}
