package plus.vplan.app.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import org.jetbrains.compose.resources.DrawableResource

typealias ButtonSize = plus.vplan.app.core.ui.components.ButtonSize
typealias ButtonType = plus.vplan.app.core.ui.components.ButtonType
typealias ButtonState = plus.vplan.app.core.ui.components.ButtonState

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
) = plus.vplan.app.core.ui.components.Button(
    modifier = modifier,
    text = text,
    icon = icon,
    state = state,
    size = size,
    type = type,
    onlyEventOnActive = onlyEventOnActive,
    center = center,
    onClick = onClick
)
