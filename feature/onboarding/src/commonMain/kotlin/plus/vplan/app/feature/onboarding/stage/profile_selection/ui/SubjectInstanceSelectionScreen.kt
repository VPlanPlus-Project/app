package plus.vplan.app.feature.onboarding.stage.profile_selection.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.Course
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.Button
import plus.vplan.app.core.ui.components.ButtonSize
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.core.ui.theme.displayFontFamily
import plus.vplan.app.core.ui.util.paddingvalues.copy
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.ui.components.OnboardingHeader
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Composable
internal fun SubjectInstanceSelectionScreen(
    studentProfile: OnboardingProfile.StudentProfile,
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
    )
}

@Composable
private fun SubjectInstanceSelectionContent(
    state: ProfileSelectionState,
    onEvent: (ProfileSelectionEvent) -> Unit,
) {
    val haptic = LocalHapticFeedback.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(WindowInsets.safeDrawing.asPaddingValues().copy(bottom = 0.dp))
            .padding(horizontal = 16.dp),
    ) {
        OnboardingHeader(
            title = "Passe deinen Stundenplan an",
            subtitle = "Deaktiviere Fächer und Kurse, die dich nicht betreffen. Du erhälst nur Benachrichtigungen zu Fächern und Kursen, die du aktiviert hast."
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp + WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding())
        ) {
            if (state.courses.isNotEmpty()) Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Text(
                    text = "Kurse",
                    style = MaterialTheme.typography.titleMedium,
                    fontFamily = displayFontFamily(),
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    state.courses.forEach { course ->
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
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onEvent(ProfileSelectionEvent.ToggleCourse(course))
                                }
                                .padding(end = 16.dp, start = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TriStateCheckbox(
                                state = if (isCourseFullySelected) ToggleableState.On
                                else if (isCoursePartiallySelected) ToggleableState.Indeterminate
                                else ToggleableState.Off,
                                onClick = {
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onEvent(ProfileSelectionEvent.ToggleCourse(course))
                                }
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

            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                    modifier = Modifier.clip(RoundedCornerShape(16.dp))
                ) {
                    Text(
                        text = "Fächer",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = displayFontFamily(),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    ) {
                        state.subjectInstances.entries
                            .sortedBy { "${it.key.subject}_${it.key.course?.name ?: ""}" }
                            .forEach { (subjectInstance, enabled) ->
                                Row(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                        .clickable {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onEvent(ProfileSelectionEvent.ToggleSubjectInstance(subjectInstance))
                                        }
                                        .padding(end = 16.dp, start = 4.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Checkbox(
                                        checked = enabled,
                                        onCheckedChange = {
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onEvent(ProfileSelectionEvent.ToggleSubjectInstance(subjectInstance))
                                        }
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
            }

            Button(
                text = "Speichern",
                state = state.saveState.toButtonState(),
                icon = CoreUiRes.drawable.arrow_right,
                modifier = Modifier.padding(bottom = 16.dp),
                size = ButtonSize.Big,
                onlyEventOnActive = true,
                onClick = {
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    onEvent(ProfileSelectionEvent.CommitProfile)
                }
            )
        }
    }
}

@Composable
@Preview
private fun SubjectInstanceSelectionPreview() {
    AppTheme(dynamicColor = false) {
        val now = Clock.System.now()
        val course = Course(
            id = Uuid.random(),
            groups = emptySet(),
            name = "m1",
            teacher = null,
            cachedAt = now,
            aliases = emptySet()
        )
        val subjectInstance1 = SubjectInstance(
            id = Uuid.random(),
            subject = "Mathematik",
            course = course,
            teacher = null,
            groups = emptyList(),
            cachedAt = now,
            aliases = setOf(Alias(AliasProvider.Sp24, "1", 1))
        )
        val subjectInstance2 = SubjectInstance(
            id = Uuid.random(),
            subject = "Deutsch",
            course = null,
            teacher = null,
            groups = emptyList(),
            cachedAt = now,
            aliases = setOf(Alias(AliasProvider.Sp24, "2", 1))
        )

        val profile = OnboardingProfile.StudentProfile(
            name = "10A",
            alias = Alias(AliasProvider.Sp24, "1", 1),
            isTrustedName = true,
            subjectInstances = listOf(subjectInstance1, subjectInstance2)
        )

        SubjectInstanceSelectionContent(
            state = ProfileSelectionState(
                selectedProfile = profile,
                subjectInstances = mapOf(
                    subjectInstance1 to true,
                    subjectInstance2 to false
                )
            ),
            onEvent = {}
        )
    }
}