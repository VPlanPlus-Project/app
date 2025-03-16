package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign

@Composable
@Deprecated("Use FeedTitle instead", replaceWith = ReplaceWith("FeedTitle", "plus.vplan.app.feature.home.ui.components.FeedTitle"))
fun SectionTitle(
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        SectionTitle(title)
        SectionSubtitle(subtitle)
    }
}

@Composable
@Deprecated("Use FeedTitle instead", replaceWith = ReplaceWith("FeedTitle", "plus.vplan.app.feature.home.ui.components.FeedTitle"))
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
}

@Composable
private fun SectionSubtitle(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.End
    )
}