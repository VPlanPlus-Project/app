package plus.vplan.app

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import platform.UIKit.UIViewController
import plus.vplan.app.domain.model.OpenQuicklook

@Suppress("unused") // Is called in SwiftUI
fun initKoin() {
    plus.vplan.app.di.initKoin()
}

lateinit var mainViewController: UIViewController
lateinit var quicklook: OpenQuicklook

@Suppress("unused") // Is called in SwiftUI
fun mainViewController(
    url: String,
    quicklookImpl: OpenQuicklook
): UIViewController {
    var task: StartTask? = null
    quicklook = quicklookImpl
    if (url.startsWith("vpp://app/auth")) {
        Logger.i { "vpp.ID authentication" }
        val token = url.substringAfter("vpp://app/auth/")
        task = StartTask.VppIdLogin(token)
    }
    mainViewController = ComposeUIViewController { App(task = task) }
    return mainViewController
}