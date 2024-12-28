package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import kotlinx.datetime.LocalDate
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

@Composable
fun Week(
    startDate: LocalDate,
    height: Dp
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(height),
    ) {
        repeat(7) {
            Day(startDate + it.days, height)
        }
    }
}