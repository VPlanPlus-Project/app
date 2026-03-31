package plus.vplan.app.core.ui.util

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import plus.vplan.app.core.ui.util.paddingvalues.copy

expect fun getNativeNavigationBarHeight(): Dp

@Composable
fun PaddingValues.minusNativeNavigationBarHeight() = this.copy(
    top = (this.calculateTopPadding() - getNativeNavigationBarHeight()).coerceAtLeast(0.dp)
)