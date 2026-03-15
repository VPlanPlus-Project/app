package plus.vplan.app.core.ui.modifier

import androidx.compose.ui.Modifier

fun Modifier.thenIf(modifier: Modifier, predicate: () -> Boolean): Modifier {
    return this.then(if (predicate()) modifier else Modifier)
}