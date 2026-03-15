package plus.vplan.app.feature.calendar.ui.components.agenda

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.AppEntity
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.SubjectIcon
import plus.vplan.app.core.ui.subjectColor
import plus.vplan.app.core.ui.theme.CustomColor
import plus.vplan.app.core.ui.theme.colors
import plus.vplan.app.core.ui.util.textunit.toDp
import plus.vplan.app.core.utils.date.regularDateFormat


@Composable
fun HomeworkCard(
    homework: Homework,
    profile: Profile?,
    onClick: () -> Unit
) {
    val localDensity = LocalDensity.current

    var boxHeight by remember { mutableStateOf(0.dp) }
    val tasks = homework.tasks
    if (tasks.isEmpty()) return
    Box(
        modifier = Modifier
            .padding(end = 8.dp)
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .onSizeChanged { with(localDensity) { boxHeight = it.height.toDp() } }
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(4.dp)
                .height((boxHeight - 32.dp).coerceAtLeast(0.dp))
                .clip(RoundedCornerShape(0, 50, 50, 0))
                .background(homework.subjectInstance?.subject.subjectColor().getGroup().color)
        )
        Column(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth()
        ) {
            Row {
                AnimatedContent(
                    targetState = profile is Profile.StudentProfile && tasks.isNotEmpty() && tasks.all { it.isDone(profile) },
                    modifier = Modifier.size(MaterialTheme.typography.titleLarge.lineHeight.toDp())
                ) { allDone ->
                    if (allDone) {
                        val greenGroup = colors[CustomColor.Green]!!.getGroup()
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(CircleShape)
                                .background(greenGroup.color)
                                .padding(4.dp)
                        ) {
                            Icon(
                                painter = painterResource(CoreUiRes.drawable.check),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                tint = greenGroup.onColor
                            )
                        }
                    } else SubjectIcon(
                        modifier = Modifier.fillMaxSize(),
                        subject = homework.subjectInstance?.subject
                    )
                }
                Spacer(Modifier.size(8.dp))
                Column {
                    Text(
                        text = buildString {
                            if (homework.subjectInstance != null) {
                                append(homework.subjectInstance?.subject ?: "Unbekanntes Fach")
                                append(": ")
                            }
                            append("Hausaufgabe")
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                    val taskFont = MaterialTheme.typography.bodyMedium
                    tasks.forEach { task ->
                        val isTaskDone = profile is Profile.StudentProfile && task.isDone(profile)
                        Row {
                            Box(
                                modifier = Modifier
                                    .padding(end = 4.dp)
                                    .size(taskFont.lineHeight.toDp()),
                                contentAlignment = Alignment.Center
                            ) {
                                if (isTaskDone) Icon(
                                    painter = painterResource(CoreUiRes.drawable.check),
                                    modifier = Modifier.size(taskFont.fontSize.toDp()),
                                    contentDescription = null
                                ) else {
                                    Text(
                                        text = "-",
                                        style = taskFont
                                    )
                                }
                            }
                            AnimatedContent(
                                targetState = isTaskDone,
                                modifier = Modifier.fillMaxWidth(),
                                transitionSpec = { fadeIn() togetherWith fadeOut() }
                            ) { showDone ->
                                Text(
                                    text = task.content,
                                    style = taskFont,
                                    textDecoration = if (showDone) TextDecoration.LineThrough else null
                                )
                            }
                        }
                    }
                }
            }
            HorizontalDivider(Modifier.padding(8.dp))
            Row(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val createdByFont = MaterialTheme.typography.labelMedium
                Row {
                    when (val creator = homework.creator) {
                        is AppEntity.Profile -> {
                            Text(
                                text = "Profil " + creator.profile.name,
                                style = createdByFont
                            )
                        }
                        is AppEntity.VppId -> {
                            Text(
                                text = creator.vppId.name,
                                style = createdByFont
                            )
                        }
                    }
                    Text(
                        text = buildString {
                            append(", am ")
                            append(homework.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date.format(regularDateFormat))
                            append(" erstellt")
                        },
                        style = createdByFont,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
            }
        }
    }
}