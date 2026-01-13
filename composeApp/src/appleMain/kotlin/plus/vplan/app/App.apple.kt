package plus.vplan.app

import platform.Foundation.NSBundle

actual val versionCode: Int = NSBundle.mainBundle
    .objectForInfoDictionaryKey("CFBundleVersion")
    ?.toString()
    ?.toInt()
    ?: -1

actual val versionName: String = NSBundle.mainBundle
    .objectForInfoDictionaryKey("CFBundleShortVersionString")
    ?.toString()
    ?: "unknown"