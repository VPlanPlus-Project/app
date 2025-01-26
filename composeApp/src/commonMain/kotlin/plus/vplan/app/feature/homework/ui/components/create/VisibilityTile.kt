package plus.vplan.app.feature.homework.ui.components.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DividerDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.check
import vplanplus.composeapp.generated.resources.user

@Composable
fun VisibilityTile(
    isPublic: Boolean,
    selectedDefaultLesson: DefaultLesson?,
    group: Group,
    onSetVisibility: (to: Boolean) -> Unit
) {
    Column {
        Spacer(Modifier.height(16.dp))
        Text(
            text = "Sichtbarkeit",
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(horizontal = 16.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(72.dp)
                .padding(horizontal = 16.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f, true)
                    .height(72.dp)
            ) {
                AnimatedContent(
                    targetState = isPublic
                ) { displayVisibility ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (!displayVisibility) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .clickable { onSetVisibility(false) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter =
                            if (displayVisibility) painterResource(Res.drawable.user)
                            else painterResource(Res.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (!displayVisibility) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Nur ich",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (!displayVisibility) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
            Spacer(Modifier.padding(8.dp).width(DividerDefaults.Thickness))
            Box(
                modifier = Modifier
                    .weight(1f, true)
                    .height(72.dp)
            ) {
                AnimatedContent(
                    targetState = isPublic
                ) { displayVisibility ->
                    Row(
                        modifier = Modifier
                            .fillMaxSize()
                            .clip(RoundedCornerShape(16.dp))
                            .background(if (displayVisibility) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surface)
                            .clickable { onSetVisibility(true) }
                            .padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter =
                            if (!displayVisibility) painterResource(Res.drawable.user)
                            else painterResource(Res.drawable.check),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp),
                            tint = if (displayVisibility) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text =
                            if ((selectedDefaultLesson?.groupItems?.size ?: 0) > 1) "Klassen ${selectedDefaultLesson?.groupItems.orEmpty().joinToString { it.name }}"
                            else "Klasse ${group.name}",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (displayVisibility) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        }
    }
}