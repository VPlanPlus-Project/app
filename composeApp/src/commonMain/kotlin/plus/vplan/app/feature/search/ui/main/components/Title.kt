package plus.vplan.app.feature.search.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import plus.vplan.app.ui.components.noRippleClickable
import plus.vplan.app.ui.theme.displayFontFamily
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.search

@Composable
fun Title(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.search),
            contentDescription = null,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = "Suche",
            fontFamily = displayFontFamily(),
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier
                .fillMaxWidth()
                .noRippleClickable(onClick)
        )
    }
}

@Composable
@Preview
private fun TitlePreview() {
    Title(onClick = {})
}