package plus.vplan.app.feature.dev.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.feature.assessment.ui.components.create.NewAssessmentDrawer
import plus.vplan.app.feature.homework.ui.components.detail.HomeworkDetailDrawer

@Composable
fun DevScreen(
    contentPadding: PaddingValues,
    onToggleBottomBar: (visible: Boolean) -> Unit
) {
    val viewModel = koinViewModel<DevViewModel>()
    val state = viewModel.state

    var isDrawerOpen by rememberSaveable { mutableStateOf(false) }
    var clickAssessmentId by rememberSaveable { mutableStateOf<Int?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        Text(state.profile?.name.toString())
        Text(state.updateResponse.toString())
        Row {
            Button(
                onClick = { viewModel.onEvent(DevEvent.Refresh) }
            ) {
                Text("Refresh")
            }
            Button(
                onClick = { isDrawerOpen = true; onToggleBottomBar(false) }
            ) {
                Text("New Assessment")
            }
            Button(
                onClick = { viewModel.onEvent(DevEvent.Clear) }
            ) {
                Text("Clear Cache")
            }
        }
        state.assessments.forEach { assessment ->
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
                    .clickable { clickAssessmentId = assessment.id }
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("ID: ${assessment.id}")
                    Text(assessment.subjectInstanceItem!!.subject)
                    Text(assessment.date.toString())
                    when (assessment.creator) {
                        is AppEntity.VppId -> Text(assessment.createdByVppId!!.name)
                        is AppEntity.Profile -> Text(assessment.createdByProfile!!.groupItem!!.name)
                    }
                }
                Text(assessment.description)
            }
        }
    }
    if (isDrawerOpen) NewAssessmentDrawer(
        onDismissRequest = { isDrawerOpen = false; onToggleBottomBar(true) }
    )

    if (clickAssessmentId != null) HomeworkDetailDrawer(clickAssessmentId!!) { clickAssessmentId = null }
}