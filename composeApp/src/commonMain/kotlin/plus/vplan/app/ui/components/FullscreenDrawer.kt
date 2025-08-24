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
import androidx.compose.foundation.layout.ime
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
import androidx.compose.runtime.saveable.rememberSaveable
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
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import co.touchlab.kermit.Logger
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

private val FLING_THRESHOLD = 1000.dp // Threshold for fling to trigger drawer movement

@Composable
fun FullscreenDrawer(
    contentScrollState: ScrollState,
    preventClosingByGesture: Boolean,
    onDismissRequest: () -> Unit,
    topAppBar: @Composable (onCloseClicked: () -> Unit, modifier: Modifier, scrollProgress: Float) -> Unit = { _, _, _ -> },
    content: @Composable (context: FullscreenDrawerContext) -> Unit
) {
    val confirmationPadding = 400.dp
    
    val localDensity = LocalDensity.current
    val scope = rememberCoroutineScope()
    val localSoftwareKeyboardController = LocalSoftwareKeyboardController.current

    /**
     * Tracks the height of the drawer. This is used to calculate the offset of the drawer.
     */
    var maxHeight by remember { mutableStateOf(0.dp) }

    var isCloseRequestHeld by rememberSaveable(preventClosingByGesture) { mutableStateOf(false) }

    /**
     * Tracks whether the opening animation of the drawer has been completed.
     */
    var firstAnimationDone by remember { mutableStateOf(false) }

    /**
     * Tracks the current vertical offset of the drawer. 0.dp means the drawer is fully closed and
     * has its bottom edge at the top of the screen, while 2 * maxHeight means the drawer is fully
     * closed as well, but has its top edge at the bottom of the screen. maxHeight means the
     * drawer is fully open and has its top edge at the top of the screen.
     */
    val drawerOffset = remember { Animatable(0.dp, Dp.VectorConverter) }

    /**
     * Tracks whether the user is currently scrolling the content inside the drawer.
     */
    var isUserScrolling by remember { mutableStateOf(false) }

    /**
     * Disable the snapping behavior of the drawer temporarily. Always reset to false if the
     * operation that disabled it is done.
     */
    var disableSnapping by remember { mutableStateOf(false) }

    // LaunchedEffect für preventClosingByGesture nach der State-Initialisierung
    LaunchedEffect(preventClosingByGesture) {
        Logger.d { "Prevent closing by gesture: $preventClosingByGesture" }
        isCloseRequestHeld = false
        firstAnimationDone = false
        scope.launch {
            drawerOffset.snapTo(maxHeight)
        }.invokeOnCompletion { firstAnimationDone = true }
    }

    // If the drawer is moved outside of the screen, close it unless the drawer is being opened.
    LaunchedEffect(drawerOffset.value) {
        if (!firstAnimationDone) return@LaunchedEffect
        if (!preventClosingByGesture || !isCloseRequestHeld) {
            if (drawerOffset.value < 10.dp || drawerOffset.value > (2 * maxHeight) - 10.dp) onDismissRequest()
        }
    }

    val setDrawerOffset = remember { { value: Dp ->
        scope.launch {
            drawerOffset.snapTo(value)
        }
    } }

    val moveDrawerToUpperBound = remember(preventClosingByGesture) {
        { initialVelocity: Dp ->
            scope.launch {
                disableSnapping = true
                if (preventClosingByGesture && !isCloseRequestHeld) {
                    isCloseRequestHeld = true
                    drawerOffset.animateTo((maxHeight * 2) - confirmationPadding, initialVelocity = initialVelocity)
                }
                else {
                    isCloseRequestHeld = false
                    drawerOffset.animateTo(maxHeight * 2, initialVelocity = initialVelocity)
                }
                localSoftwareKeyboardController?.hide()
                disableSnapping = false
            }
        }
    }

    val moveDrawerToLowerBound = remember(preventClosingByGesture) {
        { initialVelocity: Dp ->
            scope.launch {
                disableSnapping = true
                if (preventClosingByGesture && !isCloseRequestHeld) {
                    isCloseRequestHeld = true
                    drawerOffset.animateTo(confirmationPadding, initialVelocity = initialVelocity)
                }
                else {
                    isCloseRequestHeld = false
                    drawerOffset.animateTo(0.dp, initialVelocity = initialVelocity)
                }
                localSoftwareKeyboardController?.hide()
                disableSnapping = false
            }
        }
    }

    val moveDrawerToCenter = remember(preventClosingByGesture) {
        { initialVelocity: Dp ->
            scope.launch {
                disableSnapping = true
                drawerOffset.animateTo(maxHeight, initialVelocity = initialVelocity)
                localSoftwareKeyboardController?.hide()
                disableSnapping = false
            }
        }
    }

    /**
     * Function that snaps the drawer to the nearest position based on its current offset.
     * Either on the top edge of the screen (0.dp), the center of the screen (maxHeight),
     * or the bottom edge of the screen (2 * maxHeight).
     * This function is called when the user stops scrolling the content inside the drawer.
     */
    val snapOffset = remember(preventClosingByGesture) {
        {
            val height = with(localDensity) { maxHeight.toPx() }
            ((with(localDensity) { drawerOffset.value.toPx() }) roundToNearest listOf(
                0f,
                height,
                2 * height
            )).let {
                scope.launch {
                    if (preventClosingByGesture) {
                        when (it) {
                            0f -> moveDrawerToUpperBound(0.dp)
                            2 * height -> moveDrawerToLowerBound(0.dp)
                            else -> moveDrawerToCenter(0.dp)
                        }
                        return@launch
                    }
                    drawerOffset.animateTo(with(localDensity) { it.toDp() })
                }
            }
        }
    }

    LaunchedEffect(contentScrollState.isScrollInProgress) {
        if (disableSnapping) return@LaunchedEffect
        if (!contentScrollState.isScrollInProgress && isUserScrolling) snapOffset()
        isUserScrolling = contentScrollState.isScrollInProgress
    }

    val scrollConnection = remember(preventClosingByGesture) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isContentAtTop = contentScrollState.value == 0
                val isContentAtBottom = contentScrollState.value == contentScrollState.maxValue
                val verticalScrollDirection = if (available.y > 0) VerticalScrollDirection.Down else VerticalScrollDirection.Up

                val scrollDistance = with(localDensity) { available.y.toDp() }

                if (verticalScrollDirection == VerticalScrollDirection.Up && (isContentAtBottom || drawerOffset.value != maxHeight)) {
                    // move drawer up
                    setDrawerOffset(drawerOffset.value - scrollDistance)
                    return Offset(0f, available.y)
                }

                if (verticalScrollDirection == VerticalScrollDirection.Down && (isContentAtTop || drawerOffset.value != maxHeight)) {
                    // move drawer down
                    setDrawerOffset(drawerOffset.value - scrollDistance)
                    return Offset(0f, available.y)
                }

                // scroll content
                return super.onPreScroll(available, source)
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                val isContentAtTop = contentScrollState.value == 0
                val isContentAtBottom = contentScrollState.value == contentScrollState.maxValue
                val verticalScrollDirection = if (available.y > 0) VerticalScrollDirection.Down else VerticalScrollDirection.Up
                val verticalScrollDistancePerSecond = with(localDensity) { available.y.toDp() }

                if (abs(verticalScrollDistancePerSecond) < FLING_THRESHOLD) return super.onPreFling(available)

                if (verticalScrollDirection == VerticalScrollDirection.Up && isContentAtTop) {
                    // fling drawer up
                    moveDrawerToUpperBound(verticalScrollDistancePerSecond)
                    return Velocity(0f, available.y)
                }

                if (verticalScrollDirection == VerticalScrollDirection.Down && isContentAtBottom) {
                    // fling drawer down
                    moveDrawerToLowerBound(verticalScrollDistancePerSecond)
                    return Velocity(0f, available.y)
                }

                // fling content
                return super.onPreFling(available)
            }
        }
    }

    val scrollProgress = (-abs(drawerOffset.value - maxHeight) + maxHeight) / maxHeight
    var horizontalOffset by remember { mutableStateOf(0.dp) }
    BackHandler(
        onProgress = { progress ->
            scope.launch {
                drawerOffset.snapTo(maxHeight * (1 - abs(progress) / 12))
            }
            horizontalOffset = progress * 16.dp
        },
        onStart = { isUserScrolling = true },
        onEnd = { isUserScrolling = false },
    ) {
        moveDrawerToLowerBound(0.dp)
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .thenIf(Modifier.background(Color.Black.transparent((tanh(6 * (1 - scrollProgress) - 4) / -8) + 0.125f))) { maxHeight != 0.dp }
            .noRippleClickable { scope.launch { drawerOffset.animateTo(maxHeight) } }
            .onSizeChanged {
                maxHeight = with(localDensity) { it.height.toDp() }
                scope.launch {
                    if (firstAnimationDone) drawerOffset.snapTo(maxHeight)
                    else drawerOffset.animateTo(maxHeight)
                }.invokeOnCompletion { firstAnimationDone = true }
            }
    ) {
        if (maxHeight == 0.dp) return@Box
        Column(
            modifier = Modifier
                .offset { with (localDensity) { IntOffset(horizontalOffset.roundToPx(), (maxHeight - drawerOffset.value).roundToPx()) } }
                .fillMaxSize()
                .scale(((1 - (((1.05 * sech(4.0 * scrollProgress).toFloat().ifNan { 0f }) - 0.05) / 6)).toFloat()).coerceIn(0f, 1f))
                .nestedScroll(scrollConnection),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(
                        bottom = WindowInsets.ime.asPaddingValues().calculateBottomPadding()
                    )
                    .clip(RoundedCornerShape((sin((1 - scrollProgress) * PI / 2).ifNan { 0.0 } * 32.dp).coerceAtLeast(0.dp)))
                    .background(MaterialTheme.colorScheme.surface)
                    .padding(
                        start = WindowInsets.safeDrawing.asPaddingValues().calculateStartPadding(LocalLayoutDirection.current),
                        end = WindowInsets.safeDrawing.asPaddingValues().calculateEndPadding(LocalLayoutDirection.current),
                        bottom = (WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() - WindowInsets.ime.asPaddingValues().calculateBottomPadding()).coerceAtLeast(0.dp)
                    )
            ) {
                val dragModifier = remember { Modifier
                    .pointerInput(Unit) {
                        detectVerticalDragGestures(
                            onDragStart = { isUserScrolling = true },
                            onDragEnd = { isUserScrolling = false; snapOffset() },
                            onDragCancel = { isUserScrolling = false; snapOffset() },
                        ) { _, dragAmount ->
                            val y = (with(localDensity) { dragAmount.toDp() })
                            scope.launch {
                                drawerOffset.snapTo(drawerOffset.value - y)
                            }
                        }
                    } }
                topAppBar(
                    { moveDrawerToLowerBound(0.dp) },
                    dragModifier,
                    scrollProgress
                )
                content(FullscreenDrawerContext(
                    scrollState = contentScrollState,
                    hideDrawer = { moveDrawerToLowerBound(0.dp) },
                    dragModifier = dragModifier,
                    isCloseRequestHeld = isCloseRequestHeld,
                    resetCloseRequest = {
                        isCloseRequestHeld = false
                        moveDrawerToCenter(0.dp)
                        horizontalOffset = 0.dp
                    },
                ) { scope.launch { drawerOffset.animateTo(0.dp) } })
            }
        }
    }
}

data class FullscreenDrawerContext(
    val scrollState: ScrollState,
    val dragModifier: Modifier,
    val hideDrawer: () -> Unit,
    val isCloseRequestHeld: Boolean,
    val resetCloseRequest: () -> Unit,
    val closeDrawerWithAnimation: () -> Unit
)

/**
 * Follows natural scrolling behavior.
 */
private enum class VerticalScrollDirection {
    Up,
    Down
}
