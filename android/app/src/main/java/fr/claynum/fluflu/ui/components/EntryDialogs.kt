package fr.claynum.fluflu.ui.components

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.claynum.fluflu.data.ContextFactor
import fr.claynum.fluflu.data.ContextValue
import fr.claynum.fluflu.ui.theme.Green
import fr.claynum.fluflu.ui.theme.Muted
import fr.claynum.fluflu.ui.theme.Orange
import java.time.Instant
import java.time.LocalDate
import java.time.LocalTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun FoodDialog(
    recentFoods: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, Long, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var at by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var meal by remember { mutableStateOf("Non précisé") }
    var quantity by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var mealMenu by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val suggestions = recentFoods.ifEmpty { listOf("Café", "Tomate", "Chocolat", "Agrumes", "Plat épicé") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Aliment consommé") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(name, { name = it }, Modifier.fillMaxWidth(), label = { Text("Aliment ou boisson") }, singleLine = true)
                Row(Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    suggestions.forEach { suggestion -> FilterChip(selected = name.equals(suggestion, true), onClick = { name = suggestion }, label = { Text(suggestion) }) }
                }
                DateTimeField(at, { showDateTimePicker(context, at) { at = it } })
                Column {
                    Text("Type de prise", style = MaterialTheme.typography.labelMedium, color = Muted)
                    OutlinedButton(onClick = { mealMenu = true }, modifier = Modifier.fillMaxWidth()) { Text(meal) }
                    DropdownMenu(expanded = mealMenu, onDismissRequest = { mealMenu = false }) {
                        listOf("Petit-déjeuner", "Déjeuner", "Dîner", "Collation", "Boisson", "Non précisé").forEach { value ->
                            DropdownMenuItem(text = { Text(value) }, onClick = { meal = value; mealMenu = false })
                        }
                    }
                }
                OutlinedTextField(quantity, { quantity = it }, Modifier.fillMaxWidth(), label = { Text("Quantité approximative") }, singleLine = true)
                OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text("Note facultative") }, minLines = 2)
            }
        },
        confirmButton = { TextButton(enabled = name.isNotBlank(), onClick = { onSave(name, at, meal, quantity, note) }) { Text("Enregistrer") } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler", color = Muted) } }
    )
}

@Composable
fun SymptomDialog(
    onDismiss: () -> Unit,
    onSave: (Long, Int, Map<ContextFactor, ContextValue>, String) -> Unit
) {
    var at by remember { mutableLongStateOf(System.currentTimeMillis()) }
    var intensity by remember { mutableIntStateOf(0) }
    var note by remember { mutableStateOf("") }
    val contexts = remember { mutableStateMapOf<ContextFactor, ContextValue>().apply { ContextFactor.entries.forEach { put(it, ContextValue.UNKNOWN) } } }
    val context = LocalContext.current

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Épisode de reflux") },
        text = {
            Column(Modifier.verticalScroll(rememberScrollState()), verticalArrangement = Arrangement.spacedBy(11.dp)) {
                DateTimeField(at, { showDateTimePicker(context, at) { at = it } })
                Text("Intensité ressentie", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    (1..4).forEach { value ->
                        FilterChip(
                            selected = intensity == value,
                            onClick = { intensity = value },
                            label = { Text("$value", fontWeight = FontWeight.Bold) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Text("Contexte dans les deux heures précédentes", style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.SemiBold)
                ContextFactor.entries.forEach { factor -> ContextSelector(factor, contexts[factor] ?: ContextValue.UNKNOWN) { contexts[factor] = it } }
                OutlinedTextField(note, { note = it }, Modifier.fillMaxWidth(), label = { Text("Note facultative") }, minLines = 2)
            }
        },
        confirmButton = { TextButton(enabled = intensity > 0, onClick = { onSave(at, intensity, contexts.toMap(), note) }) { Text("Enregistrer", color = Orange) } },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler", color = Muted) } }
    )
}

@Composable
private fun ContextSelector(factor: ContextFactor, value: ContextValue, onChange: (ContextValue) -> Unit) {
    Column {
        Text(factor.label, style = MaterialTheme.typography.bodySmall)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            listOf(ContextValue.YES to "Oui", ContextValue.NO to "Non", ContextValue.UNKNOWN to "?").forEach { (choice, label) ->
                FilterChip(selected = value == choice, onClick = { onChange(choice) }, label = { Text(label) }, modifier = Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun DateTimeField(value: Long, onClick: () -> Unit) {
    Column {
        Text("Date et heure", style = MaterialTheme.typography.labelMedium, color = Muted)
        OutlinedButton(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
            Text(formatDateTime(value), color = Green)
        }
    }
}

private fun showDateTimePicker(context: Context, initial: Long, onSelected: (Long) -> Unit) {
    val zone = ZoneId.systemDefault()
    val current = Instant.ofEpochMilli(initial).atZone(zone)
    DatePickerDialog(
        context,
        { _, year, month, day ->
            TimePickerDialog(
                context,
                { _, hour, minute ->
                    onSelected(LocalDate.of(year, month + 1, day).atTime(LocalTime.of(hour, minute)).atZone(zone).toInstant().toEpochMilli())
                },
                current.hour,
                current.minute,
                true
            ).show()
        },
        current.year,
        current.monthValue - 1,
        current.dayOfMonth
    ).show()
}

private fun formatDateTime(value: Long): String = Instant.ofEpochMilli(value)
    .atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("EEEE d MMMM · HH:mm", Locale.FRENCH))
    .replaceFirstChar { it.uppercase() }
