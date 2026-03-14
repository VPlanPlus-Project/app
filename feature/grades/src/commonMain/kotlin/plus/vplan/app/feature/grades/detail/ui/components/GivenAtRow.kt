package plus.vplan.app.feature.grades.detail.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import plus.vplan.app.core.ui.components.MetadataRow
import plus.vplan.app.core.ui.components.MetadataValueContainer
import plus.vplan.app.core.ui.components.tableNameStyle
import plus.vplan.app.core.ui.components.tableValueStyle
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.date.regularDateFormat
import plus.vplan.app.core.utils.date.untilText

@Composable
fun GivenAtRow(
    date: LocalDate
) {
    MetadataRow(
        key = {
            Text(
                text = "Erteilt am",
                style = tableNameStyle()
            )
        },
        value = {
            MetadataValueContainer(
                canEdit = false,
                onClick = {}
            ) {
                Text(
                    text = buildString {
                        append(date.format(regularDateFormat))
                        append(", ")
                        append(LocalDate.now() untilText date)
                    },
                    style = tableValueStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    )
}