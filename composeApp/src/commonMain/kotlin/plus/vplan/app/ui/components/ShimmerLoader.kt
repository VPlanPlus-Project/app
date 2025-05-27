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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import plus.vplan.app.utils.toDp

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

@Composable
fun LineShimmer() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(MaterialTheme.typography.bodyMedium.lineHeight.toDp()),
        contentAlignment = Alignment.CenterStart
    ) {
        ShimmerLoader(
            modifier = Modifier
                .fillMaxWidth()
                .height(MaterialTheme.typography.bodyMedium.fontSize.toDp())
                .clip(RoundedCornerShape(8.dp))
        )
    }
}