package plus.vplan.app.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import plus.vplan.app.utils.atStartOfDay
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import plus.vplan.app.utils.shortDayOfWeekNames
import plus.vplan.app.utils.shortMonthNames
import plus.vplan.app.utils.untilRelativeText
import kotlin.time.Duration.Companion.days

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DateSelectDrawer(
    configuration: DateSelectConfiguration,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val modalState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val scope = rememberCoroutineScope()
    val hideSheet = remember { { scope.launch { modalState.hide() }.invokeOnCompletion { onDismiss() } } }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0.dp) },
        sheetState = modalState
    ) {
        DateSelectDrawerContent(
            configuration = configuration,
            selectedDate = selectedDate,
            onSelectDate = { onSelectDate(it); hideSheet() }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DateSelectDrawerContent(
    configuration: DateSelectConfiguration,
    selectedDate: LocalDate?,
    onSelectDate: (LocalDate) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() + 16.dp)
    ) {

        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .fillMaxWidth()
        ) {
            if (configuration.title != null) Text(
                text = configuration.title,
                style = MaterialTheme.typography.headlineLarge,
            )

            if (configuration.subtitle != null) Text(
                text = configuration.subtitle,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            item { Spacer(Modifier.width(8.dp)) }
            items(7) { days ->
                val date = LocalDate.now() + (days - if (configuration.allowDatesInPast) 7/2 else 0).days
                FilterChip(
                    selected = date == selectedDate,
                    onClick = { onSelectDate(date) },
                    label = {
                        Text(
                            text = (LocalDate.now() untilRelativeText date) ?: date.format(LocalDate.Format {
                                dayOfWeek(shortDayOfWeekNames)
                                chars(", ")
                                dayOfMonth()
                                chars(". ")
                                monthName(shortMonthNames)
                            })
                        )
                    }
                )
            }
            item {}
        }
        val state = rememberDatePickerState(
            initialSelectedDateMillis = selectedDate?.atStartOfDay()?.toInstant(TimeZone.UTC)?.toEpochMilliseconds(),
            selectableDates = object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                    if (configuration.allowDatesInPast) return true
                    val date = Instant.fromEpochMilliseconds(utcTimeMillis).toLocalDateTime(TimeZone.UTC).date
                    return date >= LocalDate.now()
                }

                override fun isSelectableYear(year: Int): Boolean {
                    return configuration.allowDatesInPast || year >= LocalDate.now().year
                }
            }
        )
        LaunchedEffect(state.selectedDateMillis) {
            val date = state.selectedDateMillis?.let { Instant.fromEpochMilliseconds(it).toLocalDateTime(TimeZone.UTC).date } ?: return@LaunchedEffect
            if (date != selectedDate) onSelectDate(date)
        }
        DatePicker(
            state = state,
            modifier = Modifier.padding(horizontal = 16.dp),
            colors = DatePickerDefaults.colors(containerColor = Color.Transparent)
        )
    }
}

data class DateSelectConfiguration(
    val allowDatesInPast: Boolean,
    val title: String? = null,
    val subtitle: String? = null
)