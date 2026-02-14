package plus.vplan.app.feature.profile.settings.page.subject_instances.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TriStateCheckbox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.state.ToggleableState
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.core.model.AliasState
import plus.vplan.app.domain.cache.collectAsLoadingState
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import kotlin.uuid.Uuid

@Composable
fun ProfileSubjectInstanceScreen(
    profileId: Uuid,
    navHostController: NavHostController,
) {
    val viewModel = koinViewModel<ProfileSubjectInstanceViewModel>()
    val state = viewModel.state

    LaunchedEffect(profileId) {
        viewModel.init(profileId)
    }

    ProfileSubjectInstanceContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = navHostController::navigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
private fun ProfileSubjectInstanceContent(
    state: ProfileSubjectInstanceState,
    onEvent: (event: ProfileSubjectInstanceEvent) -> Unit,
    onBack: () -> Unit
) {
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Stundenauswahl") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_left),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehaviour
            )
        }
    ) { contentPadding ->
        AnimatedContent(
            targetState = state.profile == null
        ) { displayLoading ->
            if (displayLoading) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@AnimatedContent
            }

            LazyColumn(
                modifier = Modifier
                    .padding(contentPadding)
                    .nestedScroll(scrollBehaviour.nestedScrollConnection)
                    .fillMaxSize()
            ) {
                if (state.courses.isNotEmpty()) stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .padding(8.dp),
                    ) {
                        Text(
                            text = "Nach Kursen",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items(state.courses.toList()) { (course, isSelected) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onEvent(ProfileSubjectInstanceEvent.ToggleCourseSelection(course, isSelected?.not() ?: false)) }
                            .padding(start = 8.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TriStateCheckbox(
                            state = when (isSelected) {
                                true -> ToggleableState.On
                                false -> ToggleableState.Off
                                null -> ToggleableState.Indeterminate
                            },
                            onClick = { onEvent(ProfileSubjectInstanceEvent.ToggleCourseSelection(course, isSelected?.not() ?: false)) }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = course.name,
                                style = MaterialTheme.typography.titleMedium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            val teacher = course.teacher?.collectAsLoadingState(course.teacherId.toString())?.value
                            if (teacher is AliasState.Done) Text(
                                text = teacher.data.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
                if (state.courses.isNotEmpty()) stickyHeader {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.5f))
                            .padding(8.dp),
                    ) {
                        Text(
                            text = "Nach Fächern",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
                items(state.subjectInstance.toList()) { (subjectInstance, isSelected) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onEvent(ProfileSubjectInstanceEvent.ToggleSubjectInstanceSelection(subjectInstance, !isSelected)) }
                            .padding(start = 8.dp, end = 16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Checkbox(
                            checked = isSelected,
                            onCheckedChange = { onEvent(ProfileSubjectInstanceEvent.ToggleSubjectInstanceSelection(subjectInstance, !isSelected)) }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = subjectInstance.subject,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                if (subjectInstance.courseItem != null) Text(
                                    text = subjectInstance.courseItem!!.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                )
                            }
                            if (subjectInstance.teacherItem != null) Text(
                                text = subjectInstance.teacherItem!!.name,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            }
        }
    }
}