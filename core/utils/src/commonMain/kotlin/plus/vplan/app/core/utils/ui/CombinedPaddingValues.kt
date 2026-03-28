package plus.vplan.app.core.utils.ui

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

@Immutable
class CombinedPaddingValues(
    private val first: PaddingValues,
    private val second: PaddingValues
) : PaddingValues {
    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
        first.calculateLeftPadding(layoutDirection) + second.calculateLeftPadding(layoutDirection)

    override fun calculateTopPadding(): Dp =
        first.calculateTopPadding() + second.calculateTopPadding()

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
        first.calculateRightPadding(layoutDirection) + second.calculateRightPadding(layoutDirection)

    override fun calculateBottomPadding(): Dp =
        first.calculateBottomPadding() + second.calculateBottomPadding()
}