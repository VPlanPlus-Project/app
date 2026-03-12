package plus.vplan.app.feature.onboarding.stage.welcome.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlin.random.Random

private const val BUBBLE_COUNT = 8

@Composable
fun BlurredBackground(seed: String = "APP_VERSION") {
    val isSystemInDarkTheme = isSystemInDarkTheme()

    // 1. Prepare the bubble data based on the seed
    val bubbles = remember(seed, isSystemInDarkTheme) {
        val random = Random(seed.hashCode())
        val baseColors = listOf(Color(0xFFFFC1CC), Color(0xFFA7C7E7), Color(0xFFB5EAD7), Color(0xFFFFF5BA))

        List(BUBBLE_COUNT) {
            val color = baseColors[random.nextInt(baseColors.size)].let {
                if (isSystemInDarkTheme) it.copy(alpha = 0.3f) // Simplify blending or use your blendColor
                else it
            }
            BubbleData(
                color = color,
                radius = 200f + random.nextFloat() * 300f,
                xRatio = random.nextFloat(),
                yRatio = random.nextFloat(),
                delay = random.nextInt(0, 500) // Random stagger
            )
        }
    }

    // 2. Animate the entry progress
    val animatable = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        animatable.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 1200, easing = FastOutSlowInEasing)
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(64.dp)
            .drawWithCache {
                onDrawBehind {
                    bubbles.forEach { bubble ->
                        // Calculate individual bubble progress with its specific delay
                        // You can get fancy here, but even a simple global alpha works wonders
                        val alpha = animatable.value * 0.7f
                        val currentRadius = bubble.radius * (0.5f + (animatable.value * 0.5f))

                        drawCircle(
                            color = bubble.color,
                            radius = currentRadius,
                            center = Offset(size.width * bubble.xRatio, size.height * bubble.yRatio),
                            alpha = alpha
                        )
                    }
                }
            }
    )
}

private data class BubbleData(
    val color: Color,
    val radius: Float,
    val xRatio: Float,
    val yRatio: Float,
    val delay: Int
)

@Preview
@Composable
private fun BlurredBackgroundPreview() {
    BlurredBackground("SEEDY")
}