@file:OptIn(ExperimentalForeignApi::class)

package plus.vplan.app.core.ui.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.uikit.LocalUIViewController
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.ComposeUIViewController
import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.ObjCAction
import kotlinx.cinterop.readValue
import kotlinx.cinterop.useContents
import platform.CoreGraphics.CGAffineTransformIdentity
import platform.CoreGraphics.CGAffineTransformMakeTranslation
import platform.Foundation.NSSelectorFromString
import platform.UIKit.NSLayoutConstraint
import platform.UIKit.NSTextAlignmentCenter
import platform.UIKit.UIAction
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIBarButtonSystemItem
import platform.UIKit.UIColor
import platform.UIKit.UIFont
import platform.UIKit.UIGestureRecognizer
import platform.UIKit.UIGestureRecognizerDelegateProtocol
import platform.UIKit.UIGestureRecognizerStateBegan
import platform.UIKit.UIGestureRecognizerStateCancelled
import platform.UIKit.UIGestureRecognizerStateChanged
import platform.UIKit.UIGestureRecognizerStateEnded
import platform.UIKit.UILabel
import platform.UIKit.UILayoutConstraintAxisVertical
import platform.UIKit.UINavigationController
import platform.UIKit.UIPanGestureRecognizer
import platform.UIKit.UISheetPresentationControllerDetent
import platform.UIKit.UIStackView
import platform.UIKit.UIStackViewAlignmentCenter
import platform.UIKit.UIView
import platform.UIKit.UIViewAnimationOptionCurveEaseOut
import platform.UIKit.UIViewController
import platform.UIKit.addChildViewController
import platform.UIKit.didMoveToParentViewController
import platform.UIKit.isModalInPresentation
import platform.UIKit.navigationItem
import platform.UIKit.presentationController
import platform.UIKit.secondaryLabelColor
import platform.UIKit.sheetPresentationController
import platform.darwin.NSObject

@OptIn(ExperimentalForeignApi::class, BetaInteropApi::class)
@Composable
actual fun ModalBottomSheet(
    onDismissRequest: () -> Unit,
    configuration: SheetConfiguration,
    content: @Composable (contentPadding: PaddingValues) -> Unit
) {
    val rootController = LocalUIViewController.current
    val gestureDelegate = remember { SheetGestureDelegate() }
    val grabberHandler = remember { GrabberDismissHandler(onDismiss = onDismissRequest) }

    val contentController = remember {
        ComposeSheetViewController(
            contentProvider = content,
        ).also { vc ->
            when {
                configuration.title != null && configuration.subtitle != null ->
                    vc.navigationItem.titleView = buildTitleView(
                        configuration.title,
                        configuration.subtitle
                    )
                configuration.title != null ->
                    vc.navigationItem.title = configuration.title
                configuration.subtitle != null ->
                    vc.navigationItem.titleView = buildTitleView(
                        title = null,
                        subtitle = configuration.subtitle
                    )
            }

            if (configuration.showCloseButton) {
                vc.navigationItem.leftBarButtonItem = UIBarButtonItem(
                    barButtonSystemItem = UIBarButtonSystemItem.UIBarButtonSystemItemClose,
                    target = null,
                    action = null
                ).apply {
                    primaryAction = UIAction.actionWithHandler { _ ->
                        configuration.closeButtonAction()
                    }
                }
            }
        }
    }

    val navController = remember {
        UINavigationController(rootViewController = contentController).apply {
            isModalInPresentation()
            sheetPresentationController?.apply {
                detents = listOf(
                    UISheetPresentationControllerDetent.mediumDetent(),
                    UISheetPresentationControllerDetent.largeDetent()
                )
                prefersGrabberVisible = true
            }
            interactivePopGestureRecognizer?.delegate = gestureDelegate
        }
    }

    LaunchedEffect(Unit) {
        rootController.presentViewController(
            viewControllerToPresent = navController,
            animated = true,
            completion = {
                navController.presentationController?.presentedView?.let { sheetView ->
                    grabberHandler.attach(sheetView)
                }
            }
        )
    }

    DisposableEffect(Unit) {
        onDispose {
            navController.dismissViewControllerAnimated(true) {
                onDismissRequest()
            }
        }
    }
}

private fun buildTitleView(title: String?, subtitle: String?): UIStackView =
    UIStackView().apply {
        axis = UILayoutConstraintAxisVertical
        alignment = UIStackViewAlignmentCenter
        spacing = 2.0

        title?.let {
            addArrangedSubview(UILabel().apply {
                text = it
                font = UIFont.boldSystemFontOfSize(16.0)
                textAlignment = NSTextAlignmentCenter
            })
        }
        subtitle?.let {
            addArrangedSubview(UILabel().apply {
                text = it
                font = UIFont.systemFontOfSize(12.0)
                textColor = UIColor.secondaryLabelColor
                textAlignment = NSTextAlignmentCenter
            })
        }
    }

private class SheetGestureDelegate : NSObject(), UIGestureRecognizerDelegateProtocol {

    override fun gestureRecognizer(
        gestureRecognizer: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWithGestureRecognizer: UIGestureRecognizer
    ): Boolean = true

    override fun gestureRecognizerShouldBegin(
        gestureRecognizer: UIGestureRecognizer
    ): Boolean {
        val view = gestureRecognizer.view ?: return true
        val locationY = gestureRecognizer.locationInView(view).useContents { y }
        return locationY < 44.0 + 56.0
    }
}

internal class ComposeSheetViewController(
    private val contentProvider: @Composable (PaddingValues) -> Unit,
) : UIViewController(nibName = null, bundle = null) {

    private var currentPadding = PaddingValues(0.dp)

    override fun viewDidLoad() {
        super.viewDidLoad()

        val composeVC = ComposeUIViewController {
            contentProvider(currentPadding)
        }

        addChildViewController(composeVC)
        view.addSubview(composeVC.view)

        composeVC.view.setTranslatesAutoresizingMaskIntoConstraints(false)
        NSLayoutConstraint.activateConstraints(
            listOf(
                composeVC.view.topAnchor.constraintEqualToAnchor(view.topAnchor),
                composeVC.view.bottomAnchor.constraintEqualToAnchor(view.bottomAnchor),
                composeVC.view.leadingAnchor.constraintEqualToAnchor(view.leadingAnchor),
                composeVC.view.trailingAnchor.constraintEqualToAnchor(view.trailingAnchor),
            )
        )
        composeVC.didMoveToParentViewController(this)
    }

    override fun viewDidAppear(animated: Boolean) {
        super.viewDidAppear(animated)

        val navBarHeight = (parentViewController as? UINavigationController)
            ?.navigationBar
            ?.frame
            ?.useContents { size.height }
            ?: 0.0

        val safeAreaBottom = view.safeAreaInsets.useContents { bottom }

        currentPadding = PaddingValues(
            top = navBarHeight.dp,
            bottom = safeAreaBottom.dp,
        )
    }
}

@OptIn(BetaInteropApi::class)
private class GrabberDismissHandler(
    private val onDismiss: () -> Unit
) : NSObject(), UIGestureRecognizerDelegateProtocol {

    private var startY: Double = 0.0
    private val dismissThreshold: Double = 80.0

    fun attach(view: UIView) {
        val pan = UIPanGestureRecognizer(
            target = this,
            action = NSSelectorFromString("handlePan:")
        )
        pan.delegate = this
        view.addGestureRecognizer(pan)
    }

    @Suppress("unused")
    @ObjCAction
    fun handlePan(recognizer: UIPanGestureRecognizer) {
        val view = recognizer.view ?: return

        val translationY = recognizer.translationInView(view).useContents { y }

        when (recognizer.state) {
            UIGestureRecognizerStateBegan -> {
                startY = recognizer.locationInView(view).useContents { y }
            }
            UIGestureRecognizerStateChanged -> {
                if (startY < 44.0 && translationY > 0) {
                    view.transform = CGAffineTransformMakeTranslation(0.0, translationY)
                }
            }
            UIGestureRecognizerStateEnded -> {
                if (startY < 44.0 && translationY > dismissThreshold) {
                    onDismiss()
                } else {
                    UIView.animateWithDuration(
                        duration = 0.3,
                        delay = 0.0,
                        usingSpringWithDamping = 0.8,
                        initialSpringVelocity = 0.5,
                        options = UIViewAnimationOptionCurveEaseOut,
                        animations = {
                            view.transform = CGAffineTransformIdentity.readValue()
                        },
                        completion = null
                    )
                }
            }
            UIGestureRecognizerStateCancelled -> {
                view.transform = CGAffineTransformIdentity.readValue()
            }
            else -> {}
        }
    }

    override fun gestureRecognizer(
        gestureRecognizer: UIGestureRecognizer,
        shouldRecognizeSimultaneouslyWithGestureRecognizer: UIGestureRecognizer
    ): Boolean = true
}