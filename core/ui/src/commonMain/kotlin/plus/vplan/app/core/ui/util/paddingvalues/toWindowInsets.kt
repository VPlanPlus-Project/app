package plus.vplan.app.core.ui.util.paddingvalues

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection

@Composable
fun PaddingValues.toWindowInsets(): WindowInsets = object : WindowInsets {
    override fun getBottom(density: Density): Int = this@toWindowInsets
        .calculateBottomPadding()
        .let { with(density) { it.roundToPx() } }

    override fun getTop(density: Density): Int = this@toWindowInsets
        .calculateTopPadding()
        .let { with(density) { it.roundToPx() } }

    override fun getLeft(density: Density, layoutDirection: LayoutDirection): Int =
        this@toWindowInsets
            .calculateLeftPadding(layoutDirection)
            .let { with(density) { it.roundToPx() } }

    override fun getRight(density: Density, layoutDirection: LayoutDirection): Int =
        this@toWindowInsets
            .calculateRightPadding(layoutDirection)
            .let { with(density) { it.roundToPx() } }
}