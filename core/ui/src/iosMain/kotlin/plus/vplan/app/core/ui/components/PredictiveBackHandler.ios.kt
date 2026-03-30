package plus.vplan.app.core.ui.components

import androidx.compose.runtime.Composable

@Composable
actual fun BackHandler(
    enabled: Boolean,
    onProgress: (progress: Float) -> Unit,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    onBack: () -> Unit
) {
}