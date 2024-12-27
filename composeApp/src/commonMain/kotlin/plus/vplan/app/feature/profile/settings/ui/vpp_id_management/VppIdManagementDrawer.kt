package plus.vplan.app.feature.profile.settings.ui.vpp_id_management

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.data.Response
import plus.vplan.app.domain.model.VppId
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
        Button(
            text = "Abmelden",
            state = ButtonState.Enabled,
            size = ButtonSize.Big,
            type = ButtonType.Danger,
            icon = Res.drawable.logout,
            onClick = { isLogoutDialogVisible = true }
        )
    }

    if (isLogoutDialogVisible) AlertDialog(
        onDismissRequest = { isLogoutDialogVisible = false },
        confirmButton = {
            var boxHeight by remember<MutableState<Dp?>> { mutableStateOf(null) }
            val localDensity = LocalDensity.current
            Box(
                modifier = Modifier.then(boxHeight?.let { Modifier.height(it) } ?: Modifier)
            ) {
                AnimatedContent(
                    targetState = state.deletionState,
                    modifier = Modifier.align(Alignment.Center)
                ) { logoutState ->
                    if (logoutState is Response.Loading) CircularProgressIndicator(Modifier.size(24.dp))
                    else TextButton(
                        onClick = { onEvent(VppIdManagementEvent.Logout) },
                        modifier = Modifier.onSizeChanged {
                            boxHeight = with(localDensity) { it.height.toDp() }
                        }
                    ) {
                        Text(
                            text = "Abmelden",
                            color = MaterialTheme.colorScheme.error
                        )
                    }
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = {
                    isLogoutDialogVisible = false
                }
            ) {
                Text("Abbrechen")
            }
        },
        icon = {
            Icon(
                painter = painterResource(Res.drawable.logout),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        title = {
            Text("Abmelden")
        },
        text = {
            Column {
                Text("Bist du sicher, dass du dich abmelden möchtest?")
                AnimatedVisibility(
                    visible = state.deletionState is Response.Error,
                    modifier = Modifier.fillMaxWidth(),
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Text(
                        text = "Ein Fehler ist aufgetreten. Bitte versuche es erneut.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
            }
        },
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