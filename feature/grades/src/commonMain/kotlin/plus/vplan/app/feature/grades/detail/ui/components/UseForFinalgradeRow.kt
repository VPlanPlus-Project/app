package plus.vplan.app.feature.grades.detail.ui.components

import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import plus.vplan.app.core.ui.components.MetadataRow
import plus.vplan.app.core.ui.components.MetadataValueContainer
import plus.vplan.app.core.ui.components.tableNameStyle

@Composable
fun UseForFinalGradeRow(
    useForFinalGrade: Boolean,
    wasNotGiven: Boolean,
    onToggle: () -> Unit
) {
    MetadataRow(
        key = {
            Text(
                text = "Im Durchschnitt berücksichtigen",
                style = tableNameStyle()
            )
        },
        value = {
            MetadataValueContainer(
                canEdit = !wasNotGiven,
                editStyling = false,
                onClick = onToggle
            ) {
                Switch(
                    checked = if (wasNotGiven) false else useForFinalGrade,
                    onCheckedChange = { onToggle() },
                    enabled = !wasNotGiven
                )
            }
        }
    )
}