package plus.vplan.app.feature.onboarding.stage.d_select_profile.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle

@Composable
fun DefaultLessonTitle(onChangeProfile: () -> Unit) {
    Column {
        Text(
            text = "Passe deinen Stundenplan an",
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onSurface,
        )
        Text(
            text = buildAnnotatedString {
                withStyle(style = MaterialTheme.typography.bodyMedium.toSpanStyle().copy(color = MaterialTheme.colorScheme.onSurface)) {
                    append("Deaktiviere Fächer und Kurse, die dich nicht betreffen. Du erhälst nur Benachrichtigungen zu Fächern und Kursen, die du aktiviert hast. ")
                    withLink(
                        LinkAnnotation.Clickable(
                        tag = "change_profile",
                        linkInteractionListener = { onChangeProfile() }
                    )) {
                        withStyle(style = SpanStyle(color = MaterialTheme.colorScheme.primary, textDecoration = TextDecoration.Underline)) {
                            append("Anderes Profil wählen")
                        }
                    }
                }
            }
        )
    }
}