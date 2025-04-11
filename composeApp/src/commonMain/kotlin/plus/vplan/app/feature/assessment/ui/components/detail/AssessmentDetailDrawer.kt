package plus.vplan.app.feature.assessment.ui.components.detail

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AssessmentDetailDrawer(
    assessmentId: Int,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<AssessmentDetailViewModel>()
    val state = viewModel.state
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(assessmentId) {
        viewModel.init(assessmentId)
    }

    LaunchedEffect(state.deleteState) {
        if (state.deleteState == UnoptimisticTaskState.Success) {
            sheetState.hide()
            onDismiss()
        }
    }

    if (!state.initDone) return
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0.dp) },
        sheetState = sheetState
    ) {
        DetailPage(state, viewModel::onEvent)
    }
}