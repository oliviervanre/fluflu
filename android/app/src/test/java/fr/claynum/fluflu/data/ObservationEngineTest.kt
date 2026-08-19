package fr.claynum.fluflu.data

import org.junit.Assert.assertEquals
import org.junit.Test

class ObservationEngineTest {
    @Test
    fun `un reflux dans les trois heures est associe a la consommation`() {
        val entries = listOf(
            food("Café", at = 0),
            symptom(at = 2 * HOUR, intensity = 3),
            food("Café", at = 10 * HOUR)
        )

        val result = ObservationEngine.compute(entries, 3).single()

        assertEquals(2, result.exposures)
        assertEquals(1, result.linkedExposures)
        assertEquals(50, result.rate)
        assertEquals(3.0, result.averageIntensity, 0.01)
    }

    @Test
    fun `les variantes de casse et accents sont normalisees`() {
        val entries = listOf(food("Café", 0), food(" cafe ", HOUR))

        val result = ObservationEngine.compute(entries, 1).single()

        assertEquals(2, result.exposures)
        assertEquals("Café", result.name)
    }

    @Test
    fun `un reflux anterieur n'est jamais associe`() {
        val entries = listOf(symptom(0, 4), food("Tomate", HOUR))

        val result = ObservationEngine.compute(entries, 6).single()

        assertEquals(0, result.linkedExposures)
    }

    private fun food(name: String, at: Long) = JournalEntry("f-$at", EntryKind.FOOD, at, name)
    private fun symptom(at: Long, intensity: Int) = JournalEntry("s-$at", EntryKind.SYMPTOM, at, intensity = intensity)

    private companion object { const val HOUR = 3_600_000L }
}
