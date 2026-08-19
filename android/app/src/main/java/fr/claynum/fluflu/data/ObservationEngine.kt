package fr.claynum.fluflu.data

import java.text.Normalizer
import java.util.Locale

object ObservationEngine {
    fun compute(entries: List<JournalEntry>, windowHours: Int): List<FoodObservation> {
        require(windowHours > 0)
        val symptoms = entries.filter { it.kind == EntryKind.SYMPTOM }
        val foods = entries.filter { it.kind == EntryKind.FOOD }

        return foods.groupBy { normalize(it.name) }
            .filterKeys { it.isNotBlank() }
            .map { (_, exposures) ->
                val linkedIntensities = exposures.mapNotNull { food ->
                    symptoms.asSequence()
                        .filter { symptom ->
                            val delay = symptom.at - food.at
                            delay in 0..(windowHours * HOUR_MILLIS)
                        }
                        .maxOfOrNull { it.intensity }
                }
                FoodObservation(
                    name = exposures.first().name.trim(),
                    exposures = exposures.size,
                    linkedExposures = linkedIntensities.size,
                    rate = (linkedIntensities.size * 100.0 / exposures.size).toInt(),
                    averageIntensity = linkedIntensities.takeIf { it.isNotEmpty() }?.average() ?: 0.0
                )
            }
            .sortedWith(
                compareByDescending<FoodObservation> { it.rate }
                    .thenByDescending { it.exposures }
                    .thenByDescending { it.averageIntensity }
            )
    }

    internal fun normalize(value: String): String = Normalizer
        .normalize(value.trim().lowercase(Locale.FRENCH), Normalizer.Form.NFD)
        .replace("\\p{M}+".toRegex(), "")
        .replace("\\s+".toRegex(), " ")

    private const val HOUR_MILLIS = 3_600_000L
}
