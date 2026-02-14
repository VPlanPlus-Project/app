@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package plus.vplan.app.feature.grades.page.view.ui.components.subject

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.feature.grades.page.view.ui.components.GradeValue
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.utils.roundTo
import plus.vplan.app.utils.toDp
import kotlin.math.roundToInt

@Composable
private fun sectionTitleFont() = MaterialTheme.typography.titleMediumEmphasized

@Composable
fun SubjectTitle(
    modifier: Modifier = Modifier,
    subjectName: String,
    average: Double?,
    intervalType: BesteSchuleInterval.Type
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubjectIcon(
                modifier = Modifier
                    .size(sectionTitleFont().lineHeight.toDp())
                    .clip(RoundedCornerShape(4.dp)),
                subject = subjectName
            )
            Text(
                text = subjectName,
                style = sectionTitleFont(),
                maxLines = 1,
                overflow = TextOverflow.MiddleEllipsis
            )
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text("∅")
            GradeValue(
                gradeString = average?.roundTo(1)?.toString() ?: "-",
                isSelected = true,
                intervalType = intervalType,
                numericValue = average?.roundToInt()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SubjectTitlePreview() {
    SubjectTitle(Modifier.fillMaxWidth(), "Mathematik", average = 12.6, intervalType = BesteSchuleInterval.Type.Sek2)
}
