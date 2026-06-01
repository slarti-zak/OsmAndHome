package de.hochschulz.osmandhome

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import de.hochschulz.osmandhome.ui.AppViewModel
import de.hochschulz.osmandhome.ui.navigation.AppNavigation
import de.hochschulz.osmandhome.ui.theme.HaTrackerTheme

class MainActivity : ComponentActivity() {
    private val vm: AppViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            HaTrackerTheme(darkTheme = isSystemInDarkTheme()) {
                AppNavigation(vm)
            }
        }
    }
}