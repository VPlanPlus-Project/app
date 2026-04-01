package plus.vplan.app.feature.calendar.page.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.uikit.LocalUIViewController
import kotlinx.datetime.LocalDate
import platform.UIKit.UIAction
import platform.UIKit.UIBarButtonItem
import platform.UIKit.UIImage
import platform.UIKit.UIMenu
import platform.UIKit.UIMenuOptionsDisplayInline
import platform.UIKit.navigationController
import platform.UIKit.navigationItem
import plus.vplan.app.core.utils.date.now
import plus.vplan.app.feature.calendar.page.domain.model.DisplayType

@Composable
actual fun Head(
    title: String,
    subtitle: String,
    currentDisplayType: DisplayType,
    showTodayButton: Boolean,
    onTodayClicked: () -> Unit,
    onCreateHomeworkClicked: () -> Unit,
    onCreateAssessmentClicked: () -> Unit,
    onShowAgenda: () -> Unit,
    onShowCalendar: () -> Unit
) {
    val viewController = LocalUIViewController.current

    val onCreateHomework by rememberUpdatedState(onCreateHomeworkClicked)
    val onCreateAssessment by rememberUpdatedState(onCreateAssessmentClicked)
    val onAgenda by rememberUpdatedState(onShowAgenda)
    val onCalendar by rememberUpdatedState(onShowCalendar)
    val onToday by rememberUpdatedState(onTodayClicked)

    DisposableEffect(Unit) {
        val navBar = viewController.navigationController?.navigationBar ?: return@DisposableEffect onDispose {  }

        navBar.hidden = false

        onDispose {
            navBar.hidden = true
        }
    }


    DisposableEffect(viewController, title, subtitle) {
        viewController.navigationItem.subtitle = subtitle
        viewController.navigationItem.title = title

        onDispose {
            viewController.navigationItem.title = null
            viewController.navigationItem.subtitle = null
        }
    }

    DisposableEffect(viewController, currentDisplayType, showTodayButton) {
        val addMenu = UIMenu.menuWithTitle(
            title = "",
            image = null,
            identifier = null,
            options = UIMenuOptionsDisplayInline,
            children = listOf(
                UIAction.actionWithTitle(
                    title = "Leistung hinzufügen",
                    image = UIImage.systemImageNamed("pencil"),
                    identifier = null,
                    handler = { _ -> onCreateAssessment() }
                ),
                UIAction.actionWithTitle(
                    title = "Hausaufgabe hinzufügen",
                    image = UIImage.systemImageNamed("book"),
                    identifier = null,
                    handler = { _ -> onCreateHomework() }
                )
            )
        )

        val addButton = UIBarButtonItem(
            image = UIImage.systemImageNamed("plus"),
            menu = addMenu
        )

        val calendarMenu = UIMenu.menuWithTitle(
            title = "",
            image = null,
            identifier = null,
            options = UIMenuOptionsDisplayInline,
            children = listOf(
                UIAction.actionWithTitle(
                    title = "Agenda",
                    image = UIImage.systemImageNamed("list.bullet"),
                    identifier = null,
                    handler = { _ -> onAgenda() }
                ),
                UIAction.actionWithTitle(
                    title = "Kalender",
                    image = UIImage.systemImageNamed("calendar"),
                    identifier = null,
                    handler = { _ -> onCalendar() }
                )
            )
        )

        val calendarButton = UIBarButtonItem(
            image = when (currentDisplayType) {
                DisplayType.Calendar -> UIImage.systemImageNamed("calendar")
                DisplayType.Agenda -> UIImage.systemImageNamed("list.bullet")
            },
            menu = calendarMenu
        )

        val items = mutableListOf(addButton, calendarButton)

        if (showTodayButton) {
            items.add(
                index = 0,
                element = UIBarButtonItem(
                    primaryAction = UIAction.actionWithTitle(
                        title = "",
                        image = UIImage.systemImageNamed("${LocalDate.now().day}.calendar"),
                        identifier = null,
                        handler = { _ -> onToday() }
                    )
                )
            )
        }

        viewController.navigationItem.setRightBarButtonItems(
            items = items,
            animated = true
        )

        onDispose {
            viewController.navigationItem.setRightBarButtonItems(
                items = null,
                animated = true
            )
        }
    }
}