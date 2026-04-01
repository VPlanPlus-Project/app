package plus.vplan.app.feature.calendar.page.ui


import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.BlurredEdgeTreatment
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.plus
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.assessment.detail.ui.AssessmentDetailDrawer
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.ProfileType
import plus.vplan.app.core.model.application.AppPlatform
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.MultiFab
import plus.vplan.app.core.ui.components.MultiFabItem
import plus.vplan.app.core.ui.modifier.noRippleClickable
import plus.vplan.app.core.ui.modifier.premiumShadow
import plus.vplan.app.core.ui.theme.displayFontFamily
import plus.vplan.app.core.ui.theme.monospaceFontFamily
import plus.vplan.app.core.ui.util.getNativeNavigationBarHeight
import plus.vplan.app.core.ui.util.toDp
import plus.vplan.app.core.utils.date.atStartOfWeek
import plus.vplan.app.core.utils.date.inWholeMinutes
import plus.vplan.app.core.utils.date.isoWeekNumber
import plus.vplan.app.core.utils.date.longMonthNames
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.date.plus
import plus.vplan.app.core.utils.date.shortDayOfWeekNames
import plus.vplan.app.core.utils.date.until
import plus.vplan.app.core.utils.date.untilText
import plus.vplan.app.core.utils.ui.color.transparent
import plus.vplan.app.feature.assessment.create.ui.NewAssessmentDrawer
import plus.vplan.app.feature.calendar.page.ui.components.Handle
import plus.vplan.app.feature.calendar.page.ui.components.Head
import plus.vplan.app.feature.calendar.page.ui.components.day_details.AnimatableBlock
import plus.vplan.app.feature.calendar.page.ui.components.day_details.Title
import plus.vplan.app.feature.calendar.page.ui.components.day_details.assessment.Assessment
import plus.vplan.app.feature.calendar.page.ui.components.day_details.homework.Homework
import plus.vplan.app.feature.calendar.view.domain.model.LessonRendering
import plus.vplan.app.feature.calendar.view.ui.CalendarViewLessons
import plus.vplan.app.feature.calendar.view.ui.components.LessonCard
import plus.vplan.app.feature.homework.create.ui.NewHomeworkDrawer
import plus.vplan.app.feature.homework.detail.ui.HomeworkDetailDrawer
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.time.Duration.Companion.days

private const val WEEK_PAGER_SIZE = 100
private const val CONTENT_PAGER_SIZE = 800

private const val MIN_FLING_VELOCITY = 400f
private const val MAX_FLING_VELOCITY = 2000f

