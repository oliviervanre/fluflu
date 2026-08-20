package fr.claynum.fluflu.data

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

class EntryRepository(context: Context) {
    private val preferences = context.getSharedPreferences(PREFERENCES, Context.MODE_PRIVATE)

    fun loadEntries(): List<JournalEntry> = runCatching {
        val array = JSONArray(preferences.getString(KEY_ENTRIES, "[]"))
        buildList {
            for (index in 0 until array.length()) add(array.getJSONObject(index).toEntry())
        }
    }.getOrDefault(emptyList())

    fun saveEntries(entries: List<JournalEntry>) {
        val array = JSONArray()
        entries.forEach { array.put(it.toJson()) }
        preferences.edit().putString(KEY_ENTRIES, array.toString()).apply()
    }

    fun loadProfiles(): List<UserProfile> = runCatching {
        val array = JSONArray(preferences.getString(KEY_PROFILES, "[]"))
        buildList {
            for (index in 0 until array.length()) add(array.getJSONObject(index).toProfile())
        }
    }.getOrDefault(emptyList())

    fun saveProfiles(profiles: List<UserProfile>) {
        val array = JSONArray()
        profiles.forEach { array.put(it.toJson()) }
        preferences.edit().putString(KEY_PROFILES, array.toString()).apply()
    }

    fun loadActiveProfileId(): String? = preferences.getString(KEY_ACTIVE_PROFILE, null)

    fun saveActiveProfileId(profileId: String) {
        preferences.edit().putString(KEY_ACTIVE_PROFILE, profileId).apply()
    }

    private fun JournalEntry.toJson() = JSONObject().apply {
        put("id", id)
        put("profileId", profileId)
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
            profileId = getString("profileId"),
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

    private fun UserProfile.toJson() = JSONObject().apply {
        put("id", id)
        put("firstName", firstName)
        put("type", type.name)
        put("createdAt", createdAt)
    }

    private fun JSONObject.toProfile() = UserProfile(
        id = getString("id"),
        firstName = getString("firstName"),
        type = ProfileType.valueOf(getString("type")),
        createdAt = getLong("createdAt")
    )

    private companion object {
        const val PREFERENCES = "fluflu-journal"
        const val KEY_ENTRIES = "entries-v2"
        const val KEY_PROFILES = "profiles-v1"
        const val KEY_ACTIVE_PROFILE = "active-profile-v1"
    }
}
