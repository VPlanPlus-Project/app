package plus.vplan.app.core.ui.modifier

import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

expect fun Modifier.premiumShadow(
    color: Color = Color.Black.copy(alpha = 0.1f),
    blurRadius: Dp = 12.dp,
    offsetY: Dp = 8.dp,
    borderRadius: Dp = 0.dp
): Modifier