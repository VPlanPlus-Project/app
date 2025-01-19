package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import plus.vplan.app.domain.model.VppId

@Composable
fun CreatedByRow(
    createdBy: VppId
) {
    MetadataRow(
        key = {
            Text(
                text = "Erstellt von",
                style = tableNameStyle()
            )
        },
        value = {
            Text(
                text = createdBy.name,
                style = tableValueStyle()
            )
        }
    )
}