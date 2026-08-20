package fr.claynum.fluflu.data

enum class EntryKind { FOOD, SYMPTOM }

enum class ContextValue { YES, NO, UNKNOWN }

enum class ContextFactor(val label: String) {
    LYING("Position allongée"),
    ACTIVITY("Activité physique"),
    STRESS("Stress inhabituel"),
    MEDICATION("Médicament récent"),
    ALCOHOL("Alcool consommé")
}

data class JournalEntry(
    val id: String,
    val profileId: String,
    val kind: EntryKind,
    val at: Long,
    val name: String = "",
    val mealType: String = "Non précisé",
    val quantity: String = "",
    val note: String = "",
    val intensity: Int = 0,
    val context: Map<ContextFactor, ContextValue> = emptyMap()
)

data class FoodObservation(
    val name: String,
    val exposures: Int,
    val linkedExposures: Int,
    val rate: Int,
    val averageIntensity: Double
)
