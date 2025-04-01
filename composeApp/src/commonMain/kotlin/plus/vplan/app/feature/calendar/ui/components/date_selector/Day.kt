package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import plus.vplan.app.ui.grayScale
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.toDp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RowScope.Day(
    date: LocalDate,
    selectedDate: LocalDate,
    onClick: () -> Unit = {},
    height: Dp,
    isOtherMonth: Boolean,
    scrollProgress: Float,
    homework: Int,
    assessments: Int,
) {
    val isWeekSelected = selectedDate.atStartOfWeek() == date.atStartOfWeek()
    AnimatedContent(
        targetState = selectedDate == date,
        modifier = Modifier
            .weight(1f)
            .height(height),
        transitionSpec = { fadeIn() togetherWith fadeOut() }
    ) { showSelection ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .thenIf(Modifier.grayScale()) { isOtherMonth }
                .thenIf(Modifier.border(1.dp, if (showSelection) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))) { selectedDate == date }
                .thenIf(Modifier.border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))) { date == LocalDate.now() }
                .clip(RoundedCornerShape(4.dp))
                .clickable { onClick() },
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = date.format(LocalDate.Format { dayOfWeek(shortDayOfWeekNames) }),
                style = MaterialTheme.typography.labelSmall,
                color = (if (showSelection) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline).copy(alpha = 1-(if (isWeekSelected) scrollProgress.coerceAtMost(1f) else 1f))
            )
            Text(
                text = date.dayOfMonth.toString(),
                color =
                if (isOtherMonth) Color.Gray
                else if (showSelection) MaterialTheme.colorScheme.tertiary
                else if (date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface,
                style = (if (showSelection) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall).copy(fontWeight = if (showSelection) FontWeight.Black else FontWeight.Bold),
            )
            FlowRow(
                modifier = Modifier.height(MaterialTheme.typography.labelSmall.lineHeight.toDp()),
                verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
            ) {
                repeat(assessments) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors[CustomColor.WineRed]!!.getGroup().color)
                    )
                }
                repeat(homework) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(colors[CustomColor.Cyan]!!.getGroup().color)
                    )
                }
            }
        }
    }
}