@Composable
fun CalendarScreen(
    paddingValues: PaddingValues,
    viewModel: CalendarViewModel
) {
    val state by viewModel.state.collectAsState()
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
    val localHapticFeedback = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var displayHomeworkId by rememberSaveable { mutableStateOf<Int?>(null) }
    var displayAssessmentId by rememberSaveable { mutableStateOf<Int?>(null) }

    var isNewAssessmentDrawerOpen by rememberSaveable { mutableStateOf(false) }
    var isNewHomeworkDrawerOpen by rememberSaveable { mutableStateOf(false) }

    var isMultiFabExpanded by rememberSaveable { mutableStateOf(false) }
    var multiFabFabPosition by remember { mutableStateOf(Offset.Zero) }

    val contentScrollStates = remember { mutableMapOf<LocalDate, ScrollState>() }

    var lastCalendarDateSwitchInteractionSource by remember { mutableStateOf<CalendarDateSwitchInteractionSource?>(null) }

    /**
     * The default height of the date selector bar
     */
    val dateSelectorBarDefaultHeight = 48.dp

    /**
     * The height the date selector is allowed to expand to at most. This will be reached once the
     * dragToShowDayDetailsMinimumThreshold is reached.
     */
    val dateSelectorMaxExpandHeight = 64.dp

    val dateSelectorHeaderHeight = 64.dp

    /**
     * The amount that needs to be dragged until the first item in day details gets revealed.
     */
    val dragToShowDayDetailsMinimumThreshold = (dateSelectorMaxExpandHeight + dateSelectorHeaderHeight) * 2

    /**
     * Spacing below the week pager for the drag handle
     */
    val dateSelectorDragAreaHeight = 24.dp

    /**
     * Whether the user is actually dragging the date selector bar vertically
     */
    var isDraggingSelector by remember { mutableStateOf(false) }

    val contentScrollState = rememberScrollState()

    /**
     * Whether the user is scrolling in the calendar view
     */
    val isScrollingLessonView by contentScrollState.interactionSource.collectIsDraggedAsState()

    val userIsScrollingVertically = isScrollingLessonView || isDraggingSelector

    /**
     * How many px the user has dragged the date selector bar vertically
     */
    val userDragDistance = remember { Animatable(0f) }

    var dateSelectorWrapperWidth by remember { mutableStateOf<Dp?>(null) }

    val dragToShowDayDetailsMinimumThresholdPx = with(localDensity) { dragToShowDayDetailsMinimumThreshold.toPx() }

    val rawProgressToDayDetailsMinimumThreshold = (userDragDistance.value.toDp() / dragToShowDayDetailsMinimumThreshold)
        .coerceIn(0f, 1f)

    val calendarHeaderHeight = if (state.platform == AppPlatform.Android) TopAppBarDefaults.TopAppBarExpandedHeight else getNativeNavigationBarHeight() + 8.dp
    val currentDateSelectorHeight = dateSelectorBarDefaultHeight + rawProgressToDayDetailsMinimumThreshold * (dateSelectorMaxExpandHeight - dateSelectorBarDefaultHeight)

    val resultingHeadHeight = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding() +
        currentDateSelectorHeight +
        dateSelectorDragAreaHeight +
        calendarHeaderHeight

    val isMinimumDayDetailsThresholdReached = userDragDistance.value.toDp() >= dragToShowDayDetailsMinimumThreshold
    val isMinimumDayDetailsThresholdTargeted = userDragDistance.targetValue.toDp() >= dragToShowDayDetailsMinimumThreshold

    LaunchedEffect(isMinimumDayDetailsThresholdReached) {
        if (!isMinimumDayDetailsThresholdReached) return@LaunchedEffect
        if (!isDraggingSelector) return@LaunchedEffect

        localHapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
    }

    val weekPagerState = rememberPagerState(
        initialPage = WEEK_PAGER_SIZE / 2
    ) { WEEK_PAGER_SIZE }

    Box(Modifier.fillMaxSize()) {
        val velocityTracker = remember { VelocityTracker() }
        Column(
            modifier = Modifier
                .zIndex(100f)
                .fillMaxWidth()
                .height(resultingHeadHeight)
                .premiumShadow(
                    color = Color.Black.copy(alpha = 0.1f),
                    blurRadius = 8.dp,
                    offsetY = 2.dp,
                    borderRadius = 8.dp
                )
                .clip(RoundedCornerShape(bottomEnd = 16.dp, bottomStart = 16.dp))
                .background(MaterialTheme.colorScheme.surfaceContainerLow)
                .onSizeChanged { dateSelectorWrapperWidth = with(localDensity) { it.width.toDp() } }
                .pointerInput(Unit) {
                    detectVerticalDragGestures(
                        onDragStart = {
                            velocityTracker.resetTracking()
                            isDraggingSelector = true
                            scope.launch { userDragDistance.stop() }
                        },
                        onDragEnd = {
                            isDraggingSelector = false

                            val velocityY = velocityTracker.calculateVelocity().y
                            settleUserDragDistance(
                                scope = scope,
                                userDragDistance = userDragDistance,
                                dragThresholdPx = dragToShowDayDetailsMinimumThresholdPx,
                                velocityY = velocityY
                            )
                        },
                        onDragCancel = {
                            isDraggingSelector = false
                            scope.launch { userDragDistance.animateTo(0f) }
                        },
                        onVerticalDrag = { change, dragAmount ->
                            velocityTracker.addPointerInputChange(change)
                            scope.launch {
                                val newValue = (userDragDistance.value + dragAmount).coerceAtLeast(0f)
                                userDragDistance.snapTo(newValue)
                            }
                            change.consume()
                        }
                    )
                },
            verticalArrangement = Arrangement.Bottom,
        ) dateSelectorWrapper@{
            LaunchedEffect(weekPagerState.targetPage) {
                if (lastCalendarDateSwitchInteractionSource != CalendarDateSwitchInteractionSource.WeekSelector) return@LaunchedEffect
                localHapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                val targetSelectedDate = LocalDate.now() + ((weekPagerState.targetPage - WEEK_PAGER_SIZE / 2).toLong() * 7).days
                onEvent(CalendarEvent.SelectDate(targetSelectedDate))
            }

            LaunchedEffect(state.selectedDate) {
                val targetPage = floor((state.selectedDate.atStartOfWeek().toEpochDays() - LocalDate.now().atStartOfWeek().toEpochDays()) / 7 + WEEK_PAGER_SIZE / 2f).toInt()
                if (targetPage == weekPagerState.targetPage) return@LaunchedEffect
                weekPagerState.animateScrollToPage(targetPage)
            }

            val isDraggingWeekPager by weekPagerState.interactionSource.collectIsDraggedAsState()
            LaunchedEffect(isDraggingWeekPager) {
                if (isDraggingWeekPager) lastCalendarDateSwitchInteractionSource = CalendarDateSwitchInteractionSource.WeekSelector
            }

            val isMinimumReachedAnimatedSidePadding by animateFloatAsState(
                targetValue = if (isMinimumDayDetailsThresholdTargeted) 1f else 0f,
                animationSpec = spring(
                    dampingRatio = 0.6f,
                    stiffness = 270f
                ),
            )

            Head(
                title = "Kalender",
                subtitle = "KW ${state.selectedDate.isoWeekNumber()}",
                showTodayButton = state.selectedDate != LocalDate.now(),
                onTodayClicked = { onEvent(CalendarEvent.SelectDate(LocalDate.now())) },
                onCreateHomeworkClicked = { isNewHomeworkDrawerOpen = true },
                onCreateAssessmentClicked = { isNewAssessmentDrawerOpen = true },
            )

            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(currentDateSelectorHeight),
                state = weekPagerState,
                userScrollEnabled = !userIsScrollingVertically,
                beyondViewportPageCount = 2,
                snapPosition = SnapPosition.Center,
                pageSize = dateSelectorWrapperWidth?.let { PageSize.Fixed( it - (isMinimumReachedAnimatedSidePadding * 64).dp) } ?: PageSize.Fill
            ) { page ->
                val week = LocalDate.now().atStartOfWeek() + ((page - WEEK_PAGER_SIZE / 2).toLong() * 7).days

                Row(
                    modifier = Modifier
                        .padding(horizontal = ((1-isMinimumReachedAnimatedSidePadding) * 8.dp).coerceAtLeast(0.dp))
                        .fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    repeat(7) {
                        val date = week + it.days
                        val day = state.calendarDays[date] ?: CalendarDay(date)

                        plus.vplan.app.feature.calendar.page.ui.components.Day(
                            modifier = Modifier.fillMaxHeight(),
                            isSelected = state.selectedDate == date,
                            date = date,
                            isGrayedOut = page != weekPagerState.targetPage,
                            isHoliday = day.dayType == Day.DayType.WEEKEND || day.dayType == Day.DayType.HOLIDAY,
                            onClick = { onEvent(CalendarEvent.SelectDate(date)) }
                        )
                    }
                }
            }

            Handle(
                modifier = Modifier.height(dateSelectorDragAreaHeight),
                isDragging = isDraggingSelector
            )
        }

        val calendarPagerState = rememberPagerState(CONTENT_PAGER_SIZE / 2) { CONTENT_PAGER_SIZE }

        LaunchedEffect(calendarPagerState.targetPage) {
            if (lastCalendarDateSwitchInteractionSource != CalendarDateSwitchInteractionSource.CalendarPager) return@LaunchedEffect
            localHapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
            val targetSelectedDate = LocalDate.now() + (calendarPagerState.targetPage - CONTENT_PAGER_SIZE / 2).toLong().days
            onEvent(CalendarEvent.SelectDate(targetSelectedDate))
        }

        LaunchedEffect(state.selectedDate) {
            val targetPage = floor(state.selectedDate.toEpochDays() - LocalDate.now().toEpochDays() + CONTENT_PAGER_SIZE / 2f).toInt()
            if (targetPage == calendarPagerState.targetPage) return@LaunchedEffect
            calendarPagerState.animateScrollToPage(targetPage)
        }

        val isDraggingContentPager by calendarPagerState.interactionSource.collectIsDraggedAsState()
        LaunchedEffect(isDraggingContentPager) {
            if (isDraggingContentPager) lastCalendarDateSwitchInteractionSource = CalendarDateSwitchInteractionSource.CalendarPager
        }

        LaunchedEffect(calendarPagerState.currentPageOffsetFraction) {
            if (!isDraggingContentPager) return@LaunchedEffect
        }

        val minute = 1.5.dp
        val distanceToStart = minute * state.start.inWholeMinutes()

        val easedProgress = rawProgressToDayDetailsMinimumThreshold * rawProgressToDayDetailsMinimumThreshold

        val contentBlurRadius = (easedProgress * 48).dp

        Box(
            modifier = Modifier
                .zIndex(98f)
                .blur(
                    radius = contentBlurRadius,
                    edgeTreatment = BlurredEdgeTreatment.Unbounded
                )
                .padding(bottom = paddingValues.calculateBottomPadding())
                .fillMaxSize()
                .nestedScroll(
                    rememberContentNestedScrollConnection(
                        scope = scope,
                        userDragDistance = userDragDistance,
                        dragThresholdPx = dragToShowDayDetailsMinimumThresholdPx,
                        contentScrollState = contentScrollState
                    )
                )
                .verticalScroll(contentScrollState)
                .height((minute * 60 * 24 - distanceToStart).coerceAtLeast(0.dp))
        ) {
            val baseTopPadding = WindowInsets.safeDrawing.asPaddingValues().calculateTopPadding() + dateSelectorBarDefaultHeight + dateSelectorDragAreaHeight
            val contentTopPadding = baseTopPadding - distanceToStart

            var maxTimeIndicatorWidth by remember { mutableStateOf(0.dp) }

            // Draw lesson indicators
            repeat(24) {
                var timeWidth by remember { mutableStateOf(0.dp) }
                val colorScheme = MaterialTheme.colorScheme
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset {
                            IntOffset(
                                x = 0,
                                y = (minute * (it * 60) + contentTopPadding).roundToPx()
                            )
                        }
                        .drawBehind {
                            drawLine(
                                color = colorScheme.outlineVariant.transparent(.7f),
                                start = Offset(0f, 0f),
                                end = Offset(this.size.width - with(localDensity) { (timeWidth - 8.dp).toPx() }, 0f),
                                strokeWidth = with(localDensity) { 1.dp.toPx() }
                            )
                        },
                )
                val timeFont = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = it.toString().padStart(2, '0'),
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .onSizeChanged {
                            timeWidth = with(localDensity) { it.width.toDp() }
                            if (maxTimeIndicatorWidth < timeWidth) maxTimeIndicatorWidth = timeWidth
                        }
                        .padding(horizontal = 4.dp)
                        .offset {
                            IntOffset(
                                x = 0,
                                y = (minute * (it * 60) + contentTopPadding - timeFont.lineHeight.toDp() / 2).roundToPx()
                            )
                        },
                    style = timeFont,
                    fontFamily = monospaceFontFamily(),

                )
            }

            HorizontalPager(
                modifier = Modifier.fillMaxSize(),
                userScrollEnabled = !userIsScrollingVertically,
                state = calendarPagerState,
                pageSize = PageSize.Fill,
                beyondViewportPageCount = 1,
            ) { page ->
                var pageWidth by remember { mutableStateOf(0.dp) }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = maxTimeIndicatorWidth + 4.dp,
                            end = 8.dp,
                        )
                        .onSizeChanged { pageWidth = with(localDensity) { it.width.toDp() } }
                ) {
                    val date = LocalDate.now().plus((page - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
                    val day = state.calendarDays[date] ?: CalendarDay(date)
                    val lessonsForCalendarView = remember(day.lessons) {
                        CalendarViewLessons(day.lessons ?: LessonRendering.ListView(emptyMap()))
                    }

                    when (lessonsForCalendarView) {
                        is CalendarViewLessons.CalendarView -> {
                            lessonsForCalendarView.lessons.forEach { lesson ->
                                val y = lesson.lesson.lessonTime!!.start.inWholeMinutes().toFloat() * minute + contentTopPadding
                                Box(
                                    modifier = Modifier
                                        .width(pageWidth / lesson.of)
                                        .padding(horizontal = 4.dp)
                                        .height(lesson.lesson.lessonTime!!.start.until(lesson.lesson.lessonTime!!.end).inWholeMinutes.toFloat() * minute)
                                        .offset {
                                            IntOffset(
                                                x = ((pageWidth / lesson.of) * lesson.sideShift).roundToPx(),
                                                y = y.roundToPx()
                                            )
                                        }
                                ) {
                                    LessonCard(
                                        modifier = Modifier.fillMaxSize(),
                                        lesson = lesson,
                                        currentProfileType = state.currentProfile?.profileType ?: ProfileType.STUDENT
                                    )
                                }
                            }
                        }

                        is CalendarViewLessons.ListView -> {
                            // TODO
                            Text("Einige Stundenzeiten fehlen, daher kann keine Kalenderansicht gezeigt werden. Dies muss noch implementiert werden")
                        }
                    }
                }
            }
        }

        val dayDetailPager = rememberPagerState(CONTENT_PAGER_SIZE / 2) { CONTENT_PAGER_SIZE }
        if (userDragDistance.value > 0f) {
            Box(
                modifier = Modifier
                    .zIndex(99f)
                    .alpha(rawProgressToDayDetailsMinimumThreshold)
                    .padding(
                        top = resultingHeadHeight,
                        bottom = paddingValues.calculateBottomPadding()
                    )
                    .background(MaterialTheme.colorScheme.surfaceContainerLowest.transparent(.4f))
                    .fillMaxSize()
                    .noRippleClickable { scope.launch { userDragDistance.animateTo(0f) } }
            ) {
                LaunchedEffect(dayDetailPager.targetPage) {
                    if (lastCalendarDateSwitchInteractionSource != CalendarDateSwitchInteractionSource.DayDetailPager) return@LaunchedEffect
                    localHapticFeedback.performHapticFeedback(HapticFeedbackType.GestureThresholdActivate)
                    val targetSelectedDate = LocalDate.now() + (dayDetailPager.targetPage - CONTENT_PAGER_SIZE / 2).toLong().days
                    onEvent(CalendarEvent.SelectDate(targetSelectedDate))
                }

                LaunchedEffect(state.selectedDate) {
                    val targetPage = floor(state.selectedDate.toEpochDays() - LocalDate.now().toEpochDays() + CONTENT_PAGER_SIZE / 2f).toInt()
                    if (targetPage == dayDetailPager.targetPage) return@LaunchedEffect
                    dayDetailPager.animateScrollToPage(targetPage)
                }

                val isDraggingDayDetailPager by dayDetailPager.interactionSource.collectIsDraggedAsState()
                LaunchedEffect(isDraggingDayDetailPager) {
                    if (isDraggingDayDetailPager) lastCalendarDateSwitchInteractionSource = CalendarDateSwitchInteractionSource.DayDetailPager
                }

                Column(Modifier.fillMaxSize()) {
                    val scale by animateFloatAsState(
                        targetValue = if (!isDraggingSelector) 1f else .9f,
                        animationSpec = spring(
                            dampingRatio = 0.4f,
                            stiffness = 270f
                        ),
                    )
                    AnimatedVisibility(
                        visible = userDragDistance.targetValue.toDp() >= dragToShowDayDetailsMinimumThreshold,
                        enter = fadeIn(),
                        exit = fadeOut(),
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .scale(scale)
                                .padding(top = 24.dp)
                                .padding(horizontal = 16.dp)
                                .alpha(1-(dayDetailPager.currentPageOffsetFraction.absoluteValue * 2))
                        ) {
                            Title(
                                icon = null,
                                title = LocalDate.now() untilText state.selectedDate,
                            )
                            Text(
                                text = state.selectedDate.format(LocalDate.Format {
                                    dayOfWeek(shortDayOfWeekNames)
                                    chars(", ")
                                    day(Padding.NONE)
                                    chars(". ")
                                    monthName(longMonthNames)
                                    char(' ')
                                    year()
                                }),
                                modifier = Modifier.padding(horizontal = 8.dp),
                                style = MaterialTheme.typography.headlineMedium,
                                fontFamily = displayFontFamily()
                            )
                        }
                    }

                    HorizontalPager(
                        state = dayDetailPager,
                        pageSize = PageSize.Fill,
                        userScrollEnabled = !userIsScrollingVertically,
                        beyondViewportPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val date = LocalDate.now().plus((page - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
                        val day = state.calendarDays[date] ?: CalendarDay(date)

                        val dayDetailsScrollState = rememberScrollState()

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .nestedScroll(
                                    rememberDayDetailsNestedScrollConnection(
                                        scope = scope,
                                        userDragDistance = userDragDistance,
                                        dragThresholdPx = dragToShowDayDetailsMinimumThresholdPx,
                                        contentScrollState = dayDetailsScrollState,
                                    )
                                )
                                .verticalScroll(dayDetailsScrollState)
                        ) {
                            var index = 0
                            var visibleIndex by remember { mutableStateOf(-1) }

                            if (day.info != null) {
                                AnimatableBlock(
                                    scale = scale,
                                    index = index,
                                    visibleIndex = visibleIndex,
                                    icon = painterResource(CoreUiRes.drawable.megaphone),
                                    title = "Informationen deiner Schule"
                                ) {
                                    Text(
                                        text = day.info,
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp)
                                    )
                                }

                                index++
                            }

                            if (day.assessments.isNotEmpty()) {
                                AnimatableBlock(
                                    scale = scale,
                                    index = index,
                                    visibleIndex = visibleIndex,
                                    icon = painterResource(CoreUiRes.drawable.pencil),
                                    title = "Leistungen"
                                ) {
                                    day.assessments.forEach { assessment ->
                                        Assessment(
                                            assessment = assessment,
                                            onClick = { displayAssessmentId = assessment.id }
                                        )
                                    }
                                }

                                index++
                            }

                            if (day.homework.isNotEmpty()) {
                                AnimatableBlock(
                                    scale = scale,
                                    index = index,
                                    visibleIndex = visibleIndex,
                                    icon = painterResource(CoreUiRes.drawable.book_open),
                                    title = "Hausaufgaben"
                                ) {
                                    day.homework.forEach { homework ->
                                        Homework(
                                            homework = homework,
                                            currentProfile = state.currentProfile,
                                            onClick = { displayHomeworkId = homework.id }
                                        )
                                    }
                                }

                                index++
                            }

                            LaunchedEffect(isMinimumDayDetailsThresholdTargeted) {
                                if (!isMinimumDayDetailsThresholdTargeted) {
                                    visibleIndex = -1
                                    return@LaunchedEffect
                                }

                                scope.launch {
                                    repeat(index + 1) {
                                        visibleIndex++
                                        delay(50)
                                    }
                                }
                            }
                        }
                    }
                }

            }
        }

        LaunchedEffect(dayDetailPager.currentPageOffsetFraction) {
            if (lastCalendarDateSwitchInteractionSource != CalendarDateSwitchInteractionSource.DayDetailPager) return@LaunchedEffect
            calendarPagerState.scrollToPage(dayDetailPager.currentPage, dayDetailPager.currentPageOffsetFraction)
        }

        if (state.platform != AppPlatform.iOS) FloatingActionButton(
            onClick = { isMultiFabExpanded = !isMultiFabExpanded },
            modifier = Modifier
                .zIndex(101f)
                .padding(16.dp)
                .padding(bottom = paddingValues.calculateBottomPadding())
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(8.dp))
                .onGloballyPositioned { multiFabFabPosition = it.positionOnScreen() },
        ) {
            Icon(
                painter = painterResource(CoreUiRes.drawable.plus),
                contentDescription = null,
                modifier = Modifier.rotate(animateFloatAsState(if (isMultiFabExpanded) 180+45f else 0f,  label = "close button").value)
            )
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

    if (isNewAssessmentDrawerOpen) NewAssessmentDrawer(selectedDate = state.selectedDate) { isNewAssessmentDrawerOpen = false }
    if (isNewHomeworkDrawerOpen) NewHomeworkDrawer(selectedDate = state.selectedDate) { isNewHomeworkDrawerOpen = false }

    MultiFab(
        isVisible = isMultiFabExpanded,
        items = listOfNotNull(
            if (state.currentProfile is Profile.StudentProfile) MultiFabItem(
                icon = { Icon(painter = painterResource(CoreUiRes.drawable.book_marked), contentDescription = null, modifier = Modifier.size(24.dp)) },
                text = "Neue Hausaufgabe",
                textSuffix = { Spacer(Modifier.size(8.dp)) },
                onClick = { isMultiFabExpanded = false; isNewHomeworkDrawerOpen = true }
            ) else null,
            if (state.currentProfile is Profile.StudentProfile) MultiFabItem(
                icon = { Icon(painter = painterResource(CoreUiRes.drawable.pencil), contentDescription = null) },
                text = "Neue Leistungserhebung",
                textSuffix = { Spacer(Modifier.size(8.dp)) },
                onClick = { isMultiFabExpanded = false; isNewAssessmentDrawerOpen = true }
            ) else null
        ),
        fabPosition = multiFabFabPosition,
        onDismiss = { isMultiFabExpanded = false }
    )
}

