package plus.vplan.app.feature.settings.page.security.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.fingerprint

@Composable
fun EnrollBiometricAuthenticationDialog(
    onDismiss: () -> Unit,
    onOpenSettings: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        icon = {
            Icon(
                painter = painterResource(Res.drawable.fingerprint),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        title = { Text("Biometrische Anmeldung einrichten") },
        text = {
            Text("Dein Gerät unterstützt sichere Anmeldung mit Fingerabdruck oder Gesichtserkennung, du hast es jedoch noch nicht eingerichtet. Möchtest du die sichere Anmeldung jetzt einrichten?")
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text("Abbrechen")
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onDismiss(); onOpenSettings() },
            ) {
                Text("Ja")
            }
        }
    )
}