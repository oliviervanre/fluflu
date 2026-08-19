package fr.claynum.fluflu.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.claynum.fluflu.data.ContextValue
import fr.claynum.fluflu.data.EntryKind
import fr.claynum.fluflu.data.JournalEntry
import fr.claynum.fluflu.ui.theme.Green
import fr.claynum.fluflu.ui.theme.GreenSoft
import fr.claynum.fluflu.ui.theme.Line
import fr.claynum.fluflu.ui.theme.Muted
import fr.claynum.fluflu.ui.theme.Orange
import fr.claynum.fluflu.ui.theme.OrangeSoft
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle
import java.util.Locale

@Composable
fun JournalScreen(
    date: LocalDate,
    entries: List<JournalEntry>,
    onPreviousDay: () -> Unit,
    onNextDay: () -> Unit,
    onAddFood: () -> Unit,
    onAddSymptom: () -> Unit,
    onDelete: (String) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            DateHeader(date, onPreviousDay, onNextDay)
            Spacer(Modifier.size(18.dp))
            QuickAction("＋", "Ajouter un aliment", "Repas, boisson ou collation", GreenSoft, Green, onAddFood)
            Spacer(Modifier.size(9.dp))
            QuickAction("＋", "Signaler un reflux", "Intensité et contexte", OrangeSoft, Orange, onAddSymptom)
            Spacer(Modifier.size(20.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("Chronologie", style = MaterialTheme.typography.titleMedium)
                Text("${entries.size} saisie${if (entries.size > 1) "s" else ""}", color = Muted, style = MaterialTheme.typography.labelMedium)
            }
        }
        if (entries.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(16.dp), color = Color.Transparent, border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
                    Column(Modifier.fillMaxWidth().padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("◷", style = MaterialTheme.typography.headlineMedium, color = Muted)
                        Text("Aucune saisie ce jour", fontWeight = FontWeight.SemiBold)
                        Text("Ajoutez un aliment ou signalez un reflux.", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        } else {
            items(entries, key = { it.id }) { EntryCard(it, onDelete) }
        }
    }
}

@Composable
private fun DateHeader(date: LocalDate, previous: () -> Unit, next: () -> Unit) {
    val today = LocalDate.now()
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
        IconButton(onClick = previous, modifier = Modifier.background(Color.White, CircleShape)) { Text("‹", style = MaterialTheme.typography.headlineMedium) }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("JOURNAL", color = Green, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text(when (date) { today -> "Aujourd'hui"; today.minusDays(1) -> "Hier"; else -> date.format(DateTimeFormatter.ofPattern("EEEE", Locale.FRENCH)).replaceFirstChar { it.uppercase() } }, style = MaterialTheme.typography.headlineMedium)
            Text(date.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.LONG).withLocale(Locale.FRENCH)), color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        IconButton(onClick = next, enabled = date < today, modifier = Modifier.background(Color.White, CircleShape)) { Text("›", style = MaterialTheme.typography.headlineMedium) }
    }
}

@Composable
private fun QuickAction(symbol: String, title: String, subtitle: String, soft: Color, accent: Color, action: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = action),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(1.dp, Line)
    ) {
        Row(Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(11.dp), color = soft) { Text(symbol, color = accent, style = MaterialTheme.typography.headlineSmall, modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp)) }
            Column(Modifier.padding(start = 13.dp)) {
                Text(title, fontWeight = FontWeight.SemiBold)
                Text(subtitle, color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Composable
private fun EntryCard(entry: JournalEntry, onDelete: (String) -> Unit) {
    val isFood = entry.kind == EntryKind.FOOD
    val accent = if (isFood) Green else Orange
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = androidx.compose.foundation.BorderStroke(1.dp, Line)) {
        Row(Modifier.fillMaxWidth()) {
            Spacer(Modifier.size(width = 4.dp, height = 96.dp).background(accent))
            Column(Modifier.weight(1f).padding(13.dp)) {
                Text(formatTime(entry.at), color = Muted, style = MaterialTheme.typography.labelMedium)
                Text(if (isFood) entry.name else "Reflux · intensité ${entry.intensity}/4", fontWeight = FontWeight.SemiBold)
                val detail = if (isFood) listOf(entry.mealType.takeUnless { it == "Non précisé" }, entry.quantity, entry.note).filterNotNull().filter { it.isNotBlank() }.joinToString(" · ") else entry.note
                if (detail.isNotBlank()) Text(detail, color = Muted, style = MaterialTheme.typography.bodySmall)
                if (!isFood) {
                    val present = entry.context.filterValues { it == ContextValue.YES }.keys.joinToString { it.label }
                    if (present.isNotBlank()) Text(present, color = Orange, style = MaterialTheme.typography.labelSmall)
                }
            }
            TextButton(onClick = { onDelete(entry.id) }) { Text("×", color = Muted, style = MaterialTheme.typography.titleLarge) }
        }
    }
}

private fun formatTime(value: Long): String = Instant.ofEpochMilli(value).atZone(ZoneId.systemDefault())
    .format(DateTimeFormatter.ofPattern("HH:mm"))
