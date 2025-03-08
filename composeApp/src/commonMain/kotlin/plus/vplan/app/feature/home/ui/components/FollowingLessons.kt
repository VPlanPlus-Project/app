package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import plus.vplan.app.domain.model.Lesson
import plus.vplan.app.utils.transparent

@Composable
fun FollowingLessons(
    modifier: Modifier = Modifier,
    showFirstGradient: Boolean,
    date: LocalDate,
    paddingStart: Dp = 32.dp,
    lessons: Map<Int, List<Lesson>>
) {
    lessons
        .entries
        .forEachIndexed { i, (_, followingLessons) ->
            val colorScheme = MaterialTheme.colorScheme
            val headerFont = headerFont()
            Column(
                modifier = modifier
                    .drawBehind {
                        val circleY = (paddingTop + headerFont.lineHeight.toDp() / 2).toPx()

                        if (i == 0) {
                            if (showFirstGradient) drawLine( // isFirst
                                brush = Brush.verticalGradient(
                                    colors = listOf(colorScheme.tertiary.transparent(), colorScheme.tertiary, colorScheme.tertiary)
                                ),
                                start = Offset(0f, 0f),
                                end = Offset(0f, circleY),
                                strokeWidth = 2.dp.toPx()
                            )
                        } else drawLine( // had previous elements
                            color = colorScheme.tertiary,
                            start = Offset(0f, 0f),
                            end = Offset(0f, circleY),
                            strokeWidth = 2.dp.toPx()
                        )

                        if (i < lessons.size - 1) drawLine( // is not last
                            color = colorScheme.tertiary,
                            start = Offset(0f, circleY),
                            end = Offset(0f, size.height),
                            strokeWidth = 2.dp.toPx()
                        )

                        drawCircle(
                            color = colorScheme.tertiary,
                            radius = 4.dp.toPx(),
                            center = Offset(0f, (paddingTop + headerFont.lineHeight.toDp()/2).toPx())
                        )
                    }
                    .padding(start = paddingStart, bottom = 12.dp)
                    .clip(RoundedCornerShape(16.dp)),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                followingLessons.forEach { followingLesson ->
                    FollowingLesson(followingLesson, date)
                }
            }
        }
}