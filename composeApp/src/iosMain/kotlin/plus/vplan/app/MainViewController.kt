package plus.vplan.app

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import platform.UIKit.UIViewController

@Suppress("unused") // Is called in SwiftUI
fun initKoin() {
    plus.vplan.app.di.initKoin()
}

lateinit var mainViewController: UIViewController

@Suppress("unused") // Is called in SwiftUI
fun mainViewController(
    url: String
): UIViewController {
    var task: StartTask? = null
    if (url.startsWith("vpp://app/auth")) {
        Logger.i { "vpp.ID authentication" }
        val token = url.substringAfter("vpp://app/auth/")
        task = StartTask.VppIdLogin(token)
    }
    mainViewController = ComposeUIViewController { App(task = task) }
    return mainViewController
}