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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonType
import plus.vplan.app.ui.theme.monospaceFontFamily
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.info
import vplanplus.composeapp.generated.resources.rotate_cw
import vplanplus.composeapp.generated.resources.triangle_alert

@Composable
fun ErrorPage(
    error: Error,
    onOpenAppInfo: () -> Unit = {},
    onRestartApp: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(horizontal = 16.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.triangle_alert),
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
            style = MaterialTheme.typography.titleSmall,
            modifier = Modifier.padding(top = 16.dp, bottom = 4.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .horizontalScroll(rememberScrollState())
                .background(MaterialTheme.colorScheme.surfaceVariant)
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

        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                text = "Neustarten",
                icon = Res.drawable.rotate_cw,
                modifier = Modifier.weight(1f),
                onClick = onRestartApp
            )

            Button(
                text = "App-Info",
                type = ButtonType.Secondary,
                icon = Res.drawable.info,
                modifier = Modifier.weight(1f),
                onClick = onOpenAppInfo
            )
        }
    }
}

data class Error(
    val stacktrace: String
)