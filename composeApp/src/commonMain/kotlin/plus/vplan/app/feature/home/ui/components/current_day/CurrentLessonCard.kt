package plus.vplan.app.feature.home.ui.components.current_day

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.ui.subjectIcon
import plus.vplan.app.utils.progressIn
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.info

@Composable
fun CurrentLessonCard(
    currentLesson: Lesson,
    followingLessons: List<Lesson>,
    contextTime: LocalDateTime
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(horizontal = 8.dp)
            .padding(top = 8.dp),
    ) {
        Row {
            Icon(
                painter = painterResource(currentLesson.subject.subjectIcon()),
                contentDescription = null,
                modifier = Modifier.size(24.dp),
                tint =
                if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.isSubjectChanged) MaterialTheme.colorScheme.error
                else MaterialTheme.colorScheme.onSurface
            )
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(start = 8.dp, top = 2.dp),
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = buildString {
                            if (currentLesson.subject != null) append(currentLesson.subject)
                            else if (currentLesson.defaultLesson != null) append(currentLesson.defaultLesson?.subject + " entfällt")
                            else append("Entfall")
                        },
                        style = MaterialTheme.typography.titleMedium,
                        color =
                        if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.isSubjectChanged) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                    if (currentLesson.rooms != null) Text(
                        text = buildString {
                            append(currentLesson.rooms.orEmpty().joinToString { it.name })
                            if (currentLesson.rooms.orEmpty().isEmpty()) append("Kein Raum")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                        if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.isRoomChanged) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = buildString {
                            append(currentLesson.teachers.joinToString { it.name })
                            if (currentLesson.teachers.isEmpty()) append("Keine Lehrkraft")
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color =
                        if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.isTeacherChanged) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurface
                    )
                }
                if (followingLessons.isNotEmpty()) Column {
                    Row {
                        Text(
                            text = "->",
                            style = MaterialTheme.typography.labelMedium
                        )
                        Text(
                            text = "Weiter in ${
                                followingLessons.map { it.lessonTime.lessonNumber }.distinct().sorted()
                                    .joinToString { "$it." }
                            } Stunde: "
                                    + followingLessons.joinToString {
                                buildString {
                                    append(it.subject ?: "Entfall")
                                    append(" ")
                                    append(it.teachers.joinToString { it.name })
                                    append(" ")
                                    append(it.rooms.orEmpty().joinToString { it.name })
                                }
                            },
                            style = MaterialTheme.typography.labelMedium
                        )
                    }
                }

                val showInfoLines = currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.info != null
                if (showInfoLines) Spacer(Modifier.height(4.dp))

                if (currentLesson is Lesson.SubstitutionPlanLesson && currentLesson.info != null) Row {
                    Icon(
                        painter = painterResource(Res.drawable.info),
                        modifier = Modifier
                            .padding(end = 4.dp)
                            .size(MaterialTheme.typography.bodySmall.lineHeight.toDp())
                            .padding(2.dp),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = currentLesson.info,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        LinearProgressIndicator(
            progress = { (contextTime.time progressIn currentLesson.lessonTime.start..currentLesson.lessonTime.end).toFloat() },
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .height(2.dp)
                .clip(RoundedCornerShape(topStartPercent = 100, topEndPercent = 100)),
            color = MaterialTheme.colorScheme.primary,
            trackColor = MaterialTheme.colorScheme.surfaceVariant,
            drawStopIndicator = {}
        )
    }
}