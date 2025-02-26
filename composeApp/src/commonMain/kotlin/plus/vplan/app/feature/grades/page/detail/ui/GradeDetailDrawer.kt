package plus.vplan.app.feature.grades.page.detail.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDetailDrawer(
    gradeId: Int,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<GradeDetailViewModel>()
    val state = viewModel.state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(gradeId) { viewModel.init(gradeId) }

    if (!state.initDone) return

    LaunchedEffect(state.initDone) {
        if (state.initDone && state.lockState == GradeLockState.Locked) viewModel.onEvent(GradeDetailEvent.RequestGradesUnlock)
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        GradeDetailPage(state, viewModel::onEvent)
    }
}