package plus.vplan.app.feature.settings.page.info.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.ButtonType
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.send_horizontal

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedbackDrawer(
    onDismiss: () -> Unit
) {
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState
    ) {
        FeedbackDrawerContent()
    }
}

@Composable
private fun FeedbackDrawerContent() {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp))
    ) {
        Text(
            text = "Dein Feedback",
            style = MaterialTheme.typography.headlineLarge,
        )
        Text(
            text = "Möglicherweise melden wir uns mit einer Antwort zurück.",
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(Modifier.height(8.dp))
        TextField(
            value = "",
            onValueChange = {},
            placeholder = { Text("Deine Nachricht an die VPlanPlus-Entwickler") },
            minLines = 5,
            singleLine = false,
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(8.dp))
        Button(
            modifier = Modifier.fillMaxWidth(),
            text = "Absenden",
            icon = Res.drawable.send_horizontal,
            state = ButtonState.Enabled,
            size = ButtonSize.Big,
            type = ButtonType.Primary,
            onlyEventOnActive = true,
            center = false,
            onClick = {}
        )
    }
}