package plus.vplan.app.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun BackHandler(
    onProgress: (progress: Float) -> Unit,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    onBack: () -> Unit
)