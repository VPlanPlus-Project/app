package plus.vplan.app.feature.homework.ui.components.create

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.Group
import plus.vplan.app.domain.model.SubjectInstance
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.ui.thenIf
import plus.vplan.app.utils.safeBottomPadding
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.users


@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LessonSelectDrawer(
    group: Group,
    allowGroup: Boolean,
    subjectInstances: List<SubjectInstance>,
    selectedSubjectInstance: SubjectInstance?,
    onSelectSubjectInstance: (SubjectInstance?) -> Unit,
    onDismiss: () -> Unit
) {
    val modalState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val hideSheet = remember { { scope.launch { modalState.hide() }.invokeOnCompletion { onDismiss() } } }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        contentWindowInsets = { WindowInsets(0.dp) },
        sheetState = modalState
    ) {
        LessonSelectContent(
            group = group,
            allowGroup = allowGroup,
            subjectInstances = subjectInstances,
            selectedSubjectInstance = selectedSubjectInstance,
            onSelectSubjectInstance = { onSelectSubjectInstance(it); hideSheet() }
        )
    }
}

@Composable
private fun LessonSelectContent(
    group: Group,
    allowGroup: Boolean,
    subjectInstances: List<SubjectInstance>,
    selectedSubjectInstance: SubjectInstance?,
    onSelectSubjectInstance: (SubjectInstance?) -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = safeBottomPadding())
    ) {
        if (allowGroup) {
            AnimatedContent(
                targetState = selectedSubjectInstance == null,
                transitionSpec = { fadeIn() togetherWith  fadeOut() }
            ) { isSubjectInstanceNotSelected ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .defaultMinSize(minHeight = 48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .thenIf(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) { isSubjectInstanceNotSelected }
                        .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                        .clickable { onSelectSubjectInstance(null) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(Res.drawable.users),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (isSubjectInstanceNotSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Column {
                        Text(
                            text = "Kein Fach",
                            style = MaterialTheme.typography.titleSmall,
                            color = if (isSubjectInstanceNotSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = "Für Klasse ${group.name}",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isSubjectInstanceNotSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
            Spacer(Modifier.height(16.dp))
        }
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        ) {
            subjectInstances.forEach { subjectInstance ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .thenIf(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) { selectedSubjectInstance == subjectInstance }
                        .border(0.5.dp, MaterialTheme.colorScheme.outline)
                        .clickable { onSelectSubjectInstance(subjectInstance) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    if (selectedSubjectInstance == subjectInstance) SubjectIcon(Modifier.size(24.dp), subjectInstance.subject, containerColor = Color.Transparent, contentColor = MaterialTheme.colorScheme.onPrimaryContainer)
                    else SubjectIcon(Modifier.size(24.dp), subjectInstance.subject)
                    Column {
                        Text(
                            text = buildString {
                                append(subjectInstance.subject)
                                if (subjectInstance.courseId != null) append(" (${subjectInstance.courseItem!!.name})")
                            },
                            style = MaterialTheme.typography.titleSmall,
                            color = if (selectedSubjectInstance == subjectInstance) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        )
                        Text(
                            text = subjectInstance.teacherItem?.name ?: "Kein Lehrer",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (selectedSubjectInstance == subjectInstance) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                        )
                    }
                }
            }
        }
    }
}