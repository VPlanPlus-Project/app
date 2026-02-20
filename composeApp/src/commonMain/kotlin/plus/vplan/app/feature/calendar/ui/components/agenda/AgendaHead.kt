package plus.vplan.app.feature.calendar.ui.components.agenda

import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalTime
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.model.Day
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.ui.grayScale
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.now
import plus.vplan.app.utils.regularTimeFormat
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.chevron_down

@Composable
fun AgendaHead(
    date: LocalDate,
    dayType: Day.DayType,
    lessons: Int?,
    infiniteTransition: InfiniteTransition,
    start: LocalTime? = null,
    end: LocalTime? = null,
    showLessons: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .height(48.dp)
            .thenIf(Modifier.grayScale()) { date < LocalDate.now() },
        verticalArrangement = Arrangement.Center
    ) {
        CompositionLocalProvider(LocalContentColor provides if (date < LocalDate.now()) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.onSurface) {
            when (dayType) {
                Day.DayType.WEEKEND -> Text(
                    text = "Wochenende",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Day.DayType.HOLIDAY -> Text(
                    text = "Ferien",
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(start = 8.dp)
                )
                Day.DayType.REGULAR -> {
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(end = 4.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onClick() }
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Row {
                                val style = MaterialTheme.typography.titleMedium
                                if (lessons == null) ShimmerLoader(
                                    modifier = Modifier
                                        .alignByBaseline()
                                        .padding(end = 8.dp)
                                        .width(16.dp)
                                        .height(style.lineHeight.toDp())
                                        .clip(RoundedCornerShape(4.dp)),
                                    infiniteTransition = infiniteTransition
                                )
                                Text(
                                    text = "${lessons?.toString()?.plus(" ").orEmpty()}Stunden",
                                    style = style,
                                    modifier = Modifier.alignByBaseline()
                                )
                            }
                            if (start != null && end != null) Text(
                                text = buildString {
                                    append(start.format(regularTimeFormat))
                                    append(" - ")
                                    append(end.format(regularTimeFormat))
                                },
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        val iconRotation by animateFloatAsState(if (showLessons) 1f else 0f, label = "rotation animation")
                        Icon(
                            painter = painterResource(Res.drawable.chevron_down),
                            modifier = Modifier
                                .size(24.dp)
                                .rotate(-180*iconRotation),
                            contentDescription = null
                        )
                    }
                }
                Day.DayType.UNKNOWN -> Unit
            }
        }
    }
}