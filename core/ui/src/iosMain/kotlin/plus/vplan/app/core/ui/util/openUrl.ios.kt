package plus.vplan.app.core.ui.util

import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.Foundation.NSURL
import platform.SafariServices.SFSafariViewController
import platform.UIKit.UIViewController

actual fun openUrl(url: String) {
    val mainViewController = object : KoinComponent {
        val mainViewController by inject<UIViewController>()
    }.mainViewController
    val page = NSURL(string = url)
    val safariVC = SFSafariViewController(page)
    mainViewController.presentViewController(safariVC, animated = true, completion = null)
}