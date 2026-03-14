package plus.vplan.app.feature.grades.detail.ui.components


import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import plus.vplan.app.core.ui.components.MetadataRow
import plus.vplan.app.core.ui.components.MetadataValueContainer
import plus.vplan.app.core.ui.components.tableNameStyle
import plus.vplan.app.core.ui.components.tableValueStyle

@Composable
fun GivenByRow(
    teacherName: String,
) {
    MetadataRow(
        key = {
            Text(
                text = "Erteilt von",
                style = tableNameStyle()
            )
        },
        value = {
            MetadataValueContainer(
                canEdit = false,
                onClick = {}
            ) {
                Text(
                    text = teacherName,
                    style = tableValueStyle(),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    )
}