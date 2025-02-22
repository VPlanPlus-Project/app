package plus.vplan.app.feature.profile.page.ui

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.main.MainScreen
import plus.vplan.app.feature.profile.page.ui.components.ProfileTitle
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.settings

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    navHostController: NavHostController,
    viewModel: ProfileViewModel
) {
    val state = viewModel.state

    ProfileContent(
        state = state,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent,
        onOpenSettings = remember { { navHostController.navigate(MainScreen.Settings) } }
    )
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    contentPadding: PaddingValues,
    onEvent: (event: ProfileScreenEvent) -> Unit,
    onOpenSettings: () -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
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
        Spacer(Modifier.height(8.dp))
        if (state.currentProfile is Profile.StudentProfile) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
            }
        }
    }
}