package plus.vplan.app.feature.grades.page.view.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.ui.components.Grid
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.utils.blendColor

@Composable
fun AddGradeDialog(
    onDismiss: () -> Unit,
    onSelectGrade: (grade: Int) -> Unit,
    intervalType: Interval.Type
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = "Note hinzufügen") },
        text = {
            val red = colors[CustomColor.Red]!!.getGroup()
            val green = colors[CustomColor.Green]!!.getGroup()

            Column {
                Text(text = "Diese Note wird nur für die momentane Durchschnittsberechnung verwendet und nicht gespeichert.")
                Grid(
                    modifier = Modifier.fillMaxWidth(),
                    columns = if (intervalType == Interval.Type.Sek1) 3 else 4,
                    content = List(if (intervalType == Interval.Type.Sek1) 6 else 16) {
                        { _, _, index ->
                            val grade = when (intervalType) {
                                is Interval.Type.Sek2 -> index
                                else -> index + 1
                            }
                            val background = when (intervalType) {
                                is Interval.Type.Sek2 -> blendColor(blendColor(red.container, green.container, grade /15f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                                else -> blendColor(blendColor(green.container, red.container, (grade -1)/5f), MaterialTheme.colorScheme.surfaceVariant, .7f)
                            }
                            val textColor = when (intervalType) {
                                is Interval.Type.Sek2 -> blendColor(blendColor(red.onContainer, green.onContainer, grade /15f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                                else -> blendColor(blendColor(green.onContainer, red.onContainer, (grade -1)/5f), MaterialTheme.colorScheme.onSurfaceVariant, .7f)
                            }
                            Box(
                                modifier = Modifier
                                    .padding(2.dp)
                                    .fillMaxWidth()
                                    .height(40.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { onSelectGrade(grade) }
                                    .background(background)
                                    .padding(4.dp),
                                contentAlignment = androidx.compose.ui.Alignment.Center
                            ) {
                                Text(
                                    text = grade.toString(),
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = textColor
                                )
                            }
                        }
                    }
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
            ) {
                Text(
                    text = "Abbrechen",
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
    )
}