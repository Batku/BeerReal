package ee.mips.beerreal

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import ee.mips.beerreal.ui.navigation.BeerRealApp
import ee.mips.beerreal.ui.theme.BeerRealTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BeerRealTheme {
                BeerRealApp()
            }
        }
    }
}