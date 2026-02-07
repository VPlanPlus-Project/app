package plus.vplan.app.utils

import android.content.ClipboardManager
import android.content.ClipDescription
import android.content.Context
import org.koin.core.context.GlobalContext

actual fun readLatestClipboardValue(): String? {
    val context = GlobalContext.get().get<Context>()
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = clipboard.primaryClip
    if (clip != null && clip.description.hasMimeType(ClipDescription.MIMETYPE_TEXT_PLAIN)) {
        return clip.getItemAt(0).text?.toString()
    }
    return null
}