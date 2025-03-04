package plus.vplan.app.feature.calendar.ui.components

import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.feature.calendar.ui.DisplayType
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.sheet
import vplanplus.composeapp.generated.resources.table_properties

@Composable
fun DisplaySelectType(
    displayType: DisplayType,
    onSelectType: (DisplayType) -> Unit
) {
    SingleChoiceSegmentedButtonRow {
        SegmentedButton(
            shape = RoundedCornerShape(50, 0, 0, 50),
            onClick = { onSelectType(DisplayType.Calendar) },
            selected = displayType == DisplayType.Calendar
        ) { Icon(
            painter = painterResource(Res.drawable.sheet),
            modifier = Modifier.size(18.dp),
            contentDescription = null
        ) }
        SegmentedButton(
            shape = RoundedCornerShape(0, 50, 50, 0),
            onClick = { onSelectType(DisplayType.Agenda) },
            selected = displayType == DisplayType.Agenda
        ) { Icon(
            painter = painterResource(Res.drawable.table_properties),
            modifier = Modifier.size(18.dp).rotate(180f),
            contentDescription = null
        ) }
    }
}