package fr.claynum.fluflu.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import fr.claynum.fluflu.data.ProfileType
import fr.claynum.fluflu.data.UserProfile
import fr.claynum.fluflu.ui.theme.Muted

@Composable
fun ProfileSetupDialog(onCreate: (String) -> Unit) {
    var firstName by remember { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = {},
        title = { Text("Créer votre profil") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Text("Le prénom permet d'identifier les données enregistrées sur cet appareil.")
                OutlinedTextField(
                    value = firstName,
                    onValueChange = { firstName = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Prénom") },
                    singleLine = true
                )
                Text("Aucune donnée n'est envoyée en ligne.", color = Muted, style = MaterialTheme.typography.bodySmall)
            }
        },
        confirmButton = {
            TextButton(enabled = firstName.isNotBlank(), onClick = { onCreate(firstName) }) {
                Text("Créer le profil")
            }
        }
    )
}

@Composable
fun ProfileSettingsDialog(
    activeProfile: UserProfile,
    profiles: List<UserProfile>,
    onSelectProfile: (String) -> Unit,
    onRename: () -> Unit,
    onLoadDemo: () -> Unit,
    onClear: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Profil et données") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text("Profil actif", fontWeight = FontWeight.SemiBold)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    profiles.forEach { profile ->
                        FilterChip(
                            selected = profile.id == activeProfile.id,
                            onClick = { onSelectProfile(profile.id) },
                            label = { Text(profile.firstName) }
                        )
                    }
                }
                if (activeProfile.type == ProfileType.PERSONAL) {
                    TextButton(onClick = onRename) { Text("Modifier le prénom") }
                }
                Text("Les saisies restent uniquement sur cet appareil.", color = Muted, style = MaterialTheme.typography.bodySmall)
                TextButton(onClick = onLoadDemo) { Text("Charger les données de démonstration") }
                TextButton(onClick = onClear) { Text("Effacer les saisies de ce profil", color = MaterialTheme.colorScheme.secondary) }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Fermer") } }
    )
}

@Composable
fun RenameProfileDialog(currentName: String, onSave: (String) -> Unit, onDismiss: () -> Unit) {
    var firstName by remember(currentName) { mutableStateOf(currentName) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Modifier le prénom") },
        text = {
            OutlinedTextField(
                value = firstName,
                onValueChange = { firstName = it },
                modifier = Modifier.fillMaxWidth(),
                label = { Text("Prénom") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(enabled = firstName.isNotBlank(), onClick = { onSave(firstName) }) { Text("Enregistrer") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Annuler") } }
    )
}
