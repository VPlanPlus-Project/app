package plus.vplan.app.feature.calendar.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavHostController
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.until
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.assessment.ui.components.create.NewAssessmentDrawer
import plus.vplan.app.feature.assessment.ui.components.detail.AssessmentDetailDrawer
import plus.vplan.app.feature.calendar.ui.components.DisplaySelectType
import plus.vplan.app.feature.calendar.ui.components.agenda.AssessmentCard
import plus.vplan.app.feature.calendar.ui.components.agenda.Head
import plus.vplan.app.feature.calendar.ui.components.agenda.HomeworkCard
import plus.vplan.app.feature.calendar.ui.components.calendar.CalendarView
import plus.vplan.app.feature.calendar.ui.components.date_selector.ScrollableDateSelector
import plus.vplan.app.feature.calendar.ui.components.date_selector.weekHeight
import plus.vplan.app.feature.home.ui.components.FollowingLessons
import plus.vplan.app.feature.homework.ui.components.NewHomeworkDrawer
import plus.vplan.app.feature.homework.ui.components.detail.HomeworkDetailDrawer
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.ui.components.MultiFab
import plus.vplan.app.ui.components.MultiFabItem
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.inWholeMinutes
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.untilText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.book_marked
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.info
import kotlin.math.roundToInt

private const val CONTENT_PAGER_SIZE = 800
private const val CALENDAR_SCREEN_START_PADDING_MINUTES = 15

