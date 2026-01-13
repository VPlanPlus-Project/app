package plus.vplan.app

import platform.Foundation.NSBundle

actual val versionCode: Int = NSBundle.mainBundle
    .objectForInfoDictionaryKey("CFBundleShortVersionString")
    ?.toString()
    ?.also {
        require(it.toIntOrNull() != null) {
            "CFBundleShortVersionString should be Integer, was $it"
        }
    }
    ?.toInt()
    ?: -1

actual val versionName: String = NSBundle.mainBundle
    .objectForInfoDictionaryKey("CFBundleVersion")
    ?.toString()
    ?: "unknown"