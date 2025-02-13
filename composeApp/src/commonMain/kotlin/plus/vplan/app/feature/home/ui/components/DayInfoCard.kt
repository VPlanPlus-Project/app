package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import plus.vplan.app.ui.components.InfoCard
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.info

@Composable
fun DayInfoCard(
    modifier: Modifier = Modifier,
    info: String
) {
    InfoCard(
        modifier = modifier.padding(horizontal = 16.dp),
        title = "Informationen deiner Schule",
        text = info,
        imageVector = Res.drawable.info,
        shadow = false
    )
}