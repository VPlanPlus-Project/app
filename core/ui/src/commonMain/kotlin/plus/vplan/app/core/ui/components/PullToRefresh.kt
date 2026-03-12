package plus.vplan.app.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.application.AppPlatform
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.theme.AppTheme
import kotlin.math.exp

val PULL_THRESHOLD = 72.dp

@Composable
fun InformativePullToRefresh(
    platform: AppPlatform,
    isRefreshing: Boolean,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
    refreshingContent: @Composable () -> Unit,
    content: @Composable (contentPadding: PaddingValues) -> Unit,
) {
    val localDensity = LocalDensity.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var hideIndicatorHint by remember { mutableStateOf(false) }

    val maxOffsetDp = 192.dp
    val maxOffsetPx = with(localDensity) { maxOffsetDp.toPx() }
    val thresholdPx = with(localDensity) { PULL_THRESHOLD.toPx() }

    val rawPullY = remember { Animatable(0f) }
    val yOffset = with(localDensity) {
        val pull = rawPullY.value.coerceAtLeast(0f)
        (maxOffsetPx * (1f - exp(-pull / maxOffsetPx))).toDp()
    }

    val isPullThresholdReached = yOffset >= PULL_THRESHOLD

    // Haptic Feedback
    LaunchedEffect(isPullThresholdReached) {
        if (isPullThresholdReached && !isRefreshing) {
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        }
    }

    val dampingRatioRefreshingContent = remember(isRefreshing) {
        if (with(localDensity) { yOffset.toPx() } >= thresholdPx + (0.25 * maxOffsetPx - thresholdPx)) Spring.DampingRatioLowBouncy
        else Spring.DampingRatioMediumBouncy
    }

    val dampingRatioContent = remember(isRefreshing) {
        if (with(localDensity) { yOffset.toPx() } < thresholdPx + (0.25 * maxOffsetPx - thresholdPx)) Spring.DampingRatioLowBouncy
        else Spring.DampingRatioMediumBouncy
    }

    // Handle the visual state when refreshing changes externally
    LaunchedEffect(isRefreshing) {
        if (isRefreshing) {
            rawPullY.animateTo(
                targetValue = thresholdPx,
                animationSpec = spring(dampingRatio = dampingRatioContent)
            )
        } else {
            hideIndicatorHint = true
            rawPullY.animateTo(0f)
        }
    }

    val scrollConnection = remember(isRefreshing) { // Add isRefreshing to keys
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                // If refreshing, we consume NOTHING.
                // This allows the scroll to pass through to the content (LazyColumn, etc.)
                if (isRefreshing) return Offset.Zero
                hideIndicatorHint = false

                return if (source == NestedScrollSource.UserInput && available.y < 0 && rawPullY.value > 0) {
                    val newTarget = (rawPullY.value + available.y).coerceAtLeast(0f)
                    scope.launch { rawPullY.snapTo(newTarget) }
                    Offset(0f, available.y)
                } else Offset.Zero
            }

            override fun onPostScroll(consumed: Offset, available: Offset, source: NestedScrollSource): Offset {
                if (isRefreshing) return Offset.Zero
                hideIndicatorHint = false

                if (source == NestedScrollSource.UserInput && available.y > 0) {
                    val newTarget = rawPullY.value + available.y
                    scope.launch { rawPullY.snapTo(newTarget) }
                    return Offset(0f, available.y)
                }
                return Offset.Zero
            }

            override suspend fun onPreFling(available: Velocity): Velocity {
                if (isRefreshing) return Velocity.Zero
                hideIndicatorHint = false

                scope.launch {
                    rawPullY.animateTo(
                        targetValue = 0f,
                        animationSpec = tween(durationMillis = 400)
                    )
                }
                return Velocity.Zero
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .nestedScroll(scrollConnection)
            // Use pointerInput to detect the release (UP event)
            .pointerInput(isRefreshing, isPullThresholdReached) {
                awaitPointerEventScope {
                    while (true) {
                        val event = awaitPointerEvent()
                        hideIndicatorHint = false
                        val allPointersUp = event.changes.all { !it.pressed }

                        if (allPointersUp) {
                            if (isPullThresholdReached && !isRefreshing) {
                                onRefresh()
                            } else if (!isRefreshing) {
                                scope.launch {
                                    rawPullY.animateTo(0f, tween(300))
                                }
                            }
                        }
                    }
                }
            }
            .clipToBounds()
    ) {
        // Content Layer
        Box(Modifier.fillMaxSize()) {
            content(PaddingValues(top = yOffset))
        }

        // Indicator Layer
        val safeTopPadding = WindowInsets.safeContent.asPaddingValues().calculateTopPadding().let { baseTop ->
            if (platform == AppPlatform.iOS) baseTop + 16.dp
            else baseTop
        }
        val actualSafeTopPadding = safeTopPadding * (rawPullY.value / maxOffsetPx).coerceIn(0f, 1f)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(yOffset + safeTopPadding)
                .padding(top = actualSafeTopPadding),
            contentAlignment = Alignment.Center
        ) {
            AnimatedContent(
                targetState = isRefreshing,
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center,
                transitionSpec = {
                    if (isRefreshing) {
                        fadeIn() + scaleIn(spring(dampingRatio = dampingRatioRefreshingContent)) togetherWith fadeOut()
                    } else {
                        fadeIn() togetherWith fadeOut() + scaleOut(spring(dampingRatio = dampingRatioRefreshingContent))
                    }
                }
            ) { isRefreshing ->
                if (isRefreshing) {
                    refreshingContent()
                } else if (!hideIndicatorHint) {
                    PullIndicator(
                        yOffset = yOffset.value,
                        isPullThresholdReached = isPullThresholdReached
                    )
                }
            }
        }
    }
}

@Composable
private fun PullIndicator(
    yOffset: Float,
    isPullThresholdReached: Boolean
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .alpha((yOffset / 48f).coerceIn(0f, 1f))
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
    ) {
        val iconRotation by animateFloatAsState(if (isPullThresholdReached) 0f else 180f)
        Icon(
            painter = painterResource(CoreUiRes.drawable.arrow_up),
            contentDescription = null,
            modifier = Modifier.size(24.dp).rotate(iconRotation),
            tint = MaterialTheme.colorScheme.tertiary,
        )
        Row {
            AnimatedContent(targetState = isPullThresholdReached) { reached ->
                Text(
                    text = if (!reached) "Ziehen" else "Loslassen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
            Text(
                text = " zum Aktualisieren",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
        Icon(
            painter = painterResource(CoreUiRes.drawable.arrow_up),
            contentDescription = null,
            modifier = Modifier.size(24.dp).rotate(-iconRotation),
            tint = MaterialTheme.colorScheme.tertiary,
        )
    }
}

@Preview
@Composable
private fun PullIndicatorPreviewPreThreshold() {
    AppTheme(dynamicColor = false) {
        val localDensity = LocalDensity.current
        PullIndicator(
            yOffset = with(localDensity) { PULL_THRESHOLD.toPx() } / 2,
            isPullThresholdReached = false
        )
    }
}

@Preview
@Composable
private fun PullIndicatorPreviewPostThreshold() {
    AppTheme(dynamicColor = false) {
        val localDensity = LocalDensity.current
        PullIndicator(
            yOffset = with(localDensity) { PULL_THRESHOLD.toPx() },
            isPullThresholdReached = true
        )
    }
}