enum class CalendarDateSwitchInteractionSource {
    CalendarPager, WeekSelector, DayDetailPager
}

@Composable
private fun rememberContentNestedScrollConnection(
    scope: CoroutineScope,
    userDragDistance: Animatable<Float, AnimationVector1D>,
    dragThresholdPx: Float,
    contentScrollState: ScrollState,
): NestedScrollConnection {
    return remember(scope, userDragDistance, dragThresholdPx, contentScrollState) {
        ContentNestedScrollConnection(
            scope = scope,
            userDragDistance = userDragDistance,
            dragThresholdPx = dragThresholdPx,
            contentScrollState = contentScrollState,
        )
    }
}

private class ContentNestedScrollConnection(
    private val scope: CoroutineScope,
    private val userDragDistance: Animatable<Float, AnimationVector1D>,
    private val dragThresholdPx: Float,
    private val contentScrollState: ScrollState,
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val deltaY = available.y

        // Only intercept downward drags (scrolling up) when the content is already at the top
        if (deltaY <= 0f) return Offset.Zero
        if (contentScrollState.value > 0) return Offset.Zero

        val current = userDragDistance.value
        val newValue = (current + deltaY).coerceAtLeast(0f)
        if (newValue == current) return Offset.Zero

        scope.launch { userDragDistance.snapTo(newValue) }
        return Offset(x = 0f, y = deltaY)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        // If content is at top and user flings downward, convert fling to selector expansion
        if (available.y > 0f && contentScrollState.value == 0) {
            settleUserDragDistance(
                scope = scope,
                userDragDistance = userDragDistance,
                dragThresholdPx = dragThresholdPx,
                velocityY = available.y,
            )
            return available
        }
        return Velocity.Zero
    }
}

