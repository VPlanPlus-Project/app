package plus.vplan.app.ui.components.native

import platform.UIKit.UIViewController

interface NativeViewFactory {
    fun nativeButton(text: String, onClick: () -> Unit): UIViewController
}