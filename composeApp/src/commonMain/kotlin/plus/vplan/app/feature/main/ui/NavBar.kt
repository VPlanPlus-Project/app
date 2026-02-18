package plus.vplan.app.feature.main.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.ui.theme.AppTheme
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.calendar_filled
import vplanplus.composeapp.generated.resources.circle_user_round
import vplanplus.composeapp.generated.resources.circle_user_round_filled
import vplanplus.composeapp.generated.resources.house
import vplanplus.composeapp.generated.resources.house_filled
import vplanplus.composeapp.generated.resources.search
import vplanplus.composeapp.generated.resources.search_filled

@Immutable
data class NavBarEntry(
    val icon: Painter,
    val iconSelected: Painter,
    val label: String,
    val onClick: () -> Unit,
    val isSelected: Boolean,
)

@Composable
fun NavBar(
    modifier: Modifier = Modifier,
    items: List<NavBarEntry>
) {
    val colorScheme = MaterialTheme.colorScheme
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(72.dp)
            .background(MaterialTheme.colorScheme.background)
            .drawWithContent {
                drawContent()
                drawLine(
                    color = colorScheme.surfaceVariant,
                    strokeWidth = 1f,
                    start = Offset(0f, 0f),
                    end = Offset(size.width, 0f)
                )
            },
    ) {
        items.forEach { navBarEntry ->
            Column(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .clickable(onClick = navBarEntry.onClick),
                verticalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterVertically),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AnimatedContent(
                    targetState = navBarEntry.isSelected
                ) { isSelected ->
                    Icon(
                        painter = if (isSelected) navBarEntry.iconSelected else navBarEntry.icon,
                        modifier = Modifier
                            .size(26.dp)
                            .padding(2.dp),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onBackground
                    )
                }
                Text(
                    text = navBarEntry.label,
                    color = MaterialTheme.colorScheme.onBackground,
                    style = MaterialTheme.typography.labelSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
@Preview
private fun NavBarPreview() {
    AppTheme(dynamicColor = false, darkTheme = true) {
        NavBar(
            items = listOf(
                NavBarEntry(
                    icon = painterResource(Res.drawable.house),
                    iconSelected = painterResource(Res.drawable.house_filled),
                    label = "Home",
                    onClick = {},
                    isSelected = true,
                ),
                NavBarEntry(
                    icon = painterResource(Res.drawable.calendar),
                    iconSelected = painterResource(Res.drawable.calendar_filled),
                    label = "Kalender",
                    onClick = {},
                    isSelected = false,
                ),
                NavBarEntry(
                    icon = painterResource(Res.drawable.search),
                    iconSelected = painterResource(Res.drawable.search_filled),
                    label = "Suche",
                    onClick = {},
                    isSelected = false,
                ),
                NavBarEntry(
                    icon = painterResource(Res.drawable.circle_user_round),
                    iconSelected = painterResource(Res.drawable.circle_user_round_filled),
                    label = "Profil",
                    onClick = {},
                    isSelected = false,
                )
            )
        )
    }
}