package plus.vplan.app.feature.grades.page.detail.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState
import plus.vplan.app.utils.safeBottomPadding

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDetailDrawer(
    gradeId: Int,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<GradeDetailViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(gradeId) { viewModel.init(gradeId) }

    if (!state.initDone) return

    LaunchedEffect(state.initDone) {
        if (state.initDone && state.lockState == GradeLockState.Locked) viewModel.onEvent(GradeDetailEvent.RequestGradesUnlock)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0.dp) },
        sheetState = sheetState
    ) {
        Column(Modifier.padding(bottom = safeBottomPadding())) {
            GradeDetailPage(state, viewModel::onEvent)
        }
    }
}