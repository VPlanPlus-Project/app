package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.utils.regularDateFormat

@Composable
fun DueToRow(
    canEdit: Boolean,
    onClick: () -> Unit,
    dueTo: Instant
) {
    val date = dueTo.toLocalDateTime(TimeZone.currentSystemDefault()).date
    MetadataRow(
        key = {
            Text(
                text = "Fällig",
                style = tableNameStyle()
            )
        },
        value = {
            MetadataValueContainer(
                canEdit = canEdit,
                onClick = onClick
            ) {
                Text(
                    text = date.format(regularDateFormat),
                    style = tableValueStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    )
}