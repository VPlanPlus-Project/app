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
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.koinInject
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.model.application.UnoptimisticTaskState
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.core.platform.AppPlatform
import plus.vplan.app.core.platform.PlatformRepository
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.ModalBottomSheet
import plus.vplan.app.core.ui.components.SheetActionItem
import plus.vplan.app.core.ui.components.SheetConfiguration
import plus.vplan.app.core.ui.modifier.thenIf
import plus.vplan.app.feature.grades.common.domain.model.GradeLockState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDetailDrawer(
    gradeId: Int,
    onDismiss: () -> Unit
) {
    val platformRepository = koinInject<PlatformRepository>()

    val viewModel = koinViewModel<GradeDetailViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(gradeId) { viewModel.init(gradeId) }

    if (!state.initDone) return

    LaunchedEffect(state.initDone) {
        if (state.initDone && state.lockState == GradeLockState.Locked) viewModel.onEvent(
            GradeDetailEvent.RequestGradesUnlock
        )
    }

    val title = buildString {
        val grade = state.grade ?: return@buildString
        val value = if (grade.isOptional) "(${grade.value})" else grade.value
        when (grade.collection.interval.type) {
            is BesteSchuleInterval.Type.Sek2 -> {
                if (grade.value == null) append("Note")
                else append("$value Notenpunkte")
            }

            else -> {
                append("Note")
                if (grade.value != null) append(" $value")
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        configuration = SheetConfiguration(
            showCloseButton = true,
            closeButtonAction = onDismiss,
            title = title,
            subtitle = state.grade?.collection?.subject?.fullName,
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
                .thenIf(Modifier.padding(top = 16.dp)) { platformRepository.getPlatform() == AppPlatform.Android }
        ) {
            GradeDetailPage(state, viewModel::onEvent)
        }
    }
}