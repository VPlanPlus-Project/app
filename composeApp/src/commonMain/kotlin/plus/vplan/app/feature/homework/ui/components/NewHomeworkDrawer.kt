package plus.vplan.app.feature.homework.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Image
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
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import co.touchlab.kermit.Logger
import io.github.vinceglb.filekit.compose.rememberFilePickerLauncher
import io.github.vinceglb.filekit.core.PickerMode
import io.github.vinceglb.filekit.core.PickerType
import io.github.vinceglb.filekit.core.extension
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.VPP_ID_AUTH_URL
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.ButtonType
import plus.vplan.app.ui.components.FullscreenDrawerContext
import plus.vplan.app.ui.components.InfoCard
import plus.vplan.app.ui.subjectIcon
import plus.vplan.app.ui.theme.ColorToken
import plus.vplan.app.ui.theme.customColors
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.BrowserIntent
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.mediumDayOfWeekNames
import plus.vplan.app.utils.now
import plus.vplan.app.utils.shortMonthNames
import plus.vplan.app.utils.toHumanSize
import plus.vplan.app.utils.untilText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.file
import vplanplus.composeapp.generated.resources.file_text
import vplanplus.composeapp.generated.resources.image
import vplanplus.composeapp.generated.resources.info
import vplanplus.composeapp.generated.resources.pencil
import vplanplus.composeapp.generated.resources.rotate_cw
import vplanplus.composeapp.generated.resources.user
import vplanplus.composeapp.generated.resources.users
import vplanplus.composeapp.generated.resources.x
import kotlin.time.Duration.Companion.days

