package plus.vplan.app.feature.homework.ui.components

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
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBars
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
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.domain.model.DefaultLesson
import plus.vplan.app.domain.model.Group
import plus.vplan.app.ui.subjectIcon
import plus.vplan.app.ui.thenIf
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.users


@OptIn(ExperimentalMaterial3Api::class)
@Composable fun LessonSelectDrawer(
    group: Group,
    defaultLessons: List<DefaultLesson>,
    selectedDefaultLesson: DefaultLesson?,
    onSelectDefaultLesson: (DefaultLesson?) -> Unit,
    onDismiss: () -> Unit
) {
    val modalState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()
    val hideSheet = remember { { scope.launch { modalState.hide() }.invokeOnCompletion { onDismiss() } } }
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = modalState
    ) {
        LessonSelectContent(
            group = group,
            defaultLessons = defaultLessons,
            selectedDefaultLesson = selectedDefaultLesson,
            onSelectDefaultLesson = { onSelectDefaultLesson(it); hideSheet() }
        )
    }
}

@Composable
private fun LessonSelectContent(
    group: Group,
    defaultLessons: List<DefaultLesson>,
    selectedDefaultLesson: DefaultLesson?,
    onSelectDefaultLesson: (DefaultLesson?) -> Unit
) {
    Column(
        modifier = Modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp)
            .padding(bottom = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding().coerceAtLeast(16.dp))
    ) {
        AnimatedContent(
            targetState = selectedDefaultLesson == null,
            transitionSpec = { fadeIn() togetherWith  fadeOut() }
        ) { isDefaultLessonNotSelected ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .defaultMinSize(minHeight = 48.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .thenIf(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) { isDefaultLessonNotSelected }
                    .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                    .clickable { onSelectDefaultLesson(null) }
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    painter = painterResource(Res.drawable.users),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = if (isDefaultLessonNotSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                )
                Column {
                    Text(
                        text = "Kein Fach",
                        style = MaterialTheme.typography.titleSmall,
                        color = if (isDefaultLessonNotSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    )
                    Text(
                        text = "Für Klasse ${group.name}",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isDefaultLessonNotSelected) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
                    )
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .defaultMinSize(minHeight = 48.dp)
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
        ) {
            defaultLessons.forEach { defaultLesson ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .thenIf(Modifier.background(MaterialTheme.colorScheme.primaryContainer)) { selectedDefaultLesson == defaultLesson }
                        .border(0.5.dp, MaterialTheme.colorScheme.outline)
                        .clickable { onSelectDefaultLesson(defaultLesson) }
                        .padding(8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        painter = painterResource(defaultLesson.subject.subjectIcon()),
                        contentDescription = null,
                        modifier = Modifier.size(24.dp),
                        tint = if (selectedDefaultLesson == defaultLesson) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface
                    )
                    Column {
//                        Text(
//                            text = buildString {
//                                append(defaultLesson.subject)
//                                if (defaultLesson.course != null) append(" (${defaultLesson.course.toValueOrNull()!!.name})")
//                            },
//                            style = MaterialTheme.typography.titleSmall,
//                            color = if (selectedDefaultLesson == defaultLesson) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
//                        )
//                        Text(
//                            text = defaultLesson.teacher?.toValueOrNull()?.name ?: "Kein Lehrer",
//                            style = MaterialTheme.typography.bodySmall,
//                            color = if (selectedDefaultLesson == defaultLesson) MaterialTheme.colorScheme.onPrimaryContainer else MaterialTheme.colorScheme.onSurface,
//                        )
                    }
                }
            }
        }
    }
}