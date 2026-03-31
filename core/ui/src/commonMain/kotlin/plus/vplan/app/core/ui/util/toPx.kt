package plus.vplan.app.core.ui.util

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Dp.toPx(): Float = with(LocalDensity.current) { this@toPx.toPx() }

@Composable
fun Dp.roundToPx(): Int = with(LocalDensity.current) { this@roundToPx.roundToPx() }