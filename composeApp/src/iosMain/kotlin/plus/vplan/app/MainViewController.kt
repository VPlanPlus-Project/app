package plus.vplan.app

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.window.ComposeUIViewController
import co.touchlab.kermit.Logger
import org.koin.core.KoinApplication
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import org.koin.core.context.loadKoinModules
import org.koin.dsl.module
import platform.UIKit.UIBarMetricsDefault
import platform.UIKit.UIImage
import platform.UIKit.UINavigationController
import platform.UIKit.UIRectEdgeAll
import platform.UIKit.UIViewController
import plus.vplan.app.core.data.file.OpenQuicklook
import plus.vplan.app.feature.onboarding.IosDevInfoSheetHandler

@Suppress("unused") // Is called in SwiftUI
fun initKoin(
    quicklookImpl: OpenQuicklook,
    iosDevInfoSheetHandlerImpl: IosDevInfoSheetHandler,
): KoinApplication {
    quicklook = quicklookImpl
    iosDevInfoSheetHandler = iosDevInfoSheetHandlerImpl
    return plus.vplan.app.di.initKoin()
}

lateinit var mainViewController: UIViewController
lateinit var quicklook: OpenQuicklook
lateinit var iosDevInfoSheetHandler: IosDevInfoSheetHandler

inline fun <reified T : Any> getKoinInstance(): T {
    return object : KoinComponent {
        val value: T by inject<T>()
    }.value
}

var task: StartTask? by mutableStateOf(null)

@Suppress("unused") // Is called in SwiftUI
fun mainViewController(
    url: String,
    notificationTask: String?
): UIViewController {
    updateTaskFromUrl(url)
    updateTaskFromNotification(notificationTask)

    val composeVC = ComposeUIViewController { App(task = task) }
    mainViewController = composeVC

    val navController = UINavigationController(rootViewController = composeVC).apply {
        navigationBar.setBackgroundImage(
            UIImage(),
            forBarMetrics = UIBarMetricsDefault
        )
        navigationBar.shadowImage = UIImage()
        navigationBar.translucent = true

        composeVC.edgesForExtendedLayout = UIRectEdgeAll
        composeVC.extendedLayoutIncludesOpaqueBars = true
    }

    loadKoinModules(module {
        single<UIViewController> { composeVC }
    })

    return navController

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