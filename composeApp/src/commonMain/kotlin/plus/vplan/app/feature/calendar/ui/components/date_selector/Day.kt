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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import plus.vplan.app.feature.calendar.ui.DateSelectorDay
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.blendColor
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.toDp

val weekHeightDefault = 64.dp

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun RowScope.Day(
    date: LocalDate,
    selectedDate: LocalDate,
    onClick: () -> Unit = {},
    height: Dp,
    isOtherMonth: Boolean,
    scrollProgress: Float,
    homework: List<DateSelectorDay.HomeworkItem>,
    assessments: List<String>,
    isHoliday: Boolean
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
                .thenIf(Modifier.border(1.dp, if (showSelection) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline, RoundedCornerShape(4.dp))) { selectedDate == date }
                .thenIf(Modifier.border(1.dp, if (selectedDate.atStartOfWeek() == LocalDate.now().atStartOfWeek()) MaterialTheme.colorScheme.outline else blendColor(Color.Transparent, MaterialTheme.colorScheme.outline, ((2*scrollProgress-1).coerceAtMost(1f))), RoundedCornerShape(4.dp))) { date == LocalDate.now() }
                .clip(RoundedCornerShape(4.dp))
                .clickable { onClick() },
            verticalArrangement = Arrangement.Top,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            fun grayScaledIfRequired(color: Color) = if (isOtherMonth) blendColor(color, Color.Gray, scrollProgress.coerceAtMost(1f)) else color
            Text(
                text = date.format(LocalDate.Format { dayOfWeek(shortDayOfWeekNames) }),
                style = MaterialTheme.typography.labelSmall,
                color = grayScaledIfRequired(if (showSelection) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.outline).copy(alpha = 1-(if (isWeekSelected) scrollProgress.coerceAtMost(1f) else 1f))
            )
            Text(
                text = date.dayOfMonth.toString(),
                color = grayScaledIfRequired(
                    if (showSelection) MaterialTheme.colorScheme.tertiary
                    else if (date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY) || isHoliday) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
                ),
                style = (if (showSelection) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleSmall).copy(fontWeight = if (showSelection) FontWeight.Black else FontWeight.Bold),
            )
            Box(
                contentAlignment = Alignment.TopCenter
            ) {
                FlowRow(
                    modifier = Modifier
                        .height(MaterialTheme.typography.labelSmall.lineHeight.toDp())
                        .alpha((-2*scrollProgress + 3).coerceIn(0f, 1f)),
                    verticalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.spacedBy(2.dp, Alignment.CenterHorizontally)
                ) {
                    repeat(assessments.size) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(grayScaledIfRequired(colors[CustomColor.WineRed]!!.getGroup().color))
                        )
                    }
                    repeat(homework.size) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(grayScaledIfRequired(colors[CustomColor.Cyan]!!.getGroup().color))
                        )
                    }
                }
                FlowRow(
                    modifier = Modifier.alpha((2*scrollProgress - 3).coerceIn(0f, 1f)),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    horizontalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterHorizontally)
                ) {
                    repeat(assessments.size) {
                        Text(
                            text = assessments[it],
                            style = MaterialTheme.typography.labelSmall,
                            color = grayScaledIfRequired(colors[CustomColor.WineRed]!!.getGroup().color)
                        )
                    }
                    repeat(homework.size) {
                        Text(
                            text = homework[it].subject,
                            style = MaterialTheme.typography.labelSmall,
                            color = grayScaledIfRequired(colors[CustomColor.Cyan]!!.getGroup().color),
                            textDecoration = if (homework[it].isDone) TextDecoration.LineThrough else null
                        )
                    }
                }
            }
        }
    }
}