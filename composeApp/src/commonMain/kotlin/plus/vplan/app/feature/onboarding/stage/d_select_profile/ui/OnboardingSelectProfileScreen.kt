package plus.vplan.app.feature.onboarding.stage.d_select_profile.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.d_select_profile.domain.model.OnboardingProfile

@Composable
fun OnboardingSelectProfileScreen(
    navController: NavHostController
) {
    val viewModel = koinViewModel<OnboardingSelectProfileViewModel>()
    val state = viewModel.state

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
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
    ) {
        if (state.selectedProfile is OnboardingProfile.StudentProfile) {
            Button(
                onClick = { onEvent(OnboardingProfileSelectionEvent.CommitProfile) },
            ) {
                Text("Speichern")
            }
            state.defaultLessons.forEach { (defaultLesson, enabled) ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.clickable { onEvent(OnboardingProfileSelectionEvent.ToggleDefaultLesson(defaultLesson)) }
                ) {
                   Checkbox(
                       checked = enabled,
                       onCheckedChange = { onEvent(OnboardingProfileSelectionEvent.ToggleDefaultLesson(defaultLesson)) }
                   )
                    Column {
                        Text(text = "${defaultLesson.subject} ${defaultLesson.teacher?.name} (${defaultLesson.id})")
                        Text(text = "${defaultLesson.course?.id}")
                    }
                }
            }
        }
        Text("Group")
        state.options.filterIsInstance<OnboardingProfile.StudentProfile>().forEach {
            Text(
                text = "${it.name}, (${it.id})",
                modifier = Modifier.clickable { onEvent(OnboardingProfileSelectionEvent.SelectProfile(it)) }
            )
        }
        Text("Teacher")
        state.options.filterIsInstance<OnboardingProfile.TeacherProfile>().forEach {
            Text(
                text = "${it.name}, (${it.id})",
                modifier = Modifier.clickable { onEvent(OnboardingProfileSelectionEvent.SelectProfile(it)) }
            )
        }

        Text("Room")
        state.options.filterIsInstance<OnboardingProfile.RoomProfile>().forEach {
            Text(
                text = "${it.name}, (${it.id})",
                modifier = Modifier.clickable { onEvent(OnboardingProfileSelectionEvent.SelectProfile(it)) }
            )
        }
    }
}