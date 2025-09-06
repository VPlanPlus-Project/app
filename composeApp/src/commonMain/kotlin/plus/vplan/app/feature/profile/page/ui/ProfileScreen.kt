package plus.vplan.app.feature.profile.page.ui

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.VPP_ID_AUTH_URL
import plus.vplan.app.domain.cache.collectAsResultingFlowOld
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.feature.main.ui.MainScreen
import plus.vplan.app.feature.profile.page.ui.components.GradesCard
import plus.vplan.app.feature.profile.page.ui.components.GradesCardFeaturedGrade
import plus.vplan.app.feature.profile.page.ui.components.ProfileTitle
import plus.vplan.app.utils.openUrl
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.log_in
import vplanplus.composeapp.generated.resources.settings
import vplanplus.composeapp.generated.resources.undraw_profile

@Composable
fun ProfileScreen(
    navHostController: NavHostController,
    contentPadding: PaddingValues,
    viewModel: ProfileViewModel
) {
    val state = viewModel.state

    ProfileContent(
        state = state,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent,
        onOpenSettings = remember { { navHostController.navigate(MainScreen.Settings) } },
        onOpenGrades = remember(state.currentProfile?.id) { { navHostController.navigate(MainScreen.Grades((state.currentProfile as Profile.StudentProfile).vppIdId!!)) } }
    )
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    contentPadding: PaddingValues,
    onEvent: (event: ProfileScreenEvent) -> Unit,
    onOpenSettings: () -> Unit,
    onOpenGrades: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition()

    var openGradesScreenAfterUnlock by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.areGradesLocked) {
        if (!state.areGradesLocked && openGradesScreenAfterUnlock) {
            openGradesScreenAfterUnlock = false
            onOpenGrades()
        }
    }

    LaunchedEffect(openGradesScreenAfterUnlock) {
        if (openGradesScreenAfterUnlock) {
            delay(10000)
            openGradesScreenAfterUnlock = false
        }
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
    ) {
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ProfileTitle(state.currentProfile?.name.orEmpty()) {
                onEvent(
                    ProfileScreenEvent.SetProfileSwitcherVisibility(
                        true
                    )
                )
            }
            FilledTonalIconButton(
                onClick = onOpenSettings
            ) {
                Icon(
                    painter = painterResource(Res.drawable.settings),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        if (state.currentProfile is Profile.StudentProfile && state.currentProfile.vppId == null) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.Center
            ) {
                Image(
                    painter = painterResource(Res.drawable.undraw_profile),
                    contentDescription = null,
                    modifier = Modifier
                        .fillMaxWidth()
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Hier landen deine Noten",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = "wenn du eine vpp.ID mit beste.schule hinzufügst."
                )
                Spacer(Modifier.height(8.dp))
                TextButton(
                    onClick = { openUrl(VPP_ID_AUTH_URL) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.log_in),
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                    )
                    Spacer(Modifier.width(8.dp))
                    Text("Anmelden/Registrieren")
                }
            }
        }
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
        ) {
            Spacer(Modifier.height(8.dp))
            if (state.currentProfile is Profile.StudentProfile) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    val vppId = state.currentProfile.vppId?.collectAsResultingFlowOld()?.value
                    if (vppId is VppId.Active) {
                        val subjectInstances = state.currentProfile.subjectInstances
                            .map { it.map { subjectInstance -> subjectInstance.subject }.toSet() }
                            .distinctUntilChanged()
                            .collectAsState(emptySet()).value

                        if (vppId.schulverwalterConnection != null) {
                            GradesCard(
                                modifier = Modifier
                                    .padding(horizontal = 16.dp),
                                areGradesLocked = state.areGradesLocked,
                                subjects = subjectInstances,
                                infiniteTransition = infiniteTransition,
                                averageGrade = if (vppId.gradeIds.isEmpty() || state.averageGrade?.isNaN() == true) GradesCardFeaturedGrade.NotExisting else if (state.averageGrade == null) GradesCardFeaturedGrade.Loading else GradesCardFeaturedGrade.Value(state.averageGrade.toString()),
                                latestGrade = when (state.latestGrade) {
                                    is LatestGrade.Loading -> GradesCardFeaturedGrade.Loading
                                    is LatestGrade.NotExisting -> GradesCardFeaturedGrade.NotExisting
                                    is LatestGrade.Value -> GradesCardFeaturedGrade.Value(state.latestGrade.value)
                                },
                                onRequestUnlock = {
                                    openGradesScreenAfterUnlock = true
                                    onEvent(ProfileScreenEvent.RequestGradeUnlock)
                                },
                                onOpenGrades = onOpenGrades,
                            )
                        }
                    }
                }
            }
        }
    }
}