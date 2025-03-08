package plus.vplan.app.utils

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

fun abs(value: Dp): Dp {
    return abs(value.value).dp
}

@Composable
fun Float.toDp(): Dp {
    with (LocalDensity.current) {
        return toDp()
    }
}