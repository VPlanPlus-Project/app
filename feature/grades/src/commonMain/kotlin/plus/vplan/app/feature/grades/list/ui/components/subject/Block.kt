package plus.vplan.app.feature.grades.list.ui.components.subject

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.WavySeparator
import plus.vplan.app.core.utils.number.roundTo
import plus.vplan.app.feature.grades.common.domain.model.GradeUiItem
import plus.vplan.app.feature.grades.list.domain.model.Subject
import kotlin.math.floor

@Composable
fun Subjects(
    intervalType: BesteSchuleInterval.Type,
    subjects: List<Subject>,
    isEditModeActive: Boolean,
    onNewGradeClicked: (category: Int) -> Unit,
    onOpenGrade: (gradeId: Int) -> Unit,
    onDeleteGrade: (grade: GradeUiItem.CustomGrade) -> Unit,
) {
    val outlineVariant = MaterialTheme.colorScheme.outlineVariant

    subjects.forEachIndexed { subjectIndex, subject ->
        val categoryWeightSum = subject.categories.sumOf { it.weight }

        SubjectTitle(
            modifier = Modifier.padding(horizontal = 8.dp),
            subjectName = subject.name,
            average = subject.average,
            intervalType = intervalType
        )
        Spacer(Modifier.height(4.dp))
        subject.categories.forEach { category ->
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = category.name,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
                Canvas(
                    modifier = Modifier
                        .weight(1f)
                        .height(3.dp)
                ) {
                    // Dot
                    val width = size.width.toDp()
                    val dotSize = 1.dp
                    val dotSpacing = 2.dp

                    val dots = floor((width / (dotSize + dotSpacing))).toInt()

                    repeat(dots) {
                        drawCircle(
                            color = outlineVariant,
                            radius = dotSize.toPx() / 2,
                            center = Offset(
                                (it * (dotSize.toPx() + dotSpacing.toPx())) + (dotSize.toPx() / 2),
                                size.height / 2
                            )
                        )
                    }
                }
                if (category.average != null) Text(
                    text = buildString {
                        append("∅ ")
                        if (!category.average.isNaN()) append(category.average.roundTo(1))
                        append(" × ")
                        append((category.weight / categoryWeightSum * 100).roundTo(1))
                        append("%")
                    },
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.bodySmall
                )
            }
            category.grades.forEach { grade ->
                GradeItem(
                    modifier = Modifier.padding(horizontal = (16+4).dp),
                    grade = grade,
                    onClick = {
                        if (grade is GradeUiItem.ActualGrade) onOpenGrade(grade.grade.id)
                    },
                    onDelete = { if (grade is GradeUiItem.CustomGrade) onDeleteGrade(grade) }
                )
            }
            AnimatedVisibility(
                visible = isEditModeActive,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Canvas(
                        modifier = Modifier
                            .weight(1f, true)
                            .height(1.dp)
                    ) {
                        drawLine(
                            color = outlineVariant,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                    FilledTonalIconButton(
                        onClick = { onNewGradeClicked(category.id) },
                        modifier = Modifier.padding(horizontal = 8.dp)
                    ) {
                        Icon(
                            painter = painterResource(CoreUiRes.drawable.plus),
                            contentDescription = null,
                        )
                    }
                    Canvas(
                        modifier = Modifier
                            .weight(1f, true)
                            .height(1.dp)
                    ) {
                        drawLine(
                            color = outlineVariant,
                            start = Offset(0f, size.height / 2),
                            end = Offset(size.width, size.height / 2),
                            strokeWidth = 1.dp.toPx()
                        )
                    }
                }
            }
        }

        if (subjectIndex < subjects.lastIndex) {
            WavySeparator(
                modifier = Modifier
                    .padding(horizontal = 12.dp, vertical = 6.dp)
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .height(8.dp),
                color = MaterialTheme.colorScheme.outlineVariant
            )
        }
    }
}