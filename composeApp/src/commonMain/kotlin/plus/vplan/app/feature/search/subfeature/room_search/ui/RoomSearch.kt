package plus.vplan.app.feature.search.subfeature.room_search.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.coerceAtMost
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavHostController
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.cache.collectAsSingleFlow
import plus.vplan.app.feature.search.subfeature.room_search.domain.usecase.Occupancy
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.minusWithCapAtMidnight
import plus.vplan.app.utils.plusWithCapAtMidnight
import plus.vplan.app.utils.regularTimeFormat
import plus.vplan.app.utils.until
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import kotlin.math.PI
import kotlin.math.cos
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomSearch(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<RoomSearchViewModel>()
    val state by viewModel.state.collectAsState()
    val localDensity = LocalDensity.current

    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Freien Raum finden") },
                navigationIcon = {
                    IconButton(onClick = navHostController::navigateUp) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_left),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehaviour
            )
        }
    ) { contentPadding ->
        AnimatedContent(
            targetState = state.initDone,
            modifier = Modifier
                .fillMaxSize()
        ) { initDone ->
            if (!initDone) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@AnimatedContent
            }
            Column(Modifier.fillMaxSize()) {
                Text(
                    modifier = Modifier.padding(8.dp),
                    text = "Tippe auf eine Stunde in der Kopfzeile, um nach Räumen zu suchen, die in dieser Stunde frei sind.",
                    style = MaterialTheme.typography.bodySmall
                )
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val roomNameColumnWidth = 48.dp
                    val lineHeight = 32.dp
                    val headerHeight = 52.dp
                    val hourWidth = 64.dp

                    val uiStart = state.startTime?.time?.minusWithCapAtMidnight(1.hours) ?: LocalTime(0, 0)
                    val uiEnd = state.endTime?.time?.plusWithCapAtMidnight(1.hours) ?: LocalTime(23, 59)

                    val bodyWidth = uiStart.until(uiEnd).inWholeMinutes.toFloat() * hourWidth / 60

                    val currentTimeXOffset = uiStart.until(state.currentTime.time).inWholeMinutes.toFloat() * hourWidth / 60
                    val horizontalScrollState = rememberScrollState(with(localDensity) { (currentTimeXOffset - hourWidth).coerceAtMost(0.dp).roundToPx() })

                    val timeDivider: @Composable () -> Unit = {
                        state.lessonTimes.keys.forEach { lessonTime ->
                            val startOffset = uiStart.until(lessonTime.start).inWholeMinutes.toFloat() * hourWidth / 60
                            val endOffset = uiStart.until(lessonTime.end).inWholeMinutes.toFloat() * hourWidth / 60
                            val color = MaterialTheme.colorScheme.outline
                            Canvas(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                                    .offset(x = startOffset)
                            ) {
                                drawLine(
                                    color = color,
                                    start = Offset(x = 0.5.dp.toPx(), y = 0f),
                                    end = Offset(x = 0.5.dp.toPx(), y = size.height),
                                    cap = StrokeCap.Round,
                                    strokeWidth = 0.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        intervals = floatArrayOf(
                                            0f,
                                            2.dp.toPx()
                                        )
                                    )
                                )
                            }
                            Canvas(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(1.dp)
                                    .offset(x = endOffset)
                            ) {
                                drawLine(
                                    color = color,
                                    start = Offset(x = 0.5.dp.toPx(), y = 0f),
                                    end = Offset(x = 0.5.dp.toPx(), y = size.height),
                                    cap = StrokeCap.Round,
                                    strokeWidth = 0.5.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(
                                        intervals = floatArrayOf(
                                            0f,
                                            2.dp.toPx()
                                        )
                                    )
                                )
                            }
                        }

                        VerticalDivider(
                            color = colors[CustomColor.Red]!!.getGroup().color,
                            modifier = Modifier.offset(x = currentTimeXOffset)
                        )
                    }

                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .nestedScroll(scrollBehaviour.nestedScrollConnection)
                            .padding(contentPadding)
                            .padding(top = headerHeight)
                    ) map@{
                        Column {
                            state.rooms.forEach { occupancyMapRecord ->
                                AnimatedVisibility(
                                    visible = state.lessonTimes.filter { it.value }.keys.all { occupancyMapRecord.isAvailableAtLessonTime(it) },
                                    enter = expandVertically(animationSpec = tween(), expandFrom = Alignment.CenterVertically),
                                    exit = shrinkVertically(animationSpec = tween(), shrinkTowards = Alignment.CenterVertically)
                                ) {
                                    Column(Modifier.width(roomNameColumnWidth)) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(lineHeight),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = occupancyMapRecord.room.name,
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        HorizontalDivider()
                                    }
                                }
                            }
                        }
                        Column (
                            modifier = Modifier
                                .fillMaxSize()
                                .horizontalScroll(horizontalScrollState)
                        ) {
                            state.rooms.forEach { occupancyMapRecord ->
                                AnimatedVisibility(
                                    visible = state.lessonTimes.filter { it.value }.keys.all { occupancyMapRecord.isAvailableAtLessonTime(it) },
                                    enter = expandVertically(animationSpec = tween(), expandFrom = Alignment.CenterVertically),
                                    exit = shrinkVertically(animationSpec = tween(), shrinkTowards = Alignment.CenterVertically)
                                ) {
                                    Column {
                                        Box(
                                            modifier = Modifier
                                                .width(bodyWidth)
                                                .height(lineHeight)
                                        ) {
                                            timeDivider()

                                            occupancyMapRecord.occupancies.forEach { occupancy ->
                                                val startOffset = uiStart.until(occupancy.start.time).inWholeMinutes.toFloat() * hourWidth / 60
                                                val width = occupancy.start.time.until(occupancy.end.time).inWholeMinutes.toFloat() * hourWidth / 60
                                                Box(
                                                    modifier = Modifier
                                                        .offset(x = startOffset, y = 2.dp)
                                                        .height(lineHeight - 4.dp)
                                                        .width(width)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                                                        when (occupancy) {
                                                            is Occupancy.Lesson -> {
                                                                val groups by remember(occupancy.lesson.groupIds)  { occupancy.lesson.groups }.collectAsSingleFlow()
                                                                Text(groups.joinToString { it.name })
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        HorizontalDivider(Modifier.width(uiStart.until(uiEnd).inWholeMinutes.toFloat() * hourWidth / 60))
                                    }
                                }
                            }
                        }
                    }

                    Box(
                        modifier = Modifier
                            .height(headerHeight)
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = .7f))
                            .padding(start = roomNameColumnWidth)
                            .horizontalScroll(horizontalScrollState)
                    ) header@{
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(bodyWidth)
                        ) {

                            state.lessonTimes.forEach { (lessonTime, selected) ->
                                val startOffset = uiStart.until(lessonTime.start).inWholeMinutes.toFloat() * hourWidth / 60
                                val width = lessonTime.start.until(lessonTime.end).inWholeMinutes.toFloat() * hourWidth / 60
                                Box(
                                    modifier = Modifier
                                        .offset(x = startOffset, y = 4.dp)
                                        .width(width)
                                        .clip(RoundedCornerShape(8.dp))
                                        .clickable { viewModel.onEvent(RoomSearchEvent.ToggleLessonTimeSelection(lessonTime)) }
                                ) {
                                    AnimatedContent(
                                        targetState = selected,
                                        modifier = Modifier.fillMaxSize()
                                    ) { displaySelected ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .thenIf(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) { displaySelected },
                                            contentAlignment = Alignment.Center
                                        ) {
                                            CompositionLocalProvider(LocalContentColor provides if (displaySelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface) {
                                                Text(
                                                    text = buildString {
                                                        append(lessonTime.lessonNumber)
                                                        append(".\n")
                                                        append(lessonTime.start.format(regularTimeFormat))
                                                        append(" -\n")
                                                        append(lessonTime.end.format(regularTimeFormat))
                                                    },
                                                    style = MaterialTheme.typography.labelSmall,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            timeDivider()
                            Box(
                                modifier = Modifier
                                    .offset(x = currentTimeXOffset + 2.dp, y = 2.dp)
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(colors[CustomColor.Red]!!.getGroup().container)
                                    .padding(2.dp)
                            ) {
                                CompositionLocalProvider(LocalContentColor provides colors[CustomColor.Red]!!.getGroup().onContainer) {
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        val blinkTransition = rememberInfiniteTransition(label = "blink")
                                        val alpha by blinkTransition.animateFloat(
                                            initialValue = 1f,
                                            targetValue = 0f,
                                            animationSpec = infiniteRepeatable(
                                                animation = tween(750, easing = { x -> -cos(PI/2 * x).toFloat() + 1 }),
                                                repeatMode = RepeatMode.Reverse
                                            )
                                        )
                                        Text(
                                            text = state.currentTime.time.hour.toString().padStart(2, '0'),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                        Text(
                                            text = ":",
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
                                            modifier = Modifier.alpha(alpha)
                                        )
                                        Text(
                                            text = state.currentTime.time.minute.toString().padStart(2, '0'),
                                            style = MaterialTheme.typography.labelMedium
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}