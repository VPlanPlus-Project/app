package plus.vplan.app.feature.onboarding.stage.school_credentials.ui.components

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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.ui.CoreUiRes

@Composable
fun PasswordField(
    password: String,
    passwordFocusRequester: FocusRequester,
    areCredentialsInvalid: Boolean,
    onPasswordChanged: (String) -> Unit,
    onCheckCredentials: () -> Unit,
    passwordVisible: Boolean = false,
    onTogglePasswordVisible: (to: Boolean) -> Unit,
    shape: Shape = TextFieldDefaults.shape
) {
    OutlinedTextField(
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
        shape = shape,
        visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
        leadingIcon = {
            Icon(
                painter = painterResource(CoreUiRes.drawable.rectangle_ellipsis),
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
        },
        trailingIcon = {
            IconButton(
                onClick = { onTogglePasswordVisible(!passwordVisible) }
            ) {
                AnimatedContent(
                    targetState = passwordVisible
                ) { visible ->
                    Icon(
                        painter = painterResource(if (visible) CoreUiRes.drawable.eye else CoreUiRes.drawable.eye_off),
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