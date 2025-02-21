package plus.vplan.app.feature.profile.page.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.domain.model.School
import plus.vplan.app.ui.components.AutoResizedText
import plus.vplan.app.ui.components.Button
import plus.vplan.app.ui.components.ButtonSize
import plus.vplan.app.ui.components.ButtonState
import plus.vplan.app.ui.components.ButtonType
import plus.vplan.app.utils.blendColor
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.logo_dark

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSwitcher(
    profiles: Map<School, List<Profile>>,
    showVppIdBanner: Boolean,
    activeProfile: Profile,
    onDismiss: () -> Unit,
    onSelectProfile: (profile: Profile) -> Unit,
    onCreateNewProfile: (school: School?) -> Unit,
    onConnectVppId: () -> Unit,
    onOpenProfileSettings: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val hideSheet = { scope.launch { sheetState.hide() }.invokeOnCompletion { onDismiss() } }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState
    ) {
        Column(
            modifier = Modifier
                .padding(horizontal = 16.dp)
                .padding(bottom = 8.dp)
                .navigationBarsPadding()
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp)),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                profiles.forEach { (school, profiles) ->
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(4.dp))
                            .background(MaterialTheme.colorScheme.surfaceContainerHigh)
                            .padding(vertical = 16.dp)
                    ) {
                        Text(
                            text = school.name,
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(horizontal = 16.dp),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(Modifier.height(8.dp))
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            item { Spacer(Modifier.width(8.dp)) }
                            items(profiles) { profile ->
                                ProfileIcon(
                                    label = profile.name,
                                    type = if (activeProfile == profile) ProfileIconType.Active else ProfileIconType.Other,
                                    onClick = { hideSheet(); onSelectProfile(profile) }
                                )
                            }
                            item {
                                ProfileIcon(
                                    label = "+",
                                    type = ProfileIconType.Button,
                                    onClick = { hideSheet(); onCreateNewProfile(school) }
                                )
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(8.dp))
            if (showVppIdBanner) Row(
                modifier = Modifier
                    .padding(bottom = 8.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(8.dp))
                    .clickable { onConnectVppId() }
                    .background(Brush.horizontalGradient(
                        0f to MaterialTheme.colorScheme.tertiary,
                        .7f to blendColor(MaterialTheme.colorScheme.tertiary, MaterialTheme.colorScheme.surface, 0.4f)
                    ))
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.logo_dark),
                        contentDescription = null,
                        modifier = Modifier.size(20.dp),
                        contentScale = ContentScale.Inside
                    )
                    Text(
                        text = ".ID",
                        color = Color.White,
                        modifier = Modifier
                            .padding(start = 4.dp)
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color.Magenta)
                            .padding(1.dp)
                            .padding(horizontal = 1.dp)
                    )
                    Spacer(Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "Mit vpp.ID anmelden",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                        Text(
                            text = "Nimm am digitalen Klassenalltag teil.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onTertiary
                        )
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Button(
                    text = "Weitere Schule",
                    state = ButtonState.Enabled,
                    size = ButtonSize.Normal,
                    type = ButtonType.Outlined,
                    center = true,
                    onClick = { hideSheet(); onCreateNewProfile(null) },
                    modifier = Modifier.weight(1f, true)
                )
                Button(
                    text = "Profileinstellungen",
                    state = ButtonState.Enabled,
                    size = ButtonSize.Normal,
                    type = ButtonType.Primary,
                    center = true,
                    onClick = { hideSheet(); onOpenProfileSettings() },
                    modifier = Modifier.weight(1f, true)
                )
            }
        }
    }
}

@Composable
private fun ProfileIcon(
    label: String,
    type: ProfileIconType,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(RoundedCornerShape(50))
            .background(
                when (type) {
                    ProfileIconType.Active -> MaterialTheme.colorScheme.primary
                    ProfileIconType.Button -> MaterialTheme.colorScheme.surfaceContainerLow
                    ProfileIconType.Other -> MaterialTheme.colorScheme.tertiary
                }
            )
            .border(
                width = 1.dp,
                color = when (type) {
                    ProfileIconType.Button -> MaterialTheme.colorScheme.primary
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(50)
            )
            .clickable { onClick() }
            .padding(4.dp),
        contentAlignment = Alignment.Center
    ) {
        AutoResizedText(
            text = label,
            color = when (type) {
                ProfileIconType.Active -> MaterialTheme.colorScheme.onPrimary
                ProfileIconType.Button -> MaterialTheme.colorScheme.onSurface
                ProfileIconType.Other -> MaterialTheme.colorScheme.onTertiary
            }
        )
    }
}

private enum class ProfileIconType {
    Active, Button, Other
}