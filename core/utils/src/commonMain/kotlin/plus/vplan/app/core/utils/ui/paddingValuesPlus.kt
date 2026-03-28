package plus.vplan.app.core.utils.ui

import androidx.compose.foundation.layout.PaddingValues

infix operator fun PaddingValues.plus(other: PaddingValues): PaddingValues {
    return CombinedPaddingValues(this, other)
}