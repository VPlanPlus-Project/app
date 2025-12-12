package plus.vplan.app.feature.grades.page.view.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularWavyProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.PullToRefreshDefaults.LoadingIndicator
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState
import plus.vplan.app.feature.grades.page.detail.ui.GradeDetailDrawer
import plus.vplan.app.feature.grades.page.view.ui.components.AddGradeDialog
import plus.vplan.app.feature.grades.page.view.ui.components.SelectIntervalDrawer
import plus.vplan.app.feature.main.ui.MainScreen
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.utils.blendColor
import plus.vplan.app.utils.roundTo
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import vplanplus.composeapp.generated.resources.calculator
import vplanplus.composeapp.generated.resources.chart_no_axes_combined
import vplanplus.composeapp.generated.resources.filter
import vplanplus.composeapp.generated.resources.list_ordered
import vplanplus.composeapp.generated.resources.lock
import vplanplus.composeapp.generated.resources.lock_open
import vplanplus.composeapp.generated.resources.trash_2
import vplanplus.composeapp.generated.resources.undo_2
import vplanplus.composeapp.generated.resources.x
import kotlin.math.roundToInt

@Composable
fun GradesScreen(
    navHostController: NavHostController,
    vppId: Int
) {
    val viewModel = koinViewModel<GradesViewModel>()
    val state by viewModel.state.collectAsState()

    LaunchedEffect(vppId) { viewModel.init(vppId) }

    GradesContent(
        state = state,
        onOpenAnalytics = remember(vppId) { { navHostController.navigate(MainScreen.Analytics(vppId)) } },
        onEvent = viewModel::onEvent,
        onBack = navHostController::navigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)
@Composable
private fun GradesContent(
    state: GradesState,
    onOpenAnalytics: () -> Unit,
    onEvent: (event: GradeDetailEvent) -> Unit,
    onBack: () -> Unit
) {
    val localDensity = LocalDensity.current
    val topScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollState = rememberScrollState()
    val infiniteTransition = rememberInfiniteTransition()

    var gradeDrawerId by rememberSaveable { mutableStateOf<Int?>(null) }
    var addGradeToCategoryId by rememberSaveable { mutableStateOf<Int?>(null) }
    var firstRun by rememberSaveable { mutableStateOf(true) }

    val pullToRefreshState = rememberPullToRefreshState()

    var showIntervalFilterDrawer by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(state.gradeLockState) {
        if (state.gradeLockState == GradeLockState.Locked && firstRun) {
            firstRun = false
            onEvent(GradeDetailEvent.RequestGradeUnlock)
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Noten",
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_left),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.gradeLockState == GradeLockState.Unlocked,
                        enter = expandHorizontally(expandFrom = Alignment.CenterHorizontally) + fadeIn(),
                        exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally) + fadeOut(),
                    ) {
                        IconButton(onClick = { onEvent(GradeDetailEvent.RequestGradeLock) }) {
                            Icon(
                                painter = painterResource(Res.drawable.lock),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.gradeLockState?.canAccess == true && state.subjects.isEmpty(),
                        enter = expandHorizontally(expandFrom = Alignment.CenterHorizontally) + fadeIn(),
                        exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally) + fadeOut(),
                    ) {
                        IconButton(onClick = { onOpenAnalytics() }) {
                            Icon(
                                painter = painterResource(Res.drawable.chart_no_axes_combined),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                    androidx.compose.animation.AnimatedVisibility(
                        visible = state.gradeLockState != GradeLockState.Locked,
                        enter = expandHorizontally(expandFrom = Alignment.CenterHorizontally) + fadeIn(),
                        exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally) + fadeOut()
                    ) {
                        IconButton(onClick = { showIntervalFilterDrawer = true }) {
                            Icon(
                                painter = painterResource(Res.drawable.filter),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                },
                scrollBehavior = topScrollBehavior
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            AnimatedContent(
                targetState = state.gradeLockState != null,
                modifier = Modifier.fillMaxSize(),
            ) gradeLockStateAvailable@{ isGradeLockStateLoaded ->
                if (!isGradeLockStateLoaded) {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularWavyProgressIndicator()
                    }
                    return@gradeLockStateAvailable
                }
                AnimatedContent(
                    targetState = state.gradeLockState?.canAccess?.not() != false,
                    modifier = Modifier.fillMaxSize(),
                ) { areGradesLocked ->
                    if (areGradesLocked) {
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.lock),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                            Text(
                                text = "Noten gesperrt",
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            TextButton(
                                onClick = { onEvent(GradeDetailEvent.RequestGradeUnlock) },
                                colors = ButtonDefaults.textButtonColors(
                                    contentColor = MaterialTheme.colorScheme.tertiary
                                )
                            ) {
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.lock_open),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                    Text("Entsperren")
                                }
                            }
                        }
                        return@AnimatedContent
                    }
                    PullToRefreshBox(
                        state = pullToRefreshState,
                        onRefresh = { onEvent(GradeDetailEvent.Refresh) },
                        isRefreshing = state.isUpdating,
                        modifier = Modifier.fillMaxSize(),
                        indicator = {
                            LoadingIndicator(
                                modifier = Modifier
                                    .align(Alignment.TopCenter),
                                isRefreshing = state.isUpdating,
                                state = pullToRefreshState,
                                containerColor = MaterialTheme.colorScheme.surfaceVariant,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        },
                    ) {
                        if (state.allGrades.isEmpty()) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                                    .nestedScroll(topScrollBehavior.nestedScrollConnection),
                                verticalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterVertically),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.list_ordered),
                                    modifier = Modifier.size(24.dp),
                                    contentDescription = null
                                )
                                Text(
                                    text = buildString {
                                        append("Keine Noten ")
                                        if (state.selectedInterval != null) {
                                            if (state.selectedInterval.includedIntervalId == null) append("für das Intervall ${state.selectedInterval.name} ")
                                            else state.selectedInterval.includedInterval?.map { it?.name }?.collectAsState(null)?.let {
                                                append("für die Intervalle ${it.value} und ${state.selectedInterval.name}")
                                            }
                                        }
                                        append("verfügbar.")
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    textAlign = TextAlign.Center
                                )
                            }
                            return@PullToRefreshBox
                        }
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(topScrollBehavior.nestedScrollConnection)
                                .verticalScroll(scrollState)
                        ) {
                            AnimatedVisibility(
                                visible = !state.isInEditMode,
                                enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically),
                                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically)
                            ) {
                                Row(
                                    modifier = Modifier
                                        .padding(horizontal = 16.dp)
                                        .fillMaxWidth()
                                        .height(128.dp)
                                        .clip(RoundedCornerShape(16.dp)),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                ) {
                                    Column(Modifier.weight(1f)) {
                                        Column(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .clip(RoundedCornerShape(8.dp))
                                                .background(MaterialTheme.colorScheme.primaryContainer),
                                            horizontalAlignment = Alignment.CenterHorizontally,
                                            verticalArrangement = Arrangement.Center
                                        ) {
                                            CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onPrimaryContainer) {
                                                Row(
                                                    verticalAlignment = Alignment.CenterVertically,
                                                ) {
                                                    Text(
                                                        text = "∅ ",
                                                        style = MaterialTheme.typography.headlineLarge,
                                                    )
                                                    AnimatedContent(
                                                        targetState = state.fullAverage,
                                                    ) { average ->
                                                        if (average == null) ShimmerLoader(
                                                            modifier = Modifier.size(MaterialTheme.typography.headlineLarge.lineHeight.toDp()).clip(RoundedCornerShape(8.dp)),
                                                            infiniteTransition = infiniteTransition
                                                        ) else Text(
                                                            text = if (average.isNaN()) "-" else average.roundTo(2).toString(),
                                                            style = MaterialTheme.typography.headlineLarge,
                                                        )
                                                    }
                                                }
                                                val interval = state.selectedInterval
                                                val hasIncludedInterval = interval?.includedIntervalId != null
                                                val includeInterval = state.selectedInterval?.includedInterval?.collectAsState(null)?.value

                                                Text(
                                                    text = "Durchschnitt",
                                                    style = MaterialTheme.typography.labelMedium
                                                )
                                                if (interval == null || hasIncludedInterval && includeInterval == null) ShimmerLoader(
                                                    modifier = Modifier
                                                        .height(MaterialTheme.typography.labelMedium.lineHeight.toDp())
                                                        .width(32.dp)
                                                        .clip(RoundedCornerShape(8.dp)),
                                                    infiniteTransition = infiniteTransition
                                                ) else Text(
                                                    text = listOfNotNull(interval.name, includeInterval?.name).sorted().joinToString(),
                                                    style = MaterialTheme.typography.labelMedium,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )
                                            }
                                        }
                                    }
                                    Column(
                                        modifier = Modifier.weight(1f),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Column(Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onOpenAnalytics() }
                                                    .background(colors[CustomColor.Green]!!.getGroup().container)
                                            ) {
                                                Icon(
                                                    painter = painterResource(Res.drawable.chart_no_axes_combined),
                                                    contentDescription = null,
                                                    tint = colors[CustomColor.Green]!!.getGroup().onContainer.copy(alpha = .7f),
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .align(Alignment.BottomEnd)
                                                        .size(24.dp)
                                                )
                                                CompositionLocalProvider(LocalContentColor provides colors[CustomColor.Green]!!.getGroup().onContainer) {
                                                    Text(
                                                        text = "Analyse",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        modifier = Modifier
                                                            .padding(8.dp)
                                                            .align(Alignment.TopStart)
                                                    )
                                                }
                                            }
                                        }
                                        Column(Modifier.weight(1f)) {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxSize()
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable { onEvent(GradeDetailEvent.ToggleEditMode) }
                                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                                            ) {
                                                Icon(
                                                    painter = painterResource(Res.drawable.calculator),
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = .7f),
                                                    modifier = Modifier
                                                        .padding(8.dp)
                                                        .align(Alignment.BottomEnd)
                                                        .size(24.dp)
                                                )
                                                CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                                                    Text(
                                                        text = "Notenrechner",
                                                        style = MaterialTheme.typography.titleSmall,
                                                        modifier = Modifier
                                                            .padding(8.dp)
                                                            .align(Alignment.TopStart)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                            Spacer(Modifier.height(16.dp))
                            state.subjects.forEach { subject ->
                                Column {
                                    Row(
                                        modifier = Modifier
                                            .padding(start = 16.dp, end = 32.dp)
                                            .fillMaxWidth(),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        SubjectIcon(
                                            modifier = Modifier
                                                .size(32.dp)
                                                .clip(RoundedCornerShape(8.dp)),
                                            subject = subject.name
                                        )
                                        Text(
                                            text = subject.name,
                                            style = MaterialTheme.typography.titleMedium,
                                            modifier = Modifier.weight(1f)
                                        )
                                        AnimatedContent(
                                            targetState = subject.average,
                                        ) { subjectAverage ->
                                            if (subjectAverage == null) ShimmerLoader(
                                                modifier = Modifier
                                                    .height(MaterialTheme.typography.titleSmall.lineHeight.toDp())
                                                    .width(32.dp)
                                                    .clip(RoundedCornerShape(8.dp)),
                                                infiniteTransition = infiniteTransition
                                            ) else Text(
                                                text = "∅ ${if (subjectAverage.isNaN()) "-" else ((subjectAverage * 100).roundToInt() / 100.0)}",
                                                style = MaterialTheme.typography.labelLarge,
                                            )
                                        }
                                    }
                                    Spacer(Modifier.height(8.dp))
                                    Column(
                                        modifier = Modifier
                                            .padding(horizontal = 16.dp)
                                            .fillMaxWidth()
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .padding(4.dp)
                                    ) {
                                        subject.categories.forEach { category ->
                                            Row(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(top = 12.dp)
                                                    .padding(horizontal = 12.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(
                                                    modifier = Modifier
                                                        .weight(1f)
                                                ) {
                                                    Text(
                                                        text = category.name,
                                                        style = MaterialTheme.typography.titleSmall,
                                                    )
                                                    Text(
                                                        text = "${((category.weight / subject.categories.sumOf { it.weight }) * 100).roundToInt()}%",
                                                        style = MaterialTheme.typography.labelSmall
                                                    )
                                                }
                                                AnimatedContent(
                                                    targetState = category.average,
                                                ) { categoryAverage ->
                                                    if (categoryAverage == null) ShimmerLoader(
                                                        modifier = Modifier
                                                            .size(MaterialTheme.typography.titleSmall.lineHeight.toDp())
                                                            .width(32.dp)
                                                            .clip(RoundedCornerShape(8.dp)),
                                                        infiniteTransition = infiniteTransition
                                                    ) else Text(
                                                        text = "∅ ${if (categoryAverage.isNaN()) "-" else ((categoryAverage * 100).roundToInt() / 100.0)}",
                                                        style = MaterialTheme.typography.titleSmall,
                                                    )
                                                }
                                            }

                                            category.grades.forEach forEachGrade@{ (grade, isSelectedForFinalGrade) ->
                                                val collection = grade.collection.collectAsState(null).value
                                                val interval = collection?.interval?.collectAsState(null)?.value
                                                val teacher = collection?.teacher?.collectAsState(null)?.value
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(56.dp)
                                                        .padding(horizontal = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxHeight()
                                                            .clip(RoundedCornerShape(16.dp))
                                                            .padding(horizontal = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        val red = colors[CustomColor.Red]!!.getGroup()
                                                        val green = colors[CustomColor.Green]!!.getGroup()
                                                        val backgroundColor by animateColorAsState(
                                                            if (isSelectedForFinalGrade != true || interval == null || grade.value == null || grade.value.startsWith('+') || grade.value.startsWith('-')) Color.Gray
                                                            else when (interval.type) {
                                                                is BesteSchuleInterval.Type.Sek2 -> blendColor(blendColor(red.container, green.container, (grade.numericValue?:0)/15f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                                                                else -> blendColor(blendColor(green.container, red.container, ((grade.numericValue?:1)-1)/5f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                                                            }
                                                        )

                                                        val textColor by animateColorAsState(
                                                            if (isSelectedForFinalGrade != true || interval == null || grade.value == null || grade.value.startsWith('+') || grade.value.startsWith('-')) Color.White
                                                            else when (interval.type) {
                                                                is BesteSchuleInterval.Type.Sek2 -> blendColor(blendColor(red.onContainer, green.onContainer, (grade.numericValue?:0)/15f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                                                                else -> blendColor(blendColor(green.onContainer, red.onContainer, ((grade.numericValue?:1)-1)/5f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                                                            }
                                                        )


                                                        Box(
                                                            modifier = Modifier
                                                                .padding(2.dp)
                                                                .fillMaxHeight()
                                                                .width(42.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .clickable(enabled = isSelectedForFinalGrade != null) { onEvent(GradeDetailEvent.ToggleConsiderForFinalGrade(grade)) }
                                                                .background(backgroundColor),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = buildString {
                                                                    if (grade.isOptional) append("(")
                                                                    if (grade.value != null) append(grade.value)
                                                                    else append("-")
                                                                    if (grade.isOptional) append(")")
                                                                },
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = textColor
                                                            )
                                                        }

                                                        Column(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .clickable { gradeDrawerId = grade.id }
                                                                .padding(4.dp),
                                                            verticalArrangement = Arrangement.spacedBy(2.dp)
                                                        ) {
                                                            AnimatedContent(
                                                                targetState = collection?.name,
                                                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) { collectionName ->
                                                                val style = MaterialTheme.typography.bodyMedium
                                                                if (collectionName == null) ShimmerLoader(
                                                                    modifier = Modifier
                                                                        .height(style.lineHeight.toDp())
                                                                        .fillMaxWidth()
                                                                        .clip(RoundedCornerShape(8.dp)),
                                                                    infiniteTransition = infiniteTransition
                                                                ) else Text(
                                                                    text = collectionName,
                                                                    style = style,
                                                                    color = if (isSelectedForFinalGrade == true) MaterialTheme.colorScheme.onSurfaceVariant else Color.Gray,
                                                                    textDecoration = if (isSelectedForFinalGrade != true) TextDecoration.LineThrough else null,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                            AnimatedContent(
                                                                targetState = teacher?.let { "${it.forename} ${it.surname}" },
                                                                transitionSpec = { fadeIn() togetherWith fadeOut() },
                                                                modifier = Modifier.fillMaxWidth()
                                                            ) { teacherName ->
                                                                val style = MaterialTheme.typography.labelSmall
                                                                if (teacherName == null) ShimmerLoader(
                                                                    modifier = Modifier
                                                                        .height(style.lineHeight.toDp())
                                                                        .fillMaxWidth()
                                                                        .clip(RoundedCornerShape(8.dp)),
                                                                    infiniteTransition = infiniteTransition
                                                                ) else Text(
                                                                    text = teacherName,
                                                                    style = style,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            category.calculatorGrades.forEach { grade ->
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(56.dp)
                                                        .padding(horizontal = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxHeight()
                                                            .clip(RoundedCornerShape(16.dp))
                                                            .padding(horizontal = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        val red = colors[CustomColor.Red]!!.getGroup()
                                                        val green = colors[CustomColor.Green]!!.getGroup()
                                                        val backgroundColor by animateColorAsState(
                                                            when (state.selectedInterval!!.type) {
                                                                is BesteSchuleInterval.Type.Sek2 -> blendColor(blendColor(red.container, green.container, grade/15f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                                                                else -> blendColor(blendColor(green.container, red.container, (grade-1)/5f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                                                            }
                                                        )

                                                        val textColor by animateColorAsState(
                                                            when (state.selectedInterval.type) {
                                                                is BesteSchuleInterval.Type.Sek2 -> blendColor(blendColor(red.onContainer, green.onContainer, grade /15f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                                                                else -> blendColor(blendColor(green.onContainer, red.onContainer, (grade-1)/5f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                                                            }
                                                        )


                                                        Box(
                                                            modifier = Modifier
                                                                .padding(2.dp)
                                                                .fillMaxHeight()
                                                                .width(42.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(backgroundColor),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = grade.toString(),
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = textColor
                                                            )
                                                        }

                                                        Row(
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .padding(4.dp),
                                                            horizontalArrangement = Arrangement.SpaceBetween,
                                                            verticalAlignment = Alignment.CenterVertically
                                                        ) {
                                                            Text(
                                                                text = "Hinzugefügte Note",
                                                                style = MaterialTheme.typography.bodyMedium,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                            IconButton(
                                                                onClick = { onEvent(GradeDetailEvent.RemoveGrade(category.id, grade)) },
                                                            ) {
                                                                Icon(
                                                                    painter = painterResource(Res.drawable.trash_2),
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(24.dp)
                                                                )
                                                            }
                                                        }
                                                    }
                                                }
                                            }
                                            AnimatedVisibility(
                                                visible = state.isInEditMode,
                                                enter = expandVertically(expandFrom = Alignment.CenterVertically),
                                                exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically)
                                            ) {
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .height(56.dp)
                                                        .padding(horizontal = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                ) {
                                                    Row(
                                                        modifier = Modifier
                                                            .weight(1f)
                                                            .fillMaxHeight()
                                                            .clip(RoundedCornerShape(16.dp))
                                                            .clickable { addGradeToCategoryId = category.id }
                                                            .padding(horizontal = 4.dp),
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Box(
                                                            modifier = Modifier
                                                                .padding(2.dp)
                                                                .fillMaxHeight()
                                                                .width(42.dp)
                                                                .clip(RoundedCornerShape(8.dp))
                                                                .background(MaterialTheme.colorScheme.tertiary),
                                                            contentAlignment = Alignment.Center
                                                        ) {
                                                            Text(
                                                                text = "+",
                                                                style = MaterialTheme.typography.bodyLarge,
                                                                color = MaterialTheme.colorScheme.onTertiary,
                                                            )
                                                        }

                                                        Text(
                                                            text = "Note hinzufügen",
                                                            style = MaterialTheme.typography.titleSmall,
                                                            maxLines = 1,
                                                            overflow = TextOverflow.Ellipsis,
                                                            modifier = Modifier
                                                                .fillMaxWidth()
                                                                .padding(4.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                HorizontalDivider(Modifier.padding(vertical = 16.dp))
                            }
                        }

                        AnimatedVisibility(
                            visible = scrollState.value > with (localDensity) { 128.dp.toPx() } || state.isInEditMode,
                            enter = fadeIn() + scaleIn(animationSpec = spring(stiffness = Spring.StiffnessHigh)),
                            exit = fadeOut() + scaleOut(animationSpec = spring(stiffness = Spring.StiffnessHigh)),
                            modifier = Modifier.align(Alignment.TopCenter)
                        ) {
                            Row(
                                modifier = Modifier
                                    .padding(top = 16.dp)
                                    .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                                    .padding(8.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "∅",
                                    style = MaterialTheme.typography.headlineSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                AnimatedContent(
                                    targetState = state.fullAverage,
                                ) { average ->
                                    if (average == null) ShimmerLoader(
                                        modifier = Modifier
                                            .height(MaterialTheme.typography.headlineSmall.lineHeight.toDp())
                                            .width(32.dp)
                                            .clip(RoundedCornerShape(8.dp)),
                                        infiniteTransition = infiniteTransition
                                    ) else Text(
                                        text = if (average.isNaN()) "-" else ((average * 100).roundToInt() / 100.0).toString(),
                                        style = MaterialTheme.typography.headlineSmall,
                                        color = MaterialTheme.colorScheme.onSurface,
                                    )
                                }
                                VerticalDivider(Modifier.height(32.dp))
                                AnimatedContent(
                                    targetState = state.isInEditMode,
                                ) { editMode ->
                                    Row(
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier
                                                .clip(RoundedCornerShape(50))
                                                .background(
                                                    if (editMode) MaterialTheme.colorScheme.primaryContainer
                                                    else MaterialTheme.colorScheme.surfaceVariant
                                                )
                                                .clickable { onEvent(GradeDetailEvent.ToggleEditMode) }
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            CompositionLocalProvider(LocalContentColor provides if (editMode) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurfaceVariant) {
                                                Icon(
                                                    painter = painterResource(Res.drawable.calculator),
                                                    contentDescription = null,
                                                    modifier = Modifier.size(24.dp)
                                                )
                                                if (editMode) {
                                                    VerticalDivider(Modifier.height(16.dp))
                                                    Icon(
                                                        painter = painterResource(Res.drawable.x),
                                                        contentDescription = null,
                                                        modifier = Modifier.size(24.dp).padding(4.dp)
                                                    )
                                                }
                                            }
                                        }
                                        if (editMode) IconButton(
                                            onClick = { onEvent(GradeDetailEvent.ResetAdditionalGrades) },
                                        ) {
                                            Icon(
                                                painter = painterResource(Res.drawable.undo_2),
                                                contentDescription = null,
                                                modifier = Modifier.size(24.dp)
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

    if (gradeDrawerId != null) GradeDetailDrawer(
        gradeId = gradeDrawerId!!,
        onDismiss = { gradeDrawerId = null }
    )

    if (addGradeToCategoryId != null) AddGradeDialog(
        onDismiss = { addGradeToCategoryId = null },
        onSelectGrade = { onEvent(GradeDetailEvent.AddGrade(addGradeToCategoryId!!, it)); addGradeToCategoryId = null },
        intervalType = state.selectedInterval?.type ?: BesteSchuleInterval.Type.Sek1
    )

    if (showIntervalFilterDrawer) SelectIntervalDrawer(
        intervals = state.intervals,
        selectedInterval = state.selectedInterval,
        onDismiss = { showIntervalFilterDrawer = false },
        onClickInterval = { onEvent(GradeDetailEvent.SelectInterval(it)) }
    )
}