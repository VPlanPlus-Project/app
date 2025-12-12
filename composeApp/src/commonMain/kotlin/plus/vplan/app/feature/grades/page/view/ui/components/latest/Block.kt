package plus.vplan.app.feature.grades.page.view.ui.components.latest

import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade

@Composable
fun LatestGrades(
    grades: Map<BesteSchuleGrade, Boolean>,
    onOpenGrade: (gradeId: Int) -> Unit,
) {
    LatestGradesTitle(Modifier.padding(horizontal = 8.dp))
    Spacer(Modifier.height(4.dp))
    var subjectColumnMinWidth by remember { mutableStateOf(0.dp) }
    val infiniteTransition = rememberInfiniteTransition()

    grades.entries.forEachIndexed { index, (grade, enabled) ->
        LatestGradeItem(
            modifier = Modifier.padding(horizontal = 4.dp),
            grade = grade,
            infiniteTransition = infiniteTransition,
            isSelectedForAverage = enabled,
            subjectColumnMinWidth = subjectColumnMinWidth,
            onSubjectColumnWidthChange = { subjectColumnMinWidth = it },
            onClick = { onOpenGrade(grade.id) }
        )
        if (index < grades.size - 1) HorizontalDivider(Modifier.padding(horizontal = 8.dp))
    }
}