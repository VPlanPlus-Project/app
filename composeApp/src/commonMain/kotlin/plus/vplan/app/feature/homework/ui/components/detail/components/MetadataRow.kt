package plus.vplan.app.feature.homework.ui.components.detail.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import plus.vplan.app.ui.thenIf

@Composable fun tableNameStyle() = MaterialTheme.typography.bodyLarge.copy(Color.Gray)
@Composable fun tableValueStyle() = MaterialTheme.typography.bodyMedium

@Composable
fun MetadataRow(
    key: @Composable BoxScope.() -> Unit,
    value: @Composable BoxScope.() -> Unit
) {
    Row(
        modifier = Modifier.padding(vertical = 2.dp).fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(Modifier.weight(.7f, true)) {
            key()
        }
        Box(Modifier.weight(1f, true)) {
            value()
        }
    }
}

@Composable
fun MetadataValueContainer(
    modifier: Modifier = Modifier,
    canEdit: Boolean,
    editStyling: Boolean = true,
    onClick: () -> Unit,
    content: @Composable () -> Unit
) {
    if (canEdit) Box(
        modifier = modifier
            .defaultMinSize(minHeight = 32.dp)
            .clip(RoundedCornerShape(8.dp))
            .thenIf(Modifier.background(MaterialTheme.colorScheme.surfaceVariant)) { editStyling }
            .clickable(canEdit) { onClick() }
            .thenIf(Modifier.padding(4.dp)) { editStyling },
        contentAlignment = Alignment.CenterStart
    ) {
        content()
    } else content()
}