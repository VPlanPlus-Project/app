@file:OptIn(ExperimentalMaterial3Api::class)

package plus.vplan.app.core.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.utils.ui.plus

@Composable
actual fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    configuration: SheetConfiguration,
    sheetState: SheetState,
    content: @Composable (contentPadding: PaddingValues) -> Unit
) {
    val density = LocalDensity.current

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
        sheetState = sheetState,
        contentWindowInsets = { WindowInsets(0) },
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            var headerHeight by remember { mutableStateOf<Dp?>(null) }

            if (headerHeight != null) content(
                WindowInsets.navigationBars.asPaddingValues() + PaddingValues(
                    top = headerHeight!! + 16.dp,
                )
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .onSizeChanged { (_, height) ->
                        headerHeight = with(density) { height.toDp() }
                    }
            ) {
                SheetHeader(configuration = configuration)
            }
        }
    }
}

@Composable
private fun SheetHeader(configuration: SheetConfiguration) {
    val hasTitle   = configuration.title != null || configuration.subtitle != null
    val hasActions = configuration.showCloseButton || configuration.actions.isNotEmpty()

    if (!hasTitle && !hasActions) return

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            if (hasTitle) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(2.dp),
                ) {
                    configuration.title?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.titleMedium,
                        )
                    }
                    configuration.subtitle?.let {
                        Text(
                            text = it,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                        )
                    }
                }
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                configuration.actions.forEach { action ->
                    SheetActionButton(action = action)
                }

                if (configuration.showCloseButton) {
                    FilledTonalIconButton(
                        onClick = configuration.closeButtonAction
                    ) {
                        Icon(
                            painter = painterResource(CoreUiRes.drawable.x),
                            contentDescription = "Schließen",
                            modifier = Modifier.size(20.dp),
                        )
                    }
                }
            }
        }

        HorizontalDivider()
    }
}

@Composable
private fun SheetActionButton(action: SheetActionItem) {
    FilledTonalIconButton(
        onClick = action.onClick,
        enabled = action.enabled && !action.isLoading,
    ) {
        AnimatedContent(
            targetState = action.isLoading,
            label = "SheetActionButtonContent",
        ) { isLoading ->
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp).padding(2.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                Icon(
                    painter = action.icon.painter,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            }
        }
    }
}