package plus.vplan.app.feature.homework.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.feature.homework.ui.components.create.FileButtons
import plus.vplan.app.feature.homework.ui.components.create.FileItem
import plus.vplan.app.feature.homework.ui.components.create.LessonSelectDrawer
import plus.vplan.app.feature.homework.ui.components.create.RenameFileDialog
import plus.vplan.app.feature.homework.ui.components.create.SubjectAndDateTile
import plus.vplan.app.feature.homework.ui.components.create.VisibilityTile
import plus.vplan.app.feature.homework.ui.components.create.VppIdBanner
import plus.vplan.app.feature.homework.ui.components.detail.UnoptimisticTaskState
import plus.vplan.app.ui.common.AttachedFile
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.DateSelectConfiguration
import plus.vplan.app.ui.components.DateSelectDrawer
import plus.vplan.app.ui.components.FullscreenDrawerContext
import plus.vplan.app.ui.components.InfoCard
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.cloud_alert
import vplanplus.composeapp.generated.resources.triangle_alert
import vplanplus.composeapp.generated.resources.x

@Composable
fun NewHomeworkDrawerContent(
    viewModel: NewHomeworkViewModel,
    state: NewHomeworkState,
    context: FullscreenDrawerContext
) {
    if (state.currentProfile == null) return

    if (context.isCloseRequestHeld) {
        AlertDialog(
            icon = {
                Icon(
                    painter = painterResource(Res.drawable.triangle_alert),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            onDismissRequest = context.resetCloseRequest,
            title = { Text(text = "Hausaufgabe verwerfen?") },
            text = { Text("Deine Angaben wurden noch nicht gespeichert.") },
            confirmButton = {
                TextButton(onClick = context.hideDrawer) { Text("Verwerfen", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = context.resetCloseRequest) { Text("Weiter bearbeiten") }
            }
        )
    }

    var showLessonSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var showDateSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var fileToRename by rememberSaveable { mutableStateOf<AttachedFile?>(null) }

    LaunchedEffect(state.savingState) {
        if (state.savingState == UnoptimisticTaskState.Success) context.closeDrawerWithAnimation()
    }

    val filePickerLauncher = rememberFilePickerLauncher(
        mode = PickerMode.Multiple(),
        type = PickerType.File()
    ) { files ->
        // Handle picked files
        Logger.d { "Picked files: ${files?.map { it.path }}" }
        files?.forEach { file ->
            viewModel.onEvent(NewHomeworkEvent.AddFile(file))
        }
    }

    val imagePickerLauncher = rememberFilePickerLauncher(
        mode = PickerMode.Multiple(),
        type = PickerType.Image
    ) { images ->
        Logger.d { "Picked images: ${images?.map { it.path }}" }
        images?.forEach { image ->
            viewModel.onEvent(NewHomeworkEvent.AddFile(image))
        }
    }

    Column(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(context.scrollState)
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
            AnimatedVisibility(
                visible = state.showTasksError,
                enter = expandVertically(expandFrom = Alignment.CenterVertically),
                exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Füge mindestens eine nicht-leere Aufgabe hinzu",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Fach & Fälligkeit",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SubjectAndDateTile(
                selectedSubjectInstance = state.selectedSubjectInstance,
                selectedDate = state.selectedDate,
                group = state.currentProfile.groupItem!!,
                isAssessment = false,
                onClickSubjectInstance = { showLessonSelectDrawer = true },
                onClickDate = { showDateSelectDrawer = true }
            )
            AnimatedVisibility(
                visible = state.showDateError,
                enter = expandVertically(expandFrom = Alignment.CenterVertically),
                exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Wähle ein Datum aus",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

            if (state.isPublic != null) VisibilityTile(
                isPublic = state.isPublic,
                selectedSubjectInstance = state.selectedSubjectInstance,
                group = state.currentProfile.groupItem!!,
                onSetVisibility = { isPublic -> viewModel.onEvent(NewHomeworkEvent.SetVisibility(isPublic)) }
            )
            else VppIdBanner(
                canShow = state.canShowVppIdBanner,
                isAssessment = false,
                onHide = { viewModel.onEvent(NewHomeworkEvent.HideVppIdBanner) }
            )

            Spacer(Modifier.height(16.dp))

            FileButtons(
                onClickAddFile = { filePickerLauncher.launch() },
                onClickAddPicture = { imagePickerLauncher.launch() }
            )

            Spacer(Modifier.height(16.dp))
            state.files.forEach { file ->
                key(file.platformFile.path.hashCode()) {
                    FileItem(
                        file = file,
                        onRenameClicked = { fileToRename = file },
                        onDeleteClicked = { viewModel.onEvent(NewHomeworkEvent.RemoveFile(file)) }
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = state.savingState == UnoptimisticTaskState.Error && !state.hasInputErrors,
            enter = expandVertically(expandFrom = Alignment.CenterVertically),
            exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically),
            modifier = Modifier
                .then(context.dragModifier)
                .padding(horizontal = 16.dp)
        ) {
            InfoCard(
                imageVector = Res.drawable.cloud_alert,
                title = "Fehler",
                text = "Die Hausaufgabe wurde nicht gespeichert.",
                textColor = MaterialTheme.colorScheme.onErrorContainer,
                backgroundColor = MaterialTheme.colorScheme.errorContainer,
                modifier = Modifier.padding(bottom = 16.dp)
            )
        }

        Button(
            modifier = Modifier
                .then(context.dragModifier)
                .padding(horizontal = 16.dp)
                .padding(bottom = 16.dp),
            text = "Speichern",
            icon = Res.drawable.check,
            state = if (state.savingState == UnoptimisticTaskState.InProgress) ButtonState.Loading else ButtonState.Enabled,
            size = ButtonSize.Normal,
            onlyEventOnActive = true,
            onClick = { viewModel.onEvent(NewHomeworkEvent.Save) }
        )

        fileToRename?.let { file ->
            RenameFileDialog(
                originalFileName = file.name,
                onDismissRequest = { fileToRename = null },
                onRename = { viewModel.onEvent(NewHomeworkEvent.UpdateFile(fileToRename!!.copyBase(name = it))) }
            )
        }
    }

    if (showLessonSelectDrawer) {
        LessonSelectDrawer(
            group = state.currentProfile.groupItem!!,
            allowGroup = true,
            subjectInstances = state.currentProfile.subjectInstanceItems.filter { subjectInstance -> state.currentProfile.subjectInstanceConfiguration.filterValues { !it }.none { it.key == subjectInstance.id } }.sortedBy { it.subject },
            selectedSubjectInstance = state.selectedSubjectInstance,
            onSelectSubjectInstance = { viewModel.onEvent(NewHomeworkEvent.SelectSubjectInstance(it)) },
            onDismiss = { showLessonSelectDrawer = false }
        )
    }

    if (showDateSelectDrawer) {
        DateSelectDrawer(
            configuration = DateSelectConfiguration(
                allowDatesInPast = false
            ),
            selectedDate = state.selectedDate,
            onSelectDate = { viewModel.onEvent(NewHomeworkEvent.SelectDate(it)) },
            onDismiss = { showDateSelectDrawer = false }
        )
    }
}