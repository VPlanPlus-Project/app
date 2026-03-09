package plus.vplan.app.ui.platform

import platform.Foundation.NSURL
import platform.UIKit.UIApplication

class OpenBiometricSettingsIos: OpenBiometricSettings {
    override fun run() {
        val urlString = "App-Prefs:root=TOUCHID_PASSCODE"
        val url = NSURL.URLWithString(urlString)

        if (url != null) {
            val application = UIApplication.sharedApplication

            val options = mapOf<Any?, Any?>()

            application.openURL(url, options) { success ->
                if (!success) {
                    val fallbackUrl = NSURL.URLWithString(platform.UIKit.UIApplicationOpenSettingsURLString)
                    if (fallbackUrl != null) {
                        application.openURL(fallbackUrl, options, null)
                    }
                }
            }
        }
    }
}