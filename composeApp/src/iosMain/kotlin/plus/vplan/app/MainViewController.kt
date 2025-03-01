package plus.vplan.app

import androidx.compose.ui.window.ComposeUIViewController
import plus.vplan.app.di.initKoin

val mainViewController by lazy {
    ComposeUIViewController(
        configure = {
            initKoin()
        }
    ) { App(task = null) }
}
@Suppress("unused")
fun MainViewController() = mainViewController