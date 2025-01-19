package plus.vplan.app.ui.components

import androidx.compose.runtime.Composable

/**
 * A [BackHandler] that can be used to handle back button presses and swipe gestures.
 * @param enabled Whether the back handler should be enabled
 * @param onProgress Callback that is called when the user swipes
 * @param onStart Callback that is called when the swipe gesture starts
 * @param onEnd Callback that is called when the swipe gesture ends
 * @param onBack Callback that is called when the user presses the back button or the swipe gesture is finished
 */
@Composable
expect fun BackHandler(
    enabled: Boolean = true,
    onProgress: (progress: Float) -> Unit = { },
    onStart: () -> Unit = { },
    onEnd: () -> Unit = { },
    onBack: () -> Unit
)