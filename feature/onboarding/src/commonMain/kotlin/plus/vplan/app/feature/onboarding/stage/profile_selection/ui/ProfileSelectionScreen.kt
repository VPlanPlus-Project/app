package plus.vplan.app.feature.onboarding.stage.profile_selection.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.model.Alias
import plus.vplan.app.core.model.AliasProvider
import plus.vplan.app.core.model.ProfileType
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.InfoCard
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.core.ui.theme.ColorToken
import plus.vplan.app.core.ui.theme.customColors
import plus.vplan.app.core.ui.theme.displayFontFamily
import plus.vplan.app.core.ui.util.paddingvalues.copy
import plus.vplan.app.feature.onboarding.domain.model.OnboardingProfile
import plus.vplan.app.feature.onboarding.stage.profile_selection.ui.components.FilterRow
import plus.vplan.app.feature.onboarding.stage.profile_selection.ui.components.SomeOptionsHidden
import plus.vplan.app.feature.onboarding.ui.components.OnboardingHeader

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
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(WindowInsets.safeDrawing.asPaddingValues().copy(bottom = 0.dp))
            .padding(horizontal = 16.dp),
    ) {
        OnboardingHeader(
            title = "Wähle ein Profil",
            subtitle = "Damit werden der Stunden- und Vertretungsplan, sowie weitere Communityfunktionen auf dich zugeschnitten."
        )

        if (state.hasTeacherProfiles && state.hasTeacherProfiles) FilterRow(
            modifier = Modifier.padding(top = 16.dp, bottom = 8.dp),
            currentSelection = state.filterProfileType,
            onClick = { onEvent(ProfileSelectionEvent.SelectProfileType(it)) }
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(bottom = 16.dp + WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding())
        ) {
            AnimatedVisibility(
                visible = state.hasStudentProfiles && (state.filterProfileType == null || state.filterProfileType == ProfileType.STUDENT),
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top),
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Klassen",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = displayFontFamily(),
                        color = MaterialTheme.colorScheme.onSurface,
                    )

                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    ) {
                        state.resultingVisibleStudentOptions.forEach {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(4.dp))
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

                    if (state.hasUntrustedStudentProfiles)
                        SomeOptionsHidden(Modifier.padding(top = 8.dp))
                }
            }

            AnimatedVisibility(
                visible = state.hasTeacherProfiles && (state.filterProfileType == null || state.filterProfileType == ProfileType.TEACHER),
                enter = expandVertically(expandFrom = Alignment.Top),
                exit = shrinkVertically(shrinkTowards = Alignment.Top)
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(bottom = 16.dp)
                ) {
                    Text(
                        text = "Lehrkräfte",
                        style = MaterialTheme.typography.titleMedium,
                        fontFamily = displayFontFamily(),
                        color = MaterialTheme.colorScheme.onSurface,
                    )
                    Column(
                        verticalArrangement = Arrangement.spacedBy(2.dp),
                        modifier = Modifier.clip(RoundedCornerShape(16.dp))
                    ) {
                        state.resultingVisibleTeacherOptions.forEach {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .defaultMinSize(minHeight = 48.dp)
                                    .clip(RoundedCornerShape(4.dp))
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
                    if (state.hasUntrustedTeacherProfiles)
                        SomeOptionsHidden(Modifier.padding(top = 8.dp))
                }
            }

            AnimatedVisibility(
                visible = state.hasUntrustedProfiles && !state.showUntrustedProfiles,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Column(
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    HorizontalDivider()
                    InfoCard(
                        imageVector = CoreUiRes.drawable.message_circle_warning,
                        title = "Ausgeblendete Profile",
                        text = "Einige Optionen scheinen durch unsaubere Stundenplan24.de-Daten erstellt zu sein. Diese Optionen bilden normalerweise keine echte Klasse oder Lehrkraft ab.",
                        textColor = customColors[ColorToken.OnYellowContainer]!!.get(),
                        backgroundColor = customColors[ColorToken.YellowContainer]!!.get(),
                        modifier = Modifier.padding(bottom = 16.dp),
                        buttonText1 = "Einblenden",
                        buttonAction1 = { onEvent(ProfileSelectionEvent.ShowUntrustedProfiles) },
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun ProfileListContentPreview() {
    AppTheme(dynamicColor = false) {
        ProfileListContent(
            state = ProfileSelectionState(
                options = listOf(
                    OnboardingProfile.StudentProfile(
                        name = "10A",
                        alias = Alias(AliasProvider.Sp24, "1", 1),
                        subjectInstances = emptyList(),
                        isTrustedName = true,
                    ),
                    OnboardingProfile.StudentProfile(
                        name = "11B",
                        alias = Alias(AliasProvider.Sp24, "2", 1),
                        subjectInstances = emptyList(),
                        isTrustedName = true,
                    ),
                    OnboardingProfile.StudentProfile(
                        name = "11B/3",
                        alias = Alias(AliasProvider.Sp24, "2", 1),
                        subjectInstances = emptyList(),
                        isTrustedName = false,
                    ),
                    OnboardingProfile.TeacherProfile(
                        name = "Hr. Müller",
                        alias = Alias(AliasProvider.Sp24, "3", 1),
                        isTrustedName = true,
                    ),
                    OnboardingProfile.TeacherProfile(
                        name = "Fr. Schmidt",
                        alias = Alias(AliasProvider.Sp24, "4", 1),
                        isTrustedName = true,
                    )
                )
            ),
            onEvent = {}
        )
    }
}
