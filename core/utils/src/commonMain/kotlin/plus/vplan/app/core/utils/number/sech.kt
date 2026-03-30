package plus.vplan.app.core.utils.number

import kotlin.math.exp

fun sech(x: Double): Double {
    return 2.0 / (exp(x) + exp(-x))
}