package plus.vplan.app.feature.onboarding.stage.a_welcome.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.ui.tooling.preview.Preview
import plus.vplan.app.BuildConfig
import kotlin.random.Random

@Composable
fun BlurredBackground(seed: String = BuildConfig.APP_VERSION) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .blur(64.dp)
            .drawWithCache {
                onDrawBehind {
                    val random = Random(seed.hashCode())
                    val colors = listOf(
                        Color(0xFFFFC1CC),
                        Color(0xFFA7C7E7),
                        Color(0xFFB5EAD7),
                        Color(0xFFFFF5BA)
                    )
                    repeat(8) {
                        val color = colors[random.nextInt(colors.size)]
                        val radius = 200f + random.nextFloat() * 300f
                        val x = size.width * random.nextFloat()
                        val y = size.height * random.nextFloat()
                        drawIntoCanvas { canvas ->
                            val paint = Paint().apply {
                                this.color = color.copy(alpha = 0.7f)
                            }

                            canvas.drawCircle(Offset(x, y), radius, paint)
                        }
                    }
                }
            }
    )
}

@Preview
@Composable
private fun BlurredBackgroundPreview() {
    BlurredBackground("SEEDY")
}