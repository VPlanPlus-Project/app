package plus.vplan.app.feature.home.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.feature.calendar.view.ui.components.InfoCard


@Composable
fun DayInfoCard(
    modifier: Modifier = Modifier,
    info: String
) {
    InfoCard(
        modifier = modifier,
        title = "Informationen deiner Schule",
        text = info,
        imageVector = CoreUiRes.drawable.info,
        shadow = true
    )
}