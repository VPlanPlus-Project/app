@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package plus.vplan.app.ui.components

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.utils.toBlackAndWhite

@Composable
fun InfoCard(
    modifier: Modifier = Modifier,
    imageVector: DrawableResource,
    title: String,
    text: String,
    buttonText1: String? = null,
    buttonAction1: () -> Unit = {},
    buttonText2: String? = null,
    buttonAction2: () -> Unit = {},
    color: Color = MaterialTheme.colorScheme.onSurfaceVariant,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, color = color, RoundedCornerShape(8.dp))
            .padding(vertical = 12.dp, horizontal = 16.dp)
    ) {
        Column(Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Icon(
                    painter = painterResource(imageVector),
                    contentDescription = null,
                    modifier = Modifier.size(32.dp).padding(2.dp),
                    tint = color
                )
                Text(text = title, style = MaterialTheme.typography.titleMediumEmphasized, color = color)
            }
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(start = 44.dp),
            )

            if (buttonText1 != null) Row(
                modifier = Modifier
                    .padding(top = 8.dp)
                    .align(Alignment.End),
            ) {
                Button(
                    modifier = Modifier.wrapContentWidth(),
                    onClick = { buttonAction1() },
                    size = ButtonSize.Small,
                    type = ButtonType.Outlined,
                    center = false,
                    text = buttonText1
                )
                if (buttonText2 != null) {
                    Spacer(modifier = Modifier.size(8.dp))
                    TextButton(
                        onClick = { buttonAction2() },
                        colors = ButtonColors(
                            contentColor = color,
                            containerColor = Color.Transparent,
                            disabledContainerColor = Color.Transparent,
                            disabledContentColor = color.toBlackAndWhite()
                        )
                    ) {
                        Text(text = buttonText2)
                    }
                }
            }
        }
    }
}