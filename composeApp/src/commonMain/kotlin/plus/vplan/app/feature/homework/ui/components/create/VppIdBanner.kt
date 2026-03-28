package plus.vplan.app.feature.homework.ui.components.create

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.theme.ColorToken
import plus.vplan.app.core.ui.theme.customColors
import plus.vplan.app.feature.calendar.view.ui.components.InfoCard
import plus.vplan.app.getVppIdAuthUrl
import plus.vplan.app.utils.openUrl


@Composable
fun VppIdBanner(
    canShow: Boolean,
    isAssessment: Boolean,
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
            imageVector = CoreUiRes.drawable.info,
            title = "Cloud-Speicherung",
            text = run {
                if (isAssessment) "Teile Leistungserhebungen mit deiner Klasse, wenn du dich mit einer vpp.ID anmeldest."
                else "Teile Hausaufgaben mit deiner Klasse, wenn du dich mit einer vpp.ID anmeldest."
            },
            buttonText1 = "Ignorieren",
            buttonAction1 = onHide,
            buttonText2 = "Anmelden",
            buttonAction2 = { openUrl(getVppIdAuthUrl()) },
            backgroundColor = customColors[ColorToken.YellowContainer]!!.get(),
            textColor = customColors[ColorToken.OnYellowContainer]!!.get()
        )
    }
}