package plus.vplan.app.feature.search.ui.main.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.datetime.LocalDateTime
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.until

val hourWidth = 92.dp

@Composable
fun LessonRow(
    referenceTime: LocalDateTime,
    lessons: List<Lesson>,
    scrollState: ScrollState
) {
    if (lessons.isEmpty()) return
    val lessonHeight = 32.dp
    val lessonVerticalPadding = 2.dp
    var fullHeight by remember { mutableStateOf(0.dp) }
    val localDensity = LocalDensity.current
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .onSizeChanged { fullHeight = with(localDensity) { it.height.toDp() } }
            .horizontalScroll(scrollState)
    ) {
        Box(Modifier.width(24 * hourWidth)) {
            repeat(24) { i ->
                Box(
                    modifier = Modifier
                        .offset(x = hourWidth * i)
                        .width(1.dp)
                        .height(fullHeight)
                        .background(MaterialTheme.colorScheme.outline),
                )
            }

            lessons.forEachIndexed { index, lesson ->
                val start = lesson.lessonTimeItem!!.start
                val end = lesson.lessonTimeItem!!.end

                val alreadyDrawnLessonsThatOverlapWithThis = lessons.filterIndexed { filterIndex, filterLesson -> filterIndex < index && filterLesson.lessonTimeItem!!.start in start..end }
                Box(
                    modifier = Modifier
                        .padding(
                            start = lesson.lessonTimeItem!!.start.let { it.hour * 60 + it.minute } * (hourWidth/60),
                            top = 1.dp + alreadyDrawnLessonsThatOverlapWithThis.size * (lessonHeight + lessonVerticalPadding)
                        )
                        .width(lesson.lessonTimeItem!!.start.until(lesson.lessonTimeItem!!.end).inWholeMinutes.toInt() * (hourWidth/60))
                        .height(lessonHeight)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.Green),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Column(Modifier.padding(2.dp)) {
                        Text(
                            text = buildString {
                                append(lesson.subject!!)
                                if (lesson.teacherItems.orEmpty().isNotEmpty()) append(" $DOT ${lesson.teacherItems.orEmpty().joinToString { it.name }}")
                            },
                            style = MaterialTheme.typography.labelSmall,
                            overflow = TextOverflow.Ellipsis
                        )
                        if (lesson.roomItems.orEmpty().isNotEmpty()) Text(
                            text = lesson.roomItems.orEmpty().joinToString { it.name },
                            style = MaterialTheme.typography.labelSmall,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .offset(x = referenceTime.time.hour * hourWidth + referenceTime.time.minute * (hourWidth/60))
                    .width(1.dp)
                    .height(fullHeight)
                    .background(Color.Red),
            )
        }
    }
}