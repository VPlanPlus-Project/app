@file:OptIn(ExperimentalMaterial3ExpressiveApi::class)

package plus.vplan.app.feature.onboarding.stage.a_welcome.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseOutExpo
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.scaleIn
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3ExpressiveApi
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.AppBuildConfig
import plus.vplan.app.ui.theme.displayFontFamily
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_right
import vplanplus.composeapp.generated.resources.logo_black
import vplanplus.composeapp.generated.resources.logo_white
import kotlin.time.Duration.Companion.seconds

@Composable
fun OnboardingWelcomeScreen(
    onNext: () -> Unit
) {
    var animationState by rememberSaveable { mutableStateOf(OnboardingWelcomeScreenAnimationState.Initial) }

    LaunchedEffect(animationState) {
        when (animationState) {
            OnboardingWelcomeScreenAnimationState.Initial -> {
                delay(1.seconds)
                animationState = OnboardingWelcomeScreenAnimationState.Logo
            }
            OnboardingWelcomeScreenAnimationState.Logo -> {
                delay(2.seconds)
                animationState = OnboardingWelcomeScreenAnimationState.Title
            }
            OnboardingWelcomeScreenAnimationState.Title -> {
                delay(1.seconds)
                animationState = OnboardingWelcomeScreenAnimationState.Subtitle
            }
            OnboardingWelcomeScreenAnimationState.Subtitle -> {
                delay(2.seconds)
                animationState = OnboardingWelcomeScreenAnimationState.Button
            }
            else -> Unit
        }
    }

    OnboardingWelcomeScreenContent(
        animationState = animationState,
        onNext = onNext
    )
}

@Composable
private fun OnboardingWelcomeScreenContent(
    animationState: OnboardingWelcomeScreenAnimationState,
    onNext: () -> Unit = {}
) {
    Box(Modifier.fillMaxSize()) {
        Text(
            text = "${AppBuildConfig.APP_VERSION} (${AppBuildConfig.APP_VERSION_CODE})",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onBackground.copy(alpha = .5f),
            modifier = Modifier
                .align(Alignment.TopCenter)
                .padding(WindowInsets.safeDrawing.asPaddingValues())
                .padding(16.dp)
        )
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(64.dp)
                .fillMaxWidth()
        ) {
            AnimatedVisibility(
                visible = animationState.ordinal >= OnboardingWelcomeScreenAnimationState.Logo.ordinal,
                modifier = Modifier.fillMaxWidth(),
                enter = fadeIn(tween(2.seconds.inWholeMilliseconds.toInt())) + scaleIn(tween(2.seconds.inWholeMilliseconds.toInt(), easing = EaseOutExpo))
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Image(
                        painter = if (isSystemInDarkTheme()) painterResource(Res.drawable.logo_white) else painterResource(Res.drawable.logo_black),
                        contentDescription = "App Logo",
                        modifier = Modifier
                            .size(127.dp)
                            .clip(RoundedCornerShape(24.dp))
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = if (isSystemInDarkTheme()) .8f else .2f))
                            .padding(24.dp)
                    )
                }
            }
            Spacer(Modifier.size(24.dp))
            val titleVisible = animationState.ordinal >= OnboardingWelcomeScreenAnimationState.Title.ordinal
            val titleAlpha by animateFloatAsState(if (titleVisible) 1f else 0f, label = "titleAlpha", animationSpec = tween(delayMillis = 250, durationMillis = 1000))
            val titleScale by animateFloatAsState(if (titleVisible) 1f else 0.8f, label = "titleScale", animationSpec = tween(2000))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = titleAlpha
                        scaleX = titleScale
                        scaleY = titleScale
                    }
            ) {
                Text(
                    text = "Willkommen bei VPlanPlus",
                    fontFamily = displayFontFamily(),
                    style = MaterialTheme.typography.displaySmallEmphasized,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )
            }
            Spacer(Modifier.size(8.dp))
            val subtitleVisible = animationState.ordinal >= OnboardingWelcomeScreenAnimationState.Subtitle.ordinal
            val subtitleAlpha by animateFloatAsState(if (subtitleVisible) 1f else 0f, label = "subtitleAlpha", animationSpec = tween(delayMillis = 500, durationMillis = 1000))
            val subtitleScale by animateFloatAsState(if (subtitleVisible) 1f else 0.8f, label = "subtitleScale", animationSpec = tween(2000))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .graphicsLayer {
                        alpha = subtitleAlpha
                        scaleX = subtitleScale
                        scaleY = subtitleScale
                    }
            ) {
                Text(
                    text = "Wir helfen dir bei der Verbindung zu Stundenplan24.de.",
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.align(Alignment.Center),
                    color = MaterialTheme.colorScheme.onBackground.copy(alpha = .7f),
                    textAlign = TextAlign.Center
                )
            }
        }

        val buttonVisible = animationState.ordinal >= OnboardingWelcomeScreenAnimationState.Button.ordinal
        val buttonAlpha by animateFloatAsState(if (buttonVisible) 1f else 0f, label = "buttonAlpha", animationSpec = tween(delayMillis = 500, durationMillis = 1000))
        val buttonScale by animateFloatAsState(if (buttonVisible) 1f else 0.8f, label = "buttonScale", animationSpec = tween(2000))
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .graphicsLayer {
                    alpha = buttonAlpha
                    scaleX = buttonScale
                    scaleY = buttonScale
                }
                .padding(horizontal = 16.dp)
                .padding(bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding() + 16.dp)
        ) {
            plus.vplan.app.ui.components.Button(
                onClick = onNext,
                modifier = Modifier,
                text = "Los geht's",
                icon = Res.drawable.arrow_right
            )
        }
    }
}

@Preview
@Composable
private fun OnboardingWelcomeScreenPreview() {
    OnboardingWelcomeScreenContent(OnboardingWelcomeScreenAnimationState.Initial)
}

private enum class OnboardingWelcomeScreenAnimationState {
    Initial,
    Logo,
    Title,
    Subtitle,
    Button
}