package plus.vplan.app.feature.homework.ui.components.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import io.github.vinceglb.filekit.core.extension
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.ui.common.AttachedFile
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.file_text

@Composable
fun RenameFileDialog(
    file: AttachedFile,
    onDismissRequest: () -> Unit,
    onRename: (String) -> Unit,
) {
    val confirm: (String) -> Unit = { fileNameValue ->
        onRename(fileNameValue + "." + file.platformFile.extension)
        onDismissRequest()
    }
    var fileNameValue by rememberSaveable { mutableStateOf((file.name).substringBeforeLast(".")) }
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
        onDismissRequest = onDismissRequest,
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
                    placeholder = { Text(file.name) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { confirm(fileName.text) }),
                    trailingIcon = {
                        Text(
                            text = ".${file.platformFile.extension}",
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
                onClick = onDismissRequest
            ) {
                Text("Abbrechen")
            }
        }
    )
}