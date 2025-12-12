package plus.vplan.app.feature.grades.page.view.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.unit.dp
import plus.vplan.app.domain.model.besteschule.BesteSchuleGrade
import plus.vplan.app.domain.model.besteschule.BesteSchuleInterval
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.utils.blendColor

@Composable
fun GradeValue(
    grade: BesteSchuleGrade,
    isSelected: Boolean,
    interval: BesteSchuleInterval?,
    onClick: (() -> Unit)?,
) {
    val red = colors[CustomColor.Red]!!.getGroup()
    val green = colors[CustomColor.Green]!!.getGroup()
    val backgroundColor by animateColorAsState(
        if (!isSelected || interval == null || grade.value == null || grade.value.startsWith('+') || grade.value.startsWith('-')) Color.Gray
        else when (interval.type) {
            is BesteSchuleInterval.Type.Sek2 -> blendColor(blendColor(red.container, green.container, (grade.numericValue?:0)/15f), MaterialTheme.colorScheme.surfaceVariant, .7f)
            else -> blendColor(blendColor(green.container, red.container, ((grade.numericValue?:1)-1)/5f), MaterialTheme.colorScheme.surfaceVariant, .7f)
        }
    )

    val textColor by animateColorAsState(
        if (!isSelected || interval == null || grade.value == null || grade.value.startsWith('+') || grade.value.startsWith('-')) Color.White
        else when (interval.type) {
            is BesteSchuleInterval.Type.Sek2 -> blendColor(blendColor(red.onContainer, green.onContainer, (grade.numericValue?:0)/15f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
            else -> blendColor(blendColor(green.onContainer, red.onContainer, ((grade.numericValue?:1)-1)/5f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
        }
    )


    Box(
        modifier = Modifier
            .padding(2.dp)
            .fillMaxHeight()
            .width(42.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(enabled = onClick != null) { onClick?.invoke() }
            .background(backgroundColor),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = buildString {
                if (grade.isOptional) append("(")
                if (grade.value != null) append(grade.value)
                else append("-")
                if (grade.isOptional) append(")")
            },
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )
    }
}