package plus.vplan.app.feature.grades.page.view.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.lock
import vplanplus.composeapp.generated.resources.lock_open

@Composable
fun GradesLocked(
    modifier: Modifier = Modifier,
    onRequestGradeUnlock: () -> Unit
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
    ) {
        Icon(
            painter = painterResource(Res.drawable.lock),
            contentDescription = null,
            modifier = Modifier.size(32.dp)
        )
        Text(
            text = "Noten gesperrt",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(Modifier.height(8.dp))
        TextButton(
            onClick = onRequestGradeUnlock,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.tertiary
            )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.lock_open),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Text("Entsperren")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun GradesLockedPreview() {
    GradesLocked(Modifier.fillMaxSize()) {  }
}