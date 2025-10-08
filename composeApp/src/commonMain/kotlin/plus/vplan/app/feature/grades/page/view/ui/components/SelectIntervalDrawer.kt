package plus.vplan.app.feature.grades.page.view.ui.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
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
import plus.vplan.app.domain.model.schulverwalter.Interval
import plus.vplan.app.ui.components.SelectContainer
import plus.vplan.app.ui.components.SelectItem
import plus.vplan.app.utils.DOT
import plus.vplan.app.utils.getState
import plus.vplan.app.utils.safeBottomPadding

@Composable
private fun SelectIntervalDrawerContent(
    intervals: List<Interval>,
    selectedInterval: Interval?,
    onClickInterval: (Interval) -> Unit
) {
    SelectContainer {
        intervals.forEach { interval ->
            val includedInterval = interval.includedInterval?.getState()?.value
            val intervalYear = interval.year.getState(null).value
            SelectItem(
                icon = null,
                title = buildString {
                    append(interval.name)
                    if (intervalYear != null) append(" $DOT ${intervalYear.name}")
                },
                subtitle = buildString {
                    val parts = mutableListOf<String>()
                    includedInterval?.let { includedIntervalInstance ->
                        parts.add("Inklusive ${includedIntervalInstance.name}")
                    }
                    (interval.collectionIds.toSet() + includedInterval?.collectionIds.orEmpty().toSet()).size.let { collectionCount ->
                        if (collectionCount > 1) parts.add("$collectionCount Leistungserhebungen")
                        else if (collectionCount == 1) parts.add("eine Leistungserhebung")
                        else Unit
                    }
                    append(parts.joinToString(" $DOT "))
                },
                isSelected = selectedInterval == interval,
                onClick = { onClickInterval(interval) }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectIntervalDrawer(
    intervals: List<Interval>,
    selectedInterval: Interval?,
    onClickInterval: (Interval) -> Unit,
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
                text = "Interval auswählen",
                style = MaterialTheme.typography.headlineLarge,
            )
            Spacer(Modifier.height(8.dp))
            SelectIntervalDrawerContent(intervals, selectedInterval) {
                onClickInterval(it)
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
            }
        }
    }
}