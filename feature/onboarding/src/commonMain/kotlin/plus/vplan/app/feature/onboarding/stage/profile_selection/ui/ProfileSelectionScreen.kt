package plus.vplan.app.feature.onboarding.stage.profile_selection.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.model.ProfileType
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.profile_selection.ui.components.FilterRow

@Composable
internal fun ProfileSelectionScreen(
    options: List<OnboardingProfile>,
    onProfileSelected: (OnboardingProfile) -> Unit,
) {
    val viewModel = koinViewModel<ProfileSelectionViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(options) { viewModel.init(options) }

    LaunchedEffect(state.profileSelectedForParent) {
        val selected = state.profileSelectedForParent ?: return@LaunchedEffect
        viewModel.onProfileForwardedToParent()
        onProfileSelected(selected)
    }

    ProfileListContent(
        state = state,
        onEvent = viewModel::onEvent,
    )
}

@Composable
private fun ProfileListContent(
    state: ProfileSelectionState,
    onEvent: (ProfileSelectionEvent) -> Unit,
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
            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(CoreUiRes.drawable.user_pen),
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
                onClick = { onEvent(ProfileSelectionEvent.SelectProfileType(it)) }
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .padding(bottom = 16.dp)
            ) {
                AnimatedVisibility(
                    visible = (state.filterProfileType ?: ProfileType.STUDENT) == ProfileType.STUDENT,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Text(
                            text = "Klassen",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.options.filterIsInstance<OnboardingProfile.StudentProfile>().forEach {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                        .clickable { onEvent(ProfileSelectionEvent.SelectProfile(it)) }
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

                AnimatedVisibility(
                    visible = (state.filterProfileType ?: ProfileType.TEACHER) == ProfileType.TEACHER,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        Text(
                            text = "Lehrkräfte",
                            style = MaterialTheme.typography.headlineSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            state.options.filterIsInstance<OnboardingProfile.TeacherProfile>().forEach {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .defaultMinSize(minHeight = 48.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceContainer)
                                        .clickable { onEvent(ProfileSelectionEvent.SelectProfile(it)) }
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
