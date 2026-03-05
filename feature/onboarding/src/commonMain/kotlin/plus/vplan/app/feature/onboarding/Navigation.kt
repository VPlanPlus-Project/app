package plus.vplan.app.feature.onboarding

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.ui.NavDisplay
import kotlinx.serialization.Serializable

@Serializable
internal sealed interface Onboarding : NavKey {
    @Serializable
    data object Welcome : Onboarding

    @Serializable
    data object SchoolSelect : Onboarding

    @Serializable
    data object SchoolCredentials: Onboarding

    @Serializable
    data object LoadingData: Onboarding

    @Serializable
    data object ProfileSelection: Onboarding
}

private val enterSlideTransition: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(300))

private val exitSlideTransition: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { -it },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))

private val enterSlideTransitionRight: EnterTransition =
    slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(300)
    ) + fadeIn(animationSpec = tween(300))

private val exitSlideTransitionRight: ExitTransition =
    slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(300)
    ) + fadeOut(animationSpec = tween(300))

internal val transitionSpec = NavDisplay.transitionSpec { enterSlideTransition togetherWith exitSlideTransition } +
        NavDisplay.popTransitionSpec { enterSlideTransitionRight togetherWith exitSlideTransitionRight } +
        NavDisplay.predictivePopTransitionSpec { enterSlideTransitionRight togetherWith exitSlideTransitionRight }