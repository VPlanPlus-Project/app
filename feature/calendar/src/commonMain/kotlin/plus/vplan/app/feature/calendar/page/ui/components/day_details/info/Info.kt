package plus.vplan.app.feature.calendar.page.ui.components.day_details.info

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.feature.calendar.page.ui.components.day_details.Title

@Composable
fun Info(
    modifier: Modifier = Modifier,
    info: String
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Title(
            icon = painterResource(CoreUiRes.drawable.megaphone),
            title = "Informationen deiner Schule"
        )
        Text(
            text = info,
            style = MaterialTheme.typography.bodyLarge,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 8.dp)
        )
    }
}

@Composable
@Preview
private fun InfoPreview() {
    AppTheme(dynamicColor = false) {
        Info(
            info = "NEU: 14:55 Uhr"
        )
    }
}