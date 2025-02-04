package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.ui.subjectIcon

@Composable
fun SubjectGroupRow(
    canEdit: Boolean,
    allowGroup: Boolean,
    onClick: () -> Unit,
    defaultLesson: DefaultLesson?,
    group: Group? = null
) {
    MetadataRow(
        key = {
            Text(
                text = if (allowGroup) "Klasse/Fach" else "Fach",
                style = tableNameStyle()
            )
        },
        value = {
            MetadataValueContainer(
                canEdit = canEdit,
                onClick = onClick
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    if (defaultLesson != null) {
                        Icon(
                            painter = painterResource(defaultLesson.subject.subjectIcon()),
                            contentDescription = null,
                            modifier = Modifier.size(12.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Text(
                            text = defaultLesson.subject,
                            style = tableValueStyle(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    } else {
                        Text(
                            text = if (allowGroup) group!!.name else "Kein Fach",
                            style = tableValueStyle(),
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }
        }
    )
}