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
import androidx.compose.runtime.collectAsState
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
import plus.vplan.app.domain.cache.AliasState
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.collectAsLoadingStateOld
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.domain.model.VppId
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.subjectColor
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check

@Composable
fun HomeworkCard(
    homework: Homework,
    profile: Profile?,
    onClick: () -> Unit
) {
    val localDensity = LocalDensity.current

    val subject = homework.subjectInstance?.collectAsState(AliasState.Loading(""))?.value
    val createdBy by when (homework.creator) {
        is AppEntity.VppId -> homework.creator.vppId.collectAsLoadingStateOld("")
        is AppEntity.Profile -> homework.creator.profile.collectAsLoadingStateOld("")
    }
    var boxHeight by remember { mutableStateOf(0.dp) }
    val tasks by homework.tasks.collectAsState(emptyList())
    if (tasks.isEmpty() || subject is AliasState.Loading) return
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
                .background((subject as? AliasState.Done<SubjectInstance>)?.data?.subject.subjectColor().getGroup().color)
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
                                painter = painterResource(Res.drawable.check),
                                contentDescription = null,
                                modifier = Modifier.fillMaxSize(),
                                tint = greenGroup.onColor
                            )
                        }
                    } else SubjectIcon(
                        modifier = Modifier.fillMaxSize(),
                        subject = (subject as? AliasState.Done<SubjectInstance>)?.data?.subject
                    )
                }
                Spacer(Modifier.size(8.dp))
                Column {
                    Text(
                        text = buildString {
                            if (homework.subjectInstanceId != null) {
                                append((subject as? AliasState.Done<SubjectInstance>)?.data?.subject ?: "Unbekanntes Fach")
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
                                    painter = painterResource(Res.drawable.check),
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
                val font = MaterialTheme.typography.labelMedium
                if (createdBy is CacheState.Loading) ShimmerLoader(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .fillMaxWidth(.3f)
                        .height(font.lineHeight.toDp())
                )
                Text(
                    text = buildString {
                        val creator = (createdBy as? CacheState.Done)?.data
                        append(when (creator) {
                            is VppId -> creator.name
                            is Profile -> creator.name
                            else -> ""
                        })
                        append(", am ")
                        append(homework.createdAt.toLocalDateTime(TimeZone.currentSystemDefault()).date.format(regularDateFormat))
                        append(" erstellt")
                    },
                    style = font,
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    }
}