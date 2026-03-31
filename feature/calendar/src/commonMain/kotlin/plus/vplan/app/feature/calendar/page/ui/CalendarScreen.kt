package plus.vplan.app.feature.calendar.page.ui


import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.MutatePriority
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectVerticalDragGestures
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.gestures.stopScroll
import androidx.compose.foundation.interaction.collectIsDraggedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PageSize
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.input.pointer.util.addPointerInputChange
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.layout.positionOnScreen
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.MonthNames
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import kotlinx.datetime.until
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.assessment.detail.ui.AssessmentDetailDrawer
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.HomeworkStatus
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.ProfileType
import plus.vplan.app.core.model.application.AppPlatform
import plus.vplan.app.core.model.toName
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.Badge
import plus.vplan.app.core.ui.components.InfoCard
import plus.vplan.app.core.ui.components.MultiFab
import plus.vplan.app.core.ui.components.MultiFabItem
import plus.vplan.app.core.ui.components.SubjectIcon
import plus.vplan.app.core.ui.modifier.noRippleClickable
import plus.vplan.app.core.ui.modifier.premiumShadow
import plus.vplan.app.core.ui.modifier.thenIf
import plus.vplan.app.core.ui.theme.ColorToken
import plus.vplan.app.core.ui.theme.CustomColor
import plus.vplan.app.core.ui.theme.colors
import plus.vplan.app.core.ui.theme.customColors
import plus.vplan.app.core.ui.theme.displayFontFamily
import plus.vplan.app.core.ui.theme.getGroup
import plus.vplan.app.core.ui.theme.monospaceFontFamily
import plus.vplan.app.core.ui.util.getNativeNavigationBarHeight
import plus.vplan.app.core.ui.util.minusNativeNavigationBarHeight
import plus.vplan.app.core.ui.util.roundToPx
import plus.vplan.app.core.ui.util.toDp
import plus.vplan.app.core.utils.date.atStartOfWeek
import plus.vplan.app.core.utils.date.inWholeMinutes
import plus.vplan.app.core.utils.date.longMonthNames
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.date.plus
import plus.vplan.app.core.utils.date.shortDayOfWeekNames
import plus.vplan.app.core.utils.date.until
import plus.vplan.app.core.utils.date.untilText
import plus.vplan.app.core.utils.ui.color.transparent
import plus.vplan.app.feature.assessment.create.ui.NewAssessmentDrawer
import plus.vplan.app.feature.calendar.page.domain.model.DisplayType
import plus.vplan.app.feature.calendar.page.ui.components.DisplaySelectType
import plus.vplan.app.feature.calendar.page.ui.components.Handle
import plus.vplan.app.feature.calendar.page.ui.components.Head
import plus.vplan.app.feature.calendar.page.ui.components.date_selector.weekHeightDefault
import plus.vplan.app.feature.calendar.page.ui.components.day_details.Title
import plus.vplan.app.feature.calendar.page.ui.components.day_details.homework.TaskRow
import plus.vplan.app.feature.calendar.page.ui.components.day_details.info.Info
import plus.vplan.app.feature.calendar.view.domain.model.LessonRendering
import plus.vplan.app.feature.calendar.view.ui.CalendarView
import plus.vplan.app.feature.calendar.view.ui.CalendarViewLessons
import plus.vplan.app.feature.calendar.view.ui.components.AgendaHead
import plus.vplan.app.feature.calendar.view.ui.components.AssessmentCard
import plus.vplan.app.feature.calendar.view.ui.components.FollowingLessons
import plus.vplan.app.feature.calendar.view.ui.components.HomeworkCard
import plus.vplan.app.feature.calendar.view.ui.components.LessonCard
import plus.vplan.app.feature.homework.create.ui.NewHomeworkDrawer
import plus.vplan.app.feature.homework.detail.ui.HomeworkDetailDrawer
import kotlin.math.absoluteValue
import kotlin.math.floor
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days

