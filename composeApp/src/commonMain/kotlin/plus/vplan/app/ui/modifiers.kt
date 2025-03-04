package plus.vplan.app.ui

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector2D
import androidx.compose.animation.core.Spring.StiffnessMediumLow
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.layout.onPlaced
import androidx.compose.ui.layout.positionInParent
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.round
import kotlinx.coroutines.launch

fun Modifier.grayScale(saturation: Float = 0f): Modifier {
    val saturationMatrix = ColorMatrix().apply { setToSaturation(saturation) }
    val saturationFilter = ColorFilter.colorMatrix(saturationMatrix)
    val paint = Paint().apply { colorFilter = saturationFilter }
    return this.then(
        Modifier.drawWithCache {
            val canvasBounds = Rect(Offset.Zero, size)
            onDrawWithContent {
                drawIntoCanvas {
                    it.saveLayer(canvasBounds, paint)
                    drawContent()
                    it.restore()
                }
            }
        }
    )
}

fun Modifier.thenIf(modifier: Modifier, predicate: () -> Boolean): Modifier {
    return this.then(if (predicate()) modifier else Modifier)
}

fun Modifier.animatePlacement(): Modifier = composed {
    val scope = rememberCoroutineScope()
    var targetOffset by remember { mutableStateOf(IntOffset.Zero) }
    var animatable by remember {
        mutableStateOf<Animatable<IntOffset, AnimationVector2D>?>(null)
    }
    this.onPlaced {
        // Calculate the position in the parent layout
        targetOffset = it.positionInParent().round()
    }
        .offset {
            // Animate to the new target offset when alignment changes.
            val anim =
                animatable
                    ?: Animatable(targetOffset, IntOffset.VectorConverter).also {
                        animatable = it
                    }
            if (anim.targetValue != targetOffset) {
                scope.launch {
                    anim.animateTo(targetOffset, spring(stiffness = StiffnessMediumLow))
                }
            }
            // Offset the child in the opposite direction to the targetOffset, and slowly catch
            // up to zero offset via an animation to achieve an overall animated movement.
            animatable?.let { it.value - targetOffset } ?: IntOffset.Zero
        }
}