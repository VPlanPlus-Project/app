package plus.vplan.app.core.common.ui.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import plus.vplan.app.core.model.VppId
import plus.vplan.app.core.ui.components.MetadataRow
import plus.vplan.app.core.ui.components.tableNameStyle
import plus.vplan.app.core.ui.components.tableValueStyle

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