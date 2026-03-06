package plus.vplan.app

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_NIGHT_YES
import androidx.compose.ui.tooling.preview.AndroidUiModes.UI_MODE_TYPE_NORMAL
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.components.Button
import plus.vplan.app.core.ui.components.ButtonType
import plus.vplan.app.core.ui.theme.AppTheme
import plus.vplan.app.core.ui.theme.monospaceFontFamily
import plus.vplan.app.utils.copyToClipboard
import kotlin.time.Duration.Companion.seconds


@Composable
fun ErrorPage(
    error: Error,
    onOpenAppInfo: () -> Unit = {},
    onRestartApp: () -> Unit = {},
) {
    val showHorizontalLayout = currentWindowAdaptiveInfo().windowSizeClass.let { windowSizeClass ->
        windowSizeClass.isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND) &&
                !windowSizeClass.isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)
    }

    var showCopyButtonFull by remember { mutableStateOf(true) }
    val copyButtonAlpha by animateFloatAsState(if (showCopyButtonFull) 1f else .75f, label = "copy button alpha")

    LaunchedEffect(Unit) {
        delay(5.seconds)
        showCopyButtonFull = false
    }

    Box(
        modifier = Modifier
            .background(MaterialTheme.colorScheme.background)
            .fillMaxSize()
            .padding(WindowInsets.safeDrawing.asPaddingValues())
            .padding(16.dp)
    ) {
        if (showHorizontalLayout) Row(
            modifier = Modifier.fillMaxSize(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Info(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f, true),
                showHorizontalLayout = showHorizontalLayout,
                onRestartApp = onRestartApp,
                onOpenAppInfo = onOpenAppInfo,
            )

            ErrorContainer(
                modifier = Modifier
                    .fillMaxHeight()
                    .weight(1f, true),
                error = error,
                showHorizontalLayout = showHorizontalLayout,
                copyButtonAlpha = copyButtonAlpha,
                onRestartApp = onRestartApp,
                onOpenAppInfo = onOpenAppInfo,
            )
        } else Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Info(
                modifier = Modifier
                    .fillMaxWidth(),
                showHorizontalLayout = showHorizontalLayout,
                onRestartApp = onRestartApp,
                onOpenAppInfo = onOpenAppInfo,
            )

            ErrorContainer(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f, true),
                error = error,
                showHorizontalLayout = showHorizontalLayout,
                copyButtonAlpha = copyButtonAlpha,
                onRestartApp = onRestartApp,
                onOpenAppInfo = onOpenAppInfo,
            )
        }
    }
}

@Composable
private fun MainButtons(
    onRestartApp: () -> Unit,
    onOpenAppInfo: () -> Unit,
) {
    Row(
        modifier = Modifier
            .padding(top = 8.dp)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            text = "Neustarten",
            icon = CoreUiRes.drawable.rotate_cw,
            modifier = Modifier.weight(1f),
            onClick = onRestartApp
        )

        Button(
            text = "App-Info",
            type = ButtonType.Secondary,
            icon = CoreUiRes.drawable.info,
            modifier = Modifier.weight(1f),
            onClick = onOpenAppInfo
        )
    }
}

@Composable
private fun Info(
    modifier: Modifier = Modifier,
    showHorizontalLayout: Boolean,
    onRestartApp: () -> Unit,
    onOpenAppInfo: () -> Unit,
) {
    Column(modifier = modifier) {
        Icon(
            painter = painterResource(CoreUiRes.drawable.triangle_alert),
            contentDescription = "Error",
            tint = MaterialTheme.colorScheme.error,
            modifier = Modifier
                .padding(bottom = 16.dp)
                .size(64.dp)
        )

        Text(
            text = "Ein schwerer Fehler ist aufgetreten",
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.headlineMedium,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        Text(
            text = "Das tut uns leid. VPlanPlus muss neugestartet werden. Ein Fehlerbericht wurde erstellt und versant.",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = if (showHorizontalLayout) Modifier.weight(1f, true) else Modifier,
        )

        if (showHorizontalLayout) MainButtons(
            onRestartApp = onRestartApp,
            onOpenAppInfo = onOpenAppInfo,
        )
    }
}

@Composable
private fun ErrorContainer(
    modifier: Modifier = Modifier,
    error: Error,
    showHorizontalLayout: Boolean,
    copyButtonAlpha: Float,
    onRestartApp: () -> Unit,
    onOpenAppInfo: () -> Unit,
) {
    Column(modifier = modifier) {
        Text(
            text = "Fehlerdetails",
            style = MaterialTheme.typography.titleMedium,
            modifier = Modifier.padding(bottom = 4.dp),
            color = MaterialTheme.colorScheme.onSurface
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f, true)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .horizontalScroll(rememberScrollState())
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp)
            ) {
                Box {
                    Text(
                        text = error.stacktrace,
                        fontFamily = monospaceFontFamily(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        softWrap = false
                    )
                }
            }

            FilledTonalIconButton(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp)
                    .alpha(copyButtonAlpha),
                onClick = { copyToClipboard("Stacktrace", error.stacktrace) }
            ) {
                Icon(
                    painter = painterResource(CoreUiRes.drawable.copy),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp).padding(2.dp),
                )
            }
        }

        if (!showHorizontalLayout) MainButtons(
            onRestartApp = onRestartApp,
            onOpenAppInfo = onOpenAppInfo,
        )
    }
}

data class Error(
    val stacktrace: String
)

@Preview(name = "Phone", device = "spec:width=411dp,height=891dp")
@Preview(name = "Phone - Landscape", device = "spec:width=411dp,height=891dp,orientation=landscape,dpi=420")
@Preview(name = "Unfolded Foldable", device = "spec:width=673dp,height=841dp")
@Preview(name = "Tablet", device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait")
@Preview(name = "Tablet - Landscape", device = "spec:width=1280dp,height=800dp,dpi=240")
@Preview(name = "Desktop", device = "spec:width=1920dp,height=1080dp,dpi=160")
@Preview(uiMode = UI_MODE_NIGHT_YES or UI_MODE_TYPE_NORMAL)
@Composable
private fun ErrorPagePreview() {
    AppTheme(dynamicColor = false) {
        ErrorPage(
            error = Error(stacktrace = RuntimeException().stackTraceToString()),
            onRestartApp = {},
            onOpenAppInfo = {},
        )
    }
}