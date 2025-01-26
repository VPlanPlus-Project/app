package plus.vplan.app.feature.assessment.ui.components.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.homework.ui.components.create.DateSelectDrawer
import plus.vplan.app.feature.homework.ui.components.create.FileButtons
import plus.vplan.app.feature.homework.ui.components.create.FileItem
import plus.vplan.app.feature.homework.ui.components.create.LessonSelectDrawer
import plus.vplan.app.feature.homework.ui.components.create.RenameFileDialog
import plus.vplan.app.feature.homework.ui.components.create.SubjectAndDateTile
import plus.vplan.app.feature.homework.ui.components.create.VisibilityTile
import plus.vplan.app.feature.homework.ui.components.create.VppIdBanner
import plus.vplan.app.ui.common.AttachedFile
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.FullscreenDrawerContext
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check

@Composable
fun FullscreenDrawerContext.NewAssessmentDrawerContent() {
    val viewModel = koinViewModel<NewAssessmentViewModel>()
    val state = viewModel.state

    var showLessonSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var showDateSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var fileToRename by rememberSaveable { mutableStateOf<AttachedFile?>(null) }

    val filePickerLauncher = rememberFilePickerLauncher(
        mode = PickerMode.Multiple(),
        type = PickerType.File()
    ) { files ->
        // Handle picked files
        Logger.d { "Picked files: ${files?.map { it.path }}" }
        files?.forEach { file ->
            viewModel.onEvent(NewAssessmentEvent.AddFile(file))
        }
    }

    val imagePickerLauncher = rememberFilePickerLauncher(
        mode = PickerMode.Multiple(),
        type = PickerType.Image
    ) { images ->
        Logger.d { "Picked images: ${images?.map { it.path }}" }
        images?.forEach { image ->
            viewModel.onEvent(NewAssessmentEvent.AddFile(image))
        }
    }

    Column(
        modifier = Modifier
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp))
            .fillMaxSize()
    ) {
        if (state.currentProfile == null) return
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Thema und Beschreibung",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            TextField(
                value = state.description,
                onValueChange = { viewModel.onEvent(NewAssessmentEvent.UpdateDescription(it)) },
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                minLines = 4,
                placeholder = { Text(text = "z.B. Bruchrechnung\n\n- Grundrechenarten mit Brüchen\n- Brüche in Dezimalzahlen umwandeln") },
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Fach & Datum",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SubjectAndDateTile(
                selectedDefaultLesson = state.selectedDefaultLesson,
                selectedDate = state.selectedDate,
                group = state.currentProfile.groupItem!!,
                isAssessment = true,
                onClickDefaultLesson = { showLessonSelectDrawer = true },
                onClickDate = { showDateSelectDrawer = true }
            )

            if (state.isVisible != null) VisibilityTile(
                isPublic = state.isVisible,
                selectedDefaultLesson = state.selectedDefaultLesson,
                group = state.currentProfile.groupItem!!,
                onSetVisibility = { isPublic -> viewModel.onEvent(NewAssessmentEvent.SetVisibility(isPublic)) }
            ) else VppIdBanner(
                canShow = state.canShowVppIdBanner,
                isAssessment = true,
                onHide = { viewModel.onEvent(NewAssessmentEvent.HideVppIdBanner) }
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
                        onDeleteClicked = { viewModel.onEvent(NewAssessmentEvent.RemoveFile(file)) }
                    )
                }
            }
        }

        Button(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Speichern",
            icon = Res.drawable.check,
            state = ButtonState.Enabled,
            size = ButtonSize.Normal,
            onClick = { viewModel.onEvent(NewAssessmentEvent.Save) }
        )
    }

    fileToRename?.let { file ->
        RenameFileDialog(
            file = file,
            onDismissRequest = { fileToRename = null },
            onRename = { viewModel.onEvent(NewAssessmentEvent.UpdateFile(fileToRename!!.copyBase(name = it))) }
        )
    }

    if (showLessonSelectDrawer) LessonSelectDrawer(
        group = (state.currentProfile as Profile.StudentProfile).groupItem!!,
        allowGroup = false,
        defaultLessons = state.currentProfile.defaultLessonItems.filter { defaultLesson -> state.currentProfile.defaultLessons.filterValues { !it }.none { it.key == defaultLesson.id } }
            .sortedBy { it.subject },
        selectedDefaultLesson = state.selectedDefaultLesson,
        onSelectDefaultLesson = { if (it == null) return@LessonSelectDrawer; viewModel.onEvent(NewAssessmentEvent.SelectDefaultLesson(it)) },
        onDismiss = { showLessonSelectDrawer = false }
    )

    if (showDateSelectDrawer) DateSelectDrawer(
        selectedDate = state.selectedDate,
        onSelectDate = { viewModel.onEvent(NewAssessmentEvent.SelectDate(it)) },
        onDismiss = { showDateSelectDrawer = false }
    )
}