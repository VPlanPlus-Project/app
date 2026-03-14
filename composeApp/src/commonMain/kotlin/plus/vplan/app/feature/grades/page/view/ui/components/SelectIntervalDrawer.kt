package plus.vplan.app.feature.grades.page.view.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import plus.vplan.app.core.model.besteschule.BesteSchuleInterval
import plus.vplan.app.core.ui.components.ModalBottomSheet
import plus.vplan.app.core.ui.components.SelectContainer
import plus.vplan.app.core.ui.components.SelectItem
import plus.vplan.app.core.ui.components.SheetConfiguration
import plus.vplan.app.core.utils.string.DOT

@Composable
private fun SelectIntervalDrawerContent(
    intervals: List<BesteSchuleInterval>,
    selectedInterval: BesteSchuleInterval?,
    onClickInterval: (BesteSchuleInterval) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        intervals
            .groupBy { it.year.id }
            .forEach { (_, intervals) ->
                SelectContainer {
                    intervals.forEach { interval ->
                        SelectItem(
                            icon = null,
                            title = buildString {
                                append(interval.name)
                                append(" $DOT ${interval.year.name}")
                            },
                            subtitle = buildString {
                                val parts = mutableListOf<String>()
                                val includedInterval = interval.includedIntervalId?.let { includedIntervalId ->
                                    intervals.firstOrNull { it.id == includedIntervalId }
                                }
                                includedInterval?.let { includedIntervalInstance ->
                                    parts.add("Inklusive ${includedIntervalInstance.name}")
                                }
                                (interval.collectionIds.toSet() + includedInterval?.collectionIds.orEmpty()
                                    .toSet()).size.let { collectionCount ->
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
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SelectIntervalDrawer(
    intervals: List<BesteSchuleInterval>,
    selectedInterval: BesteSchuleInterval?,
    onClickInterval: (BesteSchuleInterval) -> Unit,
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(true)
    val scope = rememberCoroutineScope()

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        configuration = SheetConfiguration(
            title = "Halbjahr auswählen",
            showCloseButton = true,
            closeButtonAction = onDismiss,
        )
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(contentPadding)
        ) {
            SelectIntervalDrawerContent(intervals, selectedInterval) {
                onClickInterval(it)
                scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() }
            }
        }
    }
}