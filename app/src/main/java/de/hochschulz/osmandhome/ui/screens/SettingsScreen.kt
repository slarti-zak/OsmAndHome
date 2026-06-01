package de.hochschulz.osmandhome.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.hochschulz.osmandhome.ui.AppViewModel

private val INTERVAL_OPTIONS = listOf(
    15_000L to "15 seconds",
    30_000L to "30 seconds",
    60_000L to "1 minute",
    120_000L to "2 minutes",
    300_000L to "5 minutes"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(vm: AppViewModel, nav: NavController) {
    val saved by vm.settings.collectAsStateWithLifecycle()
    var draft by remember(saved) { mutableStateOf(saved) }
    var tokenVisible by remember { mutableStateOf(false) }
    var intervalExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                }
            )
        }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SectionHeader("Home Assistant Server")

            OutlinedTextField(
                value         = draft.serverUrl,
                onValueChange = { draft = draft.copy(serverUrl = it) },
                label         = { Text("Server URL") },
                placeholder   = { Text("https://homeassistant.local:8123") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
            )

            OutlinedTextField(
                value         = draft.token,
                onValueChange = { draft = draft.copy(token = it) },
                label         = { Text("Long-lived Access Token") },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true,
                visualTransformation = if (tokenVisible) VisualTransformation.None
                                       else PasswordVisualTransformation(),
                trailingIcon  = {
                    IconButton(onClick = { tokenVisible = !tokenVisible }) {
                        Icon(
                            if (tokenVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                            "Toggle visibility"
                        )
                    }
                }
            )

            HorizontalDivider()
            SectionHeader("Polling")

            ExposedDropdownMenuBox(
                expanded         = intervalExpanded,
                onExpandedChange = { intervalExpanded = it }
            ) {
                OutlinedTextField(
                    value        = INTERVAL_OPTIONS.find { it.first == draft.intervalMs }?.second ?: "1 minute",
                    onValueChange = {},
                    readOnly     = true,
                    label        = { Text("Update interval") },
                    trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(intervalExpanded) },
                    modifier     = Modifier.fillMaxWidth().menuAnchor()
                )
                ExposedDropdownMenu(expanded = intervalExpanded, onDismissRequest = { intervalExpanded = false }) {
                    INTERVAL_OPTIONS.forEach { (ms, label) ->
                        DropdownMenuItem(
                            text    = { Text(label) },
                            onClick = { draft = draft.copy(intervalMs = ms); intervalExpanded = false }
                        )
                    }
                }
            }

            HorizontalDivider()
            SectionHeader("Advanced")

            LabeledSwitch("Trust self-signed SSL certificate", draft.trustSsl) {
                draft = draft.copy(trustSsl = it)
            }
            LabeledSwitch("Auto-start on device boot", draft.autostart) {
                draft = draft.copy(autostart = it)
            }
            LabeledSwitch("Remove markers when tracker stops", draft.removeOnStop) {
                draft = draft.copy(removeOnStop = it)
            }

            Spacer(Modifier.height(8.dp))

            Button(
                onClick  = { vm.saveSettings(draft); nav.popBackStack() },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Save Settings") }
        }
    }
}

@Composable
private fun SectionHeader(text: String) =
    Text(text, style = MaterialTheme.typography.labelLarge,
         color = MaterialTheme.colorScheme.primary)

@Composable
private fun LabeledSwitch(label: String, checked: Boolean, onChanged: (Boolean) -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, modifier = Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
        Switch(checked = checked, onCheckedChange = onChanged)
    }
}