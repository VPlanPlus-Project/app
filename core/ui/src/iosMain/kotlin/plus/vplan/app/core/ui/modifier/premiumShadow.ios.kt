package plus.vplan.app.core.ui.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.unit.Dp
import org.jetbrains.skia.FilterTileMode
import org.jetbrains.skia.ImageFilter

actual fun Modifier.premiumShadow(
    color: Color,
    blurRadius: Dp,
    offsetY: Dp,
    borderRadius: Dp
): Modifier = this.drawBehind {
    drawIntoCanvas { canvas ->
        val paint = Paint()
        val skiaPaint = paint.asFrameworkPaint()

        skiaPaint.imageFilter = ImageFilter.makeBlur(
            sigmaX = blurRadius.toPx() / 2,
            sigmaY = blurRadius.toPx() / 2,
            mode = FilterTileMode.DECAL
        )

        paint.color = color

        canvas.drawRoundRect(
            left = 0f,
            top = offsetY.toPx(),
            right = size.width,
            bottom = size.height + offsetY.toPx(),
            radiusX = borderRadius.toPx(),
            radiusY = borderRadius.toPx(),
            paint = paint
        )
    }
}