package plus.vplan.app.ui.components

import androidx.activity.BackEventCompat
import androidx.activity.compose.PredictiveBackHandler
import androidx.compose.runtime.Composable
import kotlin.coroutines.cancellation.CancellationException

@Composable
actual fun BackHandler(
    enabled: Boolean,
    onProgress: (progress: Float) -> Unit,
    onStart: () -> Unit,
    onEnd: () -> Unit,
    onBack: () -> Unit
) {
    PredictiveBackHandler(enabled) { progress ->
        onStart()
        try {
            progress.collect { progressData ->
                onProgress(progressData.progress.let {
                    when (progressData.swipeEdge) {
                        BackEventCompat.EDGE_LEFT -> it
                        BackEventCompat.EDGE_RIGHT -> it * -1
                        else -> it
                    }
                })
            }
            onEnd()
            onBack()
        } catch (e: CancellationException) {
            onProgress(0f)
            onEnd()
        }
    }
}