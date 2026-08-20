package fr.claynum.fluflu.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.claynum.fluflu.data.ContextValue
import fr.claynum.fluflu.data.EntryKind
import fr.claynum.fluflu.data.FoodObservation
import fr.claynum.fluflu.data.JournalEntry
import fr.claynum.fluflu.data.ObservationEngine
import fr.claynum.fluflu.ui.theme.Green
import fr.claynum.fluflu.ui.theme.GreenSoft
import fr.claynum.fluflu.ui.theme.Line
import fr.claynum.fluflu.ui.theme.Muted
import fr.claynum.fluflu.ui.theme.Orange
import java.util.Locale

@Composable
fun ObservationsScreen(entries: List<JournalEntry>) {
    var window by remember { mutableIntStateOf(3) }
    val snapshot = entries.toList()
    val results = remember(snapshot, window) { ObservationEngine.compute(snapshot, window) }
    val symptoms = snapshot.filter { it.kind == EntryKind.SYMPTOM }
    val answers = symptoms.flatMap { it.context.values }
    val known = answers.count { it != ContextValue.UNKNOWN }
    val quality = if (answers.isEmpty()) 0 else known * 100 / answers.size

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        item {
            Text("OBSERVATIONS", color = Green, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
            Text("Aliments à surveiller", style = MaterialTheme.typography.headlineMedium)
            Text("Associations observées entre une consommation et un reflux ultérieur. Ce classement ne constitue pas un diagnostic.", color = Muted, style = MaterialTheme.typography.bodySmall)
            Spacer(Modifier.height(16.dp))
            WindowPicker(window) { window = it }
            Spacer(Modifier.height(12.dp))
            Surface(color = GreenSoft, shape = RoundedCornerShape(14.dp)) {
                Column(Modifier.padding(14.dp)) {
                    Text("Qualité du contexte : $quality % renseigné", fontWeight = FontWeight.SemiBold)
                    Text("${symptoms.size} reflux saisi${if (symptoms.size > 1) "s" else ""}. Une réponse non renseignée n'est jamais interprétée comme l'absence du facteur.", style = MaterialTheme.typography.bodySmall)
                }
            }
            Spacer(Modifier.height(2.dp))
        }
        if (results.isEmpty()) {
            item {
                Surface(shape = RoundedCornerShape(14.dp), color = Color.White, border = BorderStroke(1.dp, Line)) {
                    Text("Les premières associations apparaîtront après la saisie d'aliments et de reflux.", color = Muted, modifier = Modifier.padding(24.dp))
                }
            }
        } else {
            items(results, key = { it.name }) { ObservationCard(it) }
        }
        item {
            Text(
                "Le taux correspond à la part des consommations suivies d'au moins un reflux dans les $window heures. Une association répétée mérite d'être observée ; elle ne démontre pas un lien de causalité.",
                color = Muted,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 6.dp)
            )
        }
    }
}

@Composable
private fun WindowPicker(selected: Int, onSelect: (Int) -> Unit) {
    Row(Modifier.fillMaxWidth().background(Color(0xFFE9EFEC), RoundedCornerShape(12.dp)).padding(4.dp)) {
        listOf(1, 3, 6).forEach { value ->
            Surface(
                modifier = Modifier.weight(1f),
                color = if (selected == value) Color.White else Color.Transparent,
                shape = RoundedCornerShape(9.dp)
            ) {
                TextButton(onClick = { onSelect(value) }) {
                    Text("$value h", color = if (selected == value) Green else Muted, fontWeight = FontWeight.SemiBold)
                }
            }
        }
    }
}

@Composable
private fun ObservationCard(item: FoodObservation) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.White), border = BorderStroke(1.dp, Line)) {
        Column(Modifier.padding(15.dp)) {
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(item.name.replaceFirstChar { it.uppercase() }, fontWeight = FontWeight.SemiBold)
                Text("${item.rate} %", color = Orange, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.height(9.dp))
            Row(Modifier.fillMaxWidth().height(6.dp).background(Color(0xFFEDF1EF), RoundedCornerShape(99.dp))) {
                Spacer(Modifier.fillMaxWidth(item.rate / 100f).height(6.dp).background(Orange, RoundedCornerShape(99.dp)))
            }
            Spacer(Modifier.height(7.dp))
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text("${item.linkedExposures}/${item.exposures} consommation${if (item.exposures > 1) "s" else ""}", color = Muted, style = MaterialTheme.typography.labelSmall)
                Text(if (item.averageIntensity > 0) "int. moy. ${String.format(Locale.FRENCH, "%.1f", item.averageIntensity)}/4" else "aucun reflux", color = Muted, style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
