@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package plus.vplan.app.feature.grades.page.view.ui.components.latest

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.badge_plus

@Composable
private fun sectionTitleFont() = MaterialTheme.typography.titleMediumEmphasized

@Composable
@Preview(showBackground = true)
fun LatestGradesTitle(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            painter = painterResource(Res.drawable.badge_plus),
            contentDescription = null,
            modifier = Modifier.size(sectionTitleFont().lineHeight.toDp())
        )
        Text(
            text = "Neue Noten",
            style = sectionTitleFont(),
        )
    }
}