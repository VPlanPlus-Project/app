package plus.vplan.app.feature.onboarding.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.core.ui.CoreUiRes
import plus.vplan.app.core.ui.theme.AppTheme


enum class CurrentStage {
    SchoolSearch,
    Credentials,
    Profile,
    ProfileConfiguration,
    Notifications,
    Done
}

@Composable
fun BoxScope.ProgressIndicator(
    currentStage: CurrentStage
) {
    val currentProgress by animateFloatAsState(currentStage.ordinal.toFloat())
    Row(
        modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .widthIn(max = 720.dp)
            .padding(top = WindowInsets.systemBars.asPaddingValues().calculateTopPadding())
            .height(64.dp)
            .padding(horizontal = 48.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        StageIcon(
            icon = painterResource(CoreUiRes.drawable.search),
            targetStage = CurrentStage.SchoolSearch,
            currentProgress = currentProgress
        )
        LinearProgressIndicator(
            progress = { CurrentStage.SchoolSearch.getProgressForStage(currentProgress) },
            drawStopIndicator = {},
            gapSize = 0.dp,
            modifier = Modifier.weight(1f)
        )
        StageIcon(
            icon = painterResource(CoreUiRes.drawable.key_round),
            targetStage = CurrentStage.Credentials,
            currentProgress = currentProgress
        )
        LinearProgressIndicator(
            progress = { CurrentStage.Credentials.getProgressForStage(currentProgress) },
            drawStopIndicator = {},
            gapSize = 0.dp,
            modifier = Modifier.weight(1f)
        )
        StageIcon(
            icon = painterResource(CoreUiRes.drawable.user),
            targetStage = CurrentStage.Profile,
            currentProgress = currentProgress
        )
        LinearProgressIndicator(
            progress = { CurrentStage.Profile.getProgressForStage(currentProgress) },
            drawStopIndicator = {},
            gapSize = 0.dp,
            modifier = Modifier.weight(1f)
        )
        StageIcon(
            icon = painterResource(CoreUiRes.drawable.list_ordered),
            targetStage = CurrentStage.ProfileConfiguration,
            currentProgress = currentProgress
        )
        LinearProgressIndicator(
            progress = { CurrentStage.ProfileConfiguration.getProgressForStage(currentProgress) },
            drawStopIndicator = {},
            gapSize = 0.dp,
            modifier = Modifier.weight(1f)
        )
        StageIcon(
            icon = painterResource(CoreUiRes.drawable.bell_ring),
            targetStage = CurrentStage.Notifications,
            currentProgress = currentProgress
        )
        LinearProgressIndicator(
            progress = { CurrentStage.Notifications.getProgressForStage(currentProgress) },
            drawStopIndicator = {},
            gapSize = 0.dp,
            modifier = Modifier.weight(1f)
        )
        StageIcon(
            icon = painterResource(CoreUiRes.drawable.check),
            targetStage = CurrentStage.Done,
            currentProgress = currentProgress
        )
    }
}



private fun CurrentStage.getProgressForStage(progress: Float): Float {
    return progress.coerceIn(this.ordinal.toFloat(), this.ordinal + 1f) - this.ordinal
}

@Composable
private fun StageIcon(
    icon: Painter,
    targetStage: CurrentStage,
    currentProgress: Float,
) {
    AnimatedContent(
        targetState = targetStage.getProgressForStage(currentProgress) > 0f,
        modifier = Modifier.size(16.dp),
        transitionSpec = {
            fadeIn() + scaleIn(
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessMedium,
                )
            ) togetherWith fadeOut() + scaleOut()
        }
    ) { isDone ->
        if (isDone) Icon(
            painter = painterResource(CoreUiRes.drawable.check),
            modifier = Modifier
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
                .padding(2.dp)
                .size(16.dp),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onPrimary
        )
        else Icon(
            painter = icon,
            modifier = Modifier.size(16.dp),
            contentDescription = null,
        )
    }
}

@Preview
@Composable
private fun ProgressIndicatorPreview() {
    AppTheme(dynamicColor = false) {
        Box(Modifier.fillMaxWidth()) {
            ProgressIndicator(CurrentStage.Notifications)
        }
    }
}