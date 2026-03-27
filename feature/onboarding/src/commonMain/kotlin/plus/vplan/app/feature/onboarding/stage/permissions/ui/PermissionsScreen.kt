package plus.vplan.app.feature.onboarding.stage.permissions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.Button
import plus.vplan.app.core.ui.components.ButtonSize
import plus.vplan.app.core.ui.components.ButtonState
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.feature.onboarding.ui.components.OnboardingHeader

@Composable
internal fun PermissionsScreen(
    contentPadding: PaddingValues,
    onDone: () -> Unit
) {
    val viewModel = koinViewModel<PermissionsViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(state.isDone) {
        if (!state.isDone) return@LaunchedEffect
        onDone()
    }

    PermissionsContent(
        state = state,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun PermissionsContent(
    state: PermissionsState,
    contentPadding: PaddingValues,
    onEvent: (event: PermissionsEvent) -> Unit,
) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(contentPadding)
            .padding(horizontal = 16.dp)
    ) {
        OnboardingHeader(
            title = "Wir benötigen deine Zustimmung",
            subtitle = "Bitte erlaube VPlanPlus, Benachrichtigungen zu senden. Damit informieren wir dich " +
                    "z.B. über neue Vertretungspläne oder Hausaufgaben aus deiner Klasse."
        )

        Column(
            modifier = Modifier
                .weight(1f, true)
                .fillMaxWidth()
        ) {}

        Button(
            text = "Weiter",
            state = ButtonState.Enabled,
            icon = CoreUiRes.drawable.arrow_right,
            size = ButtonSize.Big,
            modifier = Modifier.padding(bottom = 16.dp),
            onlyEventOnActive = true,
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onEvent(PermissionsEvent.Request)
            }
        )
    }
}

@Preview
@Composable
private fun PermissionPreview() {
    AppTheme(dynamicColor = false) {
        PermissionsContent(
            state = PermissionsState(),
            contentPadding = PaddingValues(),
            onEvent = {}
        )
    }
}