@Composable
fun FullscreenDrawerContext.NewHomeworkDrawerContent() {
    val viewModel = koinViewModel<NewHomeworkViewModel>()
    val state = viewModel.state

    var showLessonSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var showDateSelectDrawer by rememberSaveable { mutableStateOf(false) }
    var fileToRename by rememberSaveable { mutableStateOf<File?>(null) }

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
//                                Text(
//                                    text = selectedDefaultLesson?.let { defaultLesson ->
//                                        "${defaultLesson.subject} $DOT ${defaultLesson.teacher?.toValueOrNull()?.name ?: "Kein Lehrer"}"
//                                    } ?: "Klasse ${state.currentProfile?.group?.toValueOrNull()?.name}",
//                                    style = MaterialTheme.typography.bodySmall,
//                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
//                                )
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

            if (state.isPublic != null) Column {
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
//                                Text(
//                                    text =
//                                    if ((state.selectedDefaultLesson?.groups?.size ?: 0) > 1) "Klassen ${state.selectedDefaultLesson?.groups.orEmpty().mapNotNull { it.toValueOrNull() }.joinToString { it.name }}"
//                                    else "Klasse ${state.currentProfile?.group?.toValueOrNull()?.name}",
//                                    style = MaterialTheme.typography.titleSmall,
//                                    color = if (displayVisibility) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
//                                )
                            }
                        }
                    }
                }
            } else {
                AnimatedVisibility(
                    visible = state.canShowVppIdBanner,
                    enter = EnterTransition.None,
                    exit = shrinkVertically() + fadeOut()
                ) {
                    InfoCard(
                        modifier = Modifier
                            .padding(top = 16.dp)
                            .padding(horizontal = 16.dp),
                        imageVector = Res.drawable.info,
                        title = "Cloud-Speicherung",
                        text = "Teile Hausaufgaben mit deiner Klasse, wenn du dich mit einer vpp.ID anmeldest.",
                        buttonText2 = "Ignorieren",
                        buttonAction2 = { viewModel.onEvent(NewHomeworkEvent.HideVppIdBanner) },
                        buttonText1 = "Anmelden",
                        buttonAction1 = { BrowserIntent.openUrl(VPP_ID_AUTH_URL) },
                        backgroundColor = customColors[ColorToken.YellowContainer]!!.get(),
                        textColor = customColors[ColorToken.OnYellowContainer]!!.get()
                    )
                }
            }

            Spacer(Modifier.height(16.dp))
            Text(
                text = "Dateianhänge",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .clip(RoundedCornerShape(16.dp)),
                verticalArrangement = Arrangement.spacedBy(2.dp)
            ) {
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Dokument anhängen",
                    icon = Res.drawable.file_text,
                    state = ButtonState.Enabled,
                    size = ButtonSize.Normal,
                    type = ButtonType.Secondary,
                    onClick = { filePickerLauncher.launch() }
                )
                Button(
                    modifier = Modifier.fillMaxWidth(),
                    text = "Bild anhängen",
                    icon = Res.drawable.image,
                    state = ButtonState.Enabled,
                    size = ButtonSize.Normal,
                    type = ButtonType.Secondary,
                    onClick = { imagePickerLauncher.launch() }
                )
            }

            Spacer(Modifier.height(16.dp))
            state.files.forEach { file ->
                key(file.platformFile.path.hashCode()) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .padding(horizontal = 16.dp)
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(92.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(MaterialTheme.colorScheme.surfaceContainer),
                            contentAlignment = Alignment.Center
                        ) image@{
                            file.bitmap?.let { bitmap ->
                                Image(
                                    bitmap = bitmap,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .thenIf(Modifier.rotate(animateFloatAsState(((file as? File.Image)?.rotation ?: 0) * 90f).value)) { file is File.Image },
                                    contentScale = ContentScale.Fit
                                )
                            } ?: Icon(
                                painter = painterResource(Res.drawable.file),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp),
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                        Column(Modifier.fillMaxWidth()) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) fileNameAndDetails@{
                                Text(
                                    text = file.platformFile.extension.uppercase(),
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    maxLines = 1,
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer)
                                        .padding(4.dp)
                                )
                                Column {
                                    Text(
                                        text = file.name,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = MaterialTheme.colorScheme.onSurface,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = buildString {
                                            append(file.size.toHumanSize())
                                            if (file is File.Document) append(" $DOT ${file.pages} Seite" + (if (file.pages > 1) "n" else ""))
                                            if (file is File.Image) append(" $DOT ${file.widthWithRotation}x${file.heightWithRotation}")
                                        },
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 1
                                    )
                                }
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.End)
                            ) tools@{
                                if (file is File.Image) {
                                    IconButton(
                                        onClick = { viewModel.onEvent(NewHomeworkEvent.UpdateFile(file.copy(rotation = file.rotation + 1))) }
                                    ) {
                                        Icon(
                                            painter = painterResource(Res.drawable.rotate_cw),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp),
                                            tint = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                }
                                IconButton(
                                    onClick = { fileToRename = file }
                                ) {
                                    Icon(
                                        painter = painterResource(Res.drawable.pencil),
                                        contentDescription = null,
                                        modifier = Modifier.size(24.dp),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                                IconButton(
                                    onClick = { viewModel.onEvent(NewHomeworkEvent.RemoveFile(file)) }
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
            }
        }
        Button(
            modifier = Modifier.padding(horizontal = 16.dp),
            text = "Speichern",
            icon = Res.drawable.check,
            state = ButtonState.Enabled,
            size = ButtonSize.Normal,
            onClick = { viewModel.onEvent(NewHomeworkEvent.Save) }
        )

        if (fileToRename != null) {
            val confirm: (String) -> Unit = { fileNameValue ->
                viewModel.onEvent(NewHomeworkEvent.UpdateFile(fileToRename!!.copyBase(name = fileNameValue + "." + fileToRename?.platformFile?.extension)))
                fileToRename = null
            }
            var fileNameValue by rememberSaveable { mutableStateOf((fileToRename?.name ?: ".file").substringBeforeLast(".")) }
            var fileName by remember {
                mutableStateOf(
                    TextFieldValue(
                        text = fileNameValue,
                        selection = TextRange(0, fileNameValue.length)
                    )
                )
            }
            LaunchedEffect(fileName.text) { fileNameValue = fileName.text }
            AlertDialog(
                onDismissRequest = { fileToRename = null },
                icon = {
                    Icon(
                        painter = painterResource(Res.drawable.file_text),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                },
                title = { Text("Datei umbenennen") },
                text = {
                    val focusRequester = remember { FocusRequester() }
                    Column {
                        TextField(
                            value = fileName,
                            onValueChange = { fileName = it },
                            label = { Text("Name") },
                            placeholder = { Text(fileToRename?.name ?: "") },
                            singleLine = true,
                            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                            keyboardActions = KeyboardActions(onDone = { confirm(fileName.text) }),
                            trailingIcon = {
                                Text(
                                    text = ".${fileToRename?.platformFile?.extension ?: ""}",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = MaterialTheme.colorScheme.outline,
                                )
                            },
                            modifier = Modifier.focusRequester(focusRequester)
                        )
                    }
                    LaunchedEffect(Unit) { focusRequester.requestFocus() }
                },
                confirmButton = {
                    TextButton(
                        onClick = { confirm(fileName.text) },
                    ) {
                        Text("Speichern")
                    }
                },
                dismissButton = {
                    TextButton(
                        onClick = { fileToRename = null }
                    ) {
                        Text("Abbrechen")
                    }
                }
            )
        }
    }

    if (showLessonSelectDrawer) {
//        LessonSelectDrawer(
//            group = (state.currentProfile as Profile.StudentProfile).group,
//            defaultLessons = state.currentProfile.defaultLessons.filterValues { it }.keys.sortedBy { it.toValueOrNull()?.subject }.mapNotNull { it.toValueOrNull() },
//            selectedDefaultLesson = state.selectedDefaultLesson,
//            onSelectDefaultLesson = { viewModel.onEvent(NewHomeworkEvent.SelectDefaultLesson(it)) },
//            onDismiss = { showLessonSelectDrawer = false }
//        )
    }

    if (showDateSelectDrawer) {
        DateSelectDrawer(
            selectedDate = state.selectedDate,
            onSelectDate = { viewModel.onEvent(NewHomeworkEvent.SelectDate(it)) },
            onDismiss = { showDateSelectDrawer = false }
        )
    }
}