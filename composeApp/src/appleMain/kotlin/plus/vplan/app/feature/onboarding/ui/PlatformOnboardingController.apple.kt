package plus.vplan.app.feature.onboarding.ui

class ApplePlatformOnboardingController : PlatformOnboardingController {
    override fun showPostOnboardingSheet(onClosed: () -> Unit) {
        PostOnboardingBridge.onClosedCallback = onClosed
        PostOnboardingBridge.showSheet()
    }
}

object PostOnboardingBridge {
    var showSheetCallback: (() -> Unit)? = null

    var onClosedCallback: (() -> Unit)? = null

    fun showSheet() {
        showSheetCallback?.invoke()
    }

    fun onClosed() {
        onClosedCallback?.invoke()
    }
}