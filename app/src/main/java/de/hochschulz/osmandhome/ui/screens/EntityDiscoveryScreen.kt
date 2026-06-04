package de.hochschulz.osmandhome.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.hochschulz.osmandhome.R
import de.hochschulz.osmandhome.api.HaState
import de.hochschulz.osmandhome.config.TrackedEntityConfig
import de.hochschulz.osmandhome.ui.AppViewModel
import de.hochschulz.osmandhome.ui.components.EntityAvatar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EntityDiscoveryScreen(vm: AppViewModel, nav: NavController) {
    val state by vm.discovery.collectAsStateWithLifecycle()
    var editTarget by remember { mutableStateOf<HaState?>(null) }

    LaunchedEffect(Unit) { if (state.allStates.isEmpty()) vm.loadEntities() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.home_assistant_entities)) },
                navigationIcon = {
                    IconButton(onClick = { nav.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, "Back")
                    }
                },
                actions = {
                    IconButton(onClick = vm::loadEntities) {
                        Icon(Icons.Default.Refresh, "Refresh")
                    }
                }
            )
        }
    ) { pad ->
        Column(Modifier.padding(pad)) {
            // Search bar
            OutlinedTextField(
                value         = state.query,
                onValueChange = vm::setQuery,
                placeholder   = { Text(stringResource(R.string.home_assistant_search_entities)) },
                leadingIcon   = { Icon(Icons.Default.Search, null) },
                modifier      = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                singleLine    = true
            )

            // Bulk actions
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AssistChip(onClick = vm::enableAll,  label = { Text(stringResource(R.string.selection_enable_all)) },
                    leadingIcon = { Icon(Icons.Default.CheckCircle, null, Modifier.size(16.dp)) })
                AssistChip(onClick = vm::disableAll, label = { Text(stringResource(R.string.selection_disable_all)) },
                    leadingIcon = { Icon(Icons.Default.Cancel, null, Modifier.size(16.dp)) })
                AssistChip(onClick = vm::resetToDefaults, label = { Text(stringResource(R.string.selection_reset)) },
                    leadingIcon = { Icon(Icons.Default.RestartAlt, null, Modifier.size(16.dp)) })
            }

            when {
                state.loading -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    CircularProgressIndicator()
                }
                state.error != null -> Box(Modifier
                    .fillMaxSize()
                    .padding(32.dp), Alignment.Center) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally,
                           verticalArrangement = Arrangement.spacedBy(12.dp)) {
                        Icon(Icons.Default.ErrorOutline, null,
                             Modifier.size(48.dp), MaterialTheme.colorScheme.error)
                        Text(state.error!!, color = MaterialTheme.colorScheme.error)
                        Button(onClick = vm::loadEntities) { Text("Retry") }
                    }
                }
                state.filtered.isEmpty() -> Box(Modifier.fillMaxSize(), Alignment.Center) {
                    Text("No entities with location found",
                         color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                else -> LazyColumn(contentPadding = PaddingValues(bottom = 16.dp)) {
                    items(state.filtered, key = { it.entityId }) { entity ->
                        EntityListItem(
                            entity  = entity,
                            config  = state.configs[entity.entityId],
                            onToggle = { vm.setEntityEnabled(entity.entityId, it) },
                            onEdit   = { editTarget = entity }
                        )
                        HorizontalDivider(Modifier.padding(horizontal = 16.dp))
                    }
                }
            }
        }
    }

    editTarget?.let { entity ->
        EntitySettingsSheet(
            entity   = entity,
            existing = state.configs[entity.entityId],
            onDismiss = { editTarget = null },
            onSave    = { cfg -> vm.upsertEntityConfig(cfg); editTarget = null }
        )
    }
}

@Composable
private fun EntityListItem(
    entity: HaState,
    config: TrackedEntityConfig?,
    onToggle: (Boolean) -> Unit,
    onEdit: () -> Unit
) {
    val displayName = config?.displayName?.takeIf { it.isNotBlank() }
        ?: entity.attributes.friendlyName ?: entity.entityId
    val enabled = config?.enabled ?: true

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        EntityAvatar(name = displayName, entityId = entity.entityId, size = 44.dp)
        Column(Modifier.weight(1f)) {
            Text(displayName, style = MaterialTheme.typography.bodyLarge)
            Text(entity.entityId, style = MaterialTheme.typography.bodySmall,
                 color = MaterialTheme.colorScheme.onSurfaceVariant)
            Surface(
                shape = MaterialTheme.shapes.extraSmall,
                color = MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.padding(top = 2.dp)
            ) {
                Text(entity.state,
                     style    = MaterialTheme.typography.labelSmall,
                     modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp))
            }
        }
        IconButton(onClick = onEdit) {
            Icon(Icons.Default.Edit, "Edit", Modifier.size(20.dp),
                 MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = enabled, onCheckedChange = onToggle)
    }
}