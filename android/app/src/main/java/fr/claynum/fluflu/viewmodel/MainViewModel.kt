package fr.claynum.fluflu.viewmodel

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import fr.claynum.fluflu.data.ContextFactor
import fr.claynum.fluflu.data.ContextValue
import fr.claynum.fluflu.data.EntryKind
import fr.claynum.fluflu.data.EntryRepository
import fr.claynum.fluflu.data.JournalEntry
import fr.claynum.fluflu.data.ObservationEngine
import fr.claynum.fluflu.data.ProfileType
import fr.claynum.fluflu.data.UserProfile
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EntryRepository(application)
    val entries = mutableStateListOf<JournalEntry>()
    val profiles = mutableStateListOf<UserProfile>()
    var activeProfileId by mutableStateOf<String?>(null)
        private set

    val activeProfile: UserProfile?
        get() = profiles.firstOrNull { it.id == activeProfileId }

    val activeEntries: List<JournalEntry>
        get() = activeProfileId?.let { id -> entries.filter { it.profileId == id } } ?: emptyList()

    val needsProfileSetup: Boolean
        get() = profiles.none { it.type == ProfileType.PERSONAL }

    init {
        profiles.addAll(repository.loadProfiles())
        entries.addAll(repository.loadEntries().sortedBy { it.at })
        activeProfileId = repository.loadActiveProfileId()
            ?.takeIf { id -> profiles.any { it.id == id } }
            ?: profiles.firstOrNull { it.type == ProfileType.PERSONAL }?.id
            ?: profiles.firstOrNull()?.id
    }

    fun createPersonalProfile(firstName: String) {
        val cleanName = firstName.trim()
        if (cleanName.isBlank()) return
        val existing = profiles.firstOrNull { it.type == ProfileType.PERSONAL }
        val profile = existing?.copy(firstName = cleanName) ?: UserProfile(
            id = id(),
            firstName = cleanName,
            type = ProfileType.PERSONAL,
            createdAt = System.currentTimeMillis()
        )
        if (existing == null) profiles.add(profile) else profiles[profiles.indexOf(existing)] = profile
        persistProfiles()
        selectProfile(profile.id)
    }

    fun renameActiveProfile(firstName: String) {
        val profile = activeProfile ?: return
        val cleanName = firstName.trim()
        if (profile.type != ProfileType.PERSONAL || cleanName.isBlank()) return
        profiles[profiles.indexOf(profile)] = profile.copy(firstName = cleanName)
        persistProfiles()
    }

    fun selectProfile(profileId: String) {
        if (profiles.none { it.id == profileId }) return
        activeProfileId = profileId
        repository.saveActiveProfileId(profileId)
    }

    fun addFood(name: String, at: Long, mealType: String, quantity: String, note: String) {
        val profileId = activeProfileId ?: return
        add(JournalEntry(id = id(), profileId = profileId, kind = EntryKind.FOOD, at = at,
            name = name.trim(), mealType = mealType, quantity = quantity.trim(), note = note.trim()))
    }

    fun addSymptom(at: Long, intensity: Int, context: Map<ContextFactor, ContextValue>, note: String) {
        val profileId = activeProfileId ?: return
        add(JournalEntry(id = id(), profileId = profileId, kind = EntryKind.SYMPTOM, at = at,
            note = note.trim(), intensity = intensity, context = context))
    }

    fun delete(id: String) {
        entries.removeAll { it.id == id }
        persistEntries()
    }

    fun clearActiveProfileEntries() {
        val profileId = activeProfileId ?: return
        entries.removeAll { it.profileId == profileId }
        persistEntries()
    }

    fun observations(windowHours: Int) = ObservationEngine.compute(activeEntries, windowHours)

    fun entriesOn(date: LocalDate): List<JournalEntry> {
        val zone = ZoneId.systemDefault()
        return activeEntries.filter { entry ->
            java.time.Instant.ofEpochMilli(entry.at).atZone(zone).toLocalDate() == date
        }.sortedBy { it.at }
    }

    fun loadDemo() {
        val demoProfile = profiles.firstOrNull { it.type == ProfileType.DEMONSTRATION }
            ?: UserProfile(id(), "Démonstration", ProfileType.DEMONSTRATION, System.currentTimeMillis()).also {
                profiles.add(it)
                persistProfiles()
            }
        entries.removeAll { it.profileId == demoProfile.id }
        val zone = ZoneId.systemDefault()
        fun at(daysAgo: Long, hour: Int, minute: Int = 0) = LocalDate.now()
            .minusDays(daysAgo).atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()
        fun unknownContext() = ContextFactor.entries.associateWith { ContextValue.UNKNOWN }
        fun context(vararg values: Pair<ContextFactor, ContextValue>) = unknownContext().toMutableMap().apply { putAll(values) }

        entries.addAll(listOf(
            JournalEntry(id(), demoProfile.id, EntryKind.FOOD, at(4, 8, 10), "Café", "Petit-déjeuner", "1 tasse"),
            JournalEntry(id(), demoProfile.id, EntryKind.FOOD, at(4, 8, 12), "Pain", "Petit-déjeuner", "2 tranches"),
            JournalEntry(id(), demoProfile.id, EntryKind.SYMPTOM, at(4, 10, 5), note = "Brûlure légère", intensity = 2,
                context = context(ContextFactor.STRESS to ContextValue.YES, ContextFactor.LYING to ContextValue.NO)),
            JournalEntry(id(), demoProfile.id, EntryKind.FOOD, at(3, 12, 35), "Tomate", "Déjeuner", "1 portion", "En salade"),
            JournalEntry(id(), demoProfile.id, EntryKind.SYMPTOM, at(3, 14, 10), intensity = 3, context = unknownContext()),
            JournalEntry(id(), demoProfile.id, EntryKind.FOOD, at(2, 9, 5), "Café", "Petit-déjeuner", "1 tasse"),
            JournalEntry(id(), demoProfile.id, EntryKind.FOOD, at(2, 16, 20), "Chocolat", "Collation", "3 carrés"),
            JournalEntry(id(), demoProfile.id, EntryKind.SYMPTOM, at(2, 18), note = "Après repos sur le canapé", intensity = 2,
                context = context(ContextFactor.LYING to ContextValue.YES)),
            JournalEntry(id(), demoProfile.id, EntryKind.FOOD, at(1, 19, 40), "Tomate", "Dîner", "1 portion", "Sauce tomate"),
            JournalEntry(id(), demoProfile.id, EntryKind.SYMPTOM, at(1, 22, 5), note = "Reflux important au coucher", intensity = 4,
                context = context(ContextFactor.LYING to ContextValue.YES)),
            JournalEntry(id(), demoProfile.id, EntryKind.FOOD, at(0, 8, 15), "Banane", "Petit-déjeuner", "1")
        ))
        entries.sortBy { it.at }
        persistEntries()
        selectProfile(demoProfile.id)
    }

    private fun add(entry: JournalEntry) {
        entries.add(entry)
        entries.sortBy { it.at }
        persistEntries()
    }

    private fun persistEntries() = repository.saveEntries(entries)
    private fun persistProfiles() = repository.saveProfiles(profiles)
    private fun id() = UUID.randomUUID().toString()
}
