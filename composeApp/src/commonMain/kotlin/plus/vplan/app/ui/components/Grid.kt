package plus.vplan.app.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun Grid(
    modifier: Modifier = Modifier,
    columns: Int = 2,
    content: List<@Composable (column: Int, row: Int, index: Int) -> Unit>
) {
    Column(modifier = modifier.fillMaxWidth()) {
        val rows = content.chunked(columns)
        var i = 0
        for (row in rows) {
            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                for (item in row) {
                    Box(
                        modifier = Modifier.weight(1f)
                    ) {
                        item(i % columns, i / columns, i)
                        i++
                    }
                }
            }
        }
    }
}