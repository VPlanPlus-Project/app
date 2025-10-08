package plus.vplan.app.feature.settings.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.utils.blendColor
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.chevron_right

@Composable
fun SettingsRecord(
    title: String,
    subtitle: String? = null,
    icon: Painter? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    showArrow: Boolean = false,
    onClick: () -> Unit
) {
    BaseSettingsRecord(
        title = title,
        subtitle = subtitle,
        icon = icon,
        isLoading = isLoading,
        enabled = enabled,
        onClick = onClick,
        endContent = if (showArrow) {
            {
                Icon(
                    painter = painterResource(Res.drawable.chevron_right),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            }
        } else null
    )
}

@Composable
fun SettingsRecordCheckbox(
    title: String,
    subtitle: String? = null,
    icon: Painter? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    BaseSettingsRecord(
        title = title,
        subtitle = subtitle,
        icon = icon,
        isLoading = isLoading,
        enabled = enabled,
        onClick = { if (enabled) onCheckedChange(!checked) },
        endContent = {
            Checkbox(
                checked = checked,
                onCheckedChange = { onCheckedChange(it) },
                enabled = enabled
            )
        }
    )
}

@Composable
private fun BaseSettingsRecord(
    title: String,
    subtitle: String? = null,
    icon: Painter? = null,
    isLoading: Boolean = false,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    endContent: (@Composable (() -> Unit))? = null
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .let { base -> if (onClick != null) base.clickable(enabled = enabled) { onClick() } else base }
            .padding(vertical = 16.dp, horizontal = 24.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.size(24.dp)) {
            if (isLoading) CircularProgressIndicator(Modifier.size(24.dp))
            else icon?.let {
                Icon(
                    painter = it,
                    contentDescription = null
                )
            }
        }
        Column(Modifier.weight(1f)) {
            AnimatedContent(
                targetState = enabled,
                transitionSpec = { fadeIn() togetherWith fadeOut() }
            ) { isEnabled ->
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = if (isEnabled) MaterialTheme.colorScheme.onSurface else blendColor(MaterialTheme.colorScheme.onSurface, MaterialTheme.colorScheme.surface, 0.5f)
                )
            }
            subtitle?.let { sub ->
                AnimatedContent(
                    targetState = enabled,
                    transitionSpec = { fadeIn() togetherWith fadeOut() }
                ) { isEnabled ->
                    Text(
                        text = sub,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isEnabled) MaterialTheme.colorScheme.onSurfaceVariant else blendColor(MaterialTheme.colorScheme.onSurfaceVariant, MaterialTheme.colorScheme.surface, 0.5f)
                    )
                }
            }
        }
        endContent?.invoke()
    }
}