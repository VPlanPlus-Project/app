package plus.vplan.app.feature.search.ui.main.components.result

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview

@Composable
fun ResultName(
    name: String,
    width: Dp,
    onWidthIncrease: (Dp) -> Unit,
) {
    val localDensity = LocalDensity.current
    Box(
        modifier = Modifier
            .sizeIn(minWidth = width, maxWidth = 72.dp)
            .onSizeChanged { with(localDensity) { it.width.toDp().let { newWidth -> if (newWidth > width) onWidthIncrease(newWidth) } } }
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.primary)
            .padding(6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = name,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onPrimary
        )
    }
}

@Preview
@Composable
private fun ResultNamePreview() {
    ResultName(
        name = "9c",
        width = 64.dp,
        onWidthIncrease = {}
    )
}

@Preview
@Composable
private fun MultipleResultNames() {
    Column {
        val names = listOf("7a", "7b", "JG11", "VERW")
        var width by remember { mutableStateOf(0.dp) }
        names.forEach { name ->
            ResultName(
                name = name,
                width = width,
                onWidthIncrease = { width = it }
            )
        }
    }
}