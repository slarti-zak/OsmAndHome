package de.hochschulz.osmandhome.ui.screens

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.hochschulz.osmandhome.ui.AppViewModel
import de.hochschulz.osmandhome.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: AppViewModel, nav: NavController) {
    val running by vm.serviceRunning.collectAsStateWithLifecycle()
    val error   by vm.error.collectAsStateWithLifecycle()
    val snack   = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let { snack.showSnackbar(it); vm.clearError() }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HA Tracker") },
                actions = {
                    IconButton(onClick = { nav.navigate(Route.Settings.path) }) {
                        Icon(Icons.Default.Settings, "Settings")
                    }
                }
            )
        },
        snackbarHost = { SnackbarHost(snack) }
    ) { pad ->
        Column(
            modifier = Modifier
                .padding(pad)
                .fillMaxSize()
                .padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Status indicator
            Surface(
                shape  = MaterialTheme.shapes.large,
                color  = if (running) MaterialTheme.colorScheme.primaryContainer
                         else MaterialTheme.colorScheme.surfaceVariant,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Icon(
                        if (running) Icons.Default.LocationOn else Icons.Default.LocationOff,
                        contentDescription = null,
                        tint = if (running) MaterialTheme.colorScheme.primary
                               else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column {
                        Text(
                            if (running) "Tracker Running" else "Tracker Stopped",
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            if (running) "Sending locations to OsmAnd" else "Tap Start to begin",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            // Start / Stop
            AnimatedContent(targetState = running, label = "startStop") { isRunning ->
                if (isRunning) {
                    Button(
                        onClick  = vm::stopService,
                        modifier = Modifier.fillMaxWidth().height(52.dp),
                        colors   = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Stop Tracker")
                    }
                } else {
                    Button(
                        onClick  = vm::startService,
                        modifier = Modifier.fillMaxWidth().height(52.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Tracker")
                    }
                }
            }

            // Entities button
            OutlinedButton(
                onClick  = { nav.navigate(Route.Discovery.path) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.People, null)
                Spacer(Modifier.width(8.dp))
                Text("Manage Entities")
            }

            OutlinedButton(
                onClick  = { nav.navigate(Route.Settings.path) },
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Default.Tune, null)
                Spacer(Modifier.width(8.dp))
                Text("Settings")
            }
        }
    }
}