package ee.mips.beerreal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ee.mips.beerreal.ui.navigation.BeerRealApp
import ee.mips.beerreal.ui.theme.BeerRealTheme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import ee.mips.beerreal.data.settings.SettingsRepository

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val darkMode by SettingsRepository.darkModeFlow(this@MainActivity)
                .collectAsState(initial = isSystemInDarkTheme())

            BeerRealTheme(darkTheme = darkMode) {
                BeerRealApp()
            }
        }
    }
}