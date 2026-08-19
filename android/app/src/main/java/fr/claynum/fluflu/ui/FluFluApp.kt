package fr.claynum.fluflu.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import fr.claynum.fluflu.ui.components.FoodDialog
import fr.claynum.fluflu.ui.components.SymptomDialog
import fr.claynum.fluflu.ui.screens.HistoryScreen
import fr.claynum.fluflu.ui.screens.JournalScreen
import fr.claynum.fluflu.ui.screens.ObservationsScreen
import fr.claynum.fluflu.ui.theme.GreenDark
import fr.claynum.fluflu.ui.theme.Paper
import fr.claynum.fluflu.viewmodel.MainViewModel
import java.time.LocalDate

private enum class AppScreen(val label: String, val symbol: String) {
    JOURNAL("Journal", "◷"),
    OBSERVATIONS("Analyse", "⌁"),
    HISTORY("Historique", "▤")
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FluFluApp(viewModel: MainViewModel) {
    var screenName by rememberSaveable { mutableStateOf(AppScreen.JOURNAL.name) }
    val screen = AppScreen.valueOf(screenName)
    var selectedDay by rememberSaveable { mutableLongStateOf(LocalDate.now().toEpochDay()) }
    var showFood by remember { mutableStateOf(false) }
    var showSymptom by remember { mutableStateOf(false) }
    var showSettings by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = Paper,
        topBar = {
            TopAppBar(
                title = { Text("FluFlu", fontWeight = FontWeight.Bold) },
                actions = { TextButton(onClick = { showSettings = true }) { Text("•••", color = Color.White) } },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = GreenDark,
                    titleContentColor = Color.White
                )
            )
        },
        bottomBar = {
            NavigationBar(containerColor = Color.White) {
                AppScreen.entries.forEach { item ->
                    NavigationBarItem(
                        selected = screen == item,
                        onClick = { screenName = item.name },
                        icon = { Text(item.symbol, style = MaterialTheme.typography.titleLarge) },
                        label = { Text(item.label) }
                    )
                }
            }
        }
    ) { padding ->
        Box(Modifier.padding(padding)) {
            when (screen) {
                AppScreen.JOURNAL -> JournalScreen(
                    date = LocalDate.ofEpochDay(selectedDay),
                    entries = viewModel.entriesOn(LocalDate.ofEpochDay(selectedDay)),
                    onPreviousDay = { selectedDay-- },
                    onNextDay = { if (selectedDay < LocalDate.now().toEpochDay()) selectedDay++ },
                    onAddFood = { showFood = true },
                    onAddSymptom = { showSymptom = true },
                    onDelete = viewModel::delete
                )
                AppScreen.OBSERVATIONS -> ObservationsScreen(viewModel.entries)
                AppScreen.HISTORY -> HistoryScreen(
                    entries = viewModel.entries,
                    onSelectDay = { date -> selectedDay = date.toEpochDay(); screenName = AppScreen.JOURNAL.name }
                )
            }
        }
    }

    if (showFood) {
        FoodDialog(
            recentFoods = viewModel.entries.mapNotNull { it.name.takeIf(String::isNotBlank) }.distinct().takeLast(5).reversed(),
            onDismiss = { showFood = false },
            onSave = { name, at, meal, quantity, note ->
                viewModel.addFood(name, at, meal, quantity, note)
                selectedDay = java.time.Instant.ofEpochMilli(at).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay()
                showFood = false
            }
        )
    }
    if (showSymptom) {
        SymptomDialog(
            onDismiss = { showSymptom = false },
            onSave = { at, intensity, context, note ->
                viewModel.addSymptom(at, intensity, context, note)
                selectedDay = java.time.Instant.ofEpochMilli(at).atZone(java.time.ZoneId.systemDefault()).toLocalDate().toEpochDay()
                showSymptom = false
            }
        )
    }
    if (showSettings) {
        AlertDialog(
            onDismissRequest = { showSettings = false },
            title = { Text("Données du prototype") },
            text = { Text("Les saisies restent uniquement sur cet appareil. Le chargement de la démonstration remplace les données actuelles.") },
            confirmButton = {
                TextButton(onClick = { viewModel.loadDemo(); showSettings = false }) { Text("Charger la démonstration") }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.clear(); showSettings = false }) { Text("Tout effacer", color = MaterialTheme.colorScheme.secondary) }
            }
        )
    }
}
