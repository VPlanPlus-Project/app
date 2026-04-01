package plus.vplan.app.feature.calendar.page.ui.components.day_details.homework

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.core.model.Group
import plus.vplan.app.core.model.Homework
import plus.vplan.app.core.model.HomeworkStatus
import plus.vplan.app.core.model.Profile
import plus.vplan.app.core.model.School
import plus.vplan.app.core.model.SubjectInstance
import plus.vplan.app.core.ui.components.Badge
import plus.vplan.app.core.ui.components.SubjectIcon
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.core.ui.theme.ColorToken
import plus.vplan.app.core.ui.theme.customColors
import plus.vplan.app.core.utils.date.minus
import plus.vplan.app.core.utils.date.plus
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.uuid.Uuid

@Composable
fun Homework(
    homework: Homework,
    currentProfile: Profile?,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 32.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SubjectIcon(
                modifier = Modifier.size(24.dp),
                subject = homework.subjectInstance?.subject
            )
            Text(
                text = buildString {
                    append(homework.subjectInstance?.subject ?: homework.group?.name ?: "?")
                },
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )

            if (homework.tasks.size == 1) {
                TaskRow(
                    modifier = Modifier.weight(1f),
                    currentProfile = currentProfile,
                    task = homework.tasks.first(),
                    isInline = true
                )
            } else Spacer(Modifier.weight(1f))

            val status = remember(homework.tasks, currentProfile) {
                if (currentProfile !is Profile.StudentProfile) return@remember null
                val tasksUndone = homework.tasks.any { !it.isDone(currentProfile) }
                if (!tasksUndone) HomeworkStatus.DONE
                if (Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date > homework.dueTo) HomeworkStatus.OVERDUE
                else HomeworkStatus.PENDING
            }
            if (status != null) when (status) {
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
            }
        }
        if (homework.tasks.size > 1) Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 32.dp)
        ) {
            homework.tasks.forEach { task ->
                TaskRow(
                    modifier = Modifier.fillMaxWidth(),
                    currentProfile = currentProfile,
                    task = task,
                    isInline = false
                )
            }
        }
    }
}

