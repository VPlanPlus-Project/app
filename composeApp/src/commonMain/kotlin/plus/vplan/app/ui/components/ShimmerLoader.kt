package plus.vplan.app.ui.components

import androidx.compose.animation.core.EaseOutCirc
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun ShimmerLoader(
    modifier: Modifier = Modifier,
    infiniteTransition: InfiniteTransition = rememberInfiniteTransition()
) {
    val offset by infiniteTransition.animateFloat(
        initialValue = 0f, targetValue = 2f, animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 2000, easing = EaseOutCirc),
            repeatMode = RepeatMode.Restart
        )
    )

    Canvas(
        modifier = modifier
            .background(Color.LightGray.copy(alpha = .5f))
    ) {
        drawRect(
            brush = Brush.horizontalGradient(
                0f to Color.Transparent,
                1f to Color.DarkGray.copy(alpha = 1-(offset/2)),
            ),
            size = Size(width = offset.coerceAtMost(1f) * size.width, height = size.height),
            topLeft = Offset(0f, 0f),
        )
    }
}