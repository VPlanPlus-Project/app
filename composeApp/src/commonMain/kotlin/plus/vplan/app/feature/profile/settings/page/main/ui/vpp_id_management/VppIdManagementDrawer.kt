package plus.vplan.app.feature.profile.settings.page.main.ui.vpp_id_management

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.format
import kotlinx.datetime.format.DayOfWeekNames
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.model.Response
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.feature.profile.settings.page.main.ui.vpp_id_management.components.LogoutDialog
import plus.vplan.app.ui.components.Badge
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.ButtonType
import plus.vplan.app.ui.theme.ColorToken
import plus.vplan.app.ui.theme.customColors
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.safeBottomPadding
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.logout

@Composable
private fun VppIdManagementDrawerContent(
    state: VppIdManagementState,
    onEvent: (VppIdManagementEvent) -> Unit
) {
    var isLogoutDialogVisible by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.logoutState) {
        if (state.logoutState is Response.Success) {
            isLogoutDialogVisible = false
        }
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = safeBottomPadding())
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "Geräte in deiner vpp.ID",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (state.devices is Response.Success) {
            Text(
                text = "Tippe auf ein Gerät, um es abzumelden. Dieser Vorgang kann nicht rückgängig gemacht werden.",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        Spacer(Modifier.height(4.dp))
        Column(Modifier.fillMaxWidth()) {
            when (state.devices) {
                is Response.Loading -> CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .size(24.dp)
                )
                is Response.Success -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp)),
                        verticalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        state.devices.data.forEach { device ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainer)
                                    .padding(16.dp),
                                horizontalArrangement = Arrangement.spacedBy(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(Modifier.weight(1f)) {
                                    FlowRow(
                                        verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Text(
                                            text = device.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        if (device.isThisDevice) Badge(
                                            color = customColors[ColorToken.Green]!!.get(),
                                            text = "Dieses Gerät"
                                        )
                                    }
                                    Text(
                                        text = device.connectedAt.format(LocalDateTime.Format {
                                            dayOfWeek(DayOfWeekNames("Montag", "Dienstag", "Mittwoch", "Donnerstag", "Freitag", "Samstag", "Sonntag"))
                                            chars(", ")
                                            day(padding = Padding.ZERO)
                                            chars(". ")
                                            monthName(MonthNames("Januar", "Februar", "März", "April", "Mai", "Juni", "Juli", "August", "September", "Oktober", "November", "Dezember"))
                                            chars(" ")
                                            year()
                                            chars(" $DOT ")
                                            hour()
                                            char(':')
                                            minute()
                                        }),
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(onClick = {
                                    if (device.isThisDevice) isLogoutDialogVisible = true
                                    else onEvent(VppIdManagementEvent.LogoutDevice(device))
                                }) {
                                    Icon(
                                        painter = painterResource(Res.drawable.logout),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.error
                                    )
                                }
                            }
                        }
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
        deletionState = state.logoutState,
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
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val hideSheet = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } }

    LaunchedEffect(state.logoutState) {
        if (state.logoutState is Response.Success) {
            hideSheet()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0.dp) },
        sheetState = sheetState
    ) {
        VppIdManagementDrawerContent(
            state = state,
            onEvent = viewModel::onEvent
        )
    }
}