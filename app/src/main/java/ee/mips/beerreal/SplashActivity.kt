package ee.mips.beerreal

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import ee.mips.beerreal.ui.theme.BeerRealTheme

class SplashActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            BeerRealTheme {
                AnimatedBeerSplash(onFinished = {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()
                })
            }
        }
    }
}

@Composable
private fun AnimatedBeerSplash(onFinished: () -> Unit) {
    val context = LocalContext.current
    val fill = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Slower rise
        fill.animateTo(1f, animationSpec = tween(2800))
        onFinished()
    }

    val infinite = rememberInfiniteTransition(label = "foam")
    val foamOffset by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(animation = tween(1200), repeatMode = RepeatMode.Reverse),
        label = "foamOffset"
    )

    val beerLight = Color(0xFFF6B44B)
    val beerDark = Color(0xFFDD8A00)
    val foam = Color(0xFFFFF6DE)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black),
        contentAlignment = Alignment.Center
    ) {
        Canvas(Modifier.fillMaxSize()) {
            val beerHeight = size.height * fill.value

            if (beerHeight > 1f) {
                // Beer liquid
                clipRect(
                    left = 0f,
                    top = size.height - beerHeight,
                    right = size.width,
                    bottom = size.height
                ) {
                    drawRect(
                        brush = Brush.verticalGradient(listOf(beerDark, beerLight)),
                        size = this@Canvas.size
                    )

                    // Bubbles only after beer is visible to avoid divide-by-zero
                    val cols = 12
                    val spacing = size.width / cols
                    for (i in 0 until cols) {
                        val radius = 4f + (i % 3) * 2.5f
                        val cycle = 140f
                        val offsetInCycle = ((i * 37f) + fill.value * cycle) % cycle
                        val y = size.height - offsetInCycle
                        val x = spacing * (i + 0.5f)
                        if (y > size.height - beerHeight) {
                            drawCircle(
                                color = Color(0x33FFFFFF),
                                radius = radius,
                                center = androidx.compose.ui.geometry.Offset(x, y)
                            )
                        }
                    }
                }

                // Bigger foam head
                val foamHeight = 32f
                val foamTop = size.height - beerHeight - foamHeight - foamOffset
                drawRect(
                    color = foam,
                    topLeft = androidx.compose.ui.geometry.Offset(0f, foamTop),
                    size = androidx.compose.ui.geometry.Size(size.width, foamHeight)
                )
                val blobCount = 28
                val gap = size.width / blobCount
                val blobRadius = 12f
                for (i in 0..blobCount) {
                    val x = i * gap
                    drawCircle(color = foam, radius = blobRadius, center = androidx.compose.ui.geometry.Offset(x, foamTop))
                }
            }
        }

        val splashId = remember { context.resources.getIdentifier("splash", "drawable", context.packageName) }
        val painter = if (splashId != 0) painterResource(id = splashId) else painterResource(id = R.drawable.ic_launcher_foreground)
        Image(
            painter = painter,
            contentDescription = "Splash foreground",
            modifier = Modifier.fillMaxSize().alpha(0.95f)
        )
    }
}