@Preview
@Composable
private fun HomeworkPendingPreview() {
    AppTheme(dynamicColor = false) {
        val school = School.AppSchool(
            id = Uuid.random(),
            name = "Sample School",
            aliases = emptySet(),
            cachedAt = Clock.System.now(),
            sp24Id = "12345",
            username = "user",
            password = "password",
            credentialsValid = true
        )
        val group = Group(
            id = Uuid.random(),
            school = school,
            name = "10A",
            cachedAt = Clock.System.now(),
            aliases = emptySet()
        )
        val subjectInstance = SubjectInstance(
            id = Uuid.random(),
            subject = "Mathematik",
            course = null,
            teacher = null,
            groups = emptyList(),
            cachedAt = Clock.System.now(),
            aliases = emptySet()
        )
        val profile = Profile.StudentProfile(
            id = Uuid.random(),
            name = "10A",
            group = group,
            subjectInstanceConfiguration = mapOf(subjectInstance to true),
            vppId = null
        )
        val task = Homework.HomeworkTask(
            id = 1,
            content = "Seite 42, Aufgabe 1-5",
            doneByProfiles = emptyList(),
            doneByVppIds = emptyList(),
            homeworkId = 1,
            cachedAt = Clock.System.now()
        )
        val homework = Homework.LocalHomework(
            id = 1,
            createdAt = Clock.System.now(),
            dueTo = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date + 1.days,
            tasks = listOf(task),
            subjectInstance = subjectInstance,
            group = group,
            files = emptyList(),
            cachedAt = Clock.System.now(),
            createdByProfile = profile
        )
        Homework(
            homework = homework,
            currentProfile = profile,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun HomeworkDonePreview() {
    AppTheme(dynamicColor = false) {
        val school = School.AppSchool(
            id = Uuid.random(),
            name = "Sample School",
            aliases = emptySet(),
            cachedAt = Clock.System.now(),
            sp24Id = "12345",
            username = "user",
            password = "password",
            credentialsValid = true
        )
        val group = Group(
            id = Uuid.random(),
            school = school,
            name = "10A",
            cachedAt = Clock.System.now(),
            aliases = emptySet()
        )
        val subjectInstance = SubjectInstance(
            id = Uuid.random(),
            subject = "Deutsch",
            course = null,
            teacher = null,
            groups = emptyList(),
            cachedAt = Clock.System.now(),
            aliases = emptySet()
        )
        val profile = Profile.StudentProfile(
            id = Uuid.random(),
            name = "10A",
            group = group,
            subjectInstanceConfiguration = mapOf(subjectInstance to true),
            vppId = null
        )
        val task = Homework.HomeworkTask(
            id = 1,
            content = "Aufsatz schreiben",
            doneByProfiles = listOf(profile.id),
            doneByVppIds = emptyList(),
            homeworkId = 1,
            cachedAt = Clock.System.now()
        )
        val homework = Homework.LocalHomework(
            id = 1,
            createdAt = Clock.System.now(),
            dueTo = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date + 1.days,
            tasks = listOf(task),
            subjectInstance = subjectInstance,
            group = group,
            files = emptyList(),
            cachedAt = Clock.System.now(),
            createdByProfile = profile
        )
        Homework(
            homework = homework,
            currentProfile = profile,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun HomeworkOverduePreview() {
    AppTheme(dynamicColor = false) {
        val school = School.AppSchool(
            id = Uuid.random(),
            name = "Sample School",
            aliases = emptySet(),
            cachedAt = Clock.System.now(),
            sp24Id = "12345",
            username = "user",
            password = "password",
            credentialsValid = true
        )
        val group = Group(
            id = Uuid.random(),
            school = school,
            name = "10A",
            cachedAt = Clock.System.now(),
            aliases = emptySet()
        )
        val subjectInstance = SubjectInstance(
            id = Uuid.random(),
            subject = "Englisch",
            course = null,
            teacher = null,
            groups = emptyList(),
            cachedAt = Clock.System.now(),
            aliases = emptySet()
        )
        val profile = Profile.StudentProfile(
            id = Uuid.random(),
            name = "10A",
            group = group,
            subjectInstanceConfiguration = mapOf(subjectInstance to true),
            vppId = null
        )
        val task = Homework.HomeworkTask(
            id = 1,
            content = "Vokabeln lernen",
            doneByProfiles = emptyList(),
            doneByVppIds = emptyList(),
            homeworkId = 1,
            cachedAt = Clock.System.now()
        )
        val homework = Homework.LocalHomework(
            id = 1,
            createdAt = Clock.System.now(),
            dueTo = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date - 1.days,
            tasks = listOf(task),
            subjectInstance = subjectInstance,
            group = group,
            files = emptyList(),
            cachedAt = Clock.System.now(),
            createdByProfile = profile
        )
        Homework(
            homework = homework,
            currentProfile = profile,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun HomeworkNoProfilePreview() {
    AppTheme(dynamicColor = false) {
        val school = School.AppSchool(
            id = Uuid.random(),
            name = "Sample School",
            aliases = emptySet(),
            cachedAt = Clock.System.now(),
            sp24Id = "12345",
            username = "user",
            password = "password",
            credentialsValid = true
        )
        val group = Group(
            id = Uuid.random(),
            school = school,
            name = "10A",
            cachedAt = Clock.System.now(),
            aliases = emptySet()
        )
        val subjectInstance = SubjectInstance(
            id = Uuid.random(),
            subject = "Physik",
            course = null,
            teacher = null,
            groups = emptyList(),
            cachedAt = Clock.System.now(),
            aliases = emptySet()
        )
        val profile = Profile.StudentProfile(
            id = Uuid.random(),
            name = "10A",
            group = group,
            subjectInstanceConfiguration = mapOf(subjectInstance to true),
            vppId = null
        )
        val task = Homework.HomeworkTask(
            id = 1,
            content = "Experiment protokollieren",
            doneByProfiles = emptyList(),
            doneByVppIds = emptyList(),
            homeworkId = 1,
            cachedAt = Clock.System.now()
        )
        val homework = Homework.LocalHomework(
            id = 1,
            createdAt = Clock.System.now(),
            dueTo = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date + 1.days,
            tasks = listOf(task),
            subjectInstance = subjectInstance,
            group = group,
            files = emptyList(),
            cachedAt = Clock.System.now(),
            createdByProfile = profile
        )
        Homework(
            homework = homework,
            currentProfile = null,
            onClick = {}
        )
    }
}

@Preview
@Composable
private fun HomeworkMultipleTasksPreview() {
    AppTheme(dynamicColor = false) {
        val school = School.AppSchool(
            id = Uuid.random(),
            name = "Sample School",
            aliases = emptySet(),
            cachedAt = Clock.System.now(),
            sp24Id = "12345",
            username = "user",
            password = "password",
            credentialsValid = true
        )
        val group = Group(
            id = Uuid.random(),
            school = school,
            name = "10A",
            cachedAt = Clock.System.now(),
            aliases = emptySet()
        )
        val subjectInstance = SubjectInstance(
            id = Uuid.random(),
            subject = "Chemie",
            course = null,
            teacher = null,
            groups = emptyList(),
            cachedAt = Clock.System.now(),
            aliases = emptySet()
        )
        val profile = Profile.StudentProfile(
            id = Uuid.random(),
            name = "10A",
            group = group,
            subjectInstanceConfiguration = mapOf(subjectInstance to true),
            vppId = null
        )
        val task1 = Homework.HomeworkTask(
            id = 1,
            content = "Periodensystem lernen",
            doneByProfiles = listOf(profile.id),
            doneByVppIds = emptyList(),
            homeworkId = 1,
            cachedAt = Clock.System.now()
        )
        val task2 = Homework.HomeworkTask(
            id = 2,
            content = "Übungsaufgaben 1-10",
            doneByProfiles = emptyList(),
            doneByVppIds = emptyList(),
            homeworkId = 1,
            cachedAt = Clock.System.now()
        )
        val homework = Homework.LocalHomework(
            id = 1,
            createdAt = Clock.System.now(),
            dueTo = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date + 2.days,
            tasks = listOf(task1, task2),
            subjectInstance = subjectInstance,
            group = group,
            files = emptyList(),
            cachedAt = Clock.System.now(),
            createdByProfile = profile
        )
        Homework(
            homework = homework,
            currentProfile = profile,
            onClick = {}
        )
    }
}