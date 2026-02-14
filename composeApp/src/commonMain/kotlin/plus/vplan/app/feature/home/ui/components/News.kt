package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import plus.vplan.app.domain.model.News
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.megaphone

@Composable
fun NewsSection(
    modifier: Modifier = Modifier,
    isUnread: Boolean,
    news: List<News>,
    onNewsClicked: (newsId: Int) -> Unit
) {
    Column(
        modifier = modifier,
    ) {
        FeedTitle(
            icon = Res.drawable.megaphone,
            title = if (isUnread) "Ungelesene Meldungen" else "Alle Meldungen"
        )
        Spacer(Modifier.size(8.dp))
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp)),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            news.forEach { news ->
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp))
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .clickable { onNewsClicked(news.id) }
                        .padding(16.dp)
                ) {
                    Text(
                        text = news.title,
                        style = MaterialTheme.typography.titleLarge,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = news.content,
                        style = MaterialTheme.typography.bodyMedium,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}