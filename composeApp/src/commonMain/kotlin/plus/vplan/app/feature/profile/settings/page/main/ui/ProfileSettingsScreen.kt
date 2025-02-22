package plus.vplan.app.feature.profile.settings.page.main.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.VPP_ID_AUTH_URL
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.main.MainScreen
import plus.vplan.app.feature.profile.settings.page.main.domain.usecase.VppIdConnectionState
import plus.vplan.app.feature.profile.settings.page.main.ui.vpp_id_management.VppIdManagementDrawer
import plus.vplan.app.feature.settings.ui.components.SettingsRecord
import plus.vplan.app.utils.BrowserIntent
import plus.vplan.app.utils.DOT
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.circle_user_round
import vplanplus.composeapp.generated.resources.graduation_cap
import vplanplus.composeapp.generated.resources.pencil
import vplanplus.composeapp.generated.resources.x

@Composable
fun ProfileSettingsScreen(
    profileId: String,
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<ProfileSettingsViewModel>()
    val state = viewModel.state

    LaunchedEffect(profileId) {
        viewModel.init(profileId)
    }

    ProfileSettingsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onOpenSubjectInstances = remember(profileId) { { navHostController.navigate(MainScreen.ProfileSubjectInstances(profileId)) } },
        onBack = { navHostController.navigateUp() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSettingsContent(
    state: ProfileSettingsState,
    onBack: () -> Unit,
    onOpenSubjectInstances: () -> Unit,
    onEvent: (event: ProfileSettingsEvent) -> Unit
) {

    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    var isVppIdManagementDrawerVisible by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Profileinstellungen") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_left),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehaviour
            )
        }
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nestedScroll(scrollBehaviour.nestedScrollConnection)
                .padding(top = 16.dp)
        ) {
            if (state.profile == null) return@Column
            var isRenamingInProgress by rememberSaveable { mutableStateOf(false) }
            AnimatedContent(
                targetState = isRenamingInProgress,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
            ) { showRenaming ->
                if (showRenaming) {
                    var newName by rememberSaveable { mutableStateOf(state.profile.name) }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextField(
                            value = newName,
                            modifier = Modifier.weight(1f, true),
                            singleLine = true,
                            onValueChange = { newName = it },
                            label = { Text("Name") },
                            placeholder = { Text(state.profile.name) }
                        )
                        IconButton(
                            onClick = { isRenamingInProgress = false },
                            modifier = Modifier.size(56.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.x),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                        FilledIconButton(
                            onClick = {
                                onEvent(ProfileSettingsEvent.RenameProfile(newName))
                                isRenamingInProgress = false
                            },
                            modifier = Modifier.size(56.dp),
                            shape = RoundedCornerShape(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.check),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                    return@AnimatedContent
                }
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = state.profile.name,
                        style = MaterialTheme.typography.headlineMedium
                    )
                    IconButton(onClick = { isRenamingInProgress = true }) {
                        Icon(
                            painter = painterResource(Res.drawable.pencil),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
            Text(
                text = buildString {
                    when (state.profile) {
                        is Profile.StudentProfile -> append("Klasse ")
                        is Profile.TeacherProfile -> append("Lehrer ")
                        is Profile.RoomProfile -> append("Raum ")
                    }
                    append(state.profile.name)
                    append(" $DOT ")
                    append(state.profile.schoolItem!!.name)
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(8.dp))
            if (state.profile is Profile.StudentProfile) {
                AnimatedContent(
                    targetState = state.profile.vppId,
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .clickable {
                            if (state.profile.vppId == null || state.isVppIdStillConnected == VppIdConnectionState.DISCONNECTED) {
                                BrowserIntent.openUrl(VPP_ID_AUTH_URL)
                                return@clickable
                            }
                            isVppIdManagementDrawerVisible = true
                        }
                        .padding(16.dp)
                ) { displayVppId ->
                    if (displayVppId != null) Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.circle_user_round),
                            contentDescription = null,
                            modifier = Modifier.size(32.dp)
                        )
                        Column {
                            Text(
                                text = state.profile.vppIdItem!!.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "vpp.ID verwalten",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            AnimatedVisibility(
                                visible = state.isVppIdStillConnected == VppIdConnectionState.DISCONNECTED,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Text(
                                    text = "Abgemeldet, tippe zum wiederanmelden",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                            AnimatedVisibility(
                                visible = state.isVppIdStillConnected == VppIdConnectionState.ERROR,
                                enter = fadeIn() + expandVertically(),
                                exit = fadeOut() + shrinkVertically()
                            ) {
                                Text(
                                    text = "Verbindungsfehler",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                        return@AnimatedContent
                    }
                    Column {
                        Text(
                            text = "vpp.ID verbinden",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Nimm am digitalen Klassenalltag teil.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            SettingsRecord(
                title = "Stundenauswahl",
                subtitle = "Wähle aus, welche Stunden dir angezeigt werden.",
                icon = painterResource(Res.drawable.graduation_cap),
                onClick = onOpenSubjectInstances
            )
        }

        if (isVppIdManagementDrawerVisible &&
            state.profile is Profile.StudentProfile &&
            state.profile.vppId != null) {
            VppIdManagementDrawer(
                vppId = state.profile.vppIdItem!!,
                onDismiss = { isVppIdManagementDrawerVisible = false }
            )
        }
    }
}