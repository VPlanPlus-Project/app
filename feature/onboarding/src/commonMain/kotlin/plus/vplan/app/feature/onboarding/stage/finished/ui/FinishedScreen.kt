package plus.vplan.app.feature.onboarding.stage.finished.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.Button
import plus.vplan.app.core.ui.components.ButtonSize
import plus.vplan.app.core.ui.components.ButtonState
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.feature.onboarding.ui.components.OnboardingHeader

@Composable
internal fun FinishedScreen(onFinish: () -> Unit) {
    val haptic = LocalHapticFeedback.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(horizontal = 16.dp)
    ) {
        OnboardingHeader(
            title = "VPlanPlus ist fertig eingerichtet",
            subtitle = "Vielen Dank für die Nutzung von VPlanPlus."
        )

        Column(
            modifier = Modifier
                .weight(1f, true)
                .fillMaxWidth()
        ) {}

        Button(
            text = "Fertig",
            state = ButtonState.Enabled,
            icon = CoreUiRes.drawable.check,
            size = ButtonSize.Big,
            onlyEventOnActive = true,
            modifier = Modifier.padding(bottom = 16.dp),
            onClick = {
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                onFinish()
            }
        )
    }
}

@Preview
@Composable
private fun FinishedPreview() {
    AppTheme(dynamicColor = false) {
        FinishedScreen {  }
    }
}
