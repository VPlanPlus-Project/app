@file:OptIn(ExperimentalMaterial3Api::class)

package plus.vplan.app.feature.grades.detail.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.model.application.UnoptimisticTaskState
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.ModalBottomSheet
import plus.vplan.app.core.ui.components.SheetActionItem
import plus.vplan.app.core.ui.components.SheetConfiguration
import plus.vplan.app.feature.grades.common.domain.model.GradeLockState

@Composable
actual fun GradeDetailDrawer(
    gradeId: Int,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<GradeDetailViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(gradeId) { viewModel.init(gradeId) }

    if (!state.initDone) return

    LaunchedEffect(true) {
        if (state.lockState == GradeLockState.Locked) viewModel.onEvent(
            GradeDetailEvent.RequestGradesUnlock
        )
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        configuration = SheetConfiguration(
            showCloseButton = true,
            closeButtonAction = onDismiss,
            title = state.title,
            subtitle = state.subtitle,
            actions = listOfNotNull(
                if (state.lockState == GradeLockState.Unlocked) SheetActionItem(
                    onClick = { viewModel.onEvent(GradeDetailEvent.LockGrades) },
                    icon = SheetActionItem.Icon(
                        painter = painterResource(CoreUiRes.drawable.lock),
                        sfName = "lock"
                    ),
                    enabled = true,
                    isLoading = false,
                ) else null,
                SheetActionItem(
                    onClick = { viewModel.onEvent(GradeDetailEvent.Reload) },
                    icon = when (state.reloadingState) {
                        UnoptimisticTaskState.Success -> SheetActionItem.Icon(
                            painter = painterResource(CoreUiRes.drawable.check),
                            sfName = "checkmark"
                        )
                        UnoptimisticTaskState.Error -> SheetActionItem.Icon(
                            painter = painterResource(CoreUiRes.drawable.info),
                            sfName = "info.circle"
                        )
                        else -> SheetActionItem.Icon(
                            painter = painterResource(CoreUiRes.drawable.rotate_cw),
                            sfName = "arrow.clockwise"
                        )
                    },
                    isLoading = state.reloadingState == UnoptimisticTaskState.InProgress,
                    enabled = state.reloadingState != UnoptimisticTaskState.InProgress,
                )
            )
        )
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding)
        ) {
            GradeDetailPage(state, viewModel::onEvent)
        }
    }
}