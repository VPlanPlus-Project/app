package plus.vplan.app.feature.grades.page.view.ui.components.subject

import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import kotlin.math.PI
import kotlin.math.sin

@Composable
@Preview
fun WavySeparator(
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.outlineVariant
) {
    Canvas(
        modifier = modifier
    ) {
        val width = size.width
        val height = size.height
        val amplitude = height / 2  // peak of the wave
        val frequency = width / 50  // number of cycles across the canvas

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
            style = Stroke(width = 1.dp.toPx())
        )
    }
}