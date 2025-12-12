package plus.vplan.app.feature.grades.page.view.ui.components.subject

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.feature.grades.page.view.ui.components.GradeValue
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.longMonthNames

@Composable
fun GradeItem(
    modifier: Modifier = Modifier,
    isSelectedForAverage: Boolean,
    grade: BesteSchuleGrade,
    onClick: () -> Unit
) {
    val collection = grade.collection.collectAsState(null).value
    val interval = collection?.interval?.collectAsState(null)?.value
    val teacher = collection?.teacher?.collectAsState(null)?.value

    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(4.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Column(Modifier.weight(1f)) {
            if (collection != null) Text(
                text = collection.name,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelMedium
            )

            Text(
                text = buildString {
                    append(grade.givenAt.format(LocalDate.Format {
                        day(Padding.ZERO)
                        chars(". ")
                        monthName(longMonthNames)
                        char(' ')
                        year()
                    }))
                    if (teacher != null) {
                        append(" $DOT ")
                        append(teacher.forename)
                        append(" ")
                        append(teacher.surname)
                    }
                },
                maxLines = 1,
                style = MaterialTheme.typography.bodySmall,
                overflow = TextOverflow.Ellipsis
            )
        }

        Column {
            GradeValue(
                grade = grade,
                interval = interval,
                isSelected = isSelectedForAverage,
                onClick = null
            )
        }
    }
}