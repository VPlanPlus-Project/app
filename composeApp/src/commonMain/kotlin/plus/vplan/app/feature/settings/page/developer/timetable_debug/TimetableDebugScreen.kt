package plus.vplan.app.feature.settings.page.developer.timetable_debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.Timetable
import plus.vplan.app.domain.model.Week
import plus.vplan.app.domain.repository.Stundenplan24Repository
import plus.vplan.app.utils.atStartOfWeek
import plus.vplan.app.utils.now
import plus.vplan.app.utils.plus
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import kotlin.time.Duration.Companion.days
import kotlin.uuid.Uuid

@Composable
fun TimetableDebugScreen(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<TimetableDebugViewModel>()
    val state by viewModel.state.collectAsState()

    TimetableDebugContent(
        onBack = navHostController::navigateUp,
        state = state
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TimetableDebugContent(
    onBack: () -> Unit,
    state: TimetableDebugState
) {
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("TT Debug") },
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
                .padding(contentPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .nestedScroll(scrollBehaviour.nestedScrollConnection)
                .padding(top = 4.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(horizontal = 8.dp)
                    .fillMaxWidth()
            ) {
                state.weeks.forEach { week ->
                    Text(
                        text = "Woche KW ${week.week.calendarWeek} (SW ${week.week.weekIndex})"
                    )
                    week.timetableMetadata?.let {
                        Text(
                            text = buildString {
                                append(it.dataState.toString())
                                append(" ")
                                append(it.id.toHexDashString())
                            },
                            maxLines = 1,
                            overflow = TextOverflow.Clip,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }

                    HorizontalDivider(Modifier.padding(4.dp))
                }
            }
        }
    }
}

@Composable
@Preview
private fun TimetableDebugContentPreview() {
    TimetableDebugContent(
        onBack = {},
        state = TimetableDebugState(
            weeks = listOf(
                TimetableDebugState.Week(
                    week = Week(
                        id = "1",
                        calendarWeek = 42,
                        weekIndex = 4,
                        start = LocalDate.now().atStartOfWeek(),
                        end = LocalDate.now().atStartOfWeek() + 7.days,
                        school = Uuid.random(),
                        weekType = null,
                    ),
                    timetableMetadata = null
                ),
                TimetableDebugState.Week(
                    week = Week(
                        id = "2",
                        calendarWeek = 43,
                        weekIndex = 5,
                        start = LocalDate.now().atStartOfWeek() + 7.days,
                        end = LocalDate.now().atStartOfWeek() + 14.days,
                        school = Uuid.random(),
                        weekType = null,
                    ),
                    timetableMetadata = Timetable(
                        id = Uuid.random(),
                        weekId = "2",
                        schoolId = Uuid.random(),
                        dataState = Stundenplan24Repository.HasData.No
                    )
                )
            )
        )
    )
}