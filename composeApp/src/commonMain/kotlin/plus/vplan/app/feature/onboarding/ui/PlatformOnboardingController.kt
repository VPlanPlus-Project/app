package plus.vplan.app.feature.onboarding.ui

interface PlatformOnboardingController {
    fun showPostOnboardingSheet(onClosed: () -> Unit)
}