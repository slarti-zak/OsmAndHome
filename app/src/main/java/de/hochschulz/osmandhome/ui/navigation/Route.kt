package de.hochschulz.osmandhome.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import de.hochschulz.osmandhome.ui.AppViewModel
import de.hochschulz.osmandhome.ui.screens.EntityDiscoveryScreen
import de.hochschulz.osmandhome.ui.screens.HomeScreen
import de.hochschulz.osmandhome.ui.screens.SettingsScreen

sealed class Route(val path: String) {
    object Home      : Route("home")
    object Settings  : Route("settings")
    object Discovery : Route("discovery")
}

@Composable
fun AppNavigation(vm: AppViewModel) {
    val nav = rememberNavController()
    NavHost(navController = nav, startDestination = Route.Home.path) {
        composable(Route.Home.path)      { HomeScreen(vm, nav) }
        composable(Route.Settings.path)  { SettingsScreen(vm, nav) }
        composable(Route.Discovery.path) { EntityDiscoveryScreen(vm, nav) }
    }
}