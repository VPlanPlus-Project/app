package plus.vplan.app.core.ui.util

import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.net.toUri
import org.koin.core.context.GlobalContext
import plus.vplan.app.core.platform.ActivityProvider

actual fun openUrl(url: String) {
    val activityProvider = GlobalContext.get().get<ActivityProvider>()
    val context = activityProvider.currentActivity ?: return
    val intent = CustomTabsIntent.Builder().build()
    intent.launchUrl(context, url.toUri())
}