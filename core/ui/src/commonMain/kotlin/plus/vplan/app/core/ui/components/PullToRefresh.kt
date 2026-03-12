package plus.vplan.app.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.ui.CoreUiRes
import kotlin.math.exp

val PULL_THRESHOLD = 64.dp

@Composable
fun InformativePullToRefresh(
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    val localDensity = LocalDensity.current
    val scope = rememberCoroutineScope()

    val maxOffsetDp = 128.dp
    var yOffsetPulled by remember { mutableStateOf(0.dp) }
    // Rubber-band curve: grows quickly at first, then decelerates asymptotically toward maxOffsetDp.
    // Formula: maxOffset * (1 - e^(-pull / maxOffset))
    val yOffsetCurved = remember(yOffsetPulled) {
        val pull = yOffsetPulled.value.coerceAtLeast(0f)
        val max = maxOffsetDp.value
        (max * (1f - exp(-pull / max))).dp
    }
    val animatable = remember { Animatable(0.dp, Dp.VectorConverter) }
    var isUserTouching by remember { mutableStateOf(false) }
    val yOffset = if (isUserTouching) yOffsetCurved else animatable.value
    val isPullThresholdReached = yOffset >= PULL_THRESHOLD

    val scrollConnection = object : NestedScrollConnection {
        override fun onPostScroll(
            consumed: Offset,
            available: Offset,
            source: NestedScrollSource
        ): Offset {
            Logger.d { "onPostScroll: $consumed, $available, $source" }
            if (consumed.y == 0f) {
                val availableDp = with(localDensity) { available.y.toDp() }
                yOffsetPulled = (yOffsetPulled + availableDp).coerceAtLeast(0.dp)
                scope.launch { animatable.snapTo(yOffsetCurved) }
                return Offset.Zero
            }
            return super.onPostScroll(consumed, available, source)
        }

        override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
            Logger.d { "onPreScroll: $available, $source" }
            if (yOffsetPulled > 0.dp) {
                val availableDp = with(localDensity) { available.y.toDp() }
                yOffsetPulled = (yOffsetPulled + availableDp).coerceAtLeast(0.dp)
                scope.launch { animatable.snapTo(yOffsetCurved) }
                return available
            }
            return super.onPreScroll(available, source)
        }
    }

    LaunchedEffect(isUserTouching) {
        if (isUserTouching) {
            animatable.stop()
            yOffsetPulled = animatable.value
            return@LaunchedEffect
        }

        if (isPullThresholdReached) {
            // Start
        }

        animatable.snapTo(yOffsetCurved)
        animatable.animateTo(0.dp, animationSpec = tween())
        yOffsetPulled = 0.dp
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitPointerEvent() // first DOWN event
                    isUserTouching = true
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    isUserTouching = false
                }
            }
            .nestedScroll(scrollConnection)
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(top = yOffset)
        ) content@{
            content()
        }

        val minHeight = 24.dp

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = WindowInsets.safeContent.asPaddingValues().calculateTopPadding() * (yOffset.coerceAtMost(minHeight)/minHeight))
                .height(yOffset.coerceAtLeast(minHeight))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .alpha(yOffset.coerceAtMost(48.dp) / 48.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                val iconRotation by animateFloatAsState(if (isPullThresholdReached) 0f else 180f)
                Icon(
                    painter = painterResource(CoreUiRes.drawable.arrow_up),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(iconRotation),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
                Row {
                    AnimatedContent(
                        targetState = isPullThresholdReached,
                        label = "Pull threshold reached"
                    ) { isPullThresholdReached ->
                        Text(
                            text =
                                if (!isPullThresholdReached) "Ziehe"
                                else "Loslassen"
                            ,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.tertiary,
                        )
                    }
                    Text(
                        text =
                            if (!isPullThresholdReached) " zum Aktualisieren"
                            else ", um zu aktualisieren"
                        ,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.tertiary,
                    )
                }
                Icon(
                    painter = painterResource(CoreUiRes.drawable.arrow_up),
                    contentDescription = null,
                    modifier = Modifier
                        .size(24.dp)
                        .rotate(-iconRotation),
                    tint = MaterialTheme.colorScheme.tertiary,
                )
            }
        }
    }
}
