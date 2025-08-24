package plus.vplan.app.feature.grades.page.analytics.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeContent
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.feature.grades.page.view.ui.components.SelectIntervalDrawer
import plus.vplan.app.ui.animatePlacement
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.utils.blendColor
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import vplanplus.composeapp.generated.resources.chevron_down
import vplanplus.composeapp.generated.resources.filter
import vplanplus.composeapp.generated.resources.x

@Composable
fun AnalyticsScreen(
    navHostController: NavHostController,
    vppIdId: Int
) {
    val viewModel = koinViewModel<AnalyticsViewModel>()
    val state = viewModel.state

    LaunchedEffect(vppIdId) { viewModel.init(vppIdId) }

    AnalyticsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = navHostController::navigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
private fun AnalyticsContent(
    state: AnalyticsState,
    onEvent: (event: AnalyticsAction) -> Unit,
    onBack: () -> Unit
) {
    val topScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    var showFilterDrawer by rememberSaveable { mutableStateOf(false) }

    val red = colors[CustomColor.Red]!!.getGroup()
    val green = colors[CustomColor.Green]!!.getGroup()

    var showIntervalFilterDrawer by rememberSaveable { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Notenanalyse") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_left),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showIntervalFilterDrawer = true }) {
                        Icon(
                            painter = painterResource(Res.drawable.filter),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                scrollBehavior = topScrollBehavior
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column gradesByGrade@{
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Notenverteilung",
                        style = MaterialTheme.typography.titleMedium
                    )
                    AnimatedContent(
                        targetState = state.filteredSubjects.isNotEmpty(),
                        transitionSpec = { fadeIn() togetherWith fadeOut() }
                    ) { isFilterActive ->
                        FilterChip(
                            selected = isFilterActive,
                            onClick = { showFilterDrawer = true },
                            label = { Text("Filter") },
                            leadingIcon = { Icon(painter = painterResource(Res.drawable.filter), contentDescription = null, modifier = Modifier.size(18.dp)) },
                            trailingIcon = { Icon(painter = painterResource(Res.drawable.chevron_down), contentDescription = null, modifier = Modifier.size(18.dp)) },
                        )
                    }
                }
                if (state.interval == null) return@gradesByGrade
                val map = remember(state.filteredGrades) {
                    var max = 0
                    when (state.interval.type) {
                        is Interval.Type.Sek2 -> (0..15).toList().associateWith { grade -> state.filteredGrades.count { it.numericValue == grade } }
                        else -> (1..6).toList().associateWith { grade -> state.filteredGrades.count { it.numericValue == grade } }
                    }
                        .also { max = it.maxOf { gradeByGrade -> gradeByGrade.value } }
                        .map {
                            GradesByGrade(
                                grades = it.value,
                                grade = it.key,
                                percentage = (it.value.toFloat() / max).let { percentage -> if (percentage.isNaN()) 0f else percentage }
                            )
                        }
                    }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(256.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    map.toList().forEachIndexed { i, category ->
                        val percentageAnimation by animateFloatAsState(
                            targetValue = category.percentage,
                            animationSpec = tween(durationMillis = 200, delayMillis = 10*i)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(percentageAnimation)
                                .clip(RoundedCornerShape(8.dp, 8.dp, 4.dp, 4.dp))
                                .background(when (state.interval.type) {
                                    is Interval.Type.Sek2 -> blendColor(red.container, green.container, i/15f)
                                    else -> blendColor(green.container, red.container, (i-1)/5f)
                                })
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    map.toList().forEach { category ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = category.grade.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = category.grades.toString() + "x",
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(8.dp))
//            Column gradesOverTime@{
//                Row(
//                    modifier = Modifier
//                        .padding(horizontal = 16.dp)
//                        .fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        text = "Notenverlauf",
//                        style = MaterialTheme.typography.titleMedium
//                    )
//                    AnimatedContent(
//                        targetState = state.filteredSubjects.isNotEmpty(),
//                        transitionSpec = { fadeIn() togetherWith fadeOut() }
//                    ) { isFilterActive ->
//                        FilterChip(
//                            selected = isFilterActive,
//                            onClick = { showFilterDrawer = true },
//                            label = { Text("Filter") },
//                            leadingIcon = { Icon(painter = painterResource(Res.drawable.filter), contentDescription = null, modifier = Modifier.size(18.dp)) },
//                            trailingIcon = { Icon(painter = painterResource(Res.drawable.chevron_down), contentDescription = null, modifier = Modifier.size(18.dp)) },
//                        )
//                    }
//                }
//                SingleChoiceSegmentedButtonRow(
//                    modifier = Modifier
//                        .padding(horizontal = 16.dp)
//                        .align(Alignment.End),
//                ) {
//                    SegmentedButton(
//                        selected = state.timeType == AnalyticsTimeType.Average,
//                        icon = {
//                            Icon(
//                                painter = painterResource(Res.drawable.circle_slash_2),
//                                contentDescription = null,
//                                modifier = Modifier.size(18.dp)
//                            )
//                        },
//                        label = { Text(text = "Durchschnitt") },
//                        onClick = { onEvent(AnalyticsAction.SetTimeType(AnalyticsTimeType.Average)) },
//                        shape = RoundedCornerShape(50, 0, 0, 50)
//                    )
//                    SegmentedButton(
//                        selected = state.timeType == AnalyticsTimeType.Value,
//                        icon = {
//                            Icon(
//                                painter = painterResource(Res.drawable.list_ordered),
//                                contentDescription = null,
//                                modifier = Modifier.size(18.dp)
//                            )
//                        },
//                        label = { Text(text = "Einzelnoten") },
//                        onClick = { onEvent(AnalyticsAction.SetTimeType(AnalyticsTimeType.Value)) },
//                        shape = RoundedCornerShape(0, 50, 50, 0)
//                    )
//                }
//                Spacer(Modifier.height(8.dp))
//                val lineHeight = 24.dp
//                if (state.interval != null) Row {
//                    Column(
//                        modifier = Modifier
//                            .padding(start = 16.dp, end = 8.dp)
//                            .width(48.dp),
//                        horizontalAlignment = Alignment.CenterHorizontally
//                    ) {
//                        when (state.interval.type) {
//                            Interval.Type.SEK1 -> {
//                                repeat(6) {
//                                    Box(
//                                        modifier = Modifier
//                                            .height(lineHeight),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Text(
//                                            text = (it+1).toString(),
//                                            style = MaterialTheme.typography.labelSmall,
//                                        )
//                                    }
//                                }
//                            }
//                            Interval.Type.SEK2 -> {
//                                repeat(16) {
//                                    Box(
//                                        modifier = Modifier
//                                            .height(lineHeight),
//                                        contentAlignment = Alignment.Center
//                                    ) {
//                                        Text(
//                                            text = (15-it).toString(),
//                                            style = MaterialTheme.typography.labelSmall,
//                                        )
//                                    }
//                                }
//                            }
//                        }
//                    }
//                    val theme = MaterialTheme.colorScheme
//                    Canvas(
//                        modifier = Modifier
//                            .padding(end = 16.dp)
//                            .fillMaxWidth()
//                            .height(
//                                when (state.interval.type) {
//                                    Interval.Type.SEK1 -> lineHeight * 6
//                                    Interval.Type.SEK2 -> lineHeight * 16
//                                }
//                            )
//                    ) {
//                        val start = state.interval.from
//                        val to = state.interval.to
//                        when (state.interval.type) {
//                            Interval.Type.SEK1 -> {
//                                repeat(6) {
//                                    drawLine(
//                                        color = theme.outline,
//                                        start = Offset(x = 0f, y = ((lineHeight / 2) + (it * lineHeight)).toPx()),
//                                        end = Offset(x = size.width, y = ((lineHeight / 2) + (it * lineHeight)).toPx()),
//                                        strokeWidth = 2.dp.toPx()
//                                    )
//                                }
//                            }
//                            Interval.Type.SEK2 -> {
//                                repeat(16) {
//                                    drawLine(
//                                        color = theme.outline,
//                                        start = Offset(x = 0f, y = ((lineHeight / 2) + (15 - it) * lineHeight).toPx()),
//                                        end = Offset(x = size.width, y = ((lineHeight / 2) + (15 - it) * lineHeight).toPx()),
//                                        strokeWidth = 2.dp.toPx()
//                                    )
//                                }
//                            }
//                        }
//                        var date = start
//                        fun x(date: LocalDate): Float = (date progressIn start..to).toFloat() * size.width
//                        fun y(grade: Double): Float = when (state.interval.type) {
//                            Interval.Type.SEK1 -> ((lineHeight / 2) + (grade * lineHeight)).toPx()
//                            Interval.Type.SEK2 -> ((lineHeight / 2) + ((15-grade) * lineHeight)).toPx()
//                        }
//
//                        state.timeDataPoints.forEachIndexed { i, data ->
//                            val x = x(data.date)
//                            val y = y(data.value)
//
//                            if (date < data.date && i > 0) {
//                                val previous = state.timeDataPoints.filterIndexed { index, it -> index < i && it.subjectId == data.subjectId }.lastOrNull()
//                                if (previous != null) {
//                                    val x2 = x(previous.date)
//                                    val y2 = y(previous.value)
//                                    drawLine(
//                                        color = theme.outline,
//                                        start = Offset(x = x2, y = y2),
//                                        end = Offset(x = x, y = y),
//                                        strokeWidth = .5.dp.toPx()
//                                    )
//                                }
//                            }
//
//                            drawCircle(
//                                color = when (state.interval.type) {
//                                    Interval.Type.SEK1 -> blendColor(green.container, red.container, (data.value-1).toFloat()/5f)
//                                    Interval.Type.SEK2 -> blendColor(red.container, green.container, data.value.toFloat()/15f)
//                                },
//                                radius = 4.dp.toPx(),
//                                center = Offset(x, y)
//                            )
//                            date = data.date
//                        }
//                    }
//                }
//
//                Row(
//                    modifier = Modifier
//                        .padding(horizontal = 16.dp)
//                        .fillMaxWidth(),
//                    verticalAlignment = Alignment.CenterVertically,
//                    horizontalArrangement = Arrangement.SpaceBetween
//                ) {
//                    Text(
//                        text = state.interval?.from?.format(regularDateFormat) ?: "-",
//                        style = MaterialTheme.typography.labelSmall
//                    )
//                    Text(
//                        text = state.interval?.to?.format(regularDateFormat) ?: "-",
//                        style = MaterialTheme.typography.labelSmall
//                    )
//                }
//            }
        }
    }

    if (showIntervalFilterDrawer) SelectIntervalDrawer(
        intervals = state.intervals,
        selectedInterval = state.interval,
        onDismiss = { showIntervalFilterDrawer = false },
        onClickInterval = { onEvent(AnalyticsAction.SetInterval(it)) }
    )

    if (showFilterDrawer) {
        val sheetState = rememberModalBottomSheetState(true)
        ModalBottomSheet(
            sheetState = sheetState,
            contentWindowInsets = { WindowInsets(0.dp) },
            onDismissRequest = { showFilterDrawer = false }
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .padding(bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() + 16.dp)
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                Text(
                    text = "Filter",
                    style = MaterialTheme.typography.headlineLarge
                )
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    state.availableSubjectFilters.forEach { subject ->
                        FilterChip(
                            modifier = Modifier.animatePlacement(),
                            selected = state.filteredSubjects.any { it.id == subject.id },
                            onClick = { onEvent(AnalyticsAction.ToggleSubjectFilter(subject)) },
                            leadingIcon = { SubjectIcon(Modifier.size(18.dp), subject.name) },
                            trailingIcon = {
                                AnimatedVisibility(
                                    visible = state.filteredSubjects.any { it.id == subject.id },
                                    enter = expandHorizontally(expandFrom = Alignment.CenterHorizontally),
                                    exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally)
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.x),
                                        contentDescription = null,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
                            ),
                            label = { Text(subject.localId) }
                        )
                    }
                }
                Spacer(Modifier.height(WindowInsets.safeContent.asPaddingValues().calculateBottomPadding()))
            }
        }
    }
}

data class GradesByGrade(
    val grades: Int,
    val grade: Int,
    val percentage: Float
)