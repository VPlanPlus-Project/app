package plus.vplan.app.utils

import android.content.Intent
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import plus.vplan.app.activity

actual object BrowserIntent {
    actual fun openUrl(url: String) {
        val intent = CustomTabsIntent.Builder().build()
        intent.intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.launchUrl(activity, url.toUri())
    }
}