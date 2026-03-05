package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.utils.toDp


@Composable
fun QuickActions(
    onNewHomeworkClicked: () -> Unit,
    onNewAssessmentClicked: () -> Unit,
    onRoomSearchClicked: () -> Unit,
    onFeedbackClicked: () -> Unit
) {
    Column {
        FeedTitle(CoreUiRes.drawable.zap, "Schnellaktionen")
        LazyRow(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.width(8.dp)) }
            item {
                androidx.compose.material3.FilledTonalButton(
                    onClick = onNewHomeworkClicked,
                ) {
                    Icon(
                        painter = painterResource(CoreUiRes.drawable.book_open),
                        contentDescription = null,
                        modifier = Modifier.size(LocalTextStyle.current.lineHeight.toDp())
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("Neue Aufgabe")
                }
            }
            item {
                androidx.compose.material3.FilledTonalButton(
                    onClick = onNewAssessmentClicked,
                ) {
                    Icon(
                        painter = painterResource(CoreUiRes.drawable.notebook_text),
                        contentDescription = null,
                        modifier = Modifier.size(LocalTextStyle.current.lineHeight.toDp())
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("Neue Leistung")
                }
            }
            item {
                androidx.compose.material3.FilledTonalButton(
                    onClick = onRoomSearchClicked,
                ) {
                    Icon(
                        painter = painterResource(CoreUiRes.drawable.door_open),
                        contentDescription = null,
                        modifier = Modifier.size(LocalTextStyle.current.lineHeight.toDp())
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("Freie Räume")
                }
            }
            item {
                androidx.compose.material3.FilledTonalButton(
                    onClick = onFeedbackClicked,
                ) {
                    Icon(
                        painter = painterResource(CoreUiRes.drawable.message_square),
                        contentDescription = null,
                        modifier = Modifier.size(LocalTextStyle.current.lineHeight.toDp())
                    )
                    Spacer(Modifier.size(4.dp))
                    Text("Feedback senden")
                }
            }
            item {}
        }
    }
}