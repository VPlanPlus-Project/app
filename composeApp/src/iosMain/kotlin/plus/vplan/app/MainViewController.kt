package plus.vplan.app

import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.UIKit.UIViewController
import plus.vplan.app.domain.model.OpenQuicklook

@Suppress("unused") // Is called in SwiftUI
fun initKoin() {
    plus.vplan.app.di.initKoin()
}

lateinit var mainViewController: UIViewController
lateinit var quicklook: OpenQuicklook

inline fun <reified T : Any> getKoinInstance(): T {
    return object : KoinComponent {
        val value: T by inject<T>()
    }.value
}

@Suppress("unused") // Is called in SwiftUI
fun mainViewController(
    url: String,
    notificationTask: String?,
    quicklookImpl: OpenQuicklook
): UIViewController {
    var task: StartTask? = null
    quicklook = quicklookImpl
    if (url.startsWith("vpp://app/auth")) {
        Logger.i { "vpp.ID authentication" }
        val token = url.substringAfter("vpp://app/auth/")
        task = StartTask.VppIdLogin(token)
    }
    if (!notificationTask.isNullOrBlank()) {
        task = getTaskFromNotificationString(notificationTask)
    }
    mainViewController = ComposeUIViewController { App(task = task) }

    return mainViewController
}