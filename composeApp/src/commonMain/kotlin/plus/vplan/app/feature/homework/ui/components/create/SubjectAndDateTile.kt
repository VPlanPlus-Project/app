package plus.vplan.app.feature.homework.ui.components.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.ui.subjectIcon
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.mediumDayOfWeekNames
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortMonthNames
import plus.vplan.app.utils.untilText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.users

@Composable
fun SubjectAndDateTile(
    selectedDefaultLesson: DefaultLesson?,
    group: Group,
    selectedDate: LocalDate?,
    isAssessment: Boolean,
    onClickDefaultLesson: () -> Unit,
    onClickDate: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .padding(horizontal = 16.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f, true)
                .height(72.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AnimatedContent(
                targetState = selectedDefaultLesson
            ) { selectedDefaultLesson ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClickDefaultLesson() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (selectedDefaultLesson == null) painterResource(Res.drawable.users)
                        else painterResource(selectedDefaultLesson.subject.subjectIcon()),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Column {
                        Text(
                            text = "Fach auswählen",
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = selectedDefaultLesson?.let { defaultLesson ->
                                "${defaultLesson.subject} $DOT ${defaultLesson.teacherItem?.name ?: "Kein Lehrer"}"
                            } ?: "Klasse ${group.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
        VerticalDivider(Modifier.padding(8.dp))
        Box(
            modifier = Modifier
                .weight(1f, true)
                .height(72.dp)
                .clip(RoundedCornerShape(16.dp))
        ) {
            AnimatedContent(
                targetState = selectedDate
            ) { selectedDate ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClickDate() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.calendar),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                    Column {
                        Text(
                            text = selectedDate?.format(LocalDate.Format {
                                dayOfWeek(mediumDayOfWeekNames)
                                chars(", ")
                                dayOfMonth()
                                chars(". ")
                                monthName(shortMonthNames)
                            }) ?: (if (isAssessment) "Datum" else "Fälligkeit"),
                            style = MaterialTheme.typography.titleSmall,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (selectedDate == null) "Nicht gewählt" else LocalDate.now() untilText selectedDate,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    }
}