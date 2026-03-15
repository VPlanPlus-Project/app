package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import plus.vplan.app.core.ui.components.MetadataRow
import plus.vplan.app.core.ui.components.tableNameStyle
import plus.vplan.app.core.ui.components.tableValueStyle

@Composable
fun SavedLocalRow() {
    MetadataRow(
        key = {
            Text(
                text = "Speicherort",
                style = tableNameStyle()
            )
        },
        value = {
            Text(
                text = "Dieses Gerät",
                style = tableValueStyle()
            )
        }
    )
}