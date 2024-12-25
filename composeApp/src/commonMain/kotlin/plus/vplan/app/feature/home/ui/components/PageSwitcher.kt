package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.lerp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.char
import plus.vplan.app.utils.blendColor
import kotlin.math.max
import kotlin.math.roundToInt

@Composable
fun PagerSwitcher(
    modifier: Modifier = Modifier,
    swipeProgress: Float,
    nextDate: LocalDate,
    onSelectPage: (Int) -> Unit = {}
) {
    var width0 by remember { mutableIntStateOf(0) }
    var width1 by remember { mutableIntStateOf(0) }
    var height by remember { mutableIntStateOf(0) }
    Box(
        modifier = modifier
            .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Box(
            modifier = Modifier
                .height(LocalDensity.current.run { height.toDp() })
                .width(lerp(LocalDensity.current.run { width0.toDp() }, LocalDensity.current.run { width1.toDp() }, swipeProgress))
                .offset { IntOffset(lerp(0f, width0.toFloat(), swipeProgress).roundToInt(), 0) }
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.primary)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .onSizeChanged { width0 = it.width; height = max(it.height, height) }
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectPage(0) }
                    .padding(8.dp)
            ) {
                Text(
                    text = "Heute",
                    style = MaterialTheme.typography.labelLarge,
                    color = blendColor(MaterialTheme.colorScheme.onPrimary, MaterialTheme.colorScheme.onSecondaryContainer, swipeProgress)
                )
            }
            Box(
                modifier = Modifier
                    .onSizeChanged { width1 = it.width; height = max(it.height, height) }
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { onSelectPage(1) }
                    .padding(8.dp)
            ) {
                Text(
                    text = nextDate.format(LocalDate.Format {
                        dayOfMonth()
                        chars(". ")
                        monthName(MonthNames("Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"))
                        char(' ')
                        year()
                    }),
                    style = MaterialTheme.typography.labelLarge,
                    color = blendColor(MaterialTheme.colorScheme.onSecondaryContainer, MaterialTheme.colorScheme.onPrimary, swipeProgress)
                )
            }
        }
    }
}