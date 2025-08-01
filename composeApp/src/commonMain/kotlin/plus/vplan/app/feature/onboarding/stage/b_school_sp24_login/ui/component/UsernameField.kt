package plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.ui.component

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left_right
import vplanplus.composeapp.generated.resources.user

@Composable
fun UsernameField(
    username: String,
    isUsernameValid: Boolean,
    areCredentialsInvalid: Boolean,
    onUsernameChanged: (String) -> Unit,
    onFocusPassword: () -> Unit,
    hideBottomLine: Boolean,
    shape: Shape = TextFieldDefaults.shape
) {
    var textFieldValueState by remember {
        mutableStateOf(TextFieldValue(text = username))
    }
    LaunchedEffect(textFieldValueState.text) {
        onUsernameChanged(textFieldValueState.text)
    }
    LaunchedEffect(username) {
        if (username == textFieldValueState.text) return@LaunchedEffect
        textFieldValueState = textFieldValueState.copy(text = username)
    }
    TextField(
        value = textFieldValueState,
        onValueChange = { textFieldValueState = it },
        label = { Text("Nutzername") },
        modifier = Modifier
            .fillMaxWidth(),
        colors = if (hideBottomLine) TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ) else TextFieldDefaults.colors(),
        shape = shape,
        singleLine = true,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        keyboardActions = KeyboardActions(onNext = { onFocusPassword() }),
        isError = !isUsernameValid || areCredentialsInvalid,
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.user),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            var clickCount by remember { mutableStateOf(0) }
            IconButton(
                onClick = {
                    clickCount++
                    val newUsername = if (username == "lehrer") "schueler" else "lehrer"
                    textFieldValueState = TextFieldValue(text = newUsername, selection = TextRange(newUsername.length))
                    onUsernameChanged(newUsername)
                }
            ) {
                val rotation by animateFloatAsState(targetValue = clickCount * 180f)
                Icon(
                    painter = painterResource(Res.drawable.arrow_left_right),
                    contentDescription = null,
                    modifier = Modifier
                        .size(20.dp)
                        .graphicsLayer { rotationY = rotation }
                )
            }
        }
    )
    AnimatedVisibility(
        visible = !isUsernameValid,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Text(
            text = "Benutzername ungültig (entweder schueler oder lehrer)",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}