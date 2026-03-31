package plus.vplan.app.core.ui.util.paddingvalues

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

@Immutable
private class CopyPaddingValues(
    private val original: PaddingValues,
    private val top: Dp?,
    private val bottom: Dp?,
    private val start: Dp?,
    private val end: Dp?
) : PaddingValues {
    override fun calculateTopPadding(): Dp = top ?: original.calculateTopPadding()
    override fun calculateBottomPadding(): Dp = bottom ?: original.calculateBottomPadding()

    override fun calculateLeftPadding(layoutDirection: LayoutDirection): Dp =
        (if (layoutDirection == LayoutDirection.Ltr) start else end) ?: original.calculateLeftPadding(layoutDirection)

    override fun calculateRightPadding(layoutDirection: LayoutDirection): Dp =
        (if (layoutDirection == LayoutDirection.Ltr) end else start) ?: original.calculateRightPadding(layoutDirection)
}

fun PaddingValues.copy(
    top: Dp? = null,
    bottom: Dp? = null,
    start: Dp? = null,
    end: Dp? = null
): PaddingValues = CopyPaddingValues(this, top, bottom, start, end)