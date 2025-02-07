package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.animation.AnimatedContent
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
    isHomework: Boolean,
    onClick: () -> Unit,
    dueTo: Instant
) {
    val date = dueTo.toLocalDateTime(TimeZone.currentSystemDefault()).date
    MetadataRow(
        key = {
            Text(
                text = if (isHomework) "Fällig" else "Datum",
                style = tableNameStyle()
            )
        },
        value = {
            MetadataValueContainer(
                canEdit = canEdit,
                onClick = onClick
            ) {
                AnimatedContent(targetState = date.format(regularDateFormat)) { displayDate ->
                    Text(
                        text = displayDate,
                        style = tableValueStyle(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    )
}