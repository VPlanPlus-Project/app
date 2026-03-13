package plus.vplan.app.core.utils.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalLayoutDirection

@Composable
infix operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    val localLayoutDirection = LocalLayoutDirection.current
    return PaddingValues(
        top = this.calculateTopPadding() + other.calculateTopPadding(),
        bottom = this.calculateBottomPadding() + other.calculateBottomPadding(),
        start = this.calculateStartPadding(localLayoutDirection) + other.calculateStartPadding(localLayoutDirection),
        end = this.calculateEndPadding(localLayoutDirection) + other.calculateEndPadding(localLayoutDirection)
    )
}