package plus.vplan.app.utils

import platform.UIKit.UIPasteboard

actual fun readLatestClipboardValue(): String? {
    return UIPasteboard.generalPasteboard.string
}