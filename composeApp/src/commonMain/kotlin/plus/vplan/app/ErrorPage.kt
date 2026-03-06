package plus.vplan.app

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.Button
import plus.vplan.app.core.ui.components.ButtonType
import plus.vplan.app.core.ui.theme.monospaceFontFamily
import plus.vplan.app.utils.copyToClipboard


@Composable
fun ErrorPage(
    error: Error,
    onOpenAppInfo: () -> Unit = {},
    onRestartApp: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            painter = painterResource(CoreUiRes.drawable.triangle_alert),
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .padding(top = 32.dp, bottom = 16.dp)
                .size(64.dp)
        )

        Text(
            text = "Ein schwerer Fehler ist aufgetreten",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Text(
            text = "Das tut uns leid. VPlanPlus muss neugestartet werden. Ein Fehlerbericht wurde erstellt und versant.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )

        Text(
            text = "Fehlerdetails",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, true)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Box {
                    Text(
                        text = error.stacktrace,
                        fontFamily = monospaceFontFamily(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        softWrap = false
                    )
                }
            }

            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp),
                onClick = { copyToClipboard("Stacktrace", error.stacktrace) }
            ) {
                Icon(
                    painter = painterResource(CoreUiRes.drawable.copy),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).padding(2.dp),
                )
            }
        }

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                text = "Neustarten",
                icon = CoreUiRes.drawable.rotate_cw,
                modifier = Modifier.weight(1f),
                onClick = onRestartApp
            )

            Button(
                text = "App-Info",
                type = ButtonType.Secondary,
                icon = CoreUiRes.drawable.info,
                modifier = Modifier.weight(1f),
                onClick = onOpenAppInfo
            )
        }
    }
}

data class Error(
    val stacktrace: String
)