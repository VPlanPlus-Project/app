package plus.vplan.app.feature.onboarding.stage.loading_data.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import plus.vplan.app.core.ui.theme.AppTheme

@Composable
fun LoadingDataDialogContent() {
    Dialog(
        onDismissRequest = {},
        content = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
            ) {
                CircularProgressIndicator()
                Text("Klassen, Lehrer und Räume werden geladen")
            }
        }
    )
}

@Preview
@Composable
private fun LoadingDataDialogPreview() {
    AppTheme(dynamicColor = false) {
        LoadingDataDialogContent()
    }
}