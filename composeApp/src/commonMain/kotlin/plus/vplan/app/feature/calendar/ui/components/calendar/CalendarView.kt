package plus.vplan.app.feature.calendar.ui.components.calendar

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
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
import androidx.compose.ui.unit.times
import androidx.compose.ui.zIndex
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.cache.getFirstValue
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.LessonTime
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.calendar.ui.components.agenda.AssessmentCard
import plus.vplan.app.feature.calendar.ui.components.agenda.HomeworkCard
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
import kotlin.time.Duration.Companion.hours

@Composable
fun CalendarView(
    profile: Profile?,
    date: LocalDate,
    lessons: List<Lesson>,
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
    val minute = 1.25.dp
    Column {
        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { availableWidth = with(localDensity) { it.width.toDp() } - 2 * 8.dp - 32.dp }
                    .let { if (contentScrollState == null) it else it.verticalScroll(contentScrollState) }
            ) {
                val lessonTimes = remember(lessons.size) { mutableStateMapOf<Int, LessonTime>() }
                LaunchedEffect(lessons.size) {
                    lessons.mapIndexed { index, lesson ->
                        lessonTimes[index] = lesson.lessonTime.getFirstValue()!!
                    }
                }

                var start by remember { mutableStateOf(LocalTime(0, 0)) }
                var end by remember { mutableStateOf(LocalTime(23, 59, 59, 99)) }

                LaunchedEffect(lessons.size, autoLimitTimeSpanToLessons, lessonTimes.size, limitTimeSpanToLessonsLowerBound) {
                    if ((!autoLimitTimeSpanToLessons || lessonTimes.size < lessons.size || lessons.isEmpty()) && limitTimeSpanToLessonsLowerBound != null) return@LaunchedEffect
                    start = limitTimeSpanToLessonsLowerBound ?: lessonTimes.values.minOf { it.start }.minusWithCapAtMidnight(1.hours)
                    end = lessonTimes.values.maxOf { it.end }.plusWithCapAtMidnight(1.hours)
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
                    if (lessonTimes.size == lessons.size) lessons.forEachIndexed { i, lesson ->
                        val lessonTimeItem = lesson.lessonTime.collectAsResultingFlow().value ?: return@forEachIndexed
                        if (!lessonTimes.containsKey(i)) lessonTimes[i] = lessonTimeItem
                        val lessonStart = lessonTimeItem.start
                        val lessonEnd = lessonTimeItem.end

                        val lessonsThatOverlapStart = lessons.filterIndexed { index, _ ->
                            val lessonTimeForComparison = lessonTimes[index] ?: return@filterIndexed false
                            lessonStart in lessonTimeForComparison.start..lessonTimeForComparison.end
                        }
                        val lessonsThatOverlapStartAndAreAlreadyDisplayed = lessons.filterIndexed { index, _ ->
                            val lessonTimeForComparison = lessonTimes[index] ?: return@filterIndexed false
                            lessonStart in lessonTimeForComparison.start..lessonTimeForComparison.end && index < i
                        }

                        val y = (lessonStart.inWholeMinutes().toFloat() - start.inWholeMinutes()) * minute
                        Box(
                            modifier = Modifier
                                .width(availableWidth / lessonsThatOverlapStart.size)
                                .padding(horizontal = 8.dp)
                                .height(lessonStart.until(lessonEnd).inWholeMinutes.toFloat() * minute)
                                .offset(y = y, x = (availableWidth / lessonsThatOverlapStart.size) * lessonsThatOverlapStartAndAreAlreadyDisplayed.size)
                                .clip(RoundedCornerShape(6.dp))
                                .clickable { }
                                .background(
                                    if (lesson.isCancelled) MaterialTheme.colorScheme.errorContainer
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
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
                                                            append((lesson as Lesson.SubstitutionPlanLesson).subjectInstanceItem!!.subject)
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
                                                    append(lessonTimeItem.lessonNumber)
                                                    append(". $DOT ")
                                                    append(lessonTimeItem.start.format(regularTimeFormat))
                                                    append(" - ")
                                                    append(lessonTimeItem.end.format(regularTimeFormat))
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
            val colorScheme = MaterialTheme.colorScheme
            Column(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .drawWithCache { onDrawBehind { drawRect(Brush.verticalGradient(listOf(colorScheme.surface.transparent(), colorScheme.surface))) } }
                    .padding(vertical = 4.dp, horizontal = 8.dp)
                    .padding(bottomIslandPadding),
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
                                            else -> "${homework.size} Leistungserhebungen"
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