package plus.vplan.app

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import platform.UIKit.UIViewController
import plus.vplan.app.domain.model.OpenQuicklook
import plus.vplan.app.ui.components.native.NativeViewFactory

val LocalNativeViewFactory = staticCompositionLocalOf<NativeViewFactory> {
    error("No NativeViewFactory provided")
}

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

var task: StartTask? by mutableStateOf(null)

@Suppress("unused") // Is called in SwiftUI
fun mainViewController(
    url: String,
    notificationTask: String?,
    quicklookImpl: OpenQuicklook,
    nativeViewFactory: NativeViewFactory
): UIViewController {
    quicklook = quicklookImpl
    updateTaskFromUrl(url)
    updateTaskFromNotification(notificationTask)

    mainViewController = ComposeUIViewController {
        CompositionLocalProvider(LocalNativeViewFactory provides nativeViewFactory) {
            App(task = task)
        }
    }

    return mainViewController
}

@Suppress("unused") // Is called in SwiftUI
fun updateView(url: String, notificationTask: String?) {
    updateTaskFromUrl(url)
    updateTaskFromNotification(notificationTask)
}

fun updateTaskFromUrl(url: String) {
    if (url.startsWith("vpp://app/auth")) {
        Logger.i { "vpp.ID authentication" }
        val token = url.substringAfter("vpp://app/auth/")
        task = StartTask.VppIdLogin(token)
    }
}

fun updateTaskFromNotification(notificationTask: String?) {
    if (!notificationTask.isNullOrBlank()) {
        task = getTaskFromNotificationString(notificationTask)
    }
}