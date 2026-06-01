package de.hochschulz.osmandhome.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.unit.dp
import de.hochschulz.osmandhome.api.HaState
import de.hochschulz.osmandhome.config.TrackedEntityConfig

private val COLOR_OPTIONS = listOf(
    "Auto"   to null,
    "Teal"   to Color(0xFF01696F),
    "Green"  to Color(0xFF437A22),
    "Purple" to Color(0xFFa12c7b),
    "Orange" to Color(0xFFda7101),
    "Blue"   to Color(0xFF006494),
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntitySettingsSheet(
    entity: HaState,
    existing: TrackedEntityConfig?,
    onDismiss: () -> Unit,
    onSave: (TrackedEntityConfig) -> Unit
) {
    var displayName  by remember { mutableStateOf(existing?.displayName ?: entity.attributes.friendlyName ?: "") }
    var enabled      by remember { mutableStateOf(existing?.enabled ?: true) }
    var showLabel    by remember { mutableStateOf(existing?.showLabel ?: true) }
    var showAvatar   by remember { mutableStateOf(existing?.showAvatar ?: true) }
    var selectedColor by remember {
        val current = existing?.customColor
        mutableStateOf(COLOR_OPTIONS.indexOfFirst { it.second?.toArgb() == current }.coerceAtLeast(0))
    }

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(entity.attributes.friendlyName ?: entity.entityId,
                 style = MaterialTheme.typography.titleLarge)
            Text(entity.entityId,
                 style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)

            HorizontalDivider()

            OutlinedTextField(
                value         = displayName,
                onValueChange = { displayName = it },
                label         = { Text("Display name") },
                placeholder   = { Text(entity.attributes.friendlyName ?: entity.entityId) },
                modifier      = Modifier.fillMaxWidth(),
                singleLine    = true
            )

            // Color picker
            Text("Marker colour", style = MaterialTheme.typography.labelLarge)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                COLOR_OPTIONS.forEachIndexed { idx, (label, color) ->
                    FilterChip(
                        selected = selectedColor == idx,
                        onClick  = { selectedColor = idx },
                        label    = { Text(label) },
                        leadingIcon = if (color != null) ({
                            Surface(color = color, shape = MaterialTheme.shapes.extraSmall,
                                    modifier = Modifier.size(12.dp)) {}
                        }) else null
                    )
                }
            }

            HorizontalDivider()

            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Enabled on map")
                Switch(checked = enabled, onCheckedChange = { enabled = it })
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Show name label")
                Switch(checked = showLabel, onCheckedChange = { showLabel = it })
            }
            Row(Modifier.fillMaxWidth(), Arrangement.SpaceBetween, Alignment.CenterVertically) {
                Text("Show avatar initials")
                Switch(checked = showAvatar, onCheckedChange = { showAvatar = it })
            }

            Button(
                onClick  = {
                    onSave(TrackedEntityConfig(
                        entityId    = entity.entityId,
                        enabled     = enabled,
                        displayName = displayName.trim().ifBlank { null },
                        customColor = COLOR_OPTIONS[selectedColor].second?.toArgb(),
                        showLabel   = showLabel,
                        showAvatar  = showAvatar
                    ))
                },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text("Save") }
        }
    }
}