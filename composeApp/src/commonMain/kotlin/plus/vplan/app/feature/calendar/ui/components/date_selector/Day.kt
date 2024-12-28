package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate

@Composable
fun RowScope.Day(
    date: LocalDate,
    height: Dp
) {
    Box(
        modifier = Modifier
            .weight(1f)
            .height(height),
        contentAlignment = Alignment.Center
    ) {
        Text(text = date.dayOfMonth.toString())
    }
}