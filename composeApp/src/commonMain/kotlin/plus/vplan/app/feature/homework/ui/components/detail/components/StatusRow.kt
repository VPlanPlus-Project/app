package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import plus.vplan.app.domain.model.HomeworkStatus
import plus.vplan.app.ui.components.Badge
import plus.vplan.app.ui.theme.ColorToken
import plus.vplan.app.ui.theme.customColors

@Composable
fun StatusRow(
    status: HomeworkStatus?
) {
    MetadataRow(
        key = {
            Text(
                text = "Status",
                style = tableNameStyle()
            )
        },
        value = {
            AnimatedContent(
                targetState = status
            ) { displayStatus ->
                when (displayStatus) {
                    HomeworkStatus.DONE -> Badge(
                        color = customColors[ColorToken.Green]!!.get(),
                        text = "Erledigt"
                    )
                    HomeworkStatus.PENDING -> Badge(
                        color = MaterialTheme.colorScheme.outline,
                        text = "Ausstehend"
                    )
                    HomeworkStatus.OVERDUE -> Badge(
                        color = MaterialTheme.colorScheme.error,
                        text = "Überfällig"
                    )
                    null -> Unit
                }
            }
        }
    )
}