package plus.vplan.app.feature.calendar.page.ui.components.day_details.homework

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.theme.CustomColor
import plus.vplan.app.core.ui.theme.colors
import plus.vplan.app.core.ui.theme.getGroup
import plus.vplan.app.core.ui.util.textunit.toDp

@Composable
fun TaskRow(
    modifier: Modifier = Modifier,
    currentProfile: Profile?,
    task: Homework.HomeworkTask,
    isInline: Boolean,
) {
    val taskFont = MaterialTheme.typography.bodyMedium
    Row(
        modifier = modifier
    ) {
        if (currentProfile is Profile.StudentProfile && task.isDone(currentProfile)) {
            val colorFamily = colors[CustomColor.Green]!!.getGroup()
            Icon(
                painter = painterResource(CoreUiRes.drawable.check),
                contentDescription = null,
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(taskFont.lineHeight.toDp())
                    .clip(CircleShape)
                    .background(colorFamily.color)
                    .padding(2.dp),
                tint = colorFamily.onColor,
            )
        } else if (!isInline) {
            Box(
                modifier = Modifier
                    .padding(end = 4.dp)
                    .size(taskFont.lineHeight.toDp()),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "-",
                    style = taskFont,
                    maxLines = if (isInline) 1 else Int.MAX_VALUE,
                    overflow = if (isInline) TextOverflow.Ellipsis else TextOverflow.Clip,
                )
            }
        }
        Text(
            text = task.content,
            style = taskFont,
            color = if (isInline) MaterialTheme.colorScheme.outline else LocalContentColor.current,
        )
    }
}