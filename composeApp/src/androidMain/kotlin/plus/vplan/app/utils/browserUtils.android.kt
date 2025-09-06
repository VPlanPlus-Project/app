package plus.vplan.app.utils

import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import plus.vplan.app.activity

actual fun openUrl(url: String) {
    val context = activity
    val intent = CustomTabsIntent.Builder().build()
    intent.launchUrl(context, url.toUri())
}
