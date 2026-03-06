package plus.vplan.app.feature.onboarding.stage.profile_selection.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.Button
import plus.vplan.app.core.ui.components.ButtonSize
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.profile_selection.ui.components.SubjectInstanceTitle

@Composable
internal fun SubjectInstanceSelectionScreen(
    studentProfile: OnboardingProfile.StudentProfile,
    onBack: () -> Unit,
    onDone: () -> Unit,
) {
    val viewModel = koinViewModel<ProfileSelectionViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(studentProfile) { viewModel.initSubjectInstances(studentProfile) }

    LaunchedEffect(state.saveState) {
        if (state.saveState == ProfileSelectionSaveState.DONE) onDone()
    }

    SubjectInstanceSelectionContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onBack,
    )
}

@Composable
private fun SubjectInstanceSelectionContent(
    state: ProfileSelectionState,
    onEvent: (ProfileSelectionEvent) -> Unit,
    onBack: () -> Unit,
) {
    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.surface)
        ) {
            SubjectInstanceTitle(onChangeProfile = onBack)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val courses = (state.selectedProfile as? OnboardingProfile.StudentProfile)
                    ?.subjectInstances.orEmpty()
                    .mapNotNull { it.course }
                    .toSet()
                    .sortedBy { it.name }

                if (courses.isNotEmpty()) {
                    Column {
                        Text(
                            text = "Kurse",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            courses.forEach { course ->
                                val isCourseFullySelected = state.subjectInstances
                                    .filterKeys { it.course != null && it.course!!.id == course.id }
                                    .values.all { it }
                                val isCoursePartiallySelected = state.subjectInstances
                                    .filterKeys { it.course != null && it.course!!.id == course.id }
                                    .values.any { it }
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                        .clickable { onEvent(ProfileSelectionEvent.ToggleCourse(course)) }
                                        .padding(vertical = 8.dp)
                                        .padding(start = 8.dp, end = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    TriStateCheckbox(
                                        state = if (isCourseFullySelected) ToggleableState.On
                                        else if (isCoursePartiallySelected) ToggleableState.Indeterminate
                                        else ToggleableState.Off,
                                        onClick = { onEvent(ProfileSelectionEvent.ToggleCourse(course)) }
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = course.name,
                                            style = MaterialTheme.typography.titleSmall,
                                            color = MaterialTheme.colorScheme.onSurface,
                                        )
                                        if (course.teacher != null) Text(
                                            text = course.teacher!!.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }

                Column {
                    Text(
                        text = "Fächer",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        state.subjectInstances.entries
                            .sortedBy { "${it.key.subject}_${it.key.course?.name ?: ""}" }
                            .forEach { (subjectInstance, enabled) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                        .clickable { onEvent(ProfileSelectionEvent.ToggleSubjectInstance(subjectInstance)) }
                                        .padding(vertical = 8.dp)
                                        .padding(start = 8.dp, end = 16.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = enabled,
                                        onCheckedChange = { onEvent(ProfileSelectionEvent.ToggleSubjectInstance(subjectInstance)) }
                                    )
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column {
                                            Text(
                                                text = subjectInstance.subject,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                            if (subjectInstance.course != null) Text(
                                                text = subjectInstance.course!!.name,
                                                style = MaterialTheme.typography.bodySmall,
                                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                            )
                                        }
                                        if (subjectInstance.teacher != null) Text(
                                            text = subjectInstance.teacher!!.name,
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        )
                                    }
                                }
                            }
                    }
                }

                Button(
                    text = "Speichern",
                    state = state.saveState.toButtonState(),
                    icon = CoreUiRes.drawable.arrow_right,
                    size = ButtonSize.Big,
                    onlyEventOnActive = true,
                    onClick = { onEvent(ProfileSelectionEvent.CommitProfile) }
                )
            }
        }
    }
}
