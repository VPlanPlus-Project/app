package plus.vplan.app

import platform.Foundation.NSBundle

actual val versionCode: Int = NSBundle.mainBundle
    .objectForInfoDictionaryKey("CFBundleVersion")
    ?.toString()
    ?.also {
        require(it.toIntOrNull() != null) {
            "CFBundleVersion should be Integer, was $it"
        }
    }
    ?.toInt()
    ?: -1

actual val versionName: String = NSBundle.mainBundle
    .objectForInfoDictionaryKey("CFBundleShortVersionString")
    ?.toString()
    ?: "unknown"