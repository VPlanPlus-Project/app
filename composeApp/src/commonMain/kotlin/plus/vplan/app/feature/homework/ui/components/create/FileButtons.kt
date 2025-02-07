package plus.vplan.app.feature.homework.ui.components.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.ButtonType
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.file_text
import vplanplus.composeapp.generated.resources.image

@Composable
fun FileButtons(
    onClickAddFile: () -> Unit,
    onClickAddPicture: () -> Unit
) {
    Text(
        text = "Dateianhänge",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.padding(horizontal = 16.dp)
    )
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(16.dp)),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        Button(
            modifier = Modifier.fillMaxWidth(),
            text = "Dokument anhängen",
            icon = Res.drawable.file_text,
            state = ButtonState.Enabled,
            size = ButtonSize.Normal,
            type = ButtonType.Secondary,
            onClick = onClickAddFile
        )
        Button(
            modifier = Modifier.fillMaxWidth(),
            text = "Bild anhängen",
            icon = Res.drawable.image,
            state = ButtonState.Enabled,
            size = ButtonSize.Normal,
            type = ButtonType.Secondary,
            onClick = onClickAddPicture
        )
    }
}