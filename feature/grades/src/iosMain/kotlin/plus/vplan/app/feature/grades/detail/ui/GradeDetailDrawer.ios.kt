package plus.vplan.app.feature.grades.detail.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import org.koin.compose.koinInject

@Composable
actual fun GradeDetailDrawer(
    gradeId: Int,
    onDismiss: () -> Unit
) {
    val launcher = koinInject<GradeDetailDrawerLauncher>()

    DisposableEffect(gradeId) {
        launcher.launch(gradeId, onDismiss)

        onDispose {
            launcher.close()
        }
    }
}

interface GradeDetailDrawerLauncher {
    fun launch(gradeId: Int, onDismiss: () -> Unit)
    fun close()
}