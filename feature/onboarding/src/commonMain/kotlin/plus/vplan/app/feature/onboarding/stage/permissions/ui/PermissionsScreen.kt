package plus.vplan.app.feature.onboarding.stage.permissions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import network.chaintech.cmpeasypermission.PermissionState
import network.chaintech.cmpeasypermission.RequestPermission
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.Button
import plus.vplan.app.core.ui.components.ButtonSize
import plus.vplan.app.core.ui.components.ButtonState

@Composable
internal fun PermissionsScreen(onDone: () -> Unit) {
    var requestPermission by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .safeDrawingPadding()
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f, true)
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                painter = painterResource(CoreUiRes.drawable.bell_ring),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = "Wir benötigen deine Zustimmung",
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
            Text(
                text = "Bitte erlaube VPlanPlus, Benachrichtigungen zu senden.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .padding(bottom = 16.dp)
                .fillMaxWidth()
        ) {
            Button(
                text = "Weiter",
                state = ButtonState.Enabled,
                icon = CoreUiRes.drawable.arrow_right,
                size = ButtonSize.Big,
                onlyEventOnActive = true,
                onClick = { requestPermission = true }
            )
        }

        if (requestPermission) RequestPermission(
            permission = PermissionState.POST_NOTIFICATIONS,
            openSetting = false,
            deniedDialogTitle = "",
            deniedDialogDesc = "",
            isGranted = { onDone() }
        )
    }
}
