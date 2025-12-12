@file:OptIn(ExperimentalMaterial3Api::class, ExperimentalMaterial3ExpressiveApi::class)

package plus.vplan.app.feature.grades.page.view.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import plus.vplan.app.feature.grades.domain.usecase.GradeLockState
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import vplanplus.composeapp.generated.resources.calendars
import vplanplus.composeapp.generated.resources.chart_no_axes_combined
import vplanplus.composeapp.generated.resources.lock

/**
 * @param onRequestGradeLock Callback to request locking the grades
 */
@Composable
fun TopBar(
    subtitle: String?,
    gradesLockState: GradeLockState,
    topScrollBehavior: TopAppBarScrollBehavior?,
    onBack: () -> Unit,
    onRequestGradeLock: () -> Unit,
    onOpenGradesAnalytics: () -> Unit,
    onOpenYearSelector: () -> Unit,
) {
    TopAppBar(
        title = { Text("Noten") },
        subtitle = {
            var lastSubtitle by remember { mutableStateOf(subtitle) }
            if (subtitle != null) lastSubtitle = subtitle
            AnimatedVisibility(
                visible = subtitle != null,
                enter = expandVertically(),
                exit = shrinkVertically()
            ) {
                Text(lastSubtitle!!)
            }
        },
        navigationIcon = {
            IconButton(onClick = onBack) {
                Icon(
                    painter = painterResource(Res.drawable.arrow_left),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.size(24.dp)
                )
            }
        },
        actions = {
            RequestLockButton(
                visible = gradesLockState == GradeLockState.Unlocked,
                onRequestGradeLock = onRequestGradeLock
            )
            androidx.compose.animation.AnimatedVisibility(
                visible = gradesLockState.canAccess,
                enter = expandHorizontally(expandFrom = Alignment.CenterHorizontally) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally) + fadeOut(),
            ) {
                IconButton(onClick = onOpenGradesAnalytics) {
                    Icon(
                        painter = painterResource(Res.drawable.chart_no_axes_combined),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
            androidx.compose.animation.AnimatedVisibility(
                visible = gradesLockState != GradeLockState.Locked,
                enter = expandHorizontally(expandFrom = Alignment.CenterHorizontally) + fadeIn(),
                exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally) + fadeOut()
            ) {
                IconButton(onClick = onOpenYearSelector) {
                    Icon(
                        painter = painterResource(Res.drawable.calendars),
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        },
        scrollBehavior = topScrollBehavior
    )
}

@Composable
private fun RequestLockButton(
    visible: Boolean,
    onRequestGradeLock: () -> Unit,
) {
    AnimatedVisibility(
        visible = visible,
        enter = expandHorizontally(expandFrom = Alignment.CenterHorizontally) + fadeIn(),
        exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally) + fadeOut(),
    ) {
        IconButton(onClick = onRequestGradeLock) {
            Icon(
                painter = painterResource(Res.drawable.lock),
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
@Preview
private fun TopBarPreview() {
    TopBar(
        subtitle = "2025/2026",
        gradesLockState = GradeLockState.Unlocked,
        topScrollBehavior = null,
        onBack = {},
        onRequestGradeLock = {},
        onOpenGradesAnalytics = {},
        onOpenYearSelector = {}
    )
}