package plus.vplan.app.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.ui.theme.bodyFontFamily
import plus.vplan.app.ui.thenIf
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
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    textColor: Color = MaterialTheme.colorScheme.onSurfaceVariant,
    shadow: Boolean = true
) {
    Column(
        modifier = modifier
            .thenIf(Modifier.shadow(2.dp, shape = RoundedCornerShape(16.dp))) { shadow }
            .clip(RoundedCornerShape(16.dp))
            .background(backgroundColor)
            .padding(start = 4.dp)
    ) {
        Row(Modifier.fillMaxWidth().padding(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                painter = painterResource(imageVector),
                contentDescription = null,
                modifier = Modifier.size(24.dp).padding(2.dp),
                tint = textColor
            )
            Column(modifier = Modifier.padding(start = 12.dp)) {
                Text(text = title, style = MaterialTheme.typography.titleSmall, color = textColor, fontFamily = bodyFontFamily())
                Text(text = text, color = textColor, style = MaterialTheme.typography.bodySmall)
            }
        }

        if (buttonText1 != null) Row(
            modifier = Modifier
                .padding(end = 8.dp)
                .align(Alignment.End),
        ) {
            TextButton(
                onClick = { buttonAction1() },
                colors = ButtonColors(
                    contentColor = textColor,
                    containerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                    disabledContentColor = textColor.toBlackAndWhite()
                )
            ) {
                Text(text = buttonText1)
            }
            if (buttonText2 != null) {
                Spacer(modifier = Modifier.size(8.dp))
                TextButton(
                    onClick = { buttonAction2() },
                    colors = ButtonColors(
                        contentColor = textColor,
                        containerColor = Color.Transparent,
                        disabledContainerColor = Color.Transparent,
                        disabledContentColor = textColor.toBlackAndWhite()
                    )
                ) {
                    Text(text = buttonText2)
                }
            }
        }
    }
}