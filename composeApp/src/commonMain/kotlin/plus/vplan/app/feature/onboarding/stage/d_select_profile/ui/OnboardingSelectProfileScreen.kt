package plus.vplan.app.feature.onboarding.stage.d_select_profile.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.collectAsLoadingState
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.model.ProfileType
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.d_select_profile.ui.components.FilterRow
import plus.vplan.app.feature.onboarding.stage.d_select_profile.ui.components.SubjectInstanceTitle
import plus.vplan.app.feature.onboarding.ui.OnboardingScreen
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_right
import vplanplus.composeapp.generated.resources.user_pen

@Composable
fun OnboardingSelectProfileScreen(
    navController: NavHostController
) {
    val viewModel = koinViewModel<OnboardingSelectProfileViewModel>()
    val state = viewModel.state

    LaunchedEffect(state.saveState) {
        if (state.saveState == OnboardingProfileSelectionSaveState.DONE) {
            navController.navigate(OnboardingScreen.OnboardingPermission)
        }
    }

    OnboardingSelectProfileScreen(
        state = state,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun OnboardingSelectProfileScreen(
    state: OnboardingSelectProfileUiState,
    onEvent: (OnboardingProfileSelectionEvent) -> Unit
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
        ) {
            AnimatedContent(
                targetState = state.selectedProfile is OnboardingProfile.StudentProfile,
                transitionSpec = {
                    if (state.selectedProfile == null) slideInHorizontally { -it / 4 } + fadeIn(tween()) togetherWith slideOutHorizontally { it / 4 } + fadeOut()
                    else slideInHorizontally { it / 4 } + fadeIn(tween()) togetherWith slideOutHorizontally { -it / 4 } + fadeOut()
                }
            ) { showSubjectInstanceSelection ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    if (showSubjectInstanceSelection) {
                        SubjectInstanceTitle { onEvent(OnboardingProfileSelectionEvent.SelectProfile(null)) }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                                .padding(bottom = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) listHost@{
                            @Suppress("ConvertCallChainIntoSequence") // collectAsResultingFlowOld is a @Composable that cannot be invoked in a sequence
                            val courses = state.subjectInstances.keys.mapNotNull { it.course }.toSet()
                                .map { App.courseSource.getById(it).collectAsResultingFlow() }
                                .mapNotNull { it.value }
                                .sortedBy { it.name }

                            if (courses.isNotEmpty()) {
                                Column {
                                    Text(
                                        text = "Kurse",
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                    Column(
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        courses.forEach { course ->
                                            val isCourseFullySelected = state.subjectInstances.filterKeys { it.course == course.id }.values.all { it }
                                            val isCoursePartiallySelected = state.subjectInstances.filterKeys { it.course == course.id }.values.any { it }
                                            Row (
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .defaultMinSize(minHeight = 48.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                                    .clickable { onEvent(OnboardingProfileSelectionEvent.ToggleCourse(course)) }
                                                    .padding(vertical = 8.dp)
                                                    .padding(start = 8.dp, end = 16.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                TriStateCheckbox(
                                                    state = if (isCourseFullySelected) ToggleableState.On else if (isCoursePartiallySelected) ToggleableState.Indeterminate else ToggleableState.Off,
                                                    onClick = { onEvent(OnboardingProfileSelectionEvent.ToggleCourse(course)) }
                                                )
                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) detailsRow@{
                                                    Text(
                                                        text = course.name,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                    )
                                                    if (course.teacherId == null) return@detailsRow
                                                    val teacherState by App.teacherSource.getById(course.teacherId).collectAsLoadingState(course.teacherId.toString())
                                                    if (teacherState !is CacheState.Done) return@detailsRow
                                                    Text(
                                                        text = (teacherState as? CacheState.Done)?.data?.name ?: "-",
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
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.subjectInstances
                                        .entries
                                        .sortedBy { "${it.key.subject}_${it.key.course ?: ""}" }
                                        .forEach { (subjectInstance, enabled) ->
                                        Row (
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                                .clickable { onEvent(OnboardingProfileSelectionEvent.ToggleSubjectInstance(subjectInstance)) }
                                                .padding(vertical = 8.dp)
                                                .padding(start = 8.dp, end = 16.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            Checkbox(
                                                checked = enabled,
                                                onCheckedChange = { onEvent(OnboardingProfileSelectionEvent.ToggleSubjectInstance(subjectInstance)) }
                                            )
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) dataRow@{
                                                Column subjectAndCourse@{
                                                    Text(
                                                        text = subjectInstance.subject,
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = MaterialTheme.colorScheme.onSurface,
                                                    )
                                                    if (subjectInstance.course == null) return@subjectAndCourse
                                                    val subjectInstanceState by App.courseSource.getById(subjectInstance.course).collectAsLoadingState(subjectInstance.course.toString())
                                                    if (subjectInstanceState is CacheState.Done) Text(
                                                        text = (subjectInstanceState as CacheState.Done).data.name,
                                                        style = MaterialTheme.typography.bodySmall,
                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    )
                                                }
                                                if (subjectInstance.teacher == null) return@dataRow
                                                val teacherState by App.teacherSource.getById(subjectInstance.teacher).collectAsLoadingState(subjectInstance.teacher.toString())
                                                if (teacherState is CacheState.Done) Text(
                                                    text = (teacherState as CacheState.Done).data.name,
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
                                icon = Res.drawable.arrow_right,
                                size = ButtonSize.Big,
                                onlyEventOnActive = true,
                                onClick = { onEvent(OnboardingProfileSelectionEvent.CommitProfile) }
                            )
                        }
                        return@AnimatedContent
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.user_pen),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Text(
                            text = "Lege ein Profil an",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                    }
                    Text(
                        text = "Wähle eine Option aus",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    FilterRow(
                        currentSelection = state.filterProfileType,
                        onClick = { onEvent(OnboardingProfileSelectionEvent.SelectProfileType(it)) }
                    )
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .verticalScroll(rememberScrollState())
                            .padding(bottom = 16.dp)
                    ) {
                        androidx.compose.animation.AnimatedVisibility(
                            visible = (state.filterProfileType ?: ProfileType.STUDENT) == ProfileType.STUDENT,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) classes@{
                            Column {
                                Text(
                                    text = "Klassen",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.options.filterIsInstance<OnboardingProfile.StudentProfile>().forEach {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                                .clickable { onEvent(OnboardingProfileSelectionEvent.SelectProfile(it)) }
                                                .padding(vertical = 8.dp, horizontal = 16.dp),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = it.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                }
                            }
                        }

                        androidx.compose.animation.AnimatedVisibility(
                            visible = (state.filterProfileType ?: ProfileType.TEACHER) == ProfileType.TEACHER,
                            enter = expandVertically(),
                            exit = shrinkVertically()
                        ) teachers@{
                            Column {
                                Text(
                                    text = "Lehrkräfte",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Column(
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    state.options.filterIsInstance<OnboardingProfile.TeacherProfile>().forEach {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .defaultMinSize(minHeight = 48.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.surfaceContainer)
                                                .clickable { onEvent(OnboardingProfileSelectionEvent.SelectProfile(it)) }
                                                .padding(vertical = 8.dp, horizontal = 16.dp),
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            Text(
                                                text = it.name,
                                                style = MaterialTheme.typography.titleSmall,
                                                color = MaterialTheme.colorScheme.onSurface,
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}