package plus.vplan.app.core.ui.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.abs

fun abs(value: Dp): Dp {
    return abs(value.value).dp
}