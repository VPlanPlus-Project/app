package plus.vplan.app.feature.grades.page.view.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDate
import kotlinx.datetime.format
import kotlinx.datetime.format.Padding
import kotlinx.datetime.format.char
import plus.vplan.app.domain.model.besteschule.BesteSchuleYear
import plus.vplan.app.ui.components.SelectContainer
import plus.vplan.app.ui.components.SelectItem
import plus.vplan.app.utils.safeBottomPadding

private val dateFormat = LocalDate.Format {
    day(Padding.ZERO)
    char('.')
    monthNumber(Padding.ZERO)
    char('.')
    year(Padding.ZERO)
}

@Composable
private fun SelectYearDrawerContent(
    years: List<BesteSchuleYear>,
    selectedYear: BesteSchuleYear?,
    onClickYear: (BesteSchuleYear) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        SelectContainer {
            years
                .forEach { year ->
                    SelectItem(
                        icon = null,
                        title = buildString {
                            append(year.name)
                        },
                        subtitle = buildString {
                            append(year.from.format(dateFormat))
                            append(" - ")
                            append(year.to.format(dateFormat))
                        },
                        isSelected = selectedYear?.id == year.id,
                        onClick = { onClickYear(year) }
                    )
                }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectYearDrawer(
    years: List<BesteSchuleYear>,
    selectedYear: BesteSchuleYear?,
    onClickYear: (BesteSchuleYear) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0.dp) },
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = safeBottomPadding())
        ) {
            Text(
                text = "Schuljahr auswählen",
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(Modifier.height(8.dp))
            SelectYearDrawerContent(years, selectedYear) {
                onClickYear(it)
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
            }
        }
    }
}