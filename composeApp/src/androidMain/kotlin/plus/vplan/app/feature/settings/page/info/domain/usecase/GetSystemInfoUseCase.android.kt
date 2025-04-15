package plus.vplan.app.feature.settings.page.info.domain.usecase

import android.os.Build

actual fun getSystemInfo(): FeedbackDeviceInfo {
    return FeedbackDeviceInfo(
        os = "Android",
        osVersion = Build.VERSION.RELEASE,
        manufacturer = Build.BRAND,
        device = Build.PRODUCT + " " + Build.MODEL,
        deviceName = Build.DEVICE
    )
}