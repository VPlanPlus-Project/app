package plus.vplan.app.feature.grades.ui.components.detail.components

import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import plus.vplan.app.feature.homework.ui.components.detail.components.MetadataRow
import plus.vplan.app.feature.homework.ui.components.detail.components.MetadataValueContainer
import plus.vplan.app.feature.homework.ui.components.detail.components.tableNameStyle

@Composable
fun UseForFinalGradeRow(
    useForFinalGrade: Boolean,
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
                canEdit = true,
                editStyling = false,
                onClick = onToggle
            ) {
                Switch(
                    checked = useForFinalGrade,
                    onCheckedChange = { onToggle() }
                )
            }
        }
    )
}