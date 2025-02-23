package plus.vplan.app.feature.grades.page.view.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.map
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.model.schulverwalter.Collection
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.domain.model.schulverwalter.Teacher
import plus.vplan.app.feature.grades.page.detail.ui.GradeDetailDrawer
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.utils.blendColor
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import kotlin.math.roundToInt

@Composable
fun GradesScreen(
    navHostController: NavHostController,
    vppId: Int
) {
    val viewModel = koinViewModel<GradesViewModel>()
    val state = viewModel.state

    LaunchedEffect(vppId) { viewModel.init(vppId) }

    GradesContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = navHostController::navigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class, FlowPreview::class)
@Composable
private fun GradesContent(
    state: GradesState,
    onEvent: (event: GradeDetailEvent) -> Unit,
    onBack: () -> Unit
) {
    val topScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()
    val scrollState = rememberLazyListState()
    val infiniteTransition = rememberInfiniteTransition()

    var gradeDrawerId by rememberSaveable { mutableStateOf<Int?>(null) }

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
                scrollBehavior = topScrollBehavior
            )
        }
    ) { contentPadding ->
        Box(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(topScrollBehavior.nestedScrollConnection),
                state = scrollState
            ) {
                item header@{
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
                                                text = if (average.isNaN()) "-" else ((average * 100).roundToInt() / 100.0).toString(),
                                                style = MaterialTheme.typography.headlineLarge,
                                            )
                                        }
                                    }
                                    val interval = state.currentInterval
                                    val hasIncludedInterval = interval?.includedIntervalId != null
                                    val includeInterval = state.currentInterval?.includedInterval?.filterIsInstance<CacheState.Done<Interval>>()?.map { it.data }?.collectAsState(null)?.value

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
                                        .background(colors[CustomColor.Green]!!.getGroup().container)
                                ) {
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
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                ) {
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
                item { Spacer(Modifier.height(16.dp)) }
                items(state.subjects) { subject ->
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
                                            style = MaterialTheme.typography.titleSmall
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
                                    val collection = grade.collection.filterIsInstance<CacheState.Done<Collection>>().map { it.data }.debounce(500).collectAsState(null).value
                                    val interval = collection?.interval?.filterIsInstance<CacheState.Done<Interval>>()?.map { it.data }?.debounce(500)?.collectAsState(null)?.value
                                    val teacher = grade.teacher.filterIsInstance<CacheState.Done<Teacher>>().map { it.data }.debounce(500).collectAsState(null).value
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

                                            Box(
                                                modifier = Modifier
                                                    .padding(2.dp)
                                                    .fillMaxHeight()
                                                    .width(42.dp)
                                                    .clip(RoundedCornerShape(8.dp))
                                                    .clickable(enabled = isSelectedForFinalGrade != null) { onEvent(GradeDetailEvent.ToggleConsiderForFinalGrade(grade)) }
                                                    .background(
                                                        if (isSelectedForFinalGrade != true || interval == null || grade.value == null || grade.value.startsWith('+') || grade.value.startsWith('-')) MaterialTheme.colorScheme.outline
                                                        else when (interval.type) {
                                                            Interval.Type.SEK1 -> blendColor(blendColor(green.container, red.container, ((grade.numericValue?:1)-1)/5f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                                                            Interval.Type.SEK2 -> blendColor(blendColor(red.container, green.container, (grade.numericValue?:0)/15f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                                                        }
                                                    ),
                                                contentAlignment = Alignment.Center
                                            ) {
                                                Text(
                                                    text = grade.value ?: "-",
                                                    style = MaterialTheme.typography.bodyLarge,
                                                    color = if (isSelectedForFinalGrade != true || interval == null || grade.value == null || grade.value.startsWith('+') || grade.value.startsWith('-')) MaterialTheme.colorScheme.onSurface
                                                    else when (interval.type) {
                                                        Interval.Type.SEK1 -> blendColor(blendColor(green.onContainer, red.onContainer, ((grade.numericValue?:1)-1)/5f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                                                        Interval.Type.SEK2 -> blendColor(blendColor(red.onContainer, green.onContainer, (grade.numericValue?:0)/15f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                                                    }
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
                                                    transitionSpec = { fadeIn() togetherWith fadeOut() }
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
                                                        maxLines = 1,
                                                        overflow = TextOverflow.Ellipsis
                                                    )
                                                }
                                                AnimatedContent(
                                                    targetState = teacher?.let { "${it.forename} ${it.name}" },
                                                    transitionSpec = { fadeIn() togetherWith fadeOut() }
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
                            }
                        }
                    }

                    HorizontalDivider(Modifier.padding(vertical = 16.dp))
                }
            }

            AnimatedVisibility(
                visible = scrollState.firstVisibleItemIndex > 0,
                enter = fadeIn() + scaleIn(animationSpec = spring(stiffness = Spring.StiffnessHigh)),
                exit = fadeOut() + scaleOut(animationSpec = spring(stiffness = Spring.StiffnessHigh)),
                modifier = Modifier.align(Alignment.TopCenter)
            ) {
                Row(
                    modifier = Modifier
                        .padding(top = 16.dp)
                        .shadow(elevation = 2.dp, shape = RoundedCornerShape(16.dp))
                        .clip(RoundedCornerShape(16.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer)
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "∅",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
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
                            color = MaterialTheme.colorScheme.onPrimaryContainer,
                        )
                    }
                }
            }
        }
    }

    if (gradeDrawerId != null) GradeDetailDrawer(
        gradeId = gradeDrawerId!!,
        onDismiss = { gradeDrawerId = null }
    )
}