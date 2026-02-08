package plus.vplan.app.feature.grades.page.view.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.list_ordered

@Composable
fun NoGradesForInterval(
    modifier: Modifier = Modifier,
    intervalName: String
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Icon(
            painter = painterResource(Res.drawable.list_ordered),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = "Keine Noten im Halbjahr \"$intervalName\"",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NoGradesForIntervalPreview() {
    NoGradesForInterval(Modifier.fillMaxSize(), "12/II")
}