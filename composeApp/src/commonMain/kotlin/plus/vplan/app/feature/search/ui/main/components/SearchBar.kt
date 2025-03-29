package plus.vplan.app.feature.search.ui.main.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.ui.components.DateSelectConfiguration
import plus.vplan.app.ui.components.DateSelectDrawer
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.utils.dateFormatDDMMMYY
import plus.vplan.app.utils.now
import plus.vplan.app.utils.toDp
import plus.vplan.app.utils.toName
import plus.vplan.app.utils.untilRelativeText
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.chevron_down
import vplanplus.composeapp.generated.resources.filter
import vplanplus.composeapp.generated.resources.search
import vplanplus.composeapp.generated.resources.x

@Composable
fun SearchBar(
    value: String,
    selectedDate: LocalDate,
    selectedSubject: String?,
    selectedAssessmentType: Assessment.Type?,
    focusRequester: FocusRequester?,
    subjects: List<String>,
    onQueryChange: (to: String) -> Unit,
    onSelectDate: (date: LocalDate) -> Unit,
    onSelectSubject: (subject: String?) -> Unit,
    onSelectAssessmentType: (type: Assessment.Type?) -> Unit
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
            if (selectedSubject != null) item {
                FilterChip(
                    selected = true,
                    leadingIcon = {
                        SubjectIcon(
                            subject = selectedSubject,
                            modifier = Modifier.size(18.dp)
                        )
                    },
                    label = { Text(selectedSubject) },
                    onClick = { onSelectSubject(null) },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.x),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
            if (selectedAssessmentType != null) item {
                FilterChip(
                    selected = true,
                    label = { Text(selectedAssessmentType.toName()) },
                    onClick = { onSelectAssessmentType(null) },
                    trailingIcon = {
                        Icon(
                            painter = painterResource(Res.drawable.x),
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                )
            }
        }

        Column(
            modifier = Modifier
                .padding(horizontal = 8.dp)
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .animateContentSize()
            ) {
                val currentFilterTest =  value.split(" ").lastOrNull().orEmpty().lowercase()
                subjects.filter { it.lowercase().startsWith(currentFilterTest) && currentFilterTest.isNotEmpty() }.forEach { subject ->
                    Row(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSelectSubject(subject); onQueryChange(value.split(" ").dropLast(1).joinToString(" ")) }
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                            Icon(
                                painter = painterResource(Res.drawable.filter),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(18.dp)
                            )
                            Text("Filter nach ")
                            SubjectIcon(
                                modifier = Modifier.size(LocalTextStyle.current.lineHeight.toDp()),
                                subject = subject
                            )
                            Text(" $subject")
                        }
                    }
                }
                Assessment.Type.entries.filter { currentFilterTest.isNotEmpty() && currentFilterTest in it.toName().lowercase() }.forEach { type ->
                    Row(
                        modifier = Modifier
                            .padding(bottom = 4.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .clickable { onSelectAssessmentType(type); onQueryChange(value.split(" ").dropLast(1).joinToString(" ")) }
                            .background(MaterialTheme.colorScheme.surfaceContainerHighest)
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        CompositionLocalProvider(LocalTextStyle provides MaterialTheme.typography.bodyMedium) {
                            Icon(
                                painter = painterResource(Res.drawable.filter),
                                contentDescription = null,
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .size(18.dp)
                            )
                            Text("Filter nach " + type.toName())
                        }
                    }
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
                singleLine = true,
                leadingIcon = {
                    Icon(
                        painter = painterResource(Res.drawable.search),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp)
                    )
                },
                trailingIcon = {
                    AnimatedVisibility(
                        visible = value.isNotBlank(),
                        enter = scaleIn() + fadeIn(),
                        exit = scaleOut() + fadeOut()
                    ) {
                        IconButton(
                            onClick = {
                                onQueryChange("")
                                onSelectSubject(null)
                            }
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.x),
                                contentDescription = null,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .let {
                        if (focusRequester != null) it.focusRequester(focusRequester)
                        else it
                    }
            )
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
}