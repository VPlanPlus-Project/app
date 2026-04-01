package plus.vplan.app.feature.calendar.page.ui.components.day_details

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import plus.vplan.app.core.ui.util.roundToPx

@Composable
fun AnimatableBlock(
    scale: Float,
    visibleIndex: Int,
    index: Int,
    title: String,
    icon: Painter?,
    content: @Composable () -> Unit
) {
    val pixelMovement = -4.dp.roundToPx()
    val defaultEnterAnimation = fadeIn() + scaleIn(initialScale = .9f) + slideInVertically(tween(easing = FastOutSlowInEasing)) { pixelMovement }
    val defaultExitAnimation = fadeOut() + scaleOut(targetScale = .9f) + slideOutVertically(tween(easing = FastOutSlowInEasing)) { pixelMovement }

    AnimatedVisibility(
        visible = visibleIndex >= index,
        enter = defaultEnterAnimation,
        exit = defaultExitAnimation,
    ) {
        Column(
            modifier = Modifier
                .padding(top = 24.dp)
                .padding(horizontal = 12.dp)
                .scale(scale)
                .fillMaxWidth()
        ) {
            Title(
                modifier = Modifier.padding(horizontal = 4.dp),
                icon = icon,
                title = title,
            )
            content()
        }
    }
}