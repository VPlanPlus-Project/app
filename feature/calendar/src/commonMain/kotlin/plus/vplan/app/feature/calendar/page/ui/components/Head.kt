package plus.vplan.app.feature.calendar.page.ui.components

import androidx.compose.runtime.Composable
import plus.vplan.app.feature.calendar.page.domain.model.DisplayType

@Composable
expect fun Head(
    title: String,
    subtitle: String,
    currentDisplayType: DisplayType,
    showTodayButton: Boolean,
    onTodayClicked: () -> Unit,
    onCreateHomeworkClicked: () -> Unit,
    onCreateAssessmentClicked: () -> Unit,
    onShowAgenda: () -> Unit,
    onShowCalendar: () -> Unit
)