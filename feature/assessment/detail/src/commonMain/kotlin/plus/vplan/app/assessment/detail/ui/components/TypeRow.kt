package plus.vplan.app.assessment.detail.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.toName
import plus.vplan.app.core.ui.components.MetadataRow
import plus.vplan.app.core.ui.components.MetadataValueContainer
import plus.vplan.app.core.ui.components.tableNameStyle
import plus.vplan.app.core.ui.components.tableValueStyle

@Composable
fun TypeRow(
    canEdit: Boolean,
    onClick: () -> Unit,
    type: Assessment.Type
) {
    MetadataRow(
        key = {
            Text(
                text = "Kategorie",
                style = tableNameStyle()
            )
        },
        value = {
            MetadataValueContainer(
                canEdit = canEdit,
                onClick = onClick
            ) {
                AnimatedContent(targetState = type.toName()) { displayType ->
                    Text(
                        text = displayType,
                        style = tableValueStyle(),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1
                    )
                }
            }
        }
    )
}