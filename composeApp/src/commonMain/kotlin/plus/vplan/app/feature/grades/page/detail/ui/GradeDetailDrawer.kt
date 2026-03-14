package plus.vplan.app.feature.grades.page.detail.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.core.ui.components.ModalBottomSheet
import plus.vplan.app.core.ui.components.SheetConfiguration
import plus.vplan.app.core.ui.util.paddingvalues.plus
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GradeDetailDrawer(
    gradeId: Int,
    onDismiss: () -> Unit
) {
    val viewModel = koinViewModel<GradeDetailViewModel>()
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(gradeId) { viewModel.init(gradeId) }

    if (!state.initDone) return

    LaunchedEffect(state.initDone) {
        if (state.initDone && state.lockState == GradeLockState.Locked) viewModel.onEvent(GradeDetailEvent.RequestGradesUnlock)
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
            subtitle = state.grade?.collection?.subject?.fullName
        )
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(contentPadding + PaddingValues(top = 16.dp, bottom = 16.dp))
        ) {
            GradeDetailPage(state, viewModel::onEvent)
        }
    }
}