package plus.vplan.app.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.painter.Painter

@Composable
expect fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    configuration: SheetConfiguration = SheetConfiguration(
        closeButtonAction = onDismissRequest
    ),
    content: @Composable (contentPadding: PaddingValues) -> Unit
)

@Immutable
data class SheetConfiguration(
    val showCloseButton: Boolean = false,
    val closeButtonAction: () -> Unit,
    val title: String? = null,
    val subtitle: String? = null,
    val actions: List<SheetActionItem> = emptyList(),
)

@Immutable
data class SheetActionItem(
    val onClick: () -> Unit,
    val icon: Icon,
    val isLoading: Boolean = false,
    val enabled: Boolean = true,
) {
    data class Icon(
        val painter: Painter,
        val sfName: String,
    )
}