@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.Lesson
import plus.vplan.app.domain.model.populated.PopulatedLesson
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.info
import vplanplus.composeapp.generated.resources.triangle_alert
import kotlin.time.ExperimentalTime

private fun LocalDateTime.format(): String {
    return toInstant(TimeZone.of("Europe/Berlin")).toLocalDateTime(TimeZone.currentSystemDefault()).format(
        LocalDateTime.Format {
            hour()
            char(':')
            minute()
        }
    )
}

val paddingTop = 8.dp

@Composable
fun headerFont() = MaterialTheme.typography.bodyMedium

@Composable
fun FollowingLesson(
    lesson: PopulatedLesson,
    date: LocalDate
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = paddingTop, start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        Row {
            if (lesson.lesson is Lesson.SubstitutionPlanLesson && (lesson.lesson as Lesson.SubstitutionPlanLesson).isSubjectChanged) SubjectIcon(
                modifier = Modifier.size(headerFont().lineHeight.toDp() + 4.dp),
                subject = lesson.lesson.subject,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
            else SubjectIcon(
                modifier = Modifier.size(headerFont().lineHeight.toDp() + 4.dp),
                subject = lesson.lesson.subject
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = buildString {
                            if (lesson.lesson.subject != null) append(lesson.lesson.subject)
                            else if (lesson is PopulatedLesson.SubstitutionPlanLesson && lesson.lesson.subjectInstanceId != null) append(lesson.subjectInstance!!.subject + " entfällt")
                            else append("Entfall")
                        },
                        style = headerFont(),
                        color =
                        if (lesson.lesson is Lesson.SubstitutionPlanLesson && (lesson.lesson as Lesson.SubstitutionPlanLesson).isSubjectChanged) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (!lesson.lesson.isCancelled) {
                        Text(
                            text = buildString {
                                if (lesson.rooms.isEmpty()) append("Kein Raum")
                                else append(lesson.rooms.joinToString { it.name })
                            },
                            style = headerFont(),
                            color =
                            if (lesson.lesson is Lesson.SubstitutionPlanLesson && (lesson.lesson as Lesson.SubstitutionPlanLesson).isRoomChanged) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    if (lesson !is PopulatedLesson.SubstitutionPlanLesson || !lesson.lesson.isCancelled) Text(
                        text = buildString {
                            append(lesson.teachers.joinToString { it.name })
                            if (lesson.teachers.isEmpty()) append("Keine Lehrkraft")
                        },
                        style = headerFont(),
                        color =
                        if (lesson.lesson is Lesson.SubstitutionPlanLesson && (lesson.lesson as Lesson.SubstitutionPlanLesson).isTeacherChanged) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = buildString {
                        append(lesson.lesson.lessonNumber)
                        append(". Stunde")
                        if (lesson.lessonTime != null) {
                            append( "$DOT ")
                            append(lesson.lessonTime!!.start.atDate(date).format())
                            append(" - ")
                            append(lesson.lessonTime!!.end.atDate(date).format())
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (lesson.lesson is Lesson.TimetableLesson && (lesson.lesson as Lesson.TimetableLesson).weekType != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) weekType@{
                            Icon(
                                painter = painterResource(Res.drawable.calendar),
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .size(MaterialTheme.typography.bodySmall.lineHeight.toDp()),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Nur in ${(lesson.lesson as Lesson.TimetableLesson).weekType}-Woche",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (lesson.lesson is Lesson.SubstitutionPlanLesson && (lesson.lesson as Lesson.SubstitutionPlanLesson).info != null) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) info@{
                            Icon(
                                painter = painterResource(Res.drawable.info),
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .size(MaterialTheme.typography.bodySmall.lineHeight.toDp()),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = (lesson.lesson as Lesson.SubstitutionPlanLesson).info ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    if (lesson.lessonTime?.interpolated == true) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(3.dp)
                        ) info@{
                            Icon(
                                painter = painterResource(Res.drawable.triangle_alert),
                                modifier = Modifier
                                    .padding(end = 2.dp)
                                    .size(MaterialTheme.typography.bodySmall.lineHeight.toDp()),
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = "Diese Stundenzeit wurde automatisch anhand der vorherigen Stundenzeit generiert. Sie stimmt möglicherweise nicht mit der tatsächlichen Planung überein.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                }
            }
        }
    }
}