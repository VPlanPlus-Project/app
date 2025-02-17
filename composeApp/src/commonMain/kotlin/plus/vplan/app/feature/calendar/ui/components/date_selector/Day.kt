package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import plus.vplan.app.ui.grayScale
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.toDp

@Composable
fun RowScope.Day(
    date: LocalDate,
    selectedDate: LocalDate,
    onClick: () -> Unit = {},
    height: Dp,
    isOtherMonth: Boolean,
    scrollProgress: Float
) {
    val isWeekSelected = selectedDate.atStartOfWeek() == date.atStartOfWeek()
    AnimatedContent(
        targetState = selectedDate == date,
        modifier = Modifier
            .weight(1f)
            .height(height),
    ) { showSelection ->
        Column (
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .thenIf(Modifier.grayScale()) { isOtherMonth },
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
            Row(
                modifier = Modifier.height(MaterialTheme.typography.labelSmall.lineHeight.toDp()),
            ) {

            }
        }
    }
}