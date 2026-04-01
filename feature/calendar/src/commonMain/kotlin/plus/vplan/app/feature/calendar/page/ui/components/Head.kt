package plus.vplan.app.feature.calendar.page.ui.components

import androidx.compose.runtime.Composable

@Composable
expect fun Head(
    title: String,
    subtitle: String,
    showTodayButton: Boolean,
    onTodayClicked: () -> Unit,
    onCreateHomeworkClicked: () -> Unit,
    onCreateAssessmentClicked: () -> Unit,
)