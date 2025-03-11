package plus.vplan.app.feature.settings.page.info.domain.usecase

import android.os.Build

actual fun getSystemInfo(): SystemInfo {
    return SystemInfo(
        os = "Android",
        osVersion = Build.VERSION.RELEASE,
        manufacturer = Build.MANUFACTURER,
        device = Build.PRODUCT + " " + Build.MODEL
    )
}