package plus.vplan.app

import androidx.compose.ui.window.ComposeUIViewController
import plus.vplan.app.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App(task = null) }