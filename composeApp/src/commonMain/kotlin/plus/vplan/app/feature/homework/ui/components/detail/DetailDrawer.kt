package plus.vplan.app.feature.homework.ui.components.detail

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import org.koin.compose.viewmodel.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeworkDetailDrawer(
    homeworkId: Int,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<DetailViewModel>()
    val state = viewModel.state

    LaunchedEffect(homeworkId) {
        viewModel.init(homeworkId)
    }

    if (!state.initDone) return
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        DetailPage(state, viewModel::onEvent)
    }
}