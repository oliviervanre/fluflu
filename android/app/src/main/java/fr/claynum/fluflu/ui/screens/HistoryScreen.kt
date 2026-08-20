package fr.claynum.fluflu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.claynum.fluflu.data.EntryKind
import fr.claynum.fluflu.data.JournalEntry
import fr.claynum.fluflu.ui.theme.Green
import fr.claynum.fluflu.ui.theme.Line
import fr.claynum.fluflu.ui.theme.Muted
import fr.claynum.fluflu.ui.theme.Orange
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

@Composable
fun HistoryScreen(entries: List<JournalEntry>, onSelectDay: (LocalDate) -> Unit) {
    val zone = ZoneId.systemDefault()
    val grouped = entries.groupBy { Instant.ofEpochMilli(it.at).atZone(zone).toLocalDate() }.toSortedMap(reverseOrder())
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("VUE D'ENSEMBLE", color = Green, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("Historique", style = MaterialTheme.typography.headlineMedium)
            Text("Les journées renseignées, du plus récent au plus ancien.", color = Muted, style = MaterialTheme.typography.bodySmall)
        }
        if (grouped.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Line)) {
                    Text("L'historique se constituera au fil des saisies.", color = Muted, modifier = Modifier.padding(24.dp))
                }
            }
        } else {
            items(grouped.entries.toList(), key = { it.key }) { (date, dayEntries) ->
                val foods = dayEntries.count { it.kind == EntryKind.FOOD }
                val symptoms = dayEntries.filter { it.kind == EntryKind.SYMPTOM }
                val maxIntensity = symptoms.maxOfOrNull { it.intensity } ?: 0
                Card(
                    modifier = Modifier.fillMaxWidth().clickable { onSelectDay(date) },
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    border = BorderStroke(1.dp, Line)
                ) {
                    Column(Modifier.padding(15.dp)) {
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(date.format(DateTimeFormatter.ofPattern("EEEE d MMMM", Locale.FRENCH)).replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold)
                            Text(if (symptoms.isEmpty()) "Aucun reflux" else "${symptoms.size} reflux · max $maxIntensity/4", color = Orange, style = MaterialTheme.typography.labelMedium)
                        }
                        Text("$foods aliment${if (foods > 1) "s" else ""} ou boisson${if (foods > 1) "s" else ""}", color = Muted, style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