@Composable
fun CalendarScreen(
    navHostController: NavHostController,
    paddingValues: PaddingValues,
    viewModel: CalendarViewModel
) {
    val state = viewModel.state
    CalendarScreenContent(
        state = state,
        paddingValues = paddingValues,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun CalendarScreenContent(
    state: CalendarState,
    paddingValues: PaddingValues,
    onEvent: (event: CalendarEvent) -> Unit
) {
    val localDensity = LocalDensity.current

    var displayHomeworkId by rememberSaveable { mutableStateOf<Int?>(null) }
    var displayAssessmentId by rememberSaveable { mutableStateOf<Int?>(null) }

    var isNewAssessmentDrawerOpen by rememberSaveable { mutableStateOf(false) }
    var isNewHomeworkDrawerOpen by rememberSaveable { mutableStateOf(false) }

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

    val minute = 1.25.dp

    val scrollConnection = remember(state.days[state.selectedDate]) {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                val isContentAtTop = with(localDensity) { contentScrollState.value <= ((state.start.inWholeMinutes().toFloat() - CALENDAR_SCREEN_START_PADDING_MINUTES) * minute).roundToPx() }
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

    var isMultiFabExpanded by rememberSaveable { mutableStateOf(false) }
    var multiFabFabPosition by remember { mutableStateOf(Offset.Zero) }

    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
            .thenIf(Modifier.nestedScroll(scrollConnection)) { state.displayType == DisplayType.Calendar }
    ) {
        Column(Modifier.fillMaxSize()) {
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
                    val item = (CONTENT_PAGER_SIZE / 2) + LocalDate.now().until(state.selectedDate, DateTimeUnit.DAY)
                    when (state.displayType) {
                        DisplayType.Calendar -> {
                            pagerState.animateScrollToPage(item)
                            lazyListState.scrollToItem(item)
                        }
                        DisplayType.Agenda -> {
                            pagerState.scrollToPage(item)
                            if (!isUserDraggingList) {
                                isScrollAnimationRunning = true
                                lazyListState.animateScrollToItem(item)
                            }
                        }
                    }
                }
            }

            LaunchedEffect(state.start) {
                contentScrollState.animateScrollTo(with(localDensity) { ((state.start.inWholeMinutes().toFloat() - CALENDAR_SCREEN_START_PADDING_MINUTES) * minute).coerceAtLeast(0.dp).roundToPx() })
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
                            val day = state.days[date]
                            CalendarView(
                                date = date,
                                lessons = day?.lessons?.toList().orEmpty().sortedBy { it.lessonTimeItem!!.start },
                                limitTimeSpanToLessonsLowerBound = state.start,
                                info = day?.day?.info,
                                contentScrollState = contentScrollState
                            )
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
                                    var stickySideHeight by remember { mutableStateOf(0) }
                                    val currentDayHeight = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
                                    Column(
                                        modifier = Modifier
                                            .width(48.dp)
                                            .thenIf(Modifier.offset { IntOffset(0, lazyListState.firstVisibleItemScrollOffset.coerceAtMost((currentDayHeight-stickySideHeight).coerceAtLeast(0))) }) { state.selectedDate == date }
                                            .onSizeChanged { stickySideHeight = it.height },
                                        horizontalAlignment = Alignment.CenterHorizontally
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
                                                    color =
                                                    if (date < LocalDate.now()) MaterialTheme.colorScheme.outline
                                                    else if (date == LocalDate.now()) MaterialTheme.colorScheme.onPrimary
                                                    else if (date.dayOfWeek.isoDayNumber >= 6) colors[CustomColor.Red]!!.getGroup().color
                                                    else MaterialTheme.colorScheme.onSurface
                                                )
                                                Text(
                                                    text = date.dayOfMonth.toString(),
                                                    style = MaterialTheme.typography.titleSmall,
                                                    color = if (date < LocalDate.now()) MaterialTheme.colorScheme.outline
                                                    else if (date == LocalDate.now()) MaterialTheme.colorScheme.onPrimary
                                                    else MaterialTheme.colorScheme.onSurface
                                                )
                                            }
                                        }
                                        if (date.dayOfWeek == DayOfWeek.MONDAY) {
                                            val week = day?.day?.week?.collectAsResultingFlow()?.value
                                            if (week != null) Text(
                                                text = listOf("KW ${week.calendarWeek}", "SW ${week.weekIndex}", week.weekType).joinToString("\n"),
                                                color = MaterialTheme.colorScheme.outline,
                                                style = MaterialTheme.typography.labelSmall,
                                                textAlign = TextAlign.Center
                                            )
                                        }
                                    }
                                    Column {
                                        Head(
                                            date = date,
                                            dayType = day?.day?.dayType ?: Day.DayType.UNKNOWN,
                                            lessons = day?.lessons.orEmpty().distinctBy { it.lessonTimeItem!!.lessonNumber }.count(),
                                            start = day?.lessons?.minOfOrNull { it.lessonTimeItem!!.start },
                                            end = day?.lessons?.maxOfOrNull { it.lessonTimeItem!!.end },
                                            showLessons = showLessons,
                                            onClick = {
                                                when (day?.day?.dayType) {
                                                    Day.DayType.REGULAR -> showLessons = !showLessons
                                                    else -> Unit
                                                }
                                            }
                                        )
                                        if (day?.day?.info != null) {
                                            InfoCard(
                                                modifier = Modifier
                                                    .padding(vertical = 4.dp, horizontal = 8.dp),
                                                imageVector = Res.drawable.info,
                                                title = "Informationen deiner Schule",
                                                text = day.day.info,
                                            )
                                        }
                                        AnimatedVisibility(
                                            visible = showLessons,
                                            enter = expandVertically(),
                                            exit = shrinkVertically(),
                                            modifier = Modifier.fillMaxWidth()
                                        ) lessonsSection@{
                                            Column {
                                                FollowingLessons(
                                                    modifier = Modifier.padding(horizontal = 4.dp),
                                                    showFirstGradient = false,
                                                    date = date,
                                                    paddingStart = 8.dp,
                                                    lessons = day?.lessons.orEmpty().groupBy { l -> l.lessonTimeItem!!.lessonNumber }.toList().sortedBy { it.first }.toMap()
                                                )
                                            }
                                        }
                                        Column(Modifier.fillMaxWidth()) assessments@{
                                            day?.day?.assessments?.collectAsState(emptyList())?.value?.let { assessments ->
                                                assessments.forEach forEachAssessment@{ assessment ->
                                                    AssessmentCard(
                                                        assessment = assessment,
                                                        onClick = { displayAssessmentId = assessment.id }
                                                    )
                                                }
                                            }
                                            if (state.currentProfile == null) return@items
                                            day?.day?.homework?.collectAsState(emptySet())?.value?.let { homeworkItems ->
                                                homeworkItems.forEach forEachHomework@{ homework ->
                                                    HomeworkCard(
                                                        homework = homework,
                                                        profile = state.currentProfile,
                                                        onClick = { displayHomeworkId = homework.id }
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

        FloatingActionButton(
            onClick = { isMultiFabExpanded = !isMultiFabExpanded },
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(8.dp))
                .onGloballyPositioned { multiFabFabPosition = it.positionOnScreen() },
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.rotate(animateFloatAsState(if (isMultiFabExpanded) 180+45f else 0f,  label = "close button").value)
            )
        }
    }

    MultiFab(
        isVisible = isMultiFabExpanded,
        items = listOfNotNull(
            if (state.currentProfile is Profile.StudentProfile) MultiFabItem(
                icon = { Icon(painter = painterResource(Res.drawable.book_marked), contentDescription = null, modifier = Modifier.size(24.dp)) },
                text = "Neue Hausaufgabe",
                textSuffix = { Spacer(Modifier.size(8.dp)) },
                onClick = { isMultiFabExpanded = false; isNewHomeworkDrawerOpen = true }
            ) else null,
            if (state.currentProfile is Profile.StudentProfile) MultiFabItem(
                icon = { Icon(imageVector = Icons.Default.Edit, contentDescription = null) },
                text = "Neue Leistungserhebung",
                textSuffix = { Spacer(Modifier.size(8.dp)) },
                onClick = { isMultiFabExpanded = false; isNewAssessmentDrawerOpen = true }
            ) else null
        ),
        fabPosition = multiFabFabPosition,
        onDismiss = { isMultiFabExpanded = false }
    )

    displayAssessmentId?.let { AssessmentDetailDrawer(
        assessmentId = it,
        onDismiss = { displayAssessmentId = null }
    ) }

    displayHomeworkId?.let { HomeworkDetailDrawer(
        homeworkId = it,
        onDismiss = { displayHomeworkId = null }
    ) }

    if (isNewAssessmentDrawerOpen) NewAssessmentDrawer { isNewAssessmentDrawerOpen = false }
    if (isNewHomeworkDrawerOpen) NewHomeworkDrawer { isNewHomeworkDrawerOpen = false }
}