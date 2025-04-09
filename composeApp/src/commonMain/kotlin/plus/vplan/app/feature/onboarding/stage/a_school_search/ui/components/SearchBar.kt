package plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
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
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.OnboardingSchoolSearchEvent
import plus.vplan.app.feature.onboarding.stage.a_school_search.ui.OnboardingSchoolSearchTextFieldError
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_right
import vplanplus.composeapp.generated.resources.search

@Composable
fun SearchBar(
    query: String,
    textFieldError: OnboardingSchoolSearchTextFieldError?,
    onEvent: (OnboardingSchoolSearchEvent) -> Unit
) {
    Row(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp)
            .padding(bottom = 16.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(
            modifier = Modifier.weight(1f)
        ) {
            TextField(
                value = query,
                onValueChange = { onEvent(OnboardingSchoolSearchEvent.OnQueryChanged(it)) },
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
                        painter = painterResource(Res.drawable.search),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp)
                    )
                },
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                keyboardActions = KeyboardActions(
                    onNext = { onEvent(OnboardingSchoolSearchEvent.OnUseIndiwareClicked) }
                ),
                isError = textFieldError != null,
            )
            AnimatedContent(
                targetState = textFieldError
            ) { error ->
                Text(
                    text = when (error) {
                        null -> ""
                        OnboardingSchoolSearchTextFieldError.BadSp24Id -> "Schulnummer muss 8-stellig sein oder nach Schule suchen"
                        OnboardingSchoolSearchTextFieldError.SchoolNotFound -> "Die angegebene Schule wurde nicht gefunden"
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
            onClick = { onEvent(OnboardingSchoolSearchEvent.OnUseIndiwareClicked) },
            modifier = Modifier.size(56.dp),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                painter = painterResource(Res.drawable.arrow_right),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}