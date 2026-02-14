package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.book_open
import vplanplus.composeapp.generated.resources.door_open
import vplanplus.composeapp.generated.resources.message_square
import vplanplus.composeapp.generated.resources.notebook_text
import vplanplus.composeapp.generated.resources.zap

@Composable
fun QuickActions(
    onNewHomeworkClicked: () -> Unit,
    onNewAssessmentClicked: () -> Unit,
    onRoomSearchClicked: () -> Unit,
    onFeedbackClicked: () -> Unit
) {
    Column {
        FeedTitle(Res.drawable.zap, "Schnellaktionen")
        LazyRow(
            modifier = Modifier
                .padding(top = 4.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                QuickAction(
                    drawable = Res.drawable.book_open,
                    text = "Neue Homework",
                    onClick = onNewHomeworkClicked
                )
            }
            item {
                QuickAction(
                    drawable = Res.drawable.notebook_text,
                    text = "Neue Leistung",
                    onClick = onNewAssessmentClicked
                )
            }
            item {
                QuickAction(
                    drawable = Res.drawable.door_open,
                    text = "Freie Räume",
                    onClick = onRoomSearchClicked
                )
            }
            item {
                QuickAction(
                    drawable = Res.drawable.message_square,
                    text = "Feedback senden",
                    onClick = onFeedbackClicked
                )
            }
        }
    }
}

@Composable
private fun QuickAction(
    drawable: DrawableResource,
    text: String,
    onClick: () -> Unit
) {
    FilledTonalButton(
        onClick = onClick,
        contentPadding = PaddingValues(horizontal = 16.dp)
    ) {
        Icon(
            painter = painterResource(drawable),
            contentDescription = null,
            modifier = Modifier.size(LocalTextStyle.current.lineHeight.toDp())
        )
        Spacer(Modifier.size(8.dp))
        Text(
            text = text,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
@Preview
private fun QuickActionPreview() {
    QuickAction(
        drawable = Res.drawable.message_square,
        text = "Feedback senden",
        onClick = {}
    )
}