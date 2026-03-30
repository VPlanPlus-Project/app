package plus.vplan.app.feature.calendar.view.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.Assessment
import plus.vplan.app.core.model.Day
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.ProfileType
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.InfoCard
import plus.vplan.app.core.utils.date.inWholeMinutes
import plus.vplan.app.core.utils.date.minusWithCapAtMidnight
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.core.utils.date.plusWithCapAtMidnight
import plus.vplan.app.core.utils.date.until
import plus.vplan.app.core.utils.ui.color.transparent
import plus.vplan.app.feature.calendar.view.domain.model.LessonLayoutingInfo
import plus.vplan.app.feature.calendar.view.domain.model.LessonRendering
import plus.vplan.app.feature.calendar.view.ui.components.AssessmentCard
import plus.vplan.app.feature.calendar.view.ui.components.FollowingLessons
import plus.vplan.app.feature.calendar.view.ui.components.HolidayScreen
import plus.vplan.app.feature.calendar.view.ui.components.HomeworkCard
import plus.vplan.app.feature.calendar.view.ui.components.LessonCard
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    profile: Profile?,
    date: LocalDate,
    dayType: Day.DayType?,
    lessons: CalendarViewLessons,
    assessments: List<Assessment>,
    homework: List<Homework>,
    bottomIslandPadding: PaddingValues = PaddingValues(0.dp),
    autoLimitTimeSpanToLessons: Boolean = false,
    limitTimeSpanToLessonsLowerBound: LocalTime? = null,
    info: String?,
    contentScrollState: ScrollState? = rememberScrollState(),
    onHomeworkClicked: (homeworkId: Int) -> Unit,
    onAssessmentClicked: (assessmentId: Int) -> Unit,
) {
    val localDensity = LocalDensity.current
    var availableWidth by remember { mutableStateOf(0.dp) }
    var bottomIslandHeight by remember { mutableStateOf(0.dp) }
    val minute = 1.5.dp
    Column(
        modifier = modifier
            .clipToBounds()
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            when (dayType) {
                Day.DayType.WEEKEND -> HolidayScreen(isWeekend = true, nextRegularSchoolDay = null)
                Day.DayType.HOLIDAY -> HolidayScreen(isWeekend = false, nextRegularSchoolDay = null)
                else -> {
                    when (lessons) {
                        is CalendarViewLessons.CalendarView -> {
                            lessons.lessons.let { lessons ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onSizeChanged { availableWidth = with(localDensity) { it.width.toDp() } - 2 * 8.dp - 32.dp }
                                        .let { if (contentScrollState == null) it else it.verticalScroll(contentScrollState) }
                                ) {
                                    var start by remember { mutableStateOf(limitTimeSpanToLessonsLowerBound ?: LocalTime(0, 0)) }
                                    var end by remember { mutableStateOf(LocalTime(23, 59, 59, 99)) }

                                    LaunchedEffect(lessons.size, autoLimitTimeSpanToLessons) {
                                        if (!autoLimitTimeSpanToLessons || lessons.isEmpty()) return@LaunchedEffect
                                        start = lessons.minOf { it.lesson.lessonTime!!.start }.minusWithCapAtMidnight(15.minutes)
                                        end = lessons.maxOf { it.lesson.lessonTime!!.end }.plusWithCapAtMidnight(30.minutes)
                                    }
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(minute * (start until end).inWholeMinutes.toInt())
                                    ) {
                                        repeat(24) {
                                            val time = LocalTime(it, 0)
                                            val y = (time.inWholeMinutes().toFloat() - start.inWholeMinutes()) * minute
                                            if (y < 0.dp) return@repeat
                                            HorizontalDivider(Modifier.fillMaxWidth().offset(y = y).zIndex(-10f))
                                            Text(
                                                text = "${it.toString().padStart(2, '0')}:00",
                                                color = Color.Gray,
                                                modifier = Modifier.offset(y = y).widthIn(max = 48.dp).align(Alignment.TopEnd),
                                                style = MaterialTheme.typography.labelSmall
                                            )
                                        }
                                        if (date == LocalDate.now()) {
                                            val currentTime = LocalTime.now()
                                            val y = (currentTime.inWholeMinutes().toFloat() - start.inWholeMinutes()) * minute
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
                                        lessons.forEach { lesson ->
                                            val y = (lesson.lesson.lessonTime!!.start.inWholeMinutes().toFloat() - start.inWholeMinutes()) * minute
                                            Box(
                                                modifier = Modifier
                                                    .width(availableWidth / lesson.of)
                                                    .padding(horizontal = 4.dp)
                                                    .height(lesson.lesson.lessonTime!!.start.until(lesson.lesson.lessonTime!!.end).inWholeMinutes.toFloat() * minute)
                                                    .offset(y = y, x = (availableWidth / lesson.of) * lesson.sideShift)
                                            ) {
                                                LessonCard(
                                                    modifier = Modifier.fillMaxSize(),
                                                    lesson = lesson,
                                                    currentProfileType = profile?.profileType ?: ProfileType.STUDENT
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        is CalendarViewLessons.ListView -> {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .let { if (contentScrollState == null) it else it.verticalScroll(contentScrollState) }
                                    .padding(bottom = bottomIslandHeight)
                            ) {
                                FollowingLessons(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(horizontal = 4.dp),
                                    date = date,
                                    showFirstGradient = false,
                                    paddingStart = 8.dp,
                                    lessons = lessons.lessons
                                )
                            }
                        }
                    }
                }
            }
            val colorScheme = MaterialTheme.colorScheme
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .drawWithCache { onDrawBehind { drawRect(Brush.verticalGradient(listOf(colorScheme.surface.transparent(), colorScheme.surface))) } }
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .padding(bottomIslandPadding)
                    .onSizeChanged { bottomIslandHeight = with(localDensity) { it.height.toDp() } },
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (info != null) {
                    InfoCard(
                        imageVector = CoreUiRes.drawable.info,
                        title = "Informationen deiner Schule",
                        text = info,
                        shadow = false
                    )
                }

                var isEntityListExpanded by rememberSaveable { mutableStateOf(false) }
                if (homework.isNotEmpty() || assessments.isNotEmpty()) Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable(!isEntityListExpanded) { isEntityListExpanded = !isEntityListExpanded }
                        .background(MaterialTheme.colorScheme.surfaceContainer)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                        .animateContentSize(),
                ) {
                    AnimatedContent(
                        targetState = isEntityListExpanded
                    ) { showEntities ->
                        if (!showEntities) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column {
                                    Text(
                                        text = "Für diesen Tag",
                                        style = MaterialTheme.typography.titleSmall
                                    )
                                    listOfNotNull(
                                        when (homework.size) {
                                            0 -> null
                                            1 -> "Eine Hausaufgabe"
                                            else -> "${homework.size} Hausaufgaben"
                                        },
                                        when (assessments.size) {
                                            0 -> null
                                            1 -> "Eine Leistungserhebung"
                                            else -> "${assessments.size} Leistungserhebungen"
                                        },
                                    ).ifEmpty { null }?.let {
                                        Text(
                                            text = it.joinToString(),
                                            style = MaterialTheme.typography.bodySmall
                                        )
                                    }
                                }
                                Icon(
                                    painter = painterResource(CoreUiRes.drawable.chevron_right),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            return@AnimatedContent
                        }

                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "Für diesen Tag",
                                    style = MaterialTheme.typography.titleMedium
                                )
                                IconButton(onClick = { isEntityListExpanded = false }) {
                                    Icon(
                                        painter = painterResource(CoreUiRes.drawable.x),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp)
                                    )
                                }
                            }
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .verticalScroll(rememberScrollState()),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                assessments.forEach { assessment ->
                                    AssessmentCard(assessment) { onAssessmentClicked(assessment.id) }
                                }
                                homework.forEach { homeworkItem ->
                                    HomeworkCard(homeworkItem, profile) { onHomeworkClicked(homeworkItem.id) }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

sealed class CalendarViewLessons {

    /**
     * Used if there are enough information to render a layout.
     */
    data class CalendarView(val lessons: List<LessonLayoutingInfo>): CalendarViewLessons()

    /**
     * Used if lesson times are missing ore unsafe.
     */
    data class ListView(val lessons: Map<Int, List<Lesson>>): CalendarViewLessons()

    companion object {
        operator fun invoke(lessonRendering: LessonRendering): CalendarViewLessons {
            return when (lessonRendering) {
                is LessonRendering.Layouted -> CalendarView(lessonRendering.lessons)
                is LessonRendering.ListView -> ListView(lessonRendering.lessons)
            }
        }
    }
}