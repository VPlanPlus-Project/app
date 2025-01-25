package plus.vplan.app.feature.assessment.ui.components.create

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.homework.ui.components.create.FileButtons
import plus.vplan.app.feature.homework.ui.components.create.SubjectAndDateTile
import plus.vplan.app.feature.homework.ui.components.create.VisibilityTile
import plus.vplan.app.feature.homework.ui.components.create.VppIdBanner
import plus.vplan.app.ui.components.FullscreenDrawerContext

@Composable
fun FullscreenDrawerContext.NewAssessmentDrawerContent() {
    val viewModel = koinViewModel<NewAssessmentViewModel>()
    val state = viewModel.state

    Column(
        modifier = Modifier
            .padding(bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp))
            .fillMaxSize()
    ) {
        if (state.currentProfile == null) return
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(scrollState)
        ) {
            Text(
                text = "Thema und Beschreibung",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )
            TextField(
                value = state.description,
                onValueChange = {},
                colors = TextFieldDefaults.colors(
                    focusedContainerColor = Color.Transparent,
                    unfocusedContainerColor = Color.Transparent,
                    disabledContainerColor = Color.Transparent,
                ),
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth(),
                minLines = 4,
                placeholder = { Text(text = "z.B. Bruchrechnung\n\n- Grundrechenarten mit Brüchen\n- Brüche in Dezimalzahlen umwandeln") },
            )
            Spacer(Modifier.height(16.dp))
            Text(
                text = "Fach & Datum",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            SubjectAndDateTile(
                selectedDefaultLesson = state.selectedDefaultLesson,
                selectedDate = state.selectedDate,
                group = state.currentProfile.groupItem!!,
                isAssessment = true,
                onClickDefaultLesson = {},
                onClickDate = {}
            )

            if (state.isVisible != null) VisibilityTile(
                isPublic = state.isVisible,
                selectedDefaultLesson = state.selectedDefaultLesson,
                group = state.currentProfile.groupItem!!,
                onSetVisibility = {}
            ) else VppIdBanner(
                canShow = state.canShowVppIdBanner,
                onHide = {}
            )

            Spacer(Modifier.height(16.dp))

            FileButtons(
                onClickAddFile = {},
                onClickAddPicture = {}
            )

            Spacer(Modifier.height(16.dp))
        }
    }
}