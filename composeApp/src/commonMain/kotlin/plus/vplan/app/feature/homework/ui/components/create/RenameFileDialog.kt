package plus.vplan.app.feature.homework.ui.components.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
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
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.file_text

@Composable
fun RenameFileDialog(
    originalFileName: String,
    onDismissRequest: () -> Unit,
    onRename: (String) -> Unit,
) {
    val extension = if ('.' in originalFileName) originalFileName.substringAfterLast(".") else null
    val originalName = if ('.' in originalFileName) originalFileName.substringBeforeLast(".") else originalFileName
    val confirm: (String) -> Unit = { fileNameValue ->
        onRename(fileNameValue.trim() + (extension?.let { ".$it" } ?: ""))
        onDismissRequest()
    }
    var fileNameValue by rememberSaveable { mutableStateOf(originalName) }
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
            val textStyle = LocalTextStyle.current
            val extensionColor = MaterialTheme.colorScheme.outline
            Column {
                TextField(
                    value = fileName,
                    onValueChange = { fileName = it },
                    label = { Text("Name") },
                    placeholder = { Text(originalName) },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                    keyboardActions = KeyboardActions(onDone = { confirm(fileName.text) }),
                    modifier = Modifier.focusRequester(focusRequester),
                    visualTransformation = if (extension == null) VisualTransformation.None else VisualTransformation {
                        return@VisualTransformation TransformedText(
                            text = buildAnnotatedString {
                                append(fileName.text)
                                withStyle(textStyle.copy(color = extensionColor).toSpanStyle()) {
                                    append(".$extension")
                                }
                            },
                            offsetMapping = object : OffsetMapping {
                                override fun originalToTransformed(offset: Int): Int {
                                    return offset.coerceAtMost(fileName.text.length)
                                }
                                override fun transformedToOriginal(offset: Int): Int {
                                    return offset.coerceAtMost(fileName.text.length)
                                }
                            }
                        )
                    }
                )
            }
            LaunchedEffect(Unit) { focusRequester.requestFocus() }
        },
        confirmButton = {
            TextButton(
                onClick = { confirm(fileName.text) },
                enabled = fileName.text.isNotBlank()
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