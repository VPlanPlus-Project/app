package plus.vplan.app.feature.onboarding.stage.b_school_sp24_login.ui.component

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.eye
import vplanplus.composeapp.generated.resources.eye_off
import vplanplus.composeapp.generated.resources.rectangle_ellipsis

@Composable
fun PasswordField(
    password: String,
    passwordFocusRequester: FocusRequester,
    areCredentialsInvalid: Boolean,
    onPasswordChanged: (String) -> Unit,
    onCheckCredentials: () -> Unit,
    hideBottomLine: Boolean,
    shape: Shape = TextFieldDefaults.shape
) {
    var passwordVisible by remember { mutableStateOf(false) }
    TextField(
        value = password,
        onValueChange = { onPasswordChanged(it) },
        label = { Text("Passwort") },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(passwordFocusRequester),
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done, keyboardType = KeyboardType.Password),
        keyboardActions = KeyboardActions(
            onDone = { onCheckCredentials() }
        ),
        singleLine = true,
        isError = areCredentialsInvalid,
        colors = if (hideBottomLine) TextFieldDefaults.colors(
            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent,
            errorIndicatorColor = Color.Transparent,
        ) else TextFieldDefaults.colors(),
        shape = shape,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.rectangle_ellipsis),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { passwordVisible = !passwordVisible }
            ) {
                AnimatedContent(
                    targetState = passwordVisible
                ) { visible ->
                    Icon(
                        painter = painterResource(if (visible) Res.drawable.eye else Res.drawable.eye_off),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    )

    AnimatedVisibility(
        visible = areCredentialsInvalid,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Text(
            text = "Zugangsdaten ungültig",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )
    }
}