private const val WEEK_PAGER_SIZE = 100
private const val CONTENT_PAGER_SIZE = 800

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
     * [dragToShowDayDetailsMinimumThreshold] is reached.
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
    var isDragging by remember { mutableStateOf(false) }

    /**
     * How many px the user has dragged the date selector bar vertically
     */
    val userDragDistance = remember { Animatable(0f) }

    var dateSelectorWrapperWidth by remember { mutableStateOf<Dp?>(null) }

    val rawProgressToDayDetailsMinimumThreshold = (userDragDistance.value.toDp() / dragToShowDayDetailsMinimumThreshold)
        .coerceIn(0f, 1f)

    val calendarHeaderHeight = if (state.platform == AppPlatform.Android) TopAppBarDefaults.TopAppBarExpandedHeight else (getNativeNavigationBarHeight() + 8.dp)
    val currentDateSelectorHeight = dateSelectorBarDefaultHeight + rawProgressToDayDetailsMinimumThreshold * (dateSelectorMaxExpandHeight - dateSelectorBarDefaultHeight)
    val currentDateSelectorHeaderHeight = dateSelectorHeaderHeight * rawProgressToDayDetailsMinimumThreshold

    val resultingHeadHeight = WindowInsets.safeDrawing.asPaddingValues()
        .minusNativeNavigationBarHeight()
        .calculateTopPadding() +
            currentDateSelectorHeight +
            dateSelectorDragAreaHeight +
            calendarHeaderHeight

    val isMinimumDayDetailsThresholdReached = userDragDistance.value.toDp() >= dragToShowDayDetailsMinimumThreshold
    val isMinimumDayDetailsThresholdTargeted = userDragDistance.targetValue.toDp() >= dragToShowDayDetailsMinimumThreshold

    LaunchedEffect(isMinimumDayDetailsThresholdReached) {
        if (!isMinimumDayDetailsThresholdReached) return@LaunchedEffect
        if (!isDragging) return@LaunchedEffect

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
                            isDragging = true
                            scope.launch { userDragDistance.stop() }
                        },
                        onDragEnd = {
                            isDragging = false

                            val minVelocity = 400f
                            val maxVelocity = 2000f

                            val progress = (userDragDistance.value / dragToShowDayDetailsMinimumThreshold.toPx())
                                .coerceIn(0f, 1f)
                            val requiredVelocity = maxVelocity - (progress * (maxVelocity - minVelocity))

                            val isThresholdReached = progress >= 1f
                            val isVelocityEnough = velocityTracker.calculateVelocity().y >= requiredVelocity

                            scope.launch {
                                userDragDistance.animateTo(
                                    targetValue = if (isThresholdReached || isVelocityEnough) dragToShowDayDetailsMinimumThreshold.toPx() else 0f,
                                    animationSpec = if (isThresholdReached) tween() else spring(
                                        dampingRatio = Spring.DampingRatioLowBouncy,
                                        stiffness = Spring.StiffnessLow
                                    )
                                )
                            }
                        },
                        onDragCancel = {
                            isDragging = false
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
                subtitle = "KW 22",
                currentDisplayType = state.displayType,
                showTodayButton = state.selectedDate != LocalDate.now(),
                onTodayClicked = { onEvent(CalendarEvent.SelectDate(LocalDate.now())) },
                onCreateHomeworkClicked = { isNewHomeworkDrawerOpen = true },
                onCreateAssessmentClicked = { isNewAssessmentDrawerOpen = true },
                onShowAgenda = { onEvent(CalendarEvent.SelectDisplayType(DisplayType.Agenda)) },
                onShowCalendar = { onEvent(CalendarEvent.SelectDisplayType(DisplayType.Calendar)) },
            )

            HorizontalPager(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(currentDateSelectorHeight),
                state = weekPagerState,
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
                isDragging = isDragging
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

        val contentScrollState = rememberScrollState()

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
                        .align(Alignment.TopEnd)
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
                state = calendarPagerState,
                pageSize = PageSize.Fill,
                beyondViewportPageCount = 1,
            ) { page ->
                var pageWidth by remember { mutableStateOf(0.dp) }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(
                            start = 8.dp,
                            end = maxTimeIndicatorWidth + 4.dp
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

                        is CalendarViewLessons.ListView -> {}
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
                        targetValue = if (!isDragging) 1f else .9f,
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

                    val pixelMovement = -4.dp.roundToPx()
                    val defaultEnterAnimation = fadeIn() + scaleIn(initialScale = .9f) + slideInVertically(tween(easing = FastOutSlowInEasing)) { pixelMovement }
                    val defaultExitAnimation = fadeOut() + scaleOut(targetScale = .9f) + slideOutVertically(tween(easing = FastOutSlowInEasing)) { pixelMovement }

                    HorizontalPager(
                        state = dayDetailPager,
                        pageSize = PageSize.Fill,
                        beyondViewportPageCount = 1,
                        modifier = Modifier.fillMaxSize()
                    ) { page ->
                        val date = LocalDate.now().plus((page - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
                        val day = state.calendarDays[date] ?: CalendarDay(date)

                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(rememberScrollState())
                        ) {
                            var index = 0
                            var visibleIndex by remember { mutableStateOf(-1) }

                            if (day.info != null) {
                                AnimatedVisibility(
                                    visible = visibleIndex >= index,
                                    enter = defaultEnterAnimation,
                                    exit = defaultExitAnimation,
                                ) {
                                    Info(
                                        modifier = Modifier
                                            .padding(top = 24.dp)
                                            .padding(horizontal = 16.dp)
                                            .scale(scale)
                                            .fillMaxWidth(),
                                        info = day.info
                                    )
                                }

                                index++
                            }

                            if (day.assessments.isNotEmpty()) AnimatedVisibility(
                                visible = visibleIndex >= index,
                                enter = defaultEnterAnimation,
                                exit = defaultExitAnimation,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(top = 24.dp)
                                        .padding(horizontal = 12.dp)
                                        .scale(scale)
                                        .fillMaxWidth()
                                ) {
                                    Title(
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        icon = painterResource(CoreUiRes.drawable.pencil),
                                        title = "Leistungen"
                                    )
                                    day.assessments.forEach { assessment ->
                                        Row(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { displayAssessmentId = assessment.id }
                                                .padding(8.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                        ) {
                                            SubjectIcon(
                                                modifier = Modifier.size(24.dp),
                                                subject = assessment.subjectInstance.subject
                                            )
                                            Text(
                                                text = buildString {
                                                    append(assessment.subjectInstance.subject)
                                                    append(" ")
                                                    append(assessment.type.toName())
                                                },
                                                style = MaterialTheme.typography.bodyMedium,
                                                fontWeight = FontWeight.Bold,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = assessment.description.replace("\n", " "),
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.outline,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                        }
                                    }
                                }

                                index++
                            }

                            if (day.homework.isNotEmpty()) AnimatedVisibility(
                                visible = visibleIndex >= index,
                                enter = defaultEnterAnimation,
                                exit = defaultExitAnimation,
                            ) {
                                Column(
                                    modifier = Modifier
                                        .padding(top = 24.dp)
                                        .padding(horizontal = 12.dp)
                                        .scale(scale)
                                        .fillMaxWidth()
                                ) {
                                    Title(
                                        modifier = Modifier.padding(horizontal = 4.dp),
                                        icon = painterResource(CoreUiRes.drawable.book_open),
                                        title = "Hausaufgaben"
                                    )
                                    day.homework.forEach { homework ->
                                        Column(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .heightIn(min = 32.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { displayHomeworkId = homework.id }
                                                .padding(8.dp),
                                        ) {
                                            Row(
                                                modifier = Modifier.fillMaxWidth(),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                                            ) {
                                                SubjectIcon(
                                                    modifier = Modifier.size(24.dp),
                                                    subject = homework.subjectInstance?.subject
                                                )
                                                Text(
                                                    text = buildString {
                                                        append(homework.subjectInstance?.subject ?: homework.group?.name ?: "?")
                                                    },
                                                    style = MaterialTheme.typography.bodyMedium,
                                                    fontWeight = FontWeight.Bold,
                                                    maxLines = 1,
                                                    overflow = TextOverflow.Ellipsis
                                                )

                                                if (homework.tasks.size == 1) {
                                                    TaskRow(
                                                        modifier = Modifier.weight(1f),
                                                        currentProfile = state.currentProfile,
                                                        task = homework.tasks.first(),
                                                        isInline = true
                                                    )
                                                } else Spacer(Modifier.weight(1f))

                                                val status = remember(homework.tasks, state.currentProfile) {
                                                    if (state.currentProfile !is Profile.StudentProfile) return@remember null
                                                    val tasksUndone = homework.tasks.any { !it.isDone(state.currentProfile) }
                                                    if (!tasksUndone) HomeworkStatus.DONE
                                                    if (Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date > homework.dueTo) HomeworkStatus.OVERDUE
                                                    else HomeworkStatus.PENDING
                                                }
                                                if (status != null) when (status) {
                                                    HomeworkStatus.DONE -> Badge(
                                                        color = customColors[ColorToken.Green]!!.get(),
                                                        text = "Erledigt"
                                                    )

                                                    HomeworkStatus.PENDING -> Badge(
                                                        color = MaterialTheme.colorScheme.outline,
                                                        text = "Ausstehend"
                                                    )

                                                    HomeworkStatus.OVERDUE -> Badge(
                                                        color = MaterialTheme.colorScheme.error,
                                                        text = "Überfällig"
                                                    )
                                                }
                                            }
                                            if (homework.tasks.size > 1) Column(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .padding(start = 32.dp)
                                            ) {
                                                homework.tasks.forEach { task ->
                                                    TaskRow(
                                                        modifier = Modifier.fillMaxWidth(),
                                                        currentProfile = state.currentProfile,
                                                        task = task,
                                                        isInline = false
                                                    )
                                                }
                                            }
                                        }
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

    return

    val pagerState = rememberPagerState(initialPage = (CONTENT_PAGER_SIZE / 2) + LocalDate.now().until(state.selectedDate, DateTimeUnit.DAY).toInt()) { CONTENT_PAGER_SIZE }
    val lazyListState = rememberLazyListState(initialFirstVisibleItemIndex = (CONTENT_PAGER_SIZE / 2) + LocalDate.now().until(state.selectedDate, DateTimeUnit.DAY).toInt())

    Box(
        modifier = Modifier
            .padding(top = weekHeightDefault)
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        Column(Modifier.fillMaxSize()) {
            Column (
                modifier = Modifier.fillMaxWidth(),
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
                        CompositionLocalProvider(
                            LocalTextStyle provides LocalTextStyle.current.merge(
                                TextStyle(
                                    fontFamily = displayFontFamily()
                                )
                            )
                        ) {
                            Row {
                                AnimatedContent(
                                    targetState = state.selectedDate.day,
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
                    }
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        DisplaySelectType(
                            displayType = state.displayType,
                            onSelectType = { onEvent(CalendarEvent.SelectDisplayType(it)) }
                        )
                        FilledTonalIconButton(
                            onClick = {
                                scope.launch {
                                    if (pagerState.isScrollInProgress) pagerState.stopScroll(MutatePriority.PreventUserInput)
                                    if (lazyListState.isScrollInProgress) lazyListState.stopScroll(MutatePriority.PreventUserInput)
                                    onEvent(CalendarEvent.SelectDate(LocalDate.now()))
                                }
                            },
                            enabled = state.selectedDate != LocalDate.now()
                        ) {
                            Icon(
                                painter = painterResource(CoreUiRes.drawable.calendar),
                                contentDescription = null,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }
            BoxWithConstraints(
                modifier = Modifier.fillMaxSize(),
            ) {
                Column {
                    AnimatedVisibility(
                        visible = state.isTimetableUpdating,
                        enter = expandVertically(),
                        exit = shrinkVertically()
                    ) {
                        LinearProgressIndicator(Modifier.fillMaxWidth())
                    }
                    HorizontalDivider()
                    val isUserDraggingPager = pagerState.interactionSource.collectIsDraggedAsState().value
                    val isUserDraggingList = lazyListState.interactionSource.collectIsDraggedAsState().value
                    var isScrollAnimationRunning by remember { mutableStateOf(false) }
                    LaunchedEffect(pagerState.targetPage, isUserDraggingPager) {
                        if (state.displayType != DisplayType.Calendar) return@LaunchedEffect
                        if (!isUserDraggingPager && isScrollAnimationRunning) return@LaunchedEffect
                        if (isUserDraggingPager) isScrollAnimationRunning = false
                        val date = LocalDate.now().plus((pagerState.targetPage - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
                        if (date != state.selectedDate) onEvent(CalendarEvent.SelectDate(date))
                    }
                    LaunchedEffect(lazyListState.firstVisibleItemIndex, isUserDraggingList) {
                        if (state.displayType != DisplayType.Agenda) return@LaunchedEffect
                        if (!isUserDraggingList && isScrollAnimationRunning) return@LaunchedEffect
                        if (isUserDraggingList) isScrollAnimationRunning = false
                        val date = LocalDate.now().plus((lazyListState.firstVisibleItemIndex - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
                        if (date != state.selectedDate) onEvent(CalendarEvent.SelectDate(date))
                    }
                    LaunchedEffect(state.selectedDate) {
                        val currentlyOpenedDate = LocalDate.now().plus(((if (state.displayType == DisplayType.Calendar) pagerState.currentPage else lazyListState.firstVisibleItemIndex) - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY)
                        if (currentlyOpenedDate != state.selectedDate) {
                            val item = (CONTENT_PAGER_SIZE / 2) + LocalDate.now().until(state.selectedDate, DateTimeUnit.DAY).toInt()
                            when (state.displayType) {
                                DisplayType.Calendar -> {
                                    if (!pagerState.isScrollInProgress) {
                                        isScrollAnimationRunning = true
                                        pagerState.animateScrollToPage(item)
                                    }
                                    lazyListState.scrollToItem(item)
                                }
                                DisplayType.Agenda -> {
                                    if (!lazyListState.isScrollInProgress) {
                                        isScrollAnimationRunning = true
                                        lazyListState.animateScrollToItem(item)
                                    }
                                    pagerState.scrollToPage(item)
                                }
                            }
                        }
                    }

                    val infiniteTransition = rememberInfiniteTransition()

                    when (state.displayType) {
                        DisplayType.Calendar -> {
                            HorizontalPager(
                                state = pagerState,
                                pageSize = PageSize.Fill,
                                verticalAlignment = Alignment.Top,
                                beyondViewportPageCount = 3,
                                modifier = Modifier.fillMaxSize()
                            ) { page ->
                                val date = remember(page) { LocalDate.now().plus((page - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY) }
                                val contentScrollState = remember(date) { contentScrollStates.getOrPut(date) { ScrollState(0) } }
                                val day = state.calendarDays[date] ?: CalendarDay(date)
                                val lessonsForCalendarView = CalendarViewLessons(
                                    day.lessons ?: LessonRendering.ListView(emptyMap())
                                )
                                CalendarView(
                                    profile = state.currentProfile,
                                    date = date,
                                    dayType = day.dayType,
                                    lessons = lessonsForCalendarView,
                                    assessments = day.assessments,
                                    homework = day.homework,
                                    bottomIslandPadding = remember { PaddingValues(end = 80.dp) },
                                    limitTimeSpanToLessonsLowerBound = state.start,
                                    info = day.info,
                                    contentScrollState = contentScrollState,
                                    onHomeworkClicked = remember { { displayHomeworkId = it } },
                                    onAssessmentClicked = remember { { displayAssessmentId = it } },
                                )
                            }
                        }
                        DisplayType.Agenda -> {
                            val heights = remember { mutableMapOf<LocalDate, Int>() }
                            LazyColumn(
                                state = lazyListState
                            ) {
                                items(CONTENT_PAGER_SIZE) { page ->
                                    val date = remember(page) { LocalDate.now().plus((page - CONTENT_PAGER_SIZE / 2), DateTimeUnit.DAY) }
                                    val day = state.calendarDays[date] ?: CalendarDay(date)
                                    var showLessons by rememberSaveable { mutableStateOf(false) }
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        verticalAlignment = Alignment.Top
                                    ) {
                                        val currentDayHeight = lazyListState.layoutInfo.visibleItemsInfo.firstOrNull()?.size ?: 0
                                        Column(
                                            modifier = Modifier
                                                .width(48.dp)
                                                .thenIf(Modifier.offset { IntOffset(0, lazyListState.firstVisibleItemScrollOffset.coerceAtMost((currentDayHeight-(heights[date] ?: 0)).coerceAtLeast(0))) }) { state.selectedDate == date }
                                                .onSizeChanged { heights[date] = it.height },
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
                                                        text = date.day.toString(),
                                                        style = MaterialTheme.typography.titleSmall,
                                                        color = if (date < LocalDate.now()) MaterialTheme.colorScheme.outline
                                                        else if (date == LocalDate.now()) MaterialTheme.colorScheme.onPrimary
                                                        else MaterialTheme.colorScheme.onSurface
                                                    )
                                                }
                                            }
                                            if (date.dayOfWeek == DayOfWeek.MONDAY) {
                                                val week = day.week
                                                if (week != null) Text(
                                                    text = listOfNotNull("KW ${week.calendarWeek}", "SW ${week.weekIndex}", week.weekType).joinToString("\n"),
                                                    color = MaterialTheme.colorScheme.outline,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    textAlign = TextAlign.Center
                                                )
                                            }
                                        }
                                        Column {
                                            val lessonCount = day.lessons?.size
                                            var start by remember { mutableStateOf<LocalTime?>(null) }
                                            var end by remember { mutableStateOf<LocalTime?>(null) }

                                            AgendaHead(
                                                date = date,
                                                dayType = day.dayType,
                                                lessons = lessonCount,
                                                start = start,
                                                end = end,
                                                showLessons = showLessons,
                                                infiniteTransition = infiniteTransition,
                                                onClick = {
                                                    when (day.dayType) {
                                                        Day.DayType.REGULAR -> showLessons = !showLessons
                                                        else -> Unit
                                                    }
                                                }
                                            )
                                            if (day.info != null) {
                                                InfoCard(
                                                    modifier = Modifier
                                                        .padding(vertical = 4.dp, horizontal = 8.dp),
                                                    imageVector = CoreUiRes.drawable.info,
                                                    title = "Informationen deiner Schule",
                                                    text = day.info!!,
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
                                                        modifier = Modifier.padding(horizontal = 8.dp),
                                                        showFirstGradient = false,
                                                        date = date,
                                                        paddingStart = 8.dp,
                                                        lessons = (day.lessons as? LessonRendering.ListView)?.lessons.orEmpty()
                                                    )
                                                }
                                            }
                                            Column(Modifier.fillMaxWidth()) assessments@{
                                                day.assessments.forEach forEachAssessment@{ assessment ->
                                                    AssessmentCard(
                                                        assessment = assessment,
                                                        onClick = { displayAssessmentId = assessment.id }
                                                    )
                                                }
                                                if (state.currentProfile == null) return@items
                                                day.homework.forEach forEachHomework@{ homework ->
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
    }
}

enum class CalendarDateSwitchInteractionSource {
    CalendarPager, WeekSelector, DayDetailPager
}
