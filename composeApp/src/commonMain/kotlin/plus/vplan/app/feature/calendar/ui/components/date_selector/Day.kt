package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import plus.vplan.app.ui.grayScale

@Composable
fun RowScope.Day(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit = {},
    height: Dp,
    isOtherMonth: Boolean
) {
    AnimatedContent(
        targetState = isSelected,
        modifier = Modifier
            .weight(1f)
            .height(height),
    ) { showSelection ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { onClick() }
                .then(
                    if (showSelection) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                    else Modifier
                )
                .then(
                    if (isOtherMonth) Modifier.grayScale()
                    else Modifier
                ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = date.dayOfMonth.toString(),
                color =
                if (isOtherMonth) Color.Gray
                else if (showSelection) MaterialTheme.colorScheme.onPrimaryContainer
                else if (date.dayOfWeek in listOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}