package plus.vplan.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun Button(
    text: String,
    icon: DrawableResource? = null,
    state: ButtonState = ButtonState.ENABLED,
    size: ButtonSize = ButtonSize.BIG,
    type: ButtonType = ButtonType.PRIMARY,
    onlyEventOnActive: Boolean = true,
    onClick: () -> Unit
) {
    androidx.compose.material3.Button(
        onClick = { if (!onlyEventOnActive || state == ButtonState.ENABLED) onClick() },
        enabled = state != ButtonState.DISABLED,
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier
            .then(
                when (size) {
                    ButtonSize.BIG -> Modifier.defaultMinSize(minHeight = 56.dp).fillMaxWidth()
                    ButtonSize.NORMAL -> Modifier.defaultMinSize(minHeight = 48.dp).fillMaxWidth()
                    ButtonSize.SMALL -> Modifier.defaultMinSize(minHeight = 48.dp).animateContentSize(tween())
                }
            ),
        colors = when (type) {
            ButtonType.PRIMARY -> ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            )
            ButtonType.SECONDARY -> ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            )
            ButtonType.TERTIARY -> ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary
            )
        }
    ) {
        AnimatedContent(
            targetState = state
        ) { displayState ->
            Box(
                modifier = Modifier
                    .then(
                        if (size == ButtonSize.SMALL) Modifier
                        else Modifier.fillMaxWidth()
                    ),
                contentAlignment = Alignment.CenterEnd
            ) {
                when (displayState) {
                    ButtonState.LOADING -> CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )

                    else -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        Text(
                            text = text,
                            color = MaterialTheme.colorScheme.onPrimary,
                        )
                        if (icon != null) Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }
            }
        }
    }
}

enum class ButtonSize {
    BIG, NORMAL, SMALL
}

enum class ButtonType {
    PRIMARY, SECONDARY, TERTIARY
}

enum class ButtonState {
    ENABLED, DISABLED, LOADING
}