package plus.vplan.app.feature.dev

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.cache.Cacheable
import plus.vplan.app.domain.model.Homework
import plus.vplan.app.feature.homework.ui.components.NewHomeworkDrawerContent
import plus.vplan.app.ui.components.FullscreenDrawer
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.x

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DevScreen(
    contentPadding: PaddingValues,
    onToggleBottomBar: (visible: Boolean) -> Unit
) {
    val viewModel = koinViewModel<DevViewModel>()
    val state = viewModel.state

    var isDrawerOpen by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(contentPadding)
    ) {
        Text(state.profile?.displayName.toString())
        Text(state.updateResponse.toString())
        Row {
            Button(
                onClick = { viewModel.onEvent(DevEvent.Refresh) }
            ) {
                Text("Refresh")
            }
            Button(
                onClick = { isDrawerOpen = true; onToggleBottomBar(false) }
            ) {
                Text("New Homework")
            }
            Button(
                onClick = { viewModel.onEvent(DevEvent.Clear) }
            ) {
                Text("Clear Cache")
            }
        }
        state.homework.forEach { homework ->
            Column(
                modifier = Modifier
                    .padding(vertical = 4.dp)
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("ID: ${homework.getItemId()}")
                    if (homework is Cacheable.Loaded) Text(homework.value.defaultLesson?.toValueOrNull()?.subject ?: homework.value.group?.toValueOrNull()?.name ?: "wtf")
                    if (homework is Cacheable.Loaded) Text(homework.value.dueTo.toString())
                    if (homework is Cacheable.Loaded && homework.value is Homework.CloudHomework) when (homework.value.createdBy) {
                        is Cacheable.Uninitialized -> Text("User ${homework.value.createdBy.id}")
                        is Cacheable.Error -> Text("Error: ${homework.value.createdBy.error}")
                        is Cacheable.Loaded -> Text("von ${homework.value.createdBy.value.name}")
                        is Cacheable.NotExisting -> Text("existiert nicht")
                    }
                }
                if (homework is Cacheable.Loaded) Text(homework.value.tasks.filterIsInstance<Cacheable.Loaded<Homework.HomeworkTask>>().joinToString("\n") { it.value.content })
            }
        }
    }
    val drawerScrollState = rememberScrollState()
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    if (isDrawerOpen) FullscreenDrawer(
        contentScrollState = drawerScrollState,
        onDismissRequest = { isDrawerOpen = false; onToggleBottomBar(true) },
        topAppBar = { onCloseClicked, modifier, scrollProgress ->
            TopAppBar(
                modifier = modifier,
                title = { Text("Neue Hausaufgabe") },
                navigationIcon = {
                    IconButton(onClick = { onCloseClicked() }) {
                        Icon(
                            painter = painterResource(Res.drawable.x),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                windowInsets = WindowInsets(
                    left = TopAppBarDefaults.windowInsets.getLeft(density, direction).let { with(density) { it.toDp() } },
                    right = TopAppBarDefaults.windowInsets.getRight(density, direction).let { with(density) { it.toDp() } },
                    top = TopAppBarDefaults.windowInsets.getTop(density).let { with(density) { it.toDp() } } * scrollProgress,
                    bottom = TopAppBarDefaults.windowInsets.getBottom(density).let { with(density) { it.toDp() } }
                )
            )
        },
        content = {
            NewHomeworkDrawerContent()
        }
    )
}