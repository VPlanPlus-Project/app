package plus.vplan.app.feature.dev.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.schulverwalter.Collection
import plus.vplan.app.domain.model.schulverwalter.Grade
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.model.schulverwalter.Year
import plus.vplan.app.feature.assessment.ui.components.create.NewAssessmentDrawer
import plus.vplan.app.feature.assessment.ui.components.detail.AssessmentDetailDrawer

@OptIn(ExperimentalLayoutApi::class)
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
        FlowRow {
            Button(
                onClick = { viewModel.onEvent(DevEvent.Refresh) }
            ) {
                Text("Refresh")
            }
            Button(
                onClick = { viewModel.onEvent(DevEvent.Sync) }
            ) {
                Text("Sync")
            }
            Button(
                onClick = { isDrawerOpen = true; onToggleBottomBar(false) }
            ) {
                Text("New Assessment")
            }
            Button(
                onClick = { viewModel.onEvent(DevEvent.Notify) }
            ) {
                Text("NTF")
            }
            Button(
                onClick = { viewModel.onEvent(DevEvent.Clear) }
            ) {
                Text("Clear Cache")
            }
        }
        App.gradeSource.getAll().collectAsState(emptyList()).value.filterIsInstance<CacheState.Done<Grade>>().map { it.data }.forEach { grade ->
            Text("${grade.id}: ${grade.value}")
            (grade.collection.collectAsState(null).value as? CacheState.Done)?.data?.let { collection ->
                Text("  ${collection.id}: ${collection.name}")
                (collection.subject.collectAsState(null).value as? CacheState.Done)?.data?.let { subject ->
                    Text("  ${subject.id}: ${subject.name}")
                }
            }
            (grade.teacher.collectAsState(null).value.let { teacher ->
                when (teacher) {
                    is CacheState.Done -> teacher.data.let { Text("  ${it.id}: ${it.name}") }
                    else -> teacher?.let {
                        Text("  Lehrer: $it")
                    }
                }
            })
            HorizontalDivider()
        }

        App.collectionSource.getAll().collectAsState(emptyList()).value.filterIsInstance<CacheState.Done<Collection>>().map { it.data }.forEach { collection ->
            Text("${collection.id}: ${collection.name}")
            (collection.subject.collectAsState(null).value as? CacheState.Done)?.data?.let { subject ->
                Text("  ${subject.id}: ${subject.name}")
            }
            HorizontalDivider()
        }
        Spacer(Modifier.height(8.dp))
        App.yearSource.getAll().collectAsState(emptyList()).value.filterIsInstance<CacheState.Done<Year>>().map { it.data }.forEach { year ->
            Text("${year.id}: ${year.name}")
            year.intervals.collectAsState(emptyList()).value.filterIsInstance<CacheState.Done<Interval>>().map { it.data }.forEach { interval ->
                Text("  ${interval.id}: ${interval.name}")
                if (interval.includedIntervalId != null) {
                    (interval.includedInterval?.collectAsState(null)?.value as? CacheState.Done)?.data?.let { includedInterval ->
                        Text("  (includes ${includedInterval.id}: ${includedInterval.name})")
                    }
                }
            }
            HorizontalDivider()
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

    if (clickAssessmentId != null) AssessmentDetailDrawer(clickAssessmentId!!) { clickAssessmentId = null }
}