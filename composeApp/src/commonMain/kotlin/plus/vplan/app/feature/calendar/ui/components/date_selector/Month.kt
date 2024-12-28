package plus.vplan.app.feature.calendar.ui.components.date_selector

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import plus.vplan.app.utils.plus
import kotlin.time.Duration.Companion.days

val weekHeight = 56.dp

@Composable
fun Month(
    startDate: LocalDate,
    keepWeek: LocalDate,
    scrollProgress: Float
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        repeat(6) {
            val date = startDate + (it*7).days
            Week(
                startDate = date,
                if (date == keepWeek) weekHeight else weekHeight * scrollProgress,
            )
        }
    }
}