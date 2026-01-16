package plus.vplan.app.ui.components.native

import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.UIKitViewController
import plus.vplan.app.LocalNativeViewFactory

@Composable
actual fun NativeButton(
    modifier: Modifier,
    text: String,
    onClick: () -> Unit
) {
    val factory = LocalNativeViewFactory.current
    UIKitViewController(
        modifier = modifier
            .wrapContentSize(),
        factory = {
            factory.nativeButton(
                text,
                onClick
            )
        }
    )
}