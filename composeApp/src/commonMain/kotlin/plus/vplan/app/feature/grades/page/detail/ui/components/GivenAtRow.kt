package plus.vplan.app.feature.grades.page.detail.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import plus.vplan.app.feature.homework.ui.components.detail.components.MetadataRow
import plus.vplan.app.feature.homework.ui.components.detail.components.MetadataValueContainer
import plus.vplan.app.feature.homework.ui.components.detail.components.tableNameStyle
import plus.vplan.app.feature.homework.ui.components.detail.components.tableValueStyle
import plus.vplan.app.utils.now
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.untilText

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