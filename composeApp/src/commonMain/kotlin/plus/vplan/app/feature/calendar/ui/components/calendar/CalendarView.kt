package plus.vplan.app.feature.calendar.ui.components.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.Profile
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Day
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.domain.model.populated.PopulatedLesson
import plus.vplan.app.feature.calendar.ui.LessonLayoutingInfo
import plus.vplan.app.feature.calendar.ui.LessonRendering
import plus.vplan.app.feature.calendar.ui.components.agenda.AssessmentCard
import plus.vplan.app.feature.calendar.ui.components.agenda.HomeworkCard
import plus.vplan.app.feature.home.ui.components.FollowingLessons
import plus.vplan.app.feature.home.ui.components.HolidayScreen
import plus.vplan.app.feature.home.ui.components.headerFont
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.inWholeMinutes
import plus.vplan.app.utils.minusWithCapAtMidnight
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plusWithCapAtMidnight
import plus.vplan.app.utils.regularTimeFormat
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.transparent
import plus.vplan.app.utils.until
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.chevron_right
import vplanplus.composeapp.generated.resources.info
import vplanplus.composeapp.generated.resources.x
import kotlin.time.Duration.Companion.minutes

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun CalendarView(
    modifier: Modifier = Modifier,
    profile: Profile?,
    date: LocalDate,
    dayType: Day.DayType,
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
    val minute = 1.25.dp
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
                                        start = lessons.minOf { it.lesson.lessonTime!!.start }.minusWithCapAtMidnight(30.minutes)
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
                                        lessons.forEachIndexed { i, lesson ->
                                            val y = (lesson.lesson.lessonTime!!.start.inWholeMinutes().toFloat() - start.inWholeMinutes()) * minute
                                            Box(
                                                modifier = Modifier
                                                    .width(availableWidth / lesson.of)
                                                    .padding(horizontal = 8.dp)
                                                    .height(lesson.lesson.lessonTime!!.start.until(lesson.lesson.lessonTime!!.end).inWholeMinutes.toFloat() * minute)
                                                    .offset(y = y, x = (availableWidth / lesson.of) * lesson.sideShift)
                                                    .clip(RoundedCornerShape(6.dp))
                                                    .background(
                                                        if (lesson.lesson.lesson is Lesson.SubstitutionPlanLesson && lesson.lesson.lesson.isCancelled) MaterialTheme.colorScheme.errorContainer
                                                        else MaterialTheme.colorScheme.surfaceVariant
                                                    )
                                                    .padding(4.dp)
                                            ) {
                                                CompositionLocalProvider(
                                                    LocalContentColor provides if (lesson.lesson.lesson is Lesson.SubstitutionPlanLesson && lesson.lesson.lesson.subject == null) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                                                ) {
                                                    Column {
                                                        Row(
                                                            verticalAlignment = Alignment.Top,
                                                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                        ) {
                                                            if (lesson.lesson.lesson is Lesson.SubstitutionPlanLesson && (lesson.lesson.lesson as Lesson.SubstitutionPlanLesson).isSubjectChanged) SubjectIcon(
                                                                modifier = Modifier.size(headerFont().lineHeight.toDp() + 4.dp),
                                                                subject = lesson.lesson.lesson.subject,
                                                                contentColor = MaterialTheme.colorScheme.onError,
                                                                containerColor = MaterialTheme.colorScheme.error
                                                            )
                                                            else SubjectIcon(
                                                                modifier = Modifier.size(headerFont().lineHeight.toDp() + 4.dp),
                                                                subject = lesson.lesson.lesson.subject
                                                            )
                                                            Column {
                                                                FlowRow(
                                                                    verticalArrangement = Arrangement.spacedBy(4.dp),
                                                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                                                ) {
                                                                    val itemHeight = max(MaterialTheme.typography.bodyMedium.lineHeight.toDp(), MaterialTheme.typography.bodySmall.lineHeight.toDp())
                                                                    Box(
                                                                        modifier = Modifier.height(itemHeight),
                                                                        contentAlignment = Alignment.BottomStart
                                                                    ) {
                                                                        Text(
                                                                            text = buildAnnotatedString {
                                                                                withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle()) {
                                                                                    if (lesson.lesson.lesson.isCancelled) withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(textDecoration = TextDecoration.LineThrough)) {
                                                                                        if (lesson.lesson is PopulatedLesson.SubstitutionPlanLesson && lesson.lesson.subjectInstance != null) append(lesson.lesson.subjectInstance.subject + " ")
                                                                                        append("Entfall")
                                                                                    } else append(lesson.lesson.lesson.subject)
                                                                                }
                                                                            },
                                                                            style = MaterialTheme.typography.bodySmall
                                                                        )
                                                                    }
                                                                    if (lesson.lesson.groups.isNotEmpty() && profile !is Profile.StudentProfile) Box(
                                                                        modifier = Modifier.height(itemHeight),
                                                                        contentAlignment = Alignment.BottomStart
                                                                    ) {
                                                                        Text(
                                                                            text = lesson.lesson.groups.joinToString { it.name },
                                                                            style = MaterialTheme.typography.labelMedium
                                                                        )
                                                                    }
                                                                    if (lesson.lesson.rooms.isNotEmpty()) Box(
                                                                        modifier = Modifier.height(itemHeight),
                                                                        contentAlignment = Alignment.BottomStart
                                                                    ) {
                                                                        Text(
                                                                            text = lesson.lesson.rooms.joinToString { it.name },
                                                                            style = MaterialTheme.typography.labelMedium
                                                                        )
                                                                    }
                                                                    if (lesson.lesson.teachers.isNotEmpty()) Box(
                                                                        modifier = Modifier.height(itemHeight),
                                                                        contentAlignment = Alignment.BottomStart
                                                                    ) {
                                                                        Text(
                                                                            text = lesson.lesson.teachers.joinToString { it.name },
                                                                            style = MaterialTheme.typography.labelMedium
                                                                        )
                                                                    }
                                                                }
                                                                Text(
                                                                    buildString {
                                                                        append(lesson.lesson.lessonTime!!.lessonNumber)
                                                                        append(". $DOT ")
                                                                        append(lesson.lesson.lessonTime!!.start.format(regularTimeFormat))
                                                                        append(" - ")
                                                                        append(lesson.lesson.lessonTime!!.end.format(regularTimeFormat))
                                                                    },
                                                                    style = MaterialTheme.typography.labelSmall,
                                                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                                                    maxLines = 1,
                                                                    overflow = TextOverflow.Ellipsis
                                                                )
                                                            }
                                                        }
                                                        if (lesson.lesson.lesson is Lesson.SubstitutionPlanLesson && (lesson.lesson.lesson as Lesson.SubstitutionPlanLesson).info != null) Row(
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
                                                                text = (lesson.lesson.lesson as Lesson.SubstitutionPlanLesson).info!!,
                                                                style = MaterialTheme.typography.bodySmall,
                                                                maxLines = 1,
                                                                overflow = TextOverflow.Ellipsis
                                                            )
                                                        }
                                                        if (lesson.lesson.lesson is Lesson.TimetableLesson && (lesson.lesson as PopulatedLesson.TimetableLesson).weeks != null) Row {
                                                            Box(
                                                                modifier = Modifier
                                                                    .padding(end = 4.dp)
                                                                    .size(MaterialTheme.typography.bodySmall.fontSize.toDp()),
                                                                contentAlignment = Alignment.Center
                                                            ) {
                                                                Icon(
                                                                    painter = painterResource(Res.drawable.info),
                                                                    contentDescription = null,
                                                                    modifier = Modifier
                                                                        .size(8.dp),
                                                                    tint = MaterialTheme.colorScheme.onSurface
                                                                )
                                                            }
                                                            if (lesson.lesson.weeks != null && lesson.lesson.weeks.isNotEmpty()) Text(
                                                                text = if (lesson.lesson.weeks.size == 1) "Nur in Schulwoche ${lesson.lesson.weeks.first()}"
                                                                else "Nur in Schulwochen ${lesson.lesson.weeks.map { it.weekIndex }.sorted().dropLast(1).joinToString()} und ${lesson.lesson.weeks.map { it.weekIndex }.maxOf { it }}",
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
                        imageVector = Res.drawable.info,
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
                                    painter = painterResource(Res.drawable.chevron_right),
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
                                        painter = painterResource(Res.drawable.x),
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
    data class ListView(val lessons: Map<Int, List<PopulatedLesson>>): CalendarViewLessons()

    companion object {
        operator fun invoke(lessonRendering: LessonRendering): CalendarViewLessons {
            return when (lessonRendering) {
                is LessonRendering.Layouted -> CalendarView(lessonRendering.lessons)
                is LessonRendering.ListView -> ListView(lessonRendering.lessons)
            }
        }
    }
}