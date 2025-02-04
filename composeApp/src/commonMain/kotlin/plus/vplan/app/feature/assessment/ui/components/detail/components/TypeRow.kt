package plus.vplan.app.feature.assessment.ui.components.detail.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.feature.homework.ui.components.detail.components.MetadataRow
import plus.vplan.app.feature.homework.ui.components.detail.components.MetadataValueContainer
import plus.vplan.app.feature.homework.ui.components.detail.components.tableNameStyle
import plus.vplan.app.feature.homework.ui.components.detail.components.tableValueStyle
import plus.vplan.app.utils.toName

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
                Text(
                    text = type.toName(),
                    style = tableValueStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    )
}