@Composable
private fun rememberDayDetailsNestedScrollConnection(
    scope: CoroutineScope,
    userDragDistance: Animatable<Float, AnimationVector1D>,
    dragThresholdPx: Float,
    contentScrollState: ScrollState,
): NestedScrollConnection {
    return remember(scope, userDragDistance, dragThresholdPx, contentScrollState) {
        DayDetailsNestedScrollConnection(
            scope = scope,
            userDragDistance = userDragDistance,
            dragThresholdPx = dragThresholdPx,
            contentScrollState = contentScrollState,
        )
    }
}

private class DayDetailsNestedScrollConnection(
    private val scope: CoroutineScope,
    private val userDragDistance: Animatable<Float, AnimationVector1D>,
    private val dragThresholdPx: Float,
    private val contentScrollState: ScrollState,
) : NestedScrollConnection {

    override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
        val deltaY = available.y

        // Only intercept when the internal scroll is at the top, so we don't
        // fight with vertical scrolling of the block content.
        if (contentScrollState.value > 0) return Offset.Zero

        // Mirror selector drag: drag down to expand, up to collapse
        val current = userDragDistance.value
        val newValue = (current + deltaY).coerceAtLeast(0f)
        if (newValue == current) return Offset.Zero

        scope.launch { userDragDistance.snapTo(newValue) }
        return Offset(x = 0f, y = deltaY)
    }

    override suspend fun onPreFling(available: Velocity): Velocity {
        if (contentScrollState.value > 0) return Velocity.Zero

        settleUserDragDistance(
            scope = scope,
            userDragDistance = userDragDistance,
            dragThresholdPx = dragThresholdPx,
            velocityY = available.y,
        )
        return available
    }
}

private fun settleUserDragDistance(
    scope: CoroutineScope,
    userDragDistance: Animatable<Float, AnimationVector1D>,
    dragThresholdPx: Float,
    velocityY: Float,
) {
    val progress = (userDragDistance.value / dragThresholdPx).coerceIn(0f, 1f)
    val requiredVelocity = MAX_FLING_VELOCITY - (progress * (MAX_FLING_VELOCITY - MIN_FLING_VELOCITY))

    val isThresholdReached = progress >= 1f
    val isVelocityEnough = velocityY >= requiredVelocity

    val targetValue = if (isThresholdReached || isVelocityEnough) dragThresholdPx else 0f

    scope.launch {
        userDragDistance.animateTo(
            targetValue = targetValue,
            animationSpec = if (isThresholdReached) tween() else spring(
                dampingRatio = Spring.DampingRatioLowBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
}
