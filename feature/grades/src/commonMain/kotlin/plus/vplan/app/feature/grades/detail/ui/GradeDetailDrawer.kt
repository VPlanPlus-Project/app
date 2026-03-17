package plus.vplan.app.feature.grades.detail.ui

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable

@OptIn(ExperimentalMaterial3Api::class)
@Composable
expect fun GradeDetailDrawer(
    gradeId: Int,
    onDismiss: () -> Unit
)