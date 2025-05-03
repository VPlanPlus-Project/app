package plus.vplan.app.utils

import android.content.Context
import android.content.ClipboardManager
import android.content.ClipDescription
import plus.vplan.app.activity

actual fun readLatestClipboardValue(): String? {
    val clipboard = activity.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = clipboard.primaryClip
    if (clip != null && clip.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
        return clip.getItemAt(0).text?.toString()
    }
    return null
}