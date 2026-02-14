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
import kotlinx.datetime.format.Padding
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.Group
import plus.vplan.app.domain.model.SubjectInstance
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
    selectedSubjectInstance: SubjectInstance?,
    group: Group,
    selectedDate: LocalDate?,
    isAssessment: Boolean,
    onClickSubjectInstance: () -> Unit,
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
                targetState = selectedSubjectInstance
            ) { selectedSubjectInstance ->
                Row(
                    modifier = Modifier
                        .fillMaxSize()
                        .clickable { onClickSubjectInstance() }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        if (selectedSubjectInstance == null) painterResource(Res.drawable.users)
                        else painterResource(selectedSubjectInstance.subject.subjectIcon()),
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
                            text = selectedSubjectInstance?.let { subjectInstance ->
                                "${subjectInstance.subject} $DOT ${subjectInstance.teacherItem?.name ?: "Kein Lehrer"}"
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
                                day(padding = Padding.ZERO)
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