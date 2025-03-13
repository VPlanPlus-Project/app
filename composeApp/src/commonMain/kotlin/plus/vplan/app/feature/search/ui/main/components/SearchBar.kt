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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.ui.components.DateSelectConfiguration
import plus.vplan.app.ui.components.DateSelectDrawer
import plus.vplan.app.utils.dateFormatDDMMMYY
import plus.vplan.app.utils.now
import plus.vplan.app.utils.untilRelativeText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.chevron_down
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

    var showDateSelectDrawer by remember { mutableStateOf(false) }

    Column {
        LazyRow(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item {}
            item {
                FilterChip(
                    selected = selectedDate != LocalDate.now(),
                    onClick = { showDateSelectDrawer = true },
                    label = { AnimatedContent(
                        targetState = (LocalDate.now() untilRelativeText selectedDate) ?: selectedDate.format(dateFormatDDMMMYY)
                    ) { Text(it) } },
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

        TextField(
            value = value,
            onValueChange = onQueryChange,
            colors = TextFieldDefaults.colors(
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent,
                disabledIndicatorColor = Color.Transparent,
                errorIndicatorColor = Color.Transparent,
            ),
            placeholder = {
                Row {
                    Text(
                        text = "Suche nach "
                    )
                    AnimatedContent(
                        targetState = searchObjects[index.toInt()],
                        modifier = Modifier.weight(1f, true),
                        transitionSpec = { fadeIn(tween(durationMillis = 200, delayMillis = 200)) togetherWith fadeOut() }
                    ) {
                        Text(
                            text = it,
                            overflow = TextOverflow.Ellipsis,
                            maxLines = 1
                        )
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
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
        )
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
}