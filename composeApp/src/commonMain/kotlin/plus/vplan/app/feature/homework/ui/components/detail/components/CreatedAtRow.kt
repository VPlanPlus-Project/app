package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.core.ui.components.MetadataRow
import plus.vplan.app.core.ui.components.tableNameStyle
import plus.vplan.app.core.ui.components.tableValueStyle
import plus.vplan.app.core.utils.date.regularDateFormat
import plus.vplan.app.utils.regularTimeFormat
import kotlin.time.Instant

@Composable
fun CreatedAtRow(
    createdAt: Instant
) {
    val time = createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).let {
        buildString {
            append(it.date.format(regularDateFormat))
            append(" ")
            append(it.time.format(regularTimeFormat))
        }
    }
    MetadataRow(
        key = {
            Text(
                text = "Erstellt am",
                style = tableNameStyle()
            )
        },
        value = {
            Text(
                text = time,
                style = tableValueStyle()
            )
        }
    )
}