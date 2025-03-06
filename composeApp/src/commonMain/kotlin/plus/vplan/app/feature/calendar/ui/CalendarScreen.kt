package plus.vplan.app.feature.calendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import androidx.navigation.NavHostController
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.collectAsLoadingState
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.feature.assessment.ui.components.detail.AssessmentDetailDrawer
import plus.vplan.app.feature.calendar.ui.components.DisplaySelectType
import plus.vplan.app.feature.calendar.ui.components.date_selector.ScrollableDateSelector
import plus.vplan.app.feature.calendar.ui.components.date_selector.weekHeight
import plus.vplan.app.feature.home.ui.components.FollowingLessons
import plus.vplan.app.feature.home.ui.components.headerFont
import plus.vplan.app.feature.homework.ui.components.detail.HomeworkDetailDrawer
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.subjectColor
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.inWholeMinutes
import plus.vplan.app.utils.now
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.regularTimeFormat
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.toName
import plus.vplan.app.utils.until
import plus.vplan.app.utils.untilText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.chevron_down
import vplanplus.composeapp.generated.resources.info
import kotlin.math.roundToInt

private const val CONTENT_PAGER_SIZE = 800

@Composable
fun CalendarScreen(
    navHostController: NavHostController,
    contentPadding: PaddingValues,
    viewModel: CalendarViewModel
) {
    val state = viewModel.state
    CalendarScreenContent(
        state = state,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun CalendarScreenContent(
    state: CalendarState,
    contentPadding: PaddingValues,
    onEvent: (event: CalendarEvent) -> Unit
) {
    val localDensity = LocalDensity.current

    var displayHomeworkId by rememberSaveable { mutableStateOf<Int?>(null) }
    var displayAssessmentId by rememberSaveable { mutableStateOf<Int?>(null) }

    var scrollProgress by remember { mutableStateOf(0f) }
    val contentScrollState = rememberScrollState()
    var isUserScrolling by remember { mutableStateOf(false) }
    var isAnimating by remember { mutableStateOf(false) }
    LaunchedEffect(contentScrollState.isScrollInProgress) {
        if (!contentScrollState.isScrollInProgress) {
            scrollProgress = scrollProgress.roundToInt().toFloat()
            isAnimating = scrollProgress.roundToInt().toFloat() != scrollProgress
        }
        isUserScrolling = contentScrollState.isScrollInProgress
    }
    val animatedScrollProgress by animateFloatAsState(
        targetValue = scrollProgress,
        label = "scrollProgress",
        finishedListener = { isAnimating = false }
    )
    val displayScrollProgress = if (isUserScrolling) scrollProgress else animatedScrollProgress

    // calendar content
    val minute = 1.25.dp
    var availableWidth by remember { mutableStateOf(0.dp) }

    val scrollConnection = remember(state.days[state.selectedDate]) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val day = state.days[state.selectedDate]
                val lessons = day?.substitutionPlan.orEmpty().ifEmpty { day?.timetable }
                val isContentAtTop = (contentScrollState.value == 0 && lessons.isNullOrEmpty()) || (lessons.orEmpty().isNotEmpty() && with(localDensity) { contentScrollState.value <= ((lessons.orEmpty().minOf { it.lessonTimeItem!!.start }.inWholeMinutes().toFloat() - 60) * minute).roundToPx() })
                val y = (with(localDensity) { available.y.toDp() }) / (5 * weekHeight)

                if ((isContentAtTop || scrollProgress > 0 && scrollProgress < 1) && available.y > 0) { // scroll to expand date picker
                    scrollProgress = (scrollProgress + y).coerceIn(0f, 1f)
                    return Offset(0f, available.y)
                }

                if (available.y < 0 && scrollProgress > 0) { // scroll to reduce date picker
                    scrollProgress = (scrollProgress + y).coerceIn(0f, 1f)
                    return Offset(0f, available.y)
                }

                return super.onPreScroll(available, source)
            }
        }
    }

    Column(
        modifier = Modifier
            .padding(contentPadding)
            .fillMaxSize()
            .thenIf(Modifier.nestedScroll(scrollConnection)) { state.displayType == DisplayType.Calendar }
    ) {
        val dateSelectorVelocityTracker = remember { VelocityTracker() }
        Column (
            modifier = Modifier
                .fillMaxWidth()
                .pointerInput(state.displayType) {
                    if (state.displayType != DisplayType.Calendar) return@pointerInput
                    detectVerticalDragGestures(
                        onDragStart = { isUserScrolling = true },
                        onDragEnd = {
                            isUserScrolling = false
                            val velocity = dateSelectorVelocityTracker.calculateVelocity().y
                            val threshold = 700
                            scrollProgress = if (velocity > threshold) 1f
                            else if (velocity < -threshold) 0f
                            else scrollProgress.roundToInt().toFloat()
                            dateSelectorVelocityTracker.resetTracking()
                        },
                        onDragCancel = {
                            isUserScrolling = false
                            scrollProgress = scrollProgress.roundToInt().toFloat()
                            dateSelectorVelocityTracker.resetTracking()
                        },
                    ) { event, dragAmount ->
                        dateSelectorVelocityTracker.addPointerInputChange(event)
                        val y = (with(localDensity) { dragAmount.toDp() }) / (5 * weekHeight)
                        scrollProgress = (scrollProgress + y).coerceIn(0f, 1f)
                    }
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Column(Modifier.padding(horizontal = 8.dp)) dateHead@{
                    AnimatedContent(
                        targetState = state.selectedDate
                    ) { displayDate ->
                        Text(
                            text = state.currentTime.date untilText displayDate,
                            style = MaterialTheme.typography.titleSmall
                        )
                    }
                    Row {
                        AnimatedContent(
                            targetState = state.selectedDate.dayOfMonth,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            modifier = Modifier.animateContentSize()
                        ) {
                            Text(
                                text = it.toString(),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = ". ",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        AnimatedContent(
                            targetState = state.selectedDate.format(LocalDate.Format {
                                monthName(MonthNames("Jan", "Feb", "Mär", "Apr", "Mai", "Jun", "Jul", "Aug", "Sep", "Okt", "Nov", "Dez"))
                            }),
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            modifier = Modifier.animateContentSize()
                        ) {
                            Text(
                                text = it,
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                        Text(
                            text = " ",
                            style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                        )
                        AnimatedContent(
                            targetState = state.selectedDate.year,
                            transitionSpec = { fadeIn() togetherWith fadeOut() },
                            modifier = Modifier.animateContentSize()
                        ) {
                            Text(
                                text = it.toString().drop(2),
                                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold)
                            )
                        }
                    }
                }
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onEvent(CalendarEvent.SelectDate(LocalDate.now())) }) {
                        Icon(painter = painterResource(Res.drawable.calendar), contentDescription = null, modifier = Modifier.size(24.dp))
                    }
                    DisplaySelectType(
                        displayType = state.displayType,
                        onSelectType = { onEvent(CalendarEvent.SelectDisplayType(it)) }
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            AnimatedVisibility(
                visible = state.displayType == DisplayType.Calendar,
                enter = fadeIn() + expandVertically(expandFrom = Alignment.CenterVertically),
                exit = fadeOut() + shrinkVertically(shrinkTowards = Alignment.CenterVertically)
            ) {
                ScrollableDateSelector(
                    scrollProgress = displayScrollProgress,
                    allowInteractions = !isUserScrolling && !isAnimating && displayScrollProgress.roundToInt().toFloat() == displayScrollProgress,
                    selectedDate = state.selectedDate,
                    onSelectDate = { onEvent(CalendarEvent.SelectDate(it)) }
                )
            }
        }
        HorizontalDivider()

        val pagerState = rememberPagerState(initialPage = (CONTENT_PAGER_SIZE / 2) + LocalDate.now().until(state.selectedDate, DateTimeUnit.DAY)) { CONTENT_PAGER_SIZE }
        val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = (CONTENT_PAGER_SIZE / 2) + LocalDate.now().until(state.selectedDate, DateTimeUnit.DAY))
        val isUserDraggingPager = pagerState.interactionSource.collectIsDraggedAsState().value
        val isUserDraggingList = lazyListState.interactionSource.collectIsDraggedAsState().value
        var isScrollAnimationRunning by remember { mutableStateOf(false) }
        LaunchedEffect(pagerState.targetPage, isUserDraggingPager) {
            if (isUserDraggingPager) return@LaunchedEffect
            val date = LocalDate.now().plus((pagerState.targetPage - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
            if (date != state.selectedDate) onEvent(CalendarEvent.SelectDate(date))
        }
        LaunchedEffect(lazyListState.firstVisibleItemIndex, isUserDraggingList) {
            if (!isUserDraggingList && isScrollAnimationRunning) return@LaunchedEffect
            if (isUserDraggingList) isScrollAnimationRunning = false
            val date = LocalDate.now().plus((lazyListState.firstVisibleItemIndex - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
            if (date != state.selectedDate) onEvent(CalendarEvent.SelectDate(date))
        }
        LaunchedEffect(state.selectedDate) {
            val currentlyOpenedDate = LocalDate.now().plus(((if (state.displayType == DisplayType.Calendar) pagerState.currentPage else lazyListState.firstVisibleItemIndex) - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
            if (currentlyOpenedDate != state.selectedDate) {
                pagerState.animateScrollToPage((CONTENT_PAGER_SIZE / 2) + LocalDate.now().until(state.selectedDate, DateTimeUnit.DAY))
                if (!isUserDraggingList) {
                    isScrollAnimationRunning = true
                    lazyListState.animateScrollToItem((CONTENT_PAGER_SIZE / 2) + LocalDate.now().until(state.selectedDate, DateTimeUnit.DAY))
                }
            }
        }
        LaunchedEffect(state.days[state.selectedDate]) {
            val day = state.days[state.selectedDate] ?: return@LaunchedEffect
            if (day.lessons.isEmpty()) return@LaunchedEffect
            val lessons = day.substitutionPlan.orEmpty().ifEmpty { day.timetable }
            if (lessons.isEmpty()) return@LaunchedEffect
            val startOfDay = lessons.minOf { it.lessonTimeItem!!.start }
            contentScrollState.animateScrollTo(with(localDensity) { ((startOfDay.inWholeMinutes().toFloat() - 60) * minute).coerceAtLeast(0.dp).roundToPx() })
        }

        AnimatedContent(
            targetState = state.displayType,
            modifier = Modifier.fillMaxSize()
        ) { displayType ->
            when (displayType) {
                DisplayType.Calendar -> {
                    HorizontalPager(
                        state = pagerState,
                        pageSize = PageSize.Fill,
                        verticalAlignment = Alignment.Top,
                        beyondViewportPageCount = 7,
                        modifier = Modifier
                            .fillMaxSize()
                    ) { page ->
                        val date = LocalDate.now().plus((page - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
                        Column {
                            val day = state.days[date]
                            if (day != null) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                ) {
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .onSizeChanged { availableWidth = with(localDensity) { it.width.toDp() } - 2 * 8.dp - 32.dp }
                                            .verticalScroll(contentScrollState)
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .height(minute * 24 * 60)
                                        ) {
                                            val lessons = day.substitutionPlan.orEmpty().ifEmpty { day.timetable }
                                            repeat(24) {
                                                val time = LocalTime(it, 0)
                                                val y = time.inWholeMinutes().toFloat() * minute
                                                if (y < 0.dp) return@repeat
                                                HorizontalDivider(Modifier.fillMaxWidth().offset(y = y).zIndex(-10f))
                                                Text(
                                                    text = "${it.toString().padStart(2, '0')}:00",
                                                    color = Color.Gray,
                                                    modifier = Modifier.offset(y = y).widthIn(max = 48.dp).align(Alignment.TopEnd),
                                                    style = MaterialTheme.typography.labelSmall
                                                )
                                            }
                                            if (date == state.currentTime.date) {
                                                val currentTime = state.currentTime.time
                                                val y = currentTime.inWholeMinutes().toFloat() * minute
                                                HorizontalDivider(
                                                    modifier = Modifier
                                                        .fillMaxWidth()
                                                        .offset(y = y + 2.dp)
                                                        .drawWithCache {
                                                            val triangleShape = Path().apply {
                                                                moveTo(0f, 1.dp.toPx())
                                                                relativeMoveTo(0f, 6.dp.toPx())
                                                                relativeLineTo(0f, -12.dp.toPx())
                                                                relativeLineTo(8.dp.toPx(), 6.dp.toPx())
                                                                relativeLineTo(-8.dp.toPx(), 6.dp.toPx())
                                                                close()
                                                            }
                                                            onDrawBehind {
                                                                drawPath(
                                                                    color = Color.Red,
                                                                    path = triangleShape,
                                                                    style = Fill
                                                                )
                                                            }
                                                        }
                                                        .zIndex(-9f),
                                                    color = Color.Red,
                                                    thickness = 2.dp
                                                )
                                            }
                                            lessons.forEachIndexed { i, lesson ->
                                                val start = lesson.lessonTimeItem!!.start
                                                val end = lesson.lessonTimeItem!!.end

                                                val lessonsThatOverlapStart = lessons.filter { start in it.lessonTimeItem!!.start..it.lessonTimeItem!!.end }
                                                val lessonsThatOverlapStartAndAreAlreadyDisplayed = lessons.filterIndexed { index, lessonCompare -> start in lessonCompare.lessonTimeItem!!.start..lessonCompare.lessonTimeItem!!.end && index < i }

                                                val y = start.inWholeMinutes().toFloat() * minute
                                                Box(
                                                    modifier = Modifier
                                                        .width(availableWidth / lessonsThatOverlapStart.size)
                                                        .padding(horizontal = 8.dp)
                                                        .height(start.until(end).inWholeMinutes.toFloat() * minute)
                                                        .offset(y = y, x = (availableWidth / lessonsThatOverlapStart.size) * lessonsThatOverlapStartAndAreAlreadyDisplayed.size)
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .clickable {  }
                                                        .background(
                                                            if (lesson.isCancelled) MaterialTheme.colorScheme.errorContainer
                                                            else MaterialTheme.colorScheme.surfaceVariant)
                                                        .padding(4.dp)
                                                ) {
                                                    CompositionLocalProvider(
                                                        LocalContentColor provides if (lesson is Lesson.SubstitutionPlanLesson && lesson.subject == null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                    ) {
                                                        Column {
                                                            Row(
                                                                verticalAlignment = Alignment.Top,
                                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                            ) {
                                                                if (lesson is Lesson.SubstitutionPlanLesson && lesson.isSubjectChanged) SubjectIcon(
                                                                    modifier = Modifier.size(headerFont().lineHeight.toDp() + 4.dp),
                                                                    subject = lesson.subject,
                                                                    contentColor = MaterialTheme.colorScheme.onError,
                                                                    containerColor = MaterialTheme.colorScheme.error
                                                                )
                                                                else SubjectIcon(
                                                                    modifier = Modifier.size(headerFont().lineHeight.toDp() + 4.dp),
                                                                    subject = lesson.subject
                                                                )
                                                                Column {
                                                                    Row(
                                                                        verticalAlignment = Alignment.CenterVertically,
                                                                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                    ) {
                                                                        Text(text = buildAnnotatedString {
                                                                            withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle()) {
                                                                                if (lesson.isCancelled) withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(textDecoration = TextDecoration.LineThrough)) {
                                                                                    append((lesson as Lesson.SubstitutionPlanLesson).defaultLessonItem!!.subject)
                                                                                } else append(lesson.subject.toString())
                                                                            }
                                                                        }, style = MaterialTheme.typography.bodySmall)
                                                                        if (lesson.roomItems != null) Text(
                                                                            text = lesson.roomItems.orEmpty().joinToString { it.name },
                                                                            style = MaterialTheme.typography.labelMedium
                                                                        )
                                                                        Text(
                                                                            text = lesson.teacherItems.orEmpty().joinToString { it.name },
                                                                            style = MaterialTheme.typography.labelMedium
                                                                        )
                                                                    }
                                                                    Text(
                                                                        buildString {
                                                                            append(lesson.lessonTimeItem!!.lessonNumber)
                                                                            append(". $DOT ")
                                                                            append(lesson.lessonTimeItem!!.start.format(regularTimeFormat))
                                                                            append(" - ")
                                                                            append(lesson.lessonTimeItem!!.end.format(regularTimeFormat))
                                                                        },
                                                                        style = MaterialTheme.typography.labelSmall,
                                                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                        maxLines = 1,
                                                                        overflow = TextOverflow.Ellipsis
                                                                    )
                                                                }
                                                            }
                                                            if (lesson is Lesson.SubstitutionPlanLesson && lesson.info != null) Row(
                                                                verticalAlignment = Alignment.CenterVertically,
                                                                horizontalArrangement = Arrangement.spacedBy(4.dp),
                                                                modifier = Modifier.padding(start = 16.dp)
                                                            ) {
                                                                Icon(
                                                                    painter = painterResource(Res.drawable.info),
                                                                    contentDescription = null,
                                                                    modifier = Modifier.size(8.dp),
                                                                    tint = MaterialTheme.colorScheme.onSurface
                                                                )
                                                                Text(
                                                                    text = lesson.info,
                                                                    style = MaterialTheme.typography.bodySmall,
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

                                    if (day.day.info != null) {
                                        InfoCard(
                                            modifier = Modifier
                                                .align(Alignment.BottomCenter)
                                                .padding(vertical = 4.dp, horizontal = 8.dp),
                                            imageVector = Res.drawable.info,
                                            title = "Informationen deiner Schule",
                                            text = day.day.info,
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
                DisplayType.Agenda -> {
                    LazyColumn(
                        state = lazyListState
                    ) {
                        items(CONTENT_PAGER_SIZE) { page ->
                            val date = LocalDate.now().plus((page - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
                            val day = state.days[date]
                            var showLessons by rememberSaveable { mutableStateOf(false) }
                            LaunchedEffect(Unit) {
                                onEvent(CalendarEvent.StartLessonUiSync(date))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(48.dp)
                                        .padding(2.dp)
                                        .clip(RoundedCornerShape(50))
                                        .thenIf(Modifier.background(MaterialTheme.colorScheme.primary)) { date == LocalDate.now() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(
                                        horizontalAlignment = Alignment.CenterHorizontally
                                    ) {
                                        Text(
                                            text = date.format(LocalDate.Format {
                                                dayOfWeek(shortDayOfWeekNames)
                                            }),
                                            style = MaterialTheme.typography.labelMedium,
                                            color = if (date == LocalDate.now()) MaterialTheme.colorScheme.onPrimary
                                            else if (date.dayOfWeek.isoDayNumber >= 6) colors[CustomColor.Red]!!.getGroup().color
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                        Text(
                                            text = date.dayOfMonth.toString(),
                                            style = MaterialTheme.typography.titleSmall,
                                            color = if (date == LocalDate.now()) MaterialTheme.colorScheme.onPrimary
                                            else MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                Column {
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(48.dp),
                                        verticalArrangement = Arrangement.Center
                                    ) {
                                        when (day?.day?.dayType) {
                                            Day.DayType.WEEKEND -> Text(
                                                text = "Wochenende",
                                                style = MaterialTheme.typography.titleMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                            Day.DayType.HOLIDAY -> Text(
                                                text = "Ferien",
                                                style = MaterialTheme.typography.titleMedium,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.padding(start = 8.dp)
                                            )
                                            Day.DayType.REGULAR -> {
                                                val lessonCount = day.lessons.distinctBy { it.lessonTimeItem!!.lessonNumber }.count()
                                                Row(
                                                    modifier = Modifier
                                                        .fillMaxSize()
                                                        .padding(end = 4.dp)
                                                        .clip(RoundedCornerShape(8.dp))
                                                        .clickable { showLessons = !showLessons }
                                                        .padding(horizontal = 8.dp, vertical = 4.dp),
                                                    verticalAlignment = Alignment.CenterVertically,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column {
                                                        Text(
                                                            text = "$lessonCount Stunden",
                                                            style = MaterialTheme.typography.titleMedium
                                                        )
                                                        if (day.lessons.isNotEmpty()) Text(
                                                            text = buildString {
                                                                append(day.lessons.minOf { it.lessonTimeItem!!.start }.format(regularTimeFormat))
                                                                append(" - ")
                                                                append(day.lessons.maxOf { it.lessonTimeItem!!.end }.format(regularTimeFormat))
                                                            },
                                                            style = MaterialTheme.typography.bodySmall
                                                        )
                                                    }
                                                    val iconRotation by animateFloatAsState(if (showLessons) 1f else 0f, label = "rotation animation")
                                                    Icon(
                                                        painter = painterResource(Res.drawable.chevron_down),
                                                        modifier = Modifier
                                                            .size(24.dp)
                                                            .rotate(-180*iconRotation),
                                                        contentDescription = null
                                                    )
                                                }
                                            }
                                            else -> Unit
                                        }
                                    }
                                    AnimatedVisibility(
                                        visible = showLessons,
                                        enter = scaleIn() + expandVertically(),
                                        exit = scaleOut() + shrinkVertically()
                                    ) {
                                        Column {
                                            FollowingLessons(
                                                modifier = Modifier.padding(horizontal = 4.dp),
                                                showFirstGradient = false,
                                                date = date,
                                                paddingStart = 8.dp,
                                                lessons = day?.lessons.orEmpty().groupBy { l -> l.lessonTimeItem!!.lessonNumber }
                                            )
                                        }
                                    }
                                    Column(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .animateContentSize()
                                    ) assessments@{
                                        day?.day?.assessments?.collectAsState(emptyList())?.value?.let { assessments ->
                                            assessments.forEach forEachAssessment@{ assessment ->
                                                val subject = assessment.subjectInstance.collectAsState(null).value
                                                val createdBy by when (assessment.creator) {
                                                    is AppEntity.VppId -> assessment.creator.vppId.collectAsLoadingState("")
                                                    is AppEntity.Profile -> assessment.creator.profile.collectAsLoadingState("")
                                                }
                                                if (subject == null) return@forEachAssessment
                                                var boxHeight by remember { mutableStateOf(0.dp) }
                                                Box(
                                                    modifier = Modifier
                                                        .padding(end = 8.dp)
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .clickable { displayAssessmentId = assessment.id }
                                                        .onSizeChanged { with(localDensity) { boxHeight = it.height.toDp() } }
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.CenterStart)
                                                            .width(4.dp)
                                                            .height((boxHeight - 32.dp).coerceAtLeast(0.dp))
                                                            .clip(RoundedCornerShape(0, 50, 50, 0))
                                                            .background(subject.subject.subjectColor().getGroup().color)
                                                    )
                                                    Column(
                                                        modifier = Modifier
                                                            .padding(16.dp)
                                                            .fillMaxWidth()
                                                    ) {
                                                        Row {
                                                            SubjectIcon(
                                                                modifier = Modifier.size(MaterialTheme.typography.titleLarge.lineHeight.toDp()),
                                                                subject = subject.subject
                                                            )
                                                            Spacer(Modifier.size(8.dp))
                                                            Column {
                                                                Text(
                                                                    text = buildString {
                                                                        append(assessment.type.toName())
                                                                        append(" in ")
                                                                        append(subject.subject)
                                                                    },
                                                                    style = MaterialTheme.typography.titleLarge
                                                                )
                                                                Text(
                                                                    text = assessment.description,
                                                                    style = MaterialTheme.typography.bodyMedium
                                                                )
                                                            }
                                                        }
                                                        HorizontalDivider(Modifier.padding(8.dp))
                                                        Row(
                                                            modifier = Modifier
                                                                .padding(horizontal = 8.dp)
                                                                .fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            if (createdBy is CacheState.Loading) CircularProgressIndicator(Modifier.size(MaterialTheme.typography.labelMedium.lineHeight.toDp()))
                                                            else Text(
                                                                text = buildString {
                                                                    val creator = (createdBy as? CacheState.Done)?.data
                                                                    append(when (creator) {
                                                                        is VppId -> creator.name
                                                                        is Profile -> creator.name
                                                                        else -> ""
                                                                    })
                                                                },
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MaterialTheme.colorScheme.outline
                                                            )
                                                            Text(
                                                                text = assessment.createdAt.date.format(regularDateFormat),
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MaterialTheme.colorScheme.outline
                                                            )
                                                        }
                                                    }
                                                }
                                            }
                                        }
                                        day?.day?.homework?.collectAsState(emptySet())?.value?.let { homeworkItems ->
                                            homeworkItems.forEach forEachHomework@{ homework ->
                                                val subject = homework.subjectInstance?.collectAsResultingFlow()?.value
                                                val createdBy by when (homework.creator) {
                                                    is AppEntity.VppId -> homework.creator.vppId.collectAsLoadingState("")
                                                    is AppEntity.Profile -> homework.creator.profile.collectAsLoadingState("")
                                                }
                                                var boxHeight by remember { mutableStateOf(0.dp) }
                                                if (subject == null) return@forEachHomework
                                                Box(
                                                    modifier = Modifier
                                                        .padding(end = 8.dp)
                                                        .fillMaxWidth()
                                                        .clip(RoundedCornerShape(16.dp))
                                                        .clickable { displayHomeworkId = homework.id }
                                                        .onSizeChanged { with(localDensity) { boxHeight = it.height.toDp() } }
                                                ) {
                                                    Box(
                                                        modifier = Modifier
                                                            .align(Alignment.CenterStart)
                                                            .width(4.dp)
                                                            .height((boxHeight - 32.dp).coerceAtLeast(0.dp))
                                                            .clip(RoundedCornerShape(0, 50, 50, 0))
                                                            .background(subject.subject.subjectColor().getGroup().color)
                                                    )
                                                    Column(
                                                        modifier = Modifier
                                                            .padding(16.dp)
                                                            .fillMaxWidth()
                                                    ) {
                                                        Row {
                                                            SubjectIcon(
                                                                modifier = Modifier.size(MaterialTheme.typography.titleLarge.lineHeight.toDp()),
                                                                subject = subject.subject
                                                            )
                                                            Spacer(Modifier.size(8.dp))
                                                            Column {
                                                                Text(
                                                                    text = buildString {
                                                                        append("Hausaufgabe in ")
                                                                        append(subject.subject)
                                                                    },
                                                                    style = MaterialTheme.typography.titleLarge
                                                                )
                                                                val tasks = homework.tasks.collectAsState(emptyList())
                                                                Text(
                                                                    text = tasks.value.joinToString("\n") { "- ${it.content}" },
                                                                    style = MaterialTheme.typography.bodyMedium
                                                                )
                                                            }
                                                        }
                                                        HorizontalDivider(Modifier.padding(8.dp))
                                                        Row(
                                                            modifier = Modifier
                                                                .padding(horizontal = 8.dp)
                                                                .fillMaxWidth(),
                                                            verticalAlignment = Alignment.CenterVertically,
                                                            horizontalArrangement = Arrangement.SpaceBetween
                                                        ) {
                                                            if (createdBy is CacheState.Loading) CircularProgressIndicator(Modifier.size(MaterialTheme.typography.labelMedium.lineHeight.toDp()))
                                                            else Text(
                                                                text = buildString {
                                                                    val creator = (createdBy as? CacheState.Done)?.data
                                                                    append(when (creator) {
                                                                        is VppId -> creator.name
                                                                        is Profile -> creator.name
                                                                        else -> ""
                                                                    })
                                                                },
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MaterialTheme.colorScheme.outline
                                                            )
                                                            Text(
                                                                text = homework.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date.format(regularDateFormat),
                                                                style = MaterialTheme.typography.labelMedium,
                                                                color = MaterialTheme.colorScheme.outline
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
                }
            }
        }
    }

    displayAssessmentId?.let { AssessmentDetailDrawer(
        assessmentId = it,
        onDismiss = { displayAssessmentId = null }
    ) }

    displayHomeworkId?.let { HomeworkDetailDrawer(
        homeworkId = it,
        onDismiss = { displayHomeworkId = null }
    ) }
}