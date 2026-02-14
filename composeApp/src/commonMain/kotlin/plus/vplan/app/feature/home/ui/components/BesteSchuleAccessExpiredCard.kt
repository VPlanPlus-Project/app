package plus.vplan.app.feature.home.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plus.vplan.app.ui.components.InfoCard
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.key_round

@Composable
fun BesteSchuleAccessExpiredCard(
    modifier: Modifier = Modifier,
    isExpired: Boolean,
    onReauthenticate: () -> Unit,
) {
    AnimatedVisibility(
        visible = isExpired,
        enter = expandVertically(expandFrom = Alignment.CenterVertically) + fadeIn(),
        exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically) + fadeOut(),
        modifier = Modifier.fillMaxWidth()
    ) {
        InfoCard(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            title = "beste.schule-Zugangsdaten abgelaufen",
            text = "Die Verbindung zu beste.schule ist nicht mehr gültig. Bitte melde dich erneut mit beste.schule an.",
            color = MaterialTheme.colorScheme.error,
            buttonAction1 = onReauthenticate,
            buttonText1 = "Aktualisieren",
            imageVector = Res.drawable.key_round
        )
    }
}

@Preview
@Composable
private fun BesteSchuleAccessExpiredCardPreview() {
    BesteSchuleAccessExpiredCard(isExpired = true, onReauthenticate = {})
}