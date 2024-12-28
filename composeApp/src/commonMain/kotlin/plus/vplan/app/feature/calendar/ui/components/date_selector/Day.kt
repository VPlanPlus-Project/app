package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate

@Composable
fun RowScope.Day(
    date: LocalDate,
    isSelected: Boolean,
    onClick: () -> Unit = {},
    height: Dp
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(height)
            .clickable { onClick() }
            .then(
                if (isSelected) Modifier.background(MaterialTheme.colorScheme.primaryContainer)
                else Modifier
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = date.dayOfMonth.toString(),
            color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
        )
    }
}