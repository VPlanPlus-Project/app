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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.format
import kotlinx.datetime.format.char
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.App
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.cache.collectAsLoadingState
import plus.vplan.app.domain.cache.collectAsResultingFlowOld
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.domain.model.Room
import plus.vplan.app.domain.model.Teacher
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
    lesson: Lesson,
    date: LocalDate
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = paddingTop, start = 8.dp, end = 8.dp, bottom = 8.dp)
    ) {
        Row {
            if (lesson is Lesson.SubstitutionPlanLesson && lesson.isSubjectChanged) SubjectIcon(
                modifier = Modifier.size(headerFont().lineHeight.toDp() + 4.dp),
                subject = lesson.subject,
                contentColor = MaterialTheme.colorScheme.onErrorContainer,
                containerColor = MaterialTheme.colorScheme.errorContainer
            )
            else SubjectIcon(
                modifier = Modifier.size(headerFont().lineHeight.toDp() + 4.dp),
                subject = lesson.subject
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Row(
                    modifier = Modifier.padding(top = 2.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val subjectInstanceState = lesson.subjectInstanceId?.let {
                        App.subjectInstanceSource.getById(it).collectAsLoadingState()
                    }
                    Text(
                        text = buildString {
                            if (lesson.subject != null) append(lesson.subject)
                            else if (subjectInstanceState?.value is AliasState.Done) append((subjectInstanceState.value as AliasState.Done).data.subject + " entfällt")
                            else append("Entfall")
                        },
                        style = headerFont(),
                        color =
                        if (lesson is Lesson.SubstitutionPlanLesson && lesson.isSubjectChanged) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (lesson.roomIds != null && !lesson.isCancelled) {
                        val rooms by (if (lesson.roomIds.isNullOrEmpty()) flowOf(emptyList()) else combine(lesson.roomIds.orEmpty().map { App.roomSource.getById(it) }) { it.toList() }).collectAsState(null)
                        Text(
                            text = buildString {
                                if (rooms == null) return@buildString
                                append(rooms!!.filterIsInstance<AliasState.Done<Room>>().joinToString { it.data.name })
                                if (rooms!!.isEmpty()) append("Kein Raum")
                            },
                            style = headerFont(),
                            color =
                            if (lesson is Lesson.SubstitutionPlanLesson && lesson.isRoomChanged) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurface
                        )
                    }
                    val teachers by (if (lesson.teacherIds.isEmpty()) flowOf(emptyList()) else combine(lesson.teacherIds.map { App.teacherSource.getById(it) }) { it.toList() }).collectAsState(null)
                    if (!lesson.isCancelled) Text(
                        text = buildString {
                            if (teachers == null) return@buildString
                            append(teachers!!.filterIsInstance<AliasState.Done<Teacher>>().joinToString { it.data.name })
                            if (teachers!!.isEmpty()) append("Keine Lehrkraft")
                        },
                        style = headerFont(),
                        color =
                        if (lesson is Lesson.SubstitutionPlanLesson && lesson.isTeacherChanged) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                val lessonTime = remember(lesson.id) { lesson.lessonTime }?.collectAsResultingFlowOld()?.value
                Text(
                    text = buildString {
                        append(lesson.lessonNumber)
                        append(". Stunde")
                        if (lessonTime != null) {
                            append( "$DOT ")
                            append(lessonTime.start.atDate(date).format())
                            append(" - ")
                            append(lessonTime.end.atDate(date).format())
                        }
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    if (lesson is Lesson.TimetableLesson && lesson.weekType != null) {
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
                                text = "Nur in ${lesson.weekType}-Woche",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
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
                    if (lessonTime?.interpolated == true) {
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