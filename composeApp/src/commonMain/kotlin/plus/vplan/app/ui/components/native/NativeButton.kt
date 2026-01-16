package plus.vplan.app.ui.components.native

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun NativeButton(
    modifier: Modifier,
    text: String,
    onClick: () -> Unit,
)