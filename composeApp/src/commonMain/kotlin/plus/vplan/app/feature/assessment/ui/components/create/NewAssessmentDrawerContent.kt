package plus.vplan.app.feature.assessment.ui.components.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.ui.components.DateSelectDrawer
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
import plus.vplan.app.ui.components.FullscreenDrawerContext
import plus.vplan.app.utils.toName
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.shapes

@Composable
fun FullscreenDrawerContext.NewAssessmentDrawerContent(
    selectedDate: LocalDate? = null
) {
    val viewModel = koinViewModel<NewAssessmentViewModel>()
    val state = viewModel.state

    LaunchedEffect(selectedDate) {
        if (selectedDate != null) viewModel.onEvent(NewAssessmentEvent.SelectDate(selectedDate))
    }

    var showLessonSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var showDateSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var showTypeSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var fileToRename by rememberSaveable { mutableStateOf<AttachedFile?>(null) }

    LaunchedEffect(state.savingState) {
        if (state.savingState == UnoptimisticTaskState.Success) this@NewAssessmentDrawerContent.closeDrawerWithAnimation()
    }

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
            AnimatedVisibility(
                visible = state.showContentError,
                enter = expandVertically(expandFrom = Alignment.CenterVertically),
                exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Beschreibung darf nicht leer sein",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Fach & Datum",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SubjectAndDateTile(
                selectedSubjectInstance = state.selectedSubjectInstance,
                selectedDate = state.selectedDate,
                group = state.currentProfile.groupItem!!,
                isAssessment = true,
                onClickSubjectInstance = { showLessonSelectDrawer = true },
                onClickDate = { showDateSelectDrawer = true }
            )
            AnimatedVisibility(
                visible = state.showSubjectInstanceError,
                enter = expandVertically(expandFrom = Alignment.CenterVertically),
                exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Wähle ein Fach aus",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }
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

            if (state.isVisible != null) VisibilityTile(
                isPublic = state.isVisible,
                selectedSubjectInstance = state.selectedSubjectInstance,
                group = state.currentProfile.groupItem!!,
                onSetVisibility = { isPublic -> viewModel.onEvent(NewAssessmentEvent.SetVisibility(isPublic)) }
            ) else VppIdBanner(
                canShow = state.canShowVppIdBanner,
                isAssessment = true,
                onHide = { viewModel.onEvent(NewAssessmentEvent.HideVppIdBanner) }
            )

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Kategorie",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Row(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxSize()
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .clickable { showTypeSelectDrawer = true }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.shapes),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                )
                AnimatedContent(
                    targetState = state.type
                ) { displayType ->
                    Text(
                        text = displayType?.toName() ?: "Keine Kategorie",
                        style = MaterialTheme.typography.titleSmall,
                    )
                }
            }
            AnimatedVisibility(
                visible = state.showTypeError,
                enter = expandVertically(expandFrom = Alignment.CenterVertically),
                exit = shrinkVertically(shrinkTowards = Alignment.CenterVertically),
                modifier = Modifier.padding(horizontal = 16.dp)
            ) {
                Text(
                    text = "Wähle eine Kategorie",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                )
            }

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
            state = if (state.savingState == UnoptimisticTaskState.InProgress) ButtonState.Loading else ButtonState.Enabled,
            size = ButtonSize.Normal,
            onlyEventOnActive = true,
            onClick = { viewModel.onEvent(NewAssessmentEvent.Save) }
        )
    }

    fileToRename?.let { file ->
        RenameFileDialog(
            originalFileName = file.name,
            onDismissRequest = { fileToRename = null },
            onRename = { viewModel.onEvent(NewAssessmentEvent.UpdateFile(fileToRename!!.copyBase(name = it))) }
        )
    }

    if (showLessonSelectDrawer) LessonSelectDrawer(
        group = (state.currentProfile as Profile.StudentProfile).groupItem!!,
        allowGroup = false,
        subjectInstances = state.currentProfile.subjectInstanceItems.filter { subjectInstance -> state.currentProfile.subjectInstanceConfiguration.filterValues { !it }.none { it.key == subjectInstance.id } }
            .sortedBy { it.subject },
        selectedSubjectInstance = state.selectedSubjectInstance,
        onSelectSubjectInstance = { if (it == null) return@LessonSelectDrawer; viewModel.onEvent(NewAssessmentEvent.SelectSubjectInstance(it)) },
        onDismiss = { showLessonSelectDrawer = false }
    )

    if (showDateSelectDrawer) DateSelectDrawer(
        configuration = DateSelectConfiguration(
            allowDatesInPast = false
        ),
        selectedDate = state.selectedDate,
        onSelectDate = { viewModel.onEvent(NewAssessmentEvent.SelectDate(it)) },
        onDismiss = { showDateSelectDrawer = false }
    )

    if (showTypeSelectDrawer) TypeDrawer(
        selectedType = state.type,
        onSelectType = { viewModel.onEvent(NewAssessmentEvent.UpdateType(it)) },
        onDismiss = { showTypeSelectDrawer = false }
    )
}