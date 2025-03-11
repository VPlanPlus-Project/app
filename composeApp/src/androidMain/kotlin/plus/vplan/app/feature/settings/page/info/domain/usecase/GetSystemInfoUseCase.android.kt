package plus.vplan.app.feature.settings.page.info.domain.usecase

import android.os.Build

actual fun getSystemInfo(): FeedbackDeviceInfo {
    return FeedbackDeviceInfo(
        os = "Android",
        osVersion = Build.VERSION.RELEASE,
        manufacturer = Build.MANUFACTURER,
        device = Build.PRODUCT + " " + Build.MODEL
    )
}