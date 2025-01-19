package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

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