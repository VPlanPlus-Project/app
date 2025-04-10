package plus.vplan.app.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.VectorConverter
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.coroutines.launch
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.abs
import plus.vplan.app.utils.ifNan
import plus.vplan.app.utils.roundToNearest
import plus.vplan.app.utils.sech
import plus.vplan.app.utils.transparent
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.tanh

@Composable
fun FullscreenDrawer(
    contentScrollState: ScrollState,
    onDismissRequest: () -> Unit,
    topAppBar: @Composable (onCloseClicked: () -> Unit, modifier: Modifier, scrollProgress: Float) -> Unit = { _, _, _ -> },
    content: @Composable FullscreenDrawerContext.() -> Unit
) {
    var maxHeight by remember { mutableStateOf(0.dp) }
    val localDensity = LocalDensity.current
    val scope = rememberCoroutineScope()
    var firstAnimationDone by remember { mutableStateOf(false) }
    val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current

    val offset = remember { Animatable(0.dp, Dp.VectorConverter) }
    var isUserScrolling by remember { mutableStateOf(false) }

    val snapOffset = remember {
        {
            val height = with(localDensity) { maxHeight.toPx() }
            ((with(localDensity) { offset.value.toPx() }) roundToNearest listOf(
                0f,
                height,
                2 * height
            )).let {
                scope.launch {
                    offset.animateTo(with(localDensity) { it.toDp() })
                }
            }
        }
    }

    LaunchedEffect(offset.value) {
        if (!firstAnimationDone) return@LaunchedEffect
        if (offset.value < 10.dp || offset.value > (2 * maxHeight) - 10.dp) onDismissRequest()
    }

    LaunchedEffect(contentScrollState.isScrollInProgress) {
        if (!contentScrollState.isScrollInProgress && isUserScrolling) snapOffset()
        isUserScrolling = contentScrollState.isScrollInProgress
    }

    val scrollConnection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isContentAtTop = contentScrollState.value == 0
                val isContentAtBottom = contentScrollState.value == contentScrollState.maxValue
                if (isContentAtTop && available.y >= 0) { // Scroll down
                    localSoftwareKeyboardController?.hide()
                    scope.launch {
                        offset.snapTo(offset.value - with(localDensity) { available.y.toDp() })
                    }
                    return Offset(0f, available.y)
                }
                if (available.y <= 0 && offset.value > 0.dp && isContentAtBottom) { // Scroll down
                    localSoftwareKeyboardController?.hide()
                    scope.launch {
                        offset.snapTo(offset.value - with(localDensity) { available.y.toDp() })
                    }
                    return Offset(0f, available.y)
                }
                if (!isContentAtBottom && offset.value != maxHeight) {
                    scope.launch {
                        offset.animateTo(offset.value - with(localDensity) { available.y.toDp() })
                    }
                    return Offset(0f, available.y)
                }
                return super.onPreScroll(Offset.Zero, source)
            }
        }
    }

    val scrollProgress = (-abs(offset.value - maxHeight) + maxHeight) / maxHeight
    var horizontalOffset by remember { mutableStateOf(0.dp) }
    BackHandler(
        onProgress = { progress ->
            scope.launch {
                offset.snapTo(maxHeight * (1 - abs(progress) / 12))
            }
            horizontalOffset = progress * 16.dp
        },
        onStart = { isUserScrolling = true },
        onEnd = { isUserScrolling = false },
    ) {
        scope.launch {
            offset.animateTo(0.dp)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .thenIf(Modifier.background(Color.Black.transparent((tanh(6 * (1 - scrollProgress) - 4) / -8) + 0.125f))) { maxHeight != 0.dp }
            .noRippleClickable { scope.launch { offset.animateTo(maxHeight) } }
            .onSizeChanged {
                maxHeight = with(localDensity) { it.height.toDp() }
                scope.launch {
                    if (firstAnimationDone) offset.snapTo(maxHeight)
                    else offset.animateTo(maxHeight)
                }.invokeOnCompletion { firstAnimationDone = true }
            }
    ) {
        if (maxHeight == 0.dp) return@Box
        Column(
            modifier = Modifier
                .offset { with (localDensity) { IntOffset(horizontalOffset.roundToPx(), (maxHeight - offset.value).roundToPx()) } }
                .fillMaxSize()
                .scale(((1 - (((1.05 * sech(4.0 * scrollProgress).toFloat().ifNan { 0f }) - 0.05) / 6)).toFloat()).coerceIn(0f, 1f))
                .clip(RoundedCornerShape((sin((1 - scrollProgress) * PI / 2).ifNan { 0.0 } * 32.dp).coerceAtLeast(0.dp)))
                .nestedScroll(scrollConnection),
        ) {
            topAppBar(
                { scope.launch { offset.animateTo(0.dp) } },
                Modifier
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { isUserScrolling = true },
                            onDragEnd = { isUserScrolling = false; snapOffset() },
                            onDragCancel = { isUserScrolling = false; snapOffset() },
                        ) { _, dragAmount ->
                            val y = (with(localDensity) { dragAmount.toDp() })
                            scope.launch {
                                offset.snapTo(offset.value - y)
                            }
                        }
                    },
                scrollProgress
            )
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(
                        start = WindowInsets.safeDrawing.asPaddingValues().calculateStartPadding(LocalLayoutDirection.current),
                        end = WindowInsets.safeDrawing.asPaddingValues().calculateEndPadding(LocalLayoutDirection.current),
                        bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding()
                    )
            ) {
                FullscreenDrawerContext(
                    scrollState = contentScrollState,
                    hideDrawer = onDismissRequest
                ) { scope.launch { offset.animateTo(0.dp) } }.content()
            }
        }
    }
}

data class FullscreenDrawerContext(
    val scrollState: ScrollState,
    val hideDrawer: () -> Unit,
    val closeDrawerWithAnimation: () -> Unit
)