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
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.ui.subjectIcon
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.info

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
    lesson: Lesson
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = paddingTop, start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        Row {
            Icon(
                painter = painterResource(lesson.subject.subjectIcon()),
                contentDescription = null,
                modifier = Modifier.size(headerFont().lineHeight.toDp() + 4.dp),
                tint =
                    if (lesson is Lesson.SubstitutionPlanLesson && lesson.isSubjectChanged) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurface
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
                            if (lesson.subject != null) append(lesson.subject)
                            else if (lesson.defaultLesson != null) append(lesson.defaultLesson?.subject + " entfällt")
                            else append("Entfall")
                        },
                        style = headerFont(),
                        color =
                        if (lesson is Lesson.SubstitutionPlanLesson && lesson.isSubjectChanged) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (lesson.rooms != null) Text(
                        text = buildString {
                            append(lesson.rooms.orEmpty().joinToString { it.name })
                            if (lesson.rooms.orEmpty().isEmpty()) append("Kein Raum")
                        },
                        style = headerFont(),
                        color =
                        if (lesson is Lesson.SubstitutionPlanLesson && lesson.isRoomChanged) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = buildString {
                            append(lesson.teachers.joinToString { it.name })
                            if (lesson.teachers.isEmpty()) append("Keine Lehrkraft")
                        },
                        style = headerFont(),
                        color =
                        if (lesson is Lesson.SubstitutionPlanLesson && lesson.isTeacherChanged) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                Text(
                    text = buildString {
                        append(lesson.lessonTime.lessonNumber)
                        append(". Stunde $DOT ")
                        append(lesson.lessonTime.start.atDate(lesson.date).format())
                        append(" - ")
                        append(lesson.lessonTime.end.atDate(lesson.date).format())
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (lesson is Lesson.SubstitutionPlanLesson && lesson.info != null) {
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
                                text = lesson.info,
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