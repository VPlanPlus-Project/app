package plus.vplan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import plus.vplan.app.utils.blendColor
import kotlin.math.absoluteValue

@Composable
fun <T> Switcher(
    modifier: Modifier = Modifier,
    items: List<T>,
    currentPage: Int,
    currentPageOffsetFraction: Float,
    onSelect: (item: T, index: Int) -> Unit = { _, _ -> },
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    block: @Composable (item: T, index: Int, contentColor: Color) -> Unit,
) {
    val localDensity = LocalDensity.current

    var selectorWidth by remember { mutableStateOf(0.dp) }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(40.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(backgroundColor)
            .border(
                1.dp,
                color = MaterialTheme.colorScheme.outlineVariant,
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 2.dp, vertical = 4.dp)
            .onSizeChanged { with(localDensity) { selectorWidth = it.width.toDp() } }
    ) {
        if (items.isEmpty()) return@Box
        val slotWidth = selectorWidth / items.size
        val selectorOffset = slotWidth * (currentPage + currentPageOffsetFraction)
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .width(slotWidth - 4.dp)
                .offset(x = selectorOffset + 2.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(MaterialTheme.colorScheme.primary),
            contentAlignment = Alignment.Center
        ) {
        }
        items.forEachIndexed { index, item ->
            val tabStart = slotWidth * index
            val progress = 1f - ((selectorOffset - tabStart) / slotWidth).absoluteValue
            val clampedProgress = progress.coerceIn(0f, 1f)

            val textColor = blendColor(
                MaterialTheme.colorScheme.onSurface,
                MaterialTheme.colorScheme.onPrimary,
                clampedProgress
            )

            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .width(slotWidth - 4.dp)
                    .offset(x = tabStart + 2.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .clickable { onSelect(item, index) },
                contentAlignment = Alignment.Center
            ) {
                CompositionLocalProvider(LocalContentColor provides textColor) {
                    block(item, index, textColor)
                }
            }
        }
    }
}