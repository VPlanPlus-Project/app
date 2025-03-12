package plus.vplan.app.feature.settings.page.info.domain.usecase

import platform.UIKit.UIDevice

actual fun getSystemInfo(): FeedbackDeviceInfo {
    return FeedbackDeviceInfo(
        os = UIDevice.currentDevice.systemName,
        osVersion = UIDevice.currentDevice.systemVersion,
        manufacturer = "Apple",
        device = UIDevice.currentDevice.name
    )
}