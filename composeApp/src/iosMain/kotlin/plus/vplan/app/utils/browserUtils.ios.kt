package plus.vplan.app.utils

import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import plus.vplan.app.mainViewController

actual object BrowserIntent {
    actual fun openUrl(url: String) {
        val page = NSURL(string = url)
        val safariVC = SFSafariViewController(page)
        mainViewController.presentViewController(safariVC, animated = true, completion = null)
    }
}