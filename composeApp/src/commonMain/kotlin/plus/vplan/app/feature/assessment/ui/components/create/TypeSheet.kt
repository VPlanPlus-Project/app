package plus.vplan.app.feature.assessment.ui.components.create

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import plus.vplan.app.domain.model.Assessment
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.toName

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TypeDrawer(
    selectedType: Assessment.Type?,
    onSelectType: (Assessment.Type) -> Unit,
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
        TypeSelectDrawerContent(
            selectedType = selectedType,
            onSelectType = { onSelectType(it); hideSheet() }
        )
    }
}

@Composable
private fun TypeSelectDrawerContent(
    selectedType: Assessment.Type?,
    onSelectType: (Assessment.Type) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = WindowInsets.safeDrawing.asPaddingValues().calculateBottomPadding() + 16.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        ) {
            Assessment.Type.entries.sortedBy { it.toName() }.forEach { type ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .thenIf(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) { selectedType == type }
                        .defaultMinSize(minHeight = 48.dp)
                        .border(0.5.dp, MaterialTheme.colorScheme.outline)
                        .clickable { onSelectType(type) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = type.toName(),
                        style = MaterialTheme.typography.titleSmall,
                        color = if (selectedType == type) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
    }
}