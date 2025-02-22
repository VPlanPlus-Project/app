package plus.vplan.app.feature.grades.ui.components.detail.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import plus.vplan.app.feature.homework.ui.components.detail.components.MetadataRow
import plus.vplan.app.feature.homework.ui.components.detail.components.MetadataValueContainer
import plus.vplan.app.feature.homework.ui.components.detail.components.tableNameStyle
import plus.vplan.app.feature.homework.ui.components.detail.components.tableValueStyle

@Composable
fun OptionalRow(
    isOptional: Boolean
) {
    MetadataRow(
        key = {
            Text(
                text = "Optional",
                style = tableNameStyle()
            )
        },
        value = {
            MetadataValueContainer(
                canEdit = false,
                onClick = {}
            ) {
                Text(
                    text = if (isOptional) "Ja" else "Nein",
                    style = tableValueStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    )
}