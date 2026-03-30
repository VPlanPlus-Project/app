package plus.vplan.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp

@Composable
fun Float.toDp(): Dp {
    with (LocalDensity.current) {
        return toDp()
    }
}