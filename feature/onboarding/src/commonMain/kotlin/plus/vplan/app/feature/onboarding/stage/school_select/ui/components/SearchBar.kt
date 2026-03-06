package plus.vplan.app.feature.onboarding.stage.school_select.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.feature.onboarding.stage.school_select.ui.SchoolSearchEvent
import plus.vplan.app.feature.onboarding.stage.school_select.ui.SchoolSearchTextFieldError

@Composable
fun SearchBar(
    modifier: Modifier = Modifier,
    query: String,
    textFieldError: SchoolSearchTextFieldError?,
    onEvent: (SchoolSearchEvent) -> Unit
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                value = query,
                onValueChange = { onEvent(SchoolSearchEvent.OnQueryChanged(it)) },
                label = { Text(
                    text = "Schule oder Stundenplan24.de-Schulnummer",
                    maxLines = 1,
                    modifier = Modifier.basicMarquee(
                        iterations = Int.MAX_VALUE
                    )
                ) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                colors = TextFieldDefaults.colors(
                    focusedIndicatorColor = Color.Transparent,
                    unfocusedIndicatorColor = Color.Transparent,
                    disabledIndicatorColor = Color.Transparent,
                    errorIndicatorColor = Color.Transparent,
                ),
                shape = RoundedCornerShape(8.dp),
                leadingIcon = {
                    Icon(
                        painter = painterResource(CoreUiRes.drawable.search),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { onEvent(SchoolSearchEvent.OnUseSp24SchoolClicked) }
                ),
                isError = textFieldError != null,
            )
            AnimatedContent(
                targetState = textFieldError
            ) { error ->
                Text(
                    text = when (error) {
                        null -> ""
                        SchoolSearchTextFieldError.BadSp24Id -> "Schulnummer muss 8-stellig sein oder nach Schule suchen"
                        SchoolSearchTextFieldError.SchoolNotFound -> "Die angegebene Schule wurde nicht gefunden"
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    maxLines = 1
                )
            }
        }
        FilledIconButton(
            onClick = { onEvent(SchoolSearchEvent.OnUseSp24SchoolClicked) },
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                painter = painterResource(CoreUiRes.drawable.arrow_right),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}