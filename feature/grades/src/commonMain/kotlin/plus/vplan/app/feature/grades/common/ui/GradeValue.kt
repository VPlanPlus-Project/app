package plus.vplan.app.feature.grades.common.ui

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.core.ui.theme.CustomColor
import plus.vplan.app.core.ui.theme.colors
import plus.vplan.app.core.ui.util.blendColor
import plus.vplan.app.feature.grades.common.domain.model.GradeUiItem

@Composable
fun GradeValue(
    grade: GradeUiItem,
    isSelected: Boolean,
    onClick: (() -> Unit)?,
) {
    GradeValue(
        gradeString = buildString {
            val isOptional = grade is GradeUiItem.ActualGrade && grade.grade.isOptional
            if (isOptional) append("(")
            if (grade.getValue() != null) append(grade.getValue())
            else append("-")
            if (isOptional) append(")")
        },
        isSelected = isSelected,
        intervalType = grade.intervalType,
        numericValue = when (grade) {
            is GradeUiItem.ActualGrade -> grade.grade.numericValue
            is GradeUiItem.CustomGrade -> grade.grade
        },
        onClick = onClick
    )
}

@Composable
fun GradeValue(
    gradeString: String,
    isSelected: Boolean,
    intervalType: BesteSchuleInterval.Type?,
    numericValue: Int? = null,
    onClick: (() -> Unit)? = null,
) {
    val red = colors[CustomColor.Red]!!.getGroup()
    val green = colors[CustomColor.Green]!!.getGroup()

    val isSpecialGrade = gradeString.startsWith('+') || gradeString.startsWith('-') || gradeString == "-" || gradeString.startsWith("(")

    val backgroundColor by animateColorAsState(
        if (!isSelected || intervalType == null || isSpecialGrade) Color.Gray
        else when (intervalType) {
            is BesteSchuleInterval.Type.Sek2 -> blendColor(
                blendColor(red.container, green.container, (numericValue ?: 0) / 15f),
                MaterialTheme.colorScheme.surfaceVariant,
                .7f
            )
            else -> blendColor(
                blendColor(green.container, red.container, ((numericValue ?: 1) - 1) / 5f),
                MaterialTheme.colorScheme.surfaceVariant,
                .7f
            )
        }
    )

    val textColor by animateColorAsState(
        if (!isSelected || intervalType == null || isSpecialGrade) Color.White
        else when (intervalType) {
            is BesteSchuleInterval.Type.Sek2 -> blendColor(
                blendColor(red.onContainer, green.onContainer, (numericValue ?: 0) / 15f),
                MaterialTheme.colorScheme.onSurfaceVariant,
                .7f
            )
            else -> blendColor(
                blendColor(green.onContainer, red.onContainer, ((numericValue ?: 1) - 1) / 5f),
                MaterialTheme.colorScheme.onSurfaceVariant,
                .7f
            )
        }
    )

    Box(
        modifier = Modifier
            .padding(2.dp)
            .width(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = gradeString,
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}

@Preview
@Composable
private fun GradeValuePreview() {
    MaterialTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text("Sekundarstufe 1 (Noten 1-6):", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.height(50.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Normale Noten für Sek1
                GradeValue(
                    gradeString = "1",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek1,
                    numericValue = 1
                )
                GradeValue(
                    gradeString = "2",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek1,
                    numericValue = 2
                )
                GradeValue(
                    gradeString = "2+",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek1,
                    numericValue = 2
                )
                GradeValue(
                    gradeString = "3",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek1,
                    numericValue = 3
                )
                GradeValue(
                    gradeString = "4",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek1,
                    numericValue = 4
                )
                GradeValue(
                    gradeString = "5",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek1,
                    numericValue = 5
                )
                GradeValue(
                    gradeString = "6",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek1,
                    numericValue = 6
                )
            }

            Text("Sekundarstufe 2 (Punkte 0-15):", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.height(50.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Punkte für Sek2
                GradeValue(
                    gradeString = "15",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek2,
                    numericValue = 15
                )
                GradeValue(
                    gradeString = "12",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek2,
                    numericValue = 12
                )
                GradeValue(
                    gradeString = "9",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek2,
                    numericValue = 9
                )
                GradeValue(
                    gradeString = "6",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek2,
                    numericValue = 6
                )
                GradeValue(
                    gradeString = "3",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek2,
                    numericValue = 3
                )
                GradeValue(
                    gradeString = "0",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek2,
                    numericValue = 0
                )
            }

            Text("Spezielle Zustände:", style = MaterialTheme.typography.titleMedium)
            Row(
                modifier = Modifier.height(50.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Spezielle Zustände
                GradeValue(
                    gradeString = "5-",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek1,
                    numericValue = null
                )
                GradeValue(
                    gradeString = "(2)",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek1,
                    numericValue = 2
                )
                GradeValue(
                    gradeString = "2+",
                    isSelected = true,
                    intervalType = BesteSchuleInterval.Type.Sek1,
                    numericValue = null
                )
                GradeValue(
                    gradeString = "3",
                    isSelected = false,
                    intervalType = BesteSchuleInterval.Type.Sek1,
                    numericValue = 3
                )
            }
        }
    }
}
