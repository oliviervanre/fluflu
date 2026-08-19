package fr.claynum.fluflu.viewmodel

import android.app.Application
import androidx.compose.runtime.mutableStateListOf
import androidx.lifecycle.AndroidViewModel
import fr.claynum.fluflu.data.ContextFactor
import fr.claynum.fluflu.data.ContextValue
import fr.claynum.fluflu.data.EntryKind
import fr.claynum.fluflu.data.EntryRepository
import fr.claynum.fluflu.data.JournalEntry
import fr.claynum.fluflu.data.ObservationEngine
import java.time.LocalDate
import java.time.ZoneId
import java.util.UUID

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = EntryRepository(application)
    val entries = mutableStateListOf<JournalEntry>()

    init {
        entries.addAll(repository.load().sortedBy { it.at })
    }

    fun addFood(name: String, at: Long, mealType: String, quantity: String, note: String) {
        add(JournalEntry(UUID.randomUUID().toString(), EntryKind.FOOD, at, name.trim(), mealType, quantity.trim(), note.trim()))
    }

    fun addSymptom(at: Long, intensity: Int, context: Map<ContextFactor, ContextValue>, note: String) {
        add(JournalEntry(UUID.randomUUID().toString(), EntryKind.SYMPTOM, at, note = note.trim(), intensity = intensity, context = context))
    }

    fun delete(id: String) {
        entries.removeAll { it.id == id }
        persist()
    }

    fun clear() {
        entries.clear()
        repository.clear()
    }

    fun observations(windowHours: Int) = ObservationEngine.compute(entries, windowHours)

    fun entriesOn(date: LocalDate): List<JournalEntry> {
        val zone = ZoneId.systemDefault()
        return entries.filter { entry ->
            java.time.Instant.ofEpochMilli(entry.at).atZone(zone).toLocalDate() == date
        }.sortedBy { it.at }
    }

    fun loadDemo() {
        val zone = ZoneId.systemDefault()
        fun at(daysAgo: Long, hour: Int, minute: Int = 0) = LocalDate.now()
            .minusDays(daysAgo).atTime(hour, minute).atZone(zone).toInstant().toEpochMilli()
        fun unknownContext() = ContextFactor.entries.associateWith { ContextValue.UNKNOWN }
        fun context(vararg values: Pair<ContextFactor, ContextValue>) = unknownContext().toMutableMap().apply { putAll(values) }

        entries.clear()
        entries.addAll(listOf(
            JournalEntry(id(), EntryKind.FOOD, at(4, 8, 10), "Café", "Petit-déjeuner", "1 tasse"),
            JournalEntry(id(), EntryKind.FOOD, at(4, 8, 12), "Pain", "Petit-déjeuner", "2 tranches"),
            JournalEntry(id(), EntryKind.SYMPTOM, at(4, 10, 5), note = "Brûlure légère", intensity = 2,
                context = context(ContextFactor.STRESS to ContextValue.YES, ContextFactor.LYING to ContextValue.NO)),
            JournalEntry(id(), EntryKind.FOOD, at(3, 12, 35), "Tomate", "Déjeuner", "1 portion", "En salade"),
            JournalEntry(id(), EntryKind.SYMPTOM, at(3, 14, 10), intensity = 3, context = unknownContext()),
            JournalEntry(id(), EntryKind.FOOD, at(2, 9, 5), "Café", "Petit-déjeuner", "1 tasse"),
            JournalEntry(id(), EntryKind.FOOD, at(2, 16, 20), "Chocolat", "Collation", "3 carrés"),
            JournalEntry(id(), EntryKind.SYMPTOM, at(2, 18), note = "Après repos sur le canapé", intensity = 2,
                context = context(ContextFactor.LYING to ContextValue.YES)),
            JournalEntry(id(), EntryKind.FOOD, at(1, 19, 40), "Tomate", "Dîner", "1 portion", "Sauce tomate"),
            JournalEntry(id(), EntryKind.SYMPTOM, at(1, 22, 5), note = "Reflux important au coucher", intensity = 4,
                context = context(ContextFactor.LYING to ContextValue.YES)),
            JournalEntry(id(), EntryKind.FOOD, at(0, 8, 15), "Banane", "Petit-déjeuner", "1")
        ).sortedBy { it.at })
        persist()
    }

    private fun add(entry: JournalEntry) {
        entries.add(entry)
        entries.sortBy { it.at }
        persist()
    }

    private fun persist() = repository.save(entries)
    private fun id() = UUID.randomUUID().toString()
}
