package plus.vplan.app.core.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.sin

@Composable
@Preview
fun WavySeparator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant,
    amplitude: Float = 2f,
    frequency: Float = 50f,
    strokeWidth: Dp = 1.dp
) {
    Canvas(
        modifier = modifier
    ) {
        val width = size.width
        val height = size.height
        val amplitude = height / amplitude  // peak of the wave
        val frequency = width / frequency // number of cycles across the canvas

        val path = Path().apply {
            moveTo(0f, height / 2)  // start at middle-left
            for (x in 0..width.toInt()) {
                val y = (height / 2) + amplitude * -sin(2*PI + 2 * PI * frequency * x / width).toFloat()
                lineTo(x.toFloat(), y)
            }
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(width = strokeWidth.toPx())
        )
    }
}