package plus.vplan.app.feature.profile.settings.page.main.ui.vpp_id_management.components

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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.Response
import plus.vplan.app.feature.profile.settings.page.main.ui.vpp_id_management.VppIdManagementEvent
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.logout

@Composable
fun LogoutDialog(
    onDismiss: () -> Unit,
    deletionState: Response<Unit>?,
    onEvent: (VppIdManagementEvent) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            var boxHeight by remember<MutableState<Dp?>> { mutableStateOf(null) }
            val localDensity = LocalDensity.current
            Box(
                modifier = Modifier.then(boxHeight?.let { Modifier.height(it) } ?: Modifier)
            ) {
                AnimatedContent(
                    targetState = deletionState,
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
                onClick = onDismiss
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
                    visible = deletionState is Response.Error,
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