package plus.vplan.app.feature.profile.settings.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.utils.DOT
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import vplanplus.composeapp.generated.resources.check
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
        onBack = { navHostController.navigateUp() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ProfileSettingsContent(
    state: ProfileSettingsState,
    onBack: () -> Unit,
    onEvent: (event: ProfileSettingsEvent) -> Unit
) {

    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

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
                    var newName by rememberSaveable { mutableStateOf(state.profile.displayName) }
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
                            placeholder = { Text(state.profile.originalName) }
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
                        text = state.profile.displayName,
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
                    append(state.profile.originalName)
                    append(" $DOT ")
                    append(state.profile.school.name)
                },
                modifier = Modifier.padding(horizontal = 16.dp),
                style = MaterialTheme.typography.labelMedium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}