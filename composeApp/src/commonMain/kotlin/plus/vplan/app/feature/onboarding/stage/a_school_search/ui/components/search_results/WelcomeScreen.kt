package plus.vplan.app.feature.onboarding.stage.a_school_search.ui.components.search_results

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandIn
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.shrinkOut
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withLink
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

@Composable
fun WelcomeScreen(
    showAnimation: Boolean
) {
    var titleVisible by remember { mutableStateOf(!showAnimation) }
    var subtitleVisible by remember { mutableStateOf(!showAnimation) }

    LaunchedEffect(Unit) {
        if (!showAnimation) return@LaunchedEffect
        delay(1000)
        titleVisible = true
        delay(2000)
        subtitleVisible = true
    }

    val animationDuration = 1000
    val enterAnimation = expandIn(tween(animationDuration), Alignment.Center) + fadeIn(tween(animationDuration)) + scaleIn(tween(animationDuration), .5f)
    val exitAnimation = shrinkOut(tween(animationDuration), Alignment.Center) + fadeOut(tween(animationDuration)) + scaleOut(tween(animationDuration))

    Box(
        modifier = Modifier
            .fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            AnimatedVisibility(
                visible = titleVisible,
                enter = enterAnimation,
                exit = exitAnimation
            ) {
                Text(
                    text = "Willkommen bei VPlanPlus",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
            AnimatedVisibility(
                visible = subtitleVisible,
                enter = enterAnimation,
                exit = exitAnimation
            ) {
                Text(
                    text = "Suche nach deiner Schule, um zu beginnen",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    textAlign = TextAlign.Center
                )
            }
        }

        Text(
            text = buildAnnotatedString {
                withStyle(MaterialTheme.typography.labelMedium.let { it.copy(lineHeight = it.fontSize) }.toSpanStyle()) {
                    append("Wenn du fortfährst, akzeptierst du die ")
                    withLink(LinkAnnotation.Url("https://vplan.plus/privacy", TextLinkStyles(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)))) {
                        append("Datenschutzerklärung")
                    }
                    append(" und die ")
                    withLink(LinkAnnotation.Url("https://vplan.plus/tos", TextLinkStyles(style = SpanStyle(fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.primary)))) {
                        append("Nutzungsbedingungen")
                    }
                    append(".")
                }
            },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = 8.dp)
                .padding(bottom = 8.dp)
        )
    }
}