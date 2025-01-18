package plus.vplan.app.feature.homework.ui.components.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.ui.components.Badge

@Composable
fun DetailPage(
    homework: Homework
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            Text(
                text = "Hausaufgabe",
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(Modifier.height(16.dp))
            val tableNameStyle = MaterialTheme.typography.bodyLarge.copy(Color.Gray)
            val tableValueStyle = MaterialTheme.typography.bodyMedium
            val tableCellModifier = Modifier.weight(1f, true)
            if (homework.defaultLesson != null) TableRow(
                key = {
                    Text(
                        text = "Fach",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    Text(
                        text = homework.defaultLessonItem!!.subject,
                        style = tableValueStyle,
                        modifier = tableCellModifier
                    )
                }
            )
            else TableRow(
                key = {
                    Text(
                        text = "Klasse",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    Text(
                        text = homework.groupItem!!.name,
                        style = tableValueStyle,
                        modifier = tableCellModifier
                    )
                }
            )
            TableRow(
                key = {
                    Text(
                        text = "Fällig",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    Text(
                        text = homework.dueTo.toLocalDateTime(TimeZone.currentSystemDefault()).format(LocalDateTime.Format {
                            dayOfMonth(Padding.ZERO)
                            char('.')
                            monthNumber(Padding.ZERO)
                            char('.')
                            year(Padding.ZERO)
                        }),
                        style = tableValueStyle,
                        modifier = tableCellModifier
                    )
                }
            )
            TableRow(
                key = {
                    Text(
                        text = "Status",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    Box(tableCellModifier) {
                        Badge(
                            color = MaterialTheme.colorScheme.error,
                            text = "Überfällig"
                        )
                    }
                }
            )
            if (homework is Homework.CloudHomework) TableRow(
                key = {
                    Text(
                        text = "Erstellt von",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    Text(
                        text = homework.createdByItem!!.name,
                        style = tableValueStyle,
                        modifier = tableCellModifier
                    )
                }
            )
            else TableRow(
                key = {
                    Text(
                        text = "Speicherort",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    Text(
                        text = "Dieses Gerät",
                        style = tableValueStyle,
                        modifier = tableCellModifier
                    )
                }
            )
            TableRow(
                key = {
                    Text(
                        text = "Erstellt am",
                        style = tableNameStyle,
                        modifier = tableCellModifier
                    )
                },
                value = {
                    Text(
                        text = homework.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).format(LocalDateTime.Format {
                            dayOfMonth(Padding.ZERO)
                            char('.')
                            monthNumber(Padding.ZERO)
                            char('.')
                            year(Padding.ZERO)
                        }),
                        style = tableValueStyle,
                        modifier = tableCellModifier
                    )
                }
            )
            HorizontalDivider(Modifier.padding(vertical = 8.dp))
        }
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            homework.taskItems!!.forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable {  }
                        .padding(end = 8.dp),
                ) {
                    Checkbox(
                        checked = false,
                        onCheckedChange = { }
                    )
                    Spacer(Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .heightIn(min = 48.dp),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        Text(
                            text = task.content,
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TableRow(
    key: @Composable () -> Unit,
    value: @Composable () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        key()
        value()
    }
}