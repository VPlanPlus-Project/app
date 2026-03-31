package plus.vplan.app.core.ui.util.paddingvalues

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp

@Composable
fun PaddingValues.copy(
    top: Dp = this.calculateTopPadding(),
    bottom: Dp = this.calculateBottomPadding(),
    start: Dp = this.calculateStartPadding(LocalLayoutDirection.current),
    end: Dp = this.calculateEndPadding(LocalLayoutDirection.current),
): PaddingValues = PaddingValues(top, bottom, start, end)

@Composable
infix operator fun PaddingValues.plus(other: PaddingValues) = PaddingValues(
    top = (this.calculateTopPadding() + other.calculateTopPadding()).coerceAtLeast(0.dp),
    bottom = this.calculateBottomPadding() + other.calculateBottomPadding().coerceAtLeast(0.dp),
    start = (this.calculateStartPadding(LocalLayoutDirection.current) + other.calculateStartPadding(LocalLayoutDirection.current)).coerceAtLeast(0.dp),
    end = (this.calculateEndPadding(LocalLayoutDirection.current) + other.calculateEndPadding(LocalLayoutDirection.current)).coerceAtLeast(0.dp),
)