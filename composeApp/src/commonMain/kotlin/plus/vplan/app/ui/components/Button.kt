package plus.vplan.app.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun Button(
    modifier: Modifier = Modifier,
    text: String,
    icon: DrawableResource? = null,
    state: ButtonState = ButtonState.Enabled,
    size: ButtonSize = ButtonSize.Big,
    type: ButtonType = ButtonType.Primary,
    onlyEventOnActive: Boolean = true,
    center: Boolean = false,
    onClick: () -> Unit
) {
    val clickEvent = { if (!onlyEventOnActive || state == ButtonState.Enabled) onClick() }
    val enabled = state != ButtonState.Disabled
    val shape = RoundedCornerShape(8.dp)
    val buttonModifier = modifier.then(
        when (size) {
            ButtonSize.Big -> Modifier.defaultMinSize(minHeight = 56.dp).fillMaxWidth()
            ButtonSize.Normal -> Modifier.defaultMinSize(minHeight = 48.dp).fillMaxWidth()
            ButtonSize.Small -> Modifier.defaultMinSize(minHeight = 48.dp).animateContentSize(tween())
        }
    )
    val content: @Composable () -> Unit = {
        AnimatedContent(
            targetState = state
        ) { displayState ->
            Box(
                modifier = Modifier
                    .then(
                        if (size == ButtonSize.Small) Modifier
                        else Modifier.fillMaxWidth()
                    ),
                contentAlignment = if (center) Alignment.Center else Alignment.CenterEnd
            ) {
                when (displayState) {
                    ButtonState.Loading -> CircularProgressIndicator(
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(24.dp)
                    )

                    else -> Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        if (icon != null && center) Spacer(Modifier.width(24.dp))
                        Text(
                            text = text,
                            style = MaterialTheme.typography.labelLarge,
                            textAlign = if (center) TextAlign.Center else TextAlign.Start,
                            modifier = Modifier.weight(1f)
                        )
                        if (icon != null) Icon(
                            painter = painterResource(icon),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }
        }
    }

    if (type in listOf(ButtonType.Outlined, ButtonType.OutlinedOnSheet)) {
        OutlinedButton(
            onClick = clickEvent,
            enabled = enabled,
            shape = shape,
            modifier = buttonModifier,
            colors = ButtonDefaults.outlinedButtonColors(
                contentColor = MaterialTheme.colorScheme.primary
            )
        ) {
            content()
        }
    } else {
        androidx.compose.material3.Button(
            onClick = clickEvent,
            enabled = enabled,
            shape = shape,
            modifier = buttonModifier,
            colors = when (type) {
                ButtonType.Primary -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                )
                ButtonType.Secondary -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.surfaceDim,
                    contentColor = MaterialTheme.colorScheme.onSurface
                )
                ButtonType.TERTIARY -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.tertiary,
                    contentColor = MaterialTheme.colorScheme.onTertiary
                )
                ButtonType.Danger -> ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
                else -> ButtonDefaults.buttonColors()
            }
        ) {
            content()
        }
    }
}

enum class ButtonSize {
    Big, Normal, Small
}

enum class ButtonType {
    Primary, Secondary, TERTIARY, Outlined, OutlinedOnSheet, Danger
}

enum class ButtonState {
    Enabled, Disabled, Loading
}