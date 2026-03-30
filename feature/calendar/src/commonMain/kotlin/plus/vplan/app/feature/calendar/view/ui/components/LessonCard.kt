package plus.vplan.app.feature.calendar.view.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.TextAutoSize
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.DayOfWeek
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.core.model.LessonTime
import plus.vplan.app.core.model.ProfileType
import plus.vplan.app.core.model.Room
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.model.Teacher
import plus.vplan.app.core.ui.components.SubjectIcon
import plus.vplan.app.core.ui.modifier.premiumShadow
import plus.vplan.app.core.ui.subjectColor
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.core.ui.theme.getGroup
import plus.vplan.app.core.utils.date.regularTimeFormat
import plus.vplan.app.feature.calendar.view.domain.model.LessonLayoutingInfo
import kotlin.time.Clock
import kotlin.uuid.Uuid

@Composable
fun LessonCard(
    modifier: Modifier = Modifier,
    lesson: LessonLayoutingInfo,
    currentProfileType: ProfileType,
) {
    val localDensity = LocalDensity.current

    val colorFamily =
        (lesson.lesson.subject ?: (lesson.lesson as? Lesson.SubstitutionPlanLesson)?.subjectInstance?.subject).subjectColor().getGroup()

    var width by remember { mutableStateOf<Dp?>(null) }
    val isSmallVariant = width != null && width!! < 120.dp

    Column(
        modifier = modifier
            .premiumShadow(
                color = Color.Black.copy(alpha = 0.1f),
                blurRadius = 8.dp,
                offsetY = 2.dp,
                borderRadius = 8.dp
            )
            .clip(RoundedCornerShape(8.dp))
            .background(colorFamily.desaturatedContainer)
            .padding(4.dp)
            .onSizeChanged {
                @Suppress("AssignedValueIsNeverRead")
                width = with(localDensity) { it.width.toDp() }
            }
    ) {
        CompositionLocalProvider(LocalContentColor provides colorFamily.onDesaturatedContainer) {

            if (isSmallVariant) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(4.dp, Alignment.CenterVertically),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    SubjectIcon(
                        modifier = Modifier.size(24.dp),
                        innerPadding = 2.dp,
                        subject = lesson.lesson.subject,
                        contentColor = colorFamily.onDesaturatedContainer,
                        containerColor = Color.Transparent
                    )
                    Text(
                        text = lesson.lesson.subject ?: "Entfall",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        autoSize = TextAutoSize.StepBased(maxFontSize = MaterialTheme.typography.labelLarge.fontSize)
                    )
                }

                return@CompositionLocalProvider
            }

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                SubjectIcon(
                    modifier = Modifier.size(24.dp),
                    innerPadding = 2.dp,
                    subject = lesson.lesson.subject,
                    contentColor = colorFamily.onDesaturatedContainer,
                    containerColor = Color.Transparent
                )

                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(
                        text = lesson.lesson.subject ?: "Entfall",
                        modifier = Modifier.alignByBaseline(),
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                        ),
                        autoSize = TextAutoSize.StepBased(maxFontSize = MaterialTheme.typography.labelLarge.fontSize)
                    )
                    Text(
                        modifier = Modifier.alignByBaseline(),
                        text = lesson.lesson.rooms.orEmpty().map { it.name }.sorted().joinToString(),
                        style = MaterialTheme.typography.labelMedium
                    )

                    Text(
                        modifier = Modifier.alignByBaseline(),
                        text = when (currentProfileType) {
                            ProfileType.STUDENT -> lesson.lesson.teachers.map { it.name }.sorted().joinToString()
                            ProfileType.TEACHER -> lesson.lesson.groups.map { it.name }.sorted().joinToString()
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 2.dp)
                    .padding(horizontal = 4.dp)
            ) {
                Text(
                    text = buildString {
                        val lessonTime = lesson.lesson.lessonTime
                        if (lessonTime != null) {
                            append(lessonTime.start.format(regularTimeFormat))
                            append(" - ")
                            append(lessonTime.end.format(regularTimeFormat))
                        } else {
                            append(lesson.lesson.lessonNumber)
                            append(".")
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                )

                val info = (lesson.lesson as? Lesson.SubstitutionPlanLesson)?.info
                if (info != null) {
                    Text(
                        text = info,
                        style = MaterialTheme.typography.labelSmall,
                    )
                }

                if ((lesson.lesson is Lesson.TimetableLesson) && lesson.lesson.limitedToWeeks != null && lesson.lesson.limitedToWeeks.orEmpty().isNotEmpty()) Text(
                    text = if (lesson.lesson.limitedToWeeks!!.size == 1) "Nur in Schulwoche ${lesson.lesson.limitedToWeeks!!.first()}"
                    else "Nur in Schulwochen ${lesson.lesson.limitedToWeeks!!.map { it.weekIndex }.sorted().dropLast(1).joinToString()} und ${lesson.lesson.limitedToWeeks!!.map { it.weekIndex }.maxOf { it }}",
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Preview
@Composable
private fun TimetableLessonPreview() {
    val school = School.AppSchool(
        id = Uuid.random(),
        name = "Sample School",
        aliases = emptySet(),
        cachedAt = Clock.System.now(),
        sp24Id = "12345",
        username = "user",
        password = "password",
        credentialsValid = true
    )
    val group = Group(
        id = Uuid.random(),
        school = school,
        name = "10A",
        cachedAt = Clock.System.now(),
        aliases = emptySet()
    )
    val teacher = Teacher(
        id = Uuid.random(),
        school = school,
        name = "Mustermann",
        cachedAt = Clock.System.now(),
        aliases = emptySet()
    )
    val room = Room(
        id = Uuid.random(),
        school = school,
        name = "A101",
        cachedAt = Clock.System.now(),
        aliases = emptySet()
    )
    val lessonTime = LessonTime(
        id = "1",
        start = LocalTime(8, 0),
        end = LocalTime(8, 45),
        lessonNumber = 1,
        group = group.id
    )
    val lesson = Lesson.TimetableLesson(
        id = Uuid.random(),
        dayOfWeek = DayOfWeek.MONDAY,
        weekId = "week1",
        subject = "Mathe",
        teachers = listOf(teacher),
        rooms = listOf(room),
        groups = listOf(group),
        lessonNumber = 1,
        lessonTime = lessonTime,
        timetableId = Uuid.random(),
        weekType = "A",
        limitedToWeeks = null
    )
    AppTheme(dynamicColor = false) {
        LessonCard(
            lesson = LessonLayoutingInfo(lesson, 0, 1),
            currentProfileType = ProfileType.STUDENT
        )
    }
}

@Preview
@Composable
private fun SubstitutionLessonPreview() {
    val school = School.AppSchool(
        id = Uuid.random(),
        name = "Sample School",
        aliases = emptySet(),
        cachedAt = Clock.System.now(),
        sp24Id = "12345",
        username = "user",
        password = "password",
        credentialsValid = true
    )
    val group = Group(
        id = Uuid.random(),
        school = school,
        name = "10A",
        cachedAt = Clock.System.now(),
        aliases = emptySet()
    )
    val lessonTime = LessonTime(
        id = "1",
        start = LocalTime(8, 0),
        end = LocalTime(8, 45),
        lessonNumber = 1,
        group = group.id
    )
    val subjectInstance = SubjectInstance(
        id = Uuid.random(),
        subject = "Mathe",
        course = null,
        teacher = null,
        groups = listOf(group),
        cachedAt = Clock.System.now(),
        aliases = emptySet()
    )
    val lesson = Lesson.SubstitutionPlanLesson(
        id = Uuid.random(),
        date = LocalDate(2024, 10, 21),
        weekId = "week1",
        subject = null,
        isSubjectChanged = false,
        teachers = emptyList(),
        isTeacherChanged = false,
        rooms = emptyList(),
        isRoomChanged = false,
        groups = listOf(group),
        subjectInstance = subjectInstance,
        lessonNumber = 1,
        lessonTime = lessonTime,
        info = "Lehrer krank"
    )
    AppTheme(dynamicColor = false) {
        LessonCard(
            lesson = LessonLayoutingInfo(lesson, 0, 1),
            currentProfileType = ProfileType.STUDENT
        )
    }
}
