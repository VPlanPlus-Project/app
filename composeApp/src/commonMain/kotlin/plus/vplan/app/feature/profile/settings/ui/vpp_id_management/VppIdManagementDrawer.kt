package plus.vplan.app.feature.profile.settings.ui.vpp_id_management

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.feature.profile.settings.ui.vpp_id_management.components.LogoutDialog
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.ButtonType
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.logout

@Composable
private fun VppIdManagementDrawerContent(
    state: VppIdManagementState,
    onEvent: (VppIdManagementEvent) -> Unit
) {
    var isLogoutDialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.deletionState) {
        if (state.deletionState is Response.Success) {
            isLogoutDialogVisible = false
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Geräte in deiner vpp.ID",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(4.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize()
                .clip(RoundedCornerShape(8.dp))
        ) {
            when (state.devices) {
                is Response.Loading -> CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(24.dp)
                )
                is Response.Success -> {
                    state.devices.data.forEach {
                        Text(text = it.toString())
                    }
                }
                is Response.Error -> {
                    Text(
                        text = "Ein Fehler ist aufgetreten. Bitte versuche es erneut.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        Spacer(Modifier.height(8.dp))
        Button(
            text = "Abmelden",
            state = ButtonState.Enabled,
            size = ButtonSize.Big,
            type = ButtonType.Danger,
            icon = Res.drawable.logout,
            onClick = { isLogoutDialogVisible = true }
        )
    }

    if (isLogoutDialogVisible) LogoutDialog(
        onDismiss = { isLogoutDialogVisible = false },
        deletionState = state.deletionState,
        onEvent = onEvent
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VppIdManagementDrawer(
    onDismiss: () -> Unit,
    vppId: VppId.Active,
) {
    val viewModel = koinViewModel<VppIdManagementViewModel>()
    val state = viewModel.state

    LaunchedEffect(vppId) {
        viewModel.init(vppId)
    }

    val scope = rememberCoroutineScope()
    val sheetState = rememberModalBottomSheetState()
    val hideSheet = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } }

    LaunchedEffect(state.deletionState) {
        if (state.deletionState is Response.Success) {
            hideSheet()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        VppIdManagementDrawerContent(
            state = state,
            onEvent = viewModel::onEvent
        )
    }
}