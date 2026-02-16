package plus.vplan.app.feature.homework.ui.components.detail

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkDetailDrawer(
    homeworkId: Int,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<HomeworkDetailViewModel>()
    val state = viewModel.state.collectAsStateWithLifecycle().value
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(homeworkId) {
        viewModel.init(homeworkId)
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