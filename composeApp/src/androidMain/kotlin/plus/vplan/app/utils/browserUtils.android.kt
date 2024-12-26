package plus.vplan.app.utils

import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import plus.vplan.app.activity

actual object BrowserIntent {
    actual fun openUrl(url: String) {
        val intent = CustomTabsIntent.Builder().build()
        intent.launchUrl(activity, url.toUri())
    }
}