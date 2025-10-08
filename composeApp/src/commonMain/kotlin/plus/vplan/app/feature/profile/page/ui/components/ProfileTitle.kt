package plus.vplan.app.feature.profile.page.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.ui.components.noRippleClickable
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.chevron_down

@Composable
fun ProfileTitle(
    modifier: Modifier = Modifier,
    currentProfileName: String,
    onClick: () -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        modifier = modifier.noRippleClickable(onClick)
    ) {
        AnimatedContent(
            targetState = currentProfileName
        ) { displayProfileName ->
            Text(
                text = displayProfileName,
                style = MaterialTheme.typography.titleMedium
            )
        }
        Box(
            modifier = Modifier.size(24.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                painter = painterResource(Res.drawable.chevron_down),
                modifier = Modifier.size(18.dp),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
        }
    }
}