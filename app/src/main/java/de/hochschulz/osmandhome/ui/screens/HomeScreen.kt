package de.hochschulz.osmandhome.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOff
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import de.hochschulz.osmandhome.ui.AppViewModel
import de.hochschulz.osmandhome.ui.navigation.Route

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(vm: AppViewModel, nav: NavController) {
    val running by vm.serviceRunning.collectAsStateWithLifecycle()
    val error by vm.error.collectAsStateWithLifecycle()
    val snack = remember { SnackbarHostState() }

    LaunchedEffect(error) {
        error?.let {
            snack.showSnackbar(it)
            vm.clearError()
        }
    }

    HomeScreenContent(
        running = running,
        snack = snack,
        onStart = vm::startService,
        onStop = vm::stopService,
        onOpenDiscovery = { nav.navigate(Route.Discovery.path) },
        onOpenSettings = { nav.navigate(Route.Settings.path) }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreenContent(
    running: Boolean,
    snack: SnackbarHostState,
    onStart: () -> Unit,
    onStop: () -> Unit,
    onOpenDiscovery: () -> Unit,
    onOpenSettings: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("HA Tracker") },
                actions = {
                    IconButton(onClick = onOpenSettings) {
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
            Surface(
                shape = MaterialTheme.shapes.large,
                color = if (running) MaterialTheme.colorScheme.primaryContainer
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

            AnimatedContent(targetState = running, label = "startStop") { isRunning ->
                if (isRunning) {
                    Button(
                        onClick = onStop,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.error
                        )
                    ) {
                        Icon(Icons.Default.Stop, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Stop Tracker")
                    }
                } else {
                    Button(
                        onClick = onStart,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(52.dp)
                    ) {
                        Icon(Icons.Default.PlayArrow, null)
                        Spacer(Modifier.width(8.dp))
                        Text("Start Tracker")
                    }
                }
            }

            OutlinedButton(
                onClick = onOpenDiscovery,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.People, null)
                Spacer(Modifier.width(8.dp))
                Text("Manage Entities")
            }

            OutlinedButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Icon(Icons.Default.Tune, null)
                Spacer(Modifier.width(8.dp))
                Text("Settings")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreviewStopped() {
    HomeScreenContent(
        running = false,
        snack = remember { SnackbarHostState() },
        onStart = {},
        onStop = {},
        onOpenDiscovery = {},
        onOpenSettings = {}
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeScreenPreviewRunning() {
    HomeScreenContent(
        running = true,
        snack = remember { SnackbarHostState() },
        onStart = {},
        onStop = {},
        onOpenDiscovery = {},
        onOpenSettings = {}
    )
}
