package plus.vplan.app.feature.calendar.page.ui.components

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plus.vplan.app.core.ui.theme.AppTheme

@Composable
fun BoxScope.Handle(
    isDragging: Boolean
) {
    val width by animateDpAsState(
        targetValue = if (isDragging) 48.dp else 32.dp,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )
    val height by animateDpAsState(
        targetValue = if (isDragging) 3.dp else 4.dp,
        animationSpec = spring(
            stiffness = Spring.StiffnessMedium,
            dampingRatio = Spring.DampingRatioMediumBouncy
        )
    )
    Box(
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 8.dp)
            .height(height)
            .width(width)
            .clip(RoundedCornerShape(50))
            .background(MaterialTheme.colorScheme.primary)
    )
}

@Composable
@Preview
private fun DraggingHandlePreview() {
    AppTheme(dynamicColor = false) {
        Box(Modifier.fillMaxWidth()) {
            Handle(isDragging = true)
        }
    }
}

@Composable
@Preview
private fun HandlePreview() {
    AppTheme(dynamicColor = false) {
        Box(Modifier.fillMaxWidth()) {
            Handle(isDragging = false)
        }
    }
}