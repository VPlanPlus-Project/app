package plus.vplan.app.feature.search.ui.main.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.ui.components.DateSelectConfiguration
import plus.vplan.app.ui.components.DateSelectDrawer
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.untilRelativeText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.chevron_down
import vplanplus.composeapp.generated.resources.door_closed
import vplanplus.composeapp.generated.resources.search

@Composable
fun SearchBar(
    value: String,
    selectedDate: LocalDate,
    onQueryChange: (to: String) -> Unit,
    onSelectDate: (date: LocalDate) -> Unit,
) {
    val searchObjects = remember { listOf("Räumen", "Lehrern", "Klassen", "Hausaufgaben", "Leistungserhebungen").shuffled() }
    val infiniteTransition = rememberInfiniteTransition(label = "infinite placeholder")
    val index by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = searchObjects.lastIndex.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500*searchObjects.size, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val focusRequester = remember { FocusRequester() }
    var showDateSelectDrawer by remember { mutableStateOf(false) }

    Column {
        TextField(
            value = value,
            onValueChange = onQueryChange,
            placeholder = {
                Row {
                    Text("Suche nach ")
                    AnimatedContent(
                        targetState = searchObjects[index.toInt()],
                        modifier = Modifier.fillMaxWidth(),
                        transitionSpec = { fadeIn(tween(durationMillis = 200, delayMillis = 200)) togetherWith fadeOut() }
                    ) {
                        Text(it)
                    }
                }
            },
            leadingIcon = {
                Icon(
                    painter = painterResource(Res.drawable.search),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .focusRequester(focusRequester)
        )

        Spacer(Modifier.height(8.dp))

        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {
                AssistChip(
                    onClick = {},
                    label = { Text("Freie Räume") },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.door_closed),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            item { VerticalDivider(Modifier.height(32.dp)) }
            item {
                AssistChip(
                    onClick = { showDateSelectDrawer = true },
                    label = { Text(Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date.untilRelativeText(selectedDate) ?: selectedDate.format(regularDateFormat)) },
                    leadingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.calendar),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.chevron_down),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }
    }

    if (showDateSelectDrawer) DateSelectDrawer(
        configuration = DateSelectConfiguration(
            allowDatesInPast = true,
            title = "Suchdatum auswählen",
            subtitle = "Dieses Datum beinflusst die Darstellung der Stunden von Klassen, Lehrern und Räumen, sie nimmt jedoch keinen Einfluss auf Hausaufgaben oder Leistungserhebungen."
        ),
        selectedDate = selectedDate,
        onSelectDate = { onSelectDate(it) },
        onDismiss = { showDateSelectDrawer = false }
    )

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}