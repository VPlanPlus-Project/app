package plus.vplan.app.feature.homework.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.ButtonType
import plus.vplan.app.ui.components.FullscreenDrawerContext
import plus.vplan.app.ui.subjectIcon
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.mediumDayOfWeekNames
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortMonthNames
import plus.vplan.app.utils.untilText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.file_text
import vplanplus.composeapp.generated.resources.image
import vplanplus.composeapp.generated.resources.user
import vplanplus.composeapp.generated.resources.users
import vplanplus.composeapp.generated.resources.x

@Composable
fun FullscreenDrawerContext.NewHomeworkDrawerContent() {
    val viewModel = koinViewModel<NewHomeworkViewModel>()
    val state = viewModel.state

    var showLessonSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var showDateSelectDrawer by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp))
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Aufgaben",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Column(Modifier.fillMaxWidth()) {
                repeat(state.tasks.size + 1) { index ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Box(
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.outlineVariant)
                                .padding(2.dp)
                                .clip(CircleShape)
                                .background(MaterialTheme.colorScheme.surface)
                        )
                        TextField(
                            value = state.tasks.toList().getOrNull(index)?.second ?: "",
                            onValueChange = {
                                val task = state.tasks.toList().getOrNull(index)
                                if (task != null) viewModel.onEvent(NewHomeworkEvent.UpdateTask(task.first, it))
                                else viewModel.onEvent(NewHomeworkEvent.AddTask(it))
                            },
                            modifier = Modifier.weight(1f),
                            colors = TextFieldDefaults.colors(
                                focusedContainerColor = Color.Transparent,
                                unfocusedContainerColor = Color.Transparent,
                                disabledContainerColor = Color.Transparent,
                                focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                                unfocusedIndicatorColor = Color.Transparent,
                                disabledIndicatorColor = MaterialTheme.colorScheme.outline,
                            ),
                            placeholder = { Text(text = "z.B. LB S. 67/4b-c") },
                        )
                        AnimatedVisibility(
                            visible = state.tasks.toList().getOrNull(index)?.first != null,
                            enter = expandHorizontally(),
                            exit = shrinkHorizontally()
                        ) {
                            IconButton(
                                modifier = Modifier.padding(start = 8.dp),
                                onClick = { state.tasks.toList().getOrNull(index)?.first?.let { viewModel.onEvent(NewHomeworkEvent.RemoveTask(it)) } },
                            ) {
                                Icon(
                                    painter = painterResource(Res.drawable.x),
                                    contentDescription = null,
                                    modifier = Modifier.size(24.dp),
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Fach & Fälligkeit",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f, true)
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AnimatedContent(
                        targetState = state.selectedDefaultLesson
                    ) { selectedDefaultLesson ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showLessonSelectDrawer = true }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                if (selectedDefaultLesson == null) painterResource(Res.drawable.users)
                                else painterResource(selectedDefaultLesson.subject.subjectIcon()),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Column {
                                Text(
                                    text = "Fach auswählen",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface,
                                )
                                Text(
                                    text = selectedDefaultLesson?.let { defaultLesson ->
                                        "${defaultLesson.subject} $DOT ${defaultLesson.teacher?.name ?: "Kein Lehrer"}"
                                    } ?: "Klasse ${state.currentProfile?.group?.name}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
                VerticalDivider(Modifier.padding(8.dp))
                Box(
                    modifier = Modifier
                        .weight(1f, true)
                        .height(72.dp)
                        .clip(RoundedCornerShape(16.dp))
                ) {
                    AnimatedContent(
                        targetState = state.selectedDate
                    ) { selectedDate ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clickable { showDateSelectDrawer = true }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.calendar),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                            Column {
                                Text(
                                    text = selectedDate?.format(LocalDate.Format {
                                        dayOfWeek(mediumDayOfWeekNames)
                                        chars(", ")
                                        dayOfMonth()
                                        chars(". ")
                                        monthName(shortMonthNames)
                                    }) ?: "Fälligkeit",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = if (selectedDate == null) "Nicht gewählt" else LocalDate.now() untilText selectedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Sichtbarkeit",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
                    .padding(horizontal = 16.dp)
            ) {
                Box(
                    modifier = Modifier
                        .weight(1f, true)
                        .height(72.dp)
                ) {
                    AnimatedContent(
                        targetState = state.isPublic
                    ) { displayVisibility ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (!displayVisibility) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .clickable { viewModel.onEvent(NewHomeworkEvent.SetVisibility(false)) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter =
                                if (displayVisibility) painterResource(Res.drawable.user)
                                else painterResource(Res.drawable.check),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (!displayVisibility) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "Nur ich",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (!displayVisibility) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
                Spacer(Modifier.padding(8.dp).width(DividerDefaults.Thickness))
                Box(
                    modifier = Modifier
                        .weight(1f, true)
                        .height(72.dp)
                ) {
                    AnimatedContent(
                        targetState = state.isPublic
                    ) { displayVisibility ->
                        Row(
                            modifier = Modifier
                                .fillMaxSize()
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (displayVisibility) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                                .clickable { viewModel.onEvent(NewHomeworkEvent.SetVisibility(true)) }
                                .padding(8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Icon(
                                painter =
                                if (!displayVisibility) painterResource(Res.drawable.user)
                                else painterResource(Res.drawable.check),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = if (displayVisibility) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text =
                                if ((state.selectedDefaultLesson?.groups?.size ?: 0) > 1) "Klassen ${state.selectedDefaultLesson?.groups.orEmpty().joinToString { it.name }}"
                                else "Klasse ${state.currentProfile?.group?.name}",
                                style = MaterialTheme.typography.titleSmall,
                                color = if (displayVisibility) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Dateianhänge",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Button(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                text = "Dokument anhängen",
                icon = Res.drawable.file_text,
                state = ButtonState.Enabled,
                size = ButtonSize.Normal,
                type = ButtonType.Secondary,
                onClick = {  }
            )
            Spacer(Modifier.height(8.dp))
            Button(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                text = "Bild anhängen",
                icon = Res.drawable.image,
                state = ButtonState.Enabled,
                size = ButtonSize.Normal,
                type = ButtonType.Secondary,
                onClick = {  }
            )
        }
        Button(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Speichern",
            icon = Res.drawable.check,
            state = ButtonState.Enabled,
            size = ButtonSize.Normal,
            onClick = {  }
        )
    }

    if (showLessonSelectDrawer) {
        LessonSelectDrawer(
            group = (state.currentProfile as Profile.StudentProfile).group,
            defaultLessons = state.currentProfile.defaultLessons.filterValues { it }.keys.sortedBy { it.subject },
            selectedDefaultLesson = state.selectedDefaultLesson,
            onSelectDefaultLesson = { viewModel.onEvent(NewHomeworkEvent.SelectDefaultLesson(it)) },
            onDismiss = { showLessonSelectDrawer = false }
        )
    }

    if (showDateSelectDrawer) {
        DateSelectDrawer(
            selectedDate = state.selectedDate,
            onSelectDate = { viewModel.onEvent(NewHomeworkEvent.SelectDate(it)) },
            onDismiss = { showDateSelectDrawer = false }
        )
    }
}