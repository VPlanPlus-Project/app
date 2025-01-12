package plus.vplan.app.feature.profile.page.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Checkbox
import androidx.compose.material3.FilledTonalIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.App
import plus.vplan.app.domain.cache.CacheState
import plus.vplan.app.domain.cache.collectAsLoadingState
import plus.vplan.app.domain.model.Profile
import plus.vplan.app.feature.profile.page.ui.components.ProfileTitle
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.settings

@Composable
fun ProfileScreen(
    contentPadding: PaddingValues,
    viewModel: ProfileViewModel
) {
    val state = viewModel.state

    ProfileContent(
        state = state,
        contentPadding = contentPadding,
        onEvent = viewModel::onEvent
    )
}

@Composable
private fun ProfileContent(
    state: ProfileState,
    contentPadding: PaddingValues,
    onEvent: (event: ProfileScreenEvent) -> Unit
) {

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(contentPadding)
    ) {
        Row(
            modifier = Modifier
                .padding(top = 8.dp)
                .padding(horizontal = 16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            ProfileTitle(state.currentProfile?.name.orEmpty()) {
                onEvent(
                    ProfileScreenEvent.SetProfileSwitcherVisibility(
                        true
                    )
                )
            }
            FilledTonalIconButton(
                onClick = {}
            ) {
                Icon(
                    painter = painterResource(Res.drawable.settings),
                    modifier = Modifier.size(24.dp),
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSecondaryContainer
                )
            }
        }
        Spacer(Modifier.height(8.dp))
        if (state.currentProfile is Profile.StudentProfile) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.currentProfile.defaultLessons.mapKeys { it.key }.forEach { (defaultLessonId, isEnabled) ->
                    val defaultLesson by App.defaultLessonSource.getById(defaultLessonId).collectAsLoadingState(defaultLessonId)
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable {
                                (defaultLesson as? CacheState.Done)?.let {
                                    onEvent(
                                        ProfileScreenEvent.ToggleDefaultLessonEnabled(
                                            state.currentProfile,
                                            it.data,
                                            !isEnabled
                                        )
                                    )
                                }
                            },
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Checkbox(
                            checked = isEnabled,
                            onCheckedChange = {
                                (defaultLesson as? CacheState.Done)?.let { value ->
                                    onEvent(
                                        ProfileScreenEvent.ToggleDefaultLessonEnabled(
                                            state.currentProfile,
                                            value.data,
                                            it
                                        )
                                    )
                                }
                            }
                        )
                        Spacer(Modifier.width(8.dp))
                        (defaultLesson as? CacheState.Done)?.data?.let { defaultLessonValue ->
                            Column {
                                Text(
                                    text = buildString {
                                        append(defaultLessonValue.subject)
//                                        if (defaultLesson.course != null) append(" (${defaultLesson.course.toValueOrNull()!!.name})")
                                    },
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
//                                Text(
//                                    text = defaultLessonValue.teacher?.toValueOrNull()?.name ?: "Keine Lehrkraft",
//                                    style = MaterialTheme.typography.bodyMedium,
//                                    color = MaterialTheme.colorScheme.onSurface
//                                )
                            }
                        }
                    }
                }
            }
        }
    }
}