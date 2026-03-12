package plus.vplan.app.feature.home.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.core.utils.date.regularDateTimeFormat
import plus.vplan.app.core.utils.date.untilRelativeText
import kotlin.time.Duration.Companion.hours
import kotlin.time.Instant

@Composable
fun LastUpdated(
    modifier: Modifier = Modifier,
    lastUpdated: Instant,
    currentTime: Instant,
) {
    val lastUpdateMoreThanTwoHoursAgo = currentTime.epochSeconds - lastUpdated.epochSeconds > 2.hours.inWholeSeconds
    Text(
        modifier = modifier
            .fillMaxWidth(),
        text = buildString {
            append("Letzte Aktualisierung: ")
            if (lastUpdateMoreThanTwoHoursAgo) {
                append(lastUpdated.toLocalDateTime(TimeZone.currentSystemDefault()).format(regularDateTimeFormat))
            } else {
                append(currentTime untilRelativeText lastUpdated)
            }
        },
        color = MaterialTheme.colorScheme.secondary,
        style = MaterialTheme.typography.bodySmall,
        textAlign = TextAlign.Center,
    )
}

@Preview
@Composable
private fun LastUpdatedPreview() {
    val currentTime = Instant.fromEpochSeconds(1700000000)
    AppTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            LastUpdated(
                lastUpdated = currentTime - 1.hours,
                currentTime = currentTime
            )
        }
    }
}

@Preview
@Composable
private fun LastUpdatedLongPreview() {
    val currentTime = Instant.fromEpochSeconds(1700000000)
    AppTheme(dynamicColor = false) {
        Column(modifier = Modifier.padding(16.dp)) {
            LastUpdated(
                lastUpdated = currentTime - 3.hours,
                currentTime = currentTime
            )
        }
    }
}
