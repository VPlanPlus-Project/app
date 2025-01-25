package plus.vplan.app.feature.homework.ui.components.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import plus.vplan.app.VPP_ID_AUTH_URL
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.ui.theme.ColorToken
import plus.vplan.app.ui.theme.customColors
import plus.vplan.app.utils.BrowserIntent
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.info

@Composable
fun VppIdBanner(
    canShow: Boolean,
    onHide: () -> Unit
) {
    AnimatedVisibility(
        visible = canShow,
        enter = EnterTransition.None,
        exit = shrinkVertically() + fadeOut()
    ) {
        InfoCard(
            modifier = Modifier
                .padding(top = 16.dp)
                .padding(horizontal = 16.dp),
            imageVector = Res.drawable.info,
            title = "Cloud-Speicherung",
            text = "Teile Hausaufgaben mit deiner Klasse, wenn du dich mit einer vpp.ID anmeldest.",
            buttonText2 = "Ignorieren",
            buttonAction2 = onHide,
            buttonText1 = "Anmelden",
            buttonAction1 = { BrowserIntent.openUrl(VPP_ID_AUTH_URL) },
            backgroundColor = customColors[ColorToken.YellowContainer]!!.get(),
            textColor = customColors[ColorToken.OnYellowContainer]!!.get()
        )
    }
}