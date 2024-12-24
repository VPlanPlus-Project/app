package plus.vplan.app.feature.home.ui.components.current_day

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atDate
import kotlinx.datetime.toInstant
import plus.vplan.app.domain.model.SchoolDay
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.utils.takeContinuousBy
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.info

@Composable
fun CurrentDayView(
    day: SchoolDay.NormalDay,
    contextTime: LocalDateTime
) {
    Column {
        val currentLessons = remember("${contextTime.hour}:${contextTime.minute}") {
            val contextZoned = contextTime.toInstant(TimeZone.currentSystemDefault())
            day.lessons.filter {
                val start = it.lessonTime.start.atDate(day.date).toInstant(TimeZone.of("Europe/Berlin"))
                val end = it.lessonTime.end.atDate(day.date).toInstant(TimeZone.of("Europe/Berlin"))
                contextZoned in start..end
            }.map { currentLesson ->
                currentLesson to day.lessons.filter {
                    it.subject == currentLesson.subject
                            && (it.teachers - currentLesson.teachers.toSet()).size != it.teachers.size
                            && ((it.rooms.orEmpty() - currentLesson.rooms.orEmpty().toSet()).size != it.rooms?.size || it.rooms.orEmpty().isEmpty() || currentLesson.rooms.orEmpty().isEmpty())
                            && it.defaultLesson == currentLesson.defaultLesson
                            && it.lessonTime.lessonNumber > currentLesson.lessonTime.lessonNumber
                }.sortedBy { it.lessonTime.lessonNumber }.takeContinuousBy { it.lessonTime.lessonNumber }
            }
        }

        Column(
            modifier = Modifier
                .padding(vertical = 4.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            currentLessons.forEach { (currentLessons, followingConnected) ->
                CurrentLessonCard(currentLessons, followingConnected, contextTime)
            }
        }
    }
    if (day.info != null) InfoCard(
        title = "Informationen für den Tag",
        text = day.info,
        imageVector = Res.drawable.info,
        modifier = Modifier.padding(vertical = 4.dp)
    )
}