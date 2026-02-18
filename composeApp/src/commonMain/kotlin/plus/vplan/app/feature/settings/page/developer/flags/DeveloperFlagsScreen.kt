package plus.vplan.app.feature.settings.page.developer.flags

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.repository.Keys
import plus.vplan.app.feature.settings.ui.components.SettingsRecordCheckbox
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left

@Composable
fun DeveloperFlagsScreen(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<DeveloperFlagsViewModel>()
    val state by viewModel.state.collectAsState()
    DeveloperFlagsContent(
        onBack = navHostController::navigateUp,
        onEvent = viewModel::onEvent,
        state = state
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DeveloperFlagsContent(
    onBack: () -> Unit = {},
    onEvent: (DeveloperFlagsEvent) -> Unit = {},
    state: DeveloperFlagsState,
) {
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flags") },
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nestedScroll(scrollBehaviour.nestedScrollConnection)
                .padding(contentPadding)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Keys.developerSettings.forEach { developerFlag ->
                when (developerFlag) {
                    is Keys.DeveloperFlag.Boolean -> {
                        SettingsRecordCheckbox(
                            title = developerFlag.key,
                            checked = state.booleans[developerFlag.key] ?: developerFlag.default,
                            onCheckedChange = remember(developerFlag.key) { { onEvent(DeveloperFlagsEvent.Toggle(developerFlag.key)) } }
                        )
                    }
                }
            }
        }
    }
}

@Composable
@Preview
private fun DeveloperFlagsContentPreview() {
    DeveloperFlagsContent(
        state = DeveloperFlagsState(
            mapOf(Keys.DS_FORCE_REDUCED_CALENDAR_VIEW to true)
        )
    )
}