package plus.vplan.app.core.common.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.koin.compose.koinInject
import plus.vplan.app.core.data.vpp_id.VppIdRepository
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.InfoCard
import plus.vplan.app.core.ui.theme.ColorToken
import plus.vplan.app.core.ui.theme.customColors
import plus.vplan.app.core.ui.util.openUrl


@Composable
fun VppIdBanner(
    canShow: Boolean,
    isAssessment: Boolean,
    onHide: () -> Unit
) {
    val vppIdRepository = koinInject<VppIdRepository>()
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
            buttonAction2 = { openUrl(vppIdRepository.getAuthUrl()) },
            backgroundColor = customColors[ColorToken.YellowContainer]!!.get(),
            textColor = customColors[ColorToken.OnYellowContainer]!!.get()
        )
    }
}