package plus.vplan.app.feature.settings.page.school.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui.component.PasswordField
import plus.vplan.app.feature.onboarding.stage.b_school_indiware_login.ui.component.UsernameField
import plus.vplan.app.feature.settings.page.school.ui.SchoolSettingsCredentialsState
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.ButtonType
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.school

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IndiwareCredentialSheet(
    schoolId: Int,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val viewModel = koinViewModel<IndiwareCredentialViewModel>()
    val state = viewModel.state
    val scope = rememberCoroutineScope()

    LaunchedEffect(schoolId) { viewModel.init(schoolId) }

    if (state != null) ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        IndiwareCredentialSheetContent(
            state = state,
            onDismiss = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } },
            onEvent = viewModel::onEvent
        )
    }
}

@Composable
private fun IndiwareCredentialSheetContent(
    state: IndiwareCredentialState,
    onDismiss: () -> Unit,
    onEvent: (IndiwareCredentialEvent) -> Unit
) {
    val passwordFocusRequester = remember { FocusRequester() }

    LaunchedEffect(state.state) {
        if (state.state == SchoolSettingsCredentialsState.Valid) onDismiss()
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 16.dp)
            .padding(bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding())
            .fillMaxWidth()
    ) {
        Text(
            text = "Stundenplan24.de",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Aktualisiere die Zugangsdaten für ${state.schoolName}",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = state.sp24Id,
            enabled = false,
            onValueChange = {},
            minLines = 1,
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Stundenplan24.de-ID") },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.school),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
            },
        )
        Spacer(Modifier.height(8.dp))
        UsernameField(
            username = state.username,
            isUsernameValid = state.username in listOf("schueler", "lehrer"),
            areCredentialsInvalid = state.state == SchoolSettingsCredentialsState.Invalid,
            onUsernameChanged = { onEvent(IndiwareCredentialEvent.SetUsername(it)) },
            onFocusPassword = { passwordFocusRequester.requestFocus() },
            hideBottomLine = false
        )
        Spacer(Modifier.height(8.dp))
        PasswordField(
            password = state.password,
            passwordFocusRequester = passwordFocusRequester,
            areCredentialsInvalid = state.state == SchoolSettingsCredentialsState.Invalid,
            onPasswordChanged = { onEvent(IndiwareCredentialEvent.SetPassword(it)) },
            onCheckCredentials = {},
            hideBottomLine = false
        )
        Spacer(Modifier.height(8.dp))
        Button(
            text = "Speichern",
            icon = Res.drawable.check,
            size = ButtonSize.Normal,
            type = ButtonType.Primary,
            state = if (state.state == SchoolSettingsCredentialsState.Loading) ButtonState.Loading else ButtonState.Enabled,
            center = false,
            onlyEventOnActive = true,
            onClick = { onEvent(IndiwareCredentialEvent.Save) }
        )
    }
}