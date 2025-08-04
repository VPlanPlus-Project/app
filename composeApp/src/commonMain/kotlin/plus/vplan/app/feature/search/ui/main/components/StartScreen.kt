package plus.vplan.app.feature.search.ui.main.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.domain.cache.collectAsResultingFlow
import plus.vplan.app.domain.model.AppEntity
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.now
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.times
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.untilRelativeText

@Composable
fun StartScreen(
    currentProfile: Profile?,
    homework: List<Homework>,
    latestAssessments: List<Assessment>,
    onHomeworkClick: (id: Int) -> Unit,
    onAssessmentClick: (id: Int) -> Unit,
) {
    var showHomework by remember { mutableStateOf(false) }
    var showAssessments by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        AnimatedVisibility(
            visible = showHomework,
            enter = fadeIn() + slideInVertically { it / 3 },
            exit = fadeOut() + slideOutVertically { it / 3 }
        ) homework@{
            Column {
                Row(Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Neueste Hausaufgaben",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = homework.size.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {}
                    items(homework) { homeworkItem ->
                        if (currentProfile == null) return@items
                        HomeworkCard(
                            homework = homeworkItem,
                            onClick = { onHomeworkClick(homeworkItem.id) },
                            profile = currentProfile
                        )
                    }
                }
            }
        }

        AnimatedVisibility(
            visible = showAssessments,
            enter = fadeIn() + slideInVertically { it / 3 },
            exit = fadeOut() + slideOutVertically { it / 3 }
        ) assessments@{
            Column(
                modifier = Modifier.padding(top = 8.dp)
            ) {
                Row(Modifier.padding(start = 8.dp)) {
                    Text(
                        text = "Neueste Leistungserhebungen",
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.alignByBaseline()
                    )
                    Spacer(Modifier.width(8.dp))
                    Text(
                        text = latestAssessments.size.toString(),
                        style = MaterialTheme.typography.labelMedium,
                        modifier = Modifier.alignByBaseline()
                    )
                }
                LazyRow(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    item {}
                    items(latestAssessments) { assessmentItem ->
                        if (currentProfile == null) return@items
                        AssessmentCard(
                            assessment = assessmentItem,
                            onClick = { onAssessmentClick(assessmentItem.id) }
                        )
                    }
                }
            }
        }
    }

    LaunchedEffect(homework.size) {
        if (homework.isNotEmpty()) showHomework = true
        if (latestAssessments.isNotEmpty()) showAssessments = true
    }
}

@Composable
fun HomeworkCard(
    homework: Homework,
    onClick: () -> Unit,
    profile: Profile
) {
    Column(
        modifier = Modifier
            .width(300.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        val subject = homework.subjectInstance?.collectAsResultingFlow()?.value
        val tasks by homework.getTasksFlow().collectAsState(emptyList())
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (subject != null) {
                    SubjectIcon(Modifier.size(24.dp), subject.subject)
                    Text(
                        text = subject.subject,
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                if (profile is Profile.StudentProfile) CircularProgressIndicator(
                    modifier = Modifier.padding(end = 4.dp).size(24.dp),
                    progress = { tasks.count { it.isDone(profile) }.toFloat() / homework.taskIds.size },
                    trackColor = MaterialTheme.colorScheme.outlineVariant,
                    color = MaterialTheme.colorScheme.tertiary
                )
                Column(Modifier.padding(start = 4.dp)) {
                    Text(
                        text = "Bis " + (LocalDate.now().untilRelativeText(homework.dueTo) ?: homework.dueTo.format(regularDateFormat)),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = when (homework) {
                            is Homework.CloudHomework -> homework.createdByItem!!.name
                            is Homework.LocalHomework -> "Profil " + homework.createdByProfileItem!!.name
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            val maxTasks = 2
            Column {
                tasks.take(maxTasks).forEach { task ->
                    Text(
                        text = "$DOT ${task.content}",
                        style = MaterialTheme.typography.bodySmall.let {
                            if (profile is Profile.StudentProfile && task.isDone(profile)) it.copy(textDecoration = TextDecoration.LineThrough)
                            else it
                        },
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (tasks.size > maxTasks) {
                    Text(
                        text = "-> +${(homework.taskIds.size - maxTasks)}",
                        style = MaterialTheme.typography.bodySmall,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                } else {
                    Spacer(Modifier.height((maxTasks - homework.getTasksFlow().collectAsState(emptyList()).value.size + 1) * MaterialTheme.typography.bodySmall.lineHeight.toDp()))
                }
            }
        }
    }
}

@Composable
fun AssessmentCard(
    assessment: Assessment,
    onClick: () -> Unit,
) {
    val subject by assessment.subjectInstance.collectAsResultingFlow()
    Column(
        modifier = Modifier
            .width(300.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(8.dp)
    ) {
        CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                if (subject != null) {
                    SubjectIcon(Modifier.size(24.dp), subject?.subject)
                    Text(
                        text = subject?.subject ?: "",
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                Column(Modifier.padding(start = 4.dp)) {
                    Text(
                        text = (LocalDate.now().untilRelativeText(assessment.date) ?: assessment.date.format(regularDateFormat)),
                        style = MaterialTheme.typography.labelMedium
                    )
                    Text(
                        text = when (assessment.creator) {
                            is AppEntity.VppId -> assessment.createdByVppId!!.name
                            is AppEntity.Profile -> "Profil " + assessment.createdByProfile!!.name
                        },
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
            Column {
                Text(
                    text = assessment.description + "\n" * (3 - assessment.description.count { it == '\n' }).coerceAtLeast(0),
                    style = MaterialTheme.typography.bodySmall,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}