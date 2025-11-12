@file:OptIn(ExperimentalMaterial3Api::class)

package plus.vplan.app.feature.settings.page.developer.logs

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlinx.datetime.LocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.bodyFontFamily
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.ui.theme.monospaceFontFamily
import plus.vplan.app.utils.blendColor
import plus.vplan.app.utils.generateColor
import plus.vplan.app.utils.now
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import vplanplus.composeapp.generated.resources.ban
import vplanplus.composeapp.generated.resources.bug
import vplanplus.composeapp.generated.resources.info
import vplanplus.composeapp.generated.resources.message_square
import vplanplus.composeapp.generated.resources.move_up
import vplanplus.composeapp.generated.resources.trash_2
import vplanplus.composeapp.generated.resources.triangle_alert

@Composable
fun DeveloperSettingsLogsScreen(
    onNavigateUp: () -> Unit,
) {
    val viewModel = koinViewModel<DeveloperLogsViewModel>()
    val state by viewModel.state.collectAsState()

    DeveloperSettingsLogsContent(
        state = state,
        onEvent = viewModel::onEvent,
        onBack = onNavigateUp
    )
}

@Composable
private fun DeveloperSettingsLogsContent(
    state: DeveloperLogsState,
    onEvent: (event: DeveloperLogsEvent) -> Unit,
    onBack: () -> Unit
) {
    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()
    val listScrollState = rememberLazyListState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Logs") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_left),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { onEvent(DeveloperLogsEvent.ClearLogs) }) {
                        Icon(
                            painter = painterResource(Res.drawable.trash_2),
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                },
                scrollBehavior = scrollBehaviour
            )
        },
        floatingActionButton = {
            AnimatedVisibility(
                visible = listScrollState.firstVisibleItemIndex > 0,
                enter = fadeIn(),
                exit = fadeOut()
            ) {
                val scope = rememberCoroutineScope()
                ExtendedFloatingActionButton(
                    shape = RoundedCornerShape(8.dp),
                    onClick = { scope.launch { listScrollState.animateScrollToItem(0, 0) } },
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.move_up),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(Modifier.size(8.dp))
                        Text("Zum Anfang")
                    }
                }
            }
        }
    ) { contentPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .horizontalScroll(rememberScrollState())
                .padding(top = 4.dp),
            contentPadding = contentPadding,
            state = listScrollState,
        ) {
            items(state.logs.size) { index ->
                val log = state.logs[index]
                LogEntry(log)
            }
        }
    }
}

@Composable
private fun LogEntry(log: Log) {
    val font = MaterialTheme.typography.bodyMedium.copy(fontFamily = monospaceFontFamily())
    val style = font.toSpanStyle()
    val backgroundColor = blendColor(MaterialTheme.colorScheme.background, log.tag.generateColor(), 0.1f)
    Row(
        modifier = Modifier
            .background(backgroundColor)
            .fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .padding(start = 4.dp)
                .size(16.dp)
        ) {
            Icon(
                painter = painterResource(
                    when (log.level) {
                        Log.LogLevel.VERBOSE -> Res.drawable.message_square
                        Log.LogLevel.DEBUG -> Res.drawable.bug
                        Log.LogLevel.INFO -> Res.drawable.info
                        Log.LogLevel.WARN -> Res.drawable.triangle_alert
                        Log.LogLevel.ERROR -> Res.drawable.ban
                    }
                ),
                modifier = Modifier.fillMaxSize(),
                contentDescription = null
            )
        }
        Text(
            text = buildAnnotatedString {
                withStyle(style) {
                    withStyle(style.copy(color = MaterialTheme.colorScheme.outline)) {
                        append("[${log.timestamp}] ")
                    }
                    withStyle(style.copy(color = when(log.level) {
                        Log.LogLevel.ERROR -> colors.getValue(CustomColor.Red).getGroup().color
                        Log.LogLevel.WARN -> colors.getValue(CustomColor.Yellow).getGroup().color
                        Log.LogLevel.INFO -> colors.getValue(CustomColor.Blue).getGroup().color
                        Log.LogLevel.DEBUG -> colors.getValue(CustomColor.DarkPurple).getGroup().color
                        Log.LogLevel.VERBOSE -> colors.getValue(CustomColor.GreenGray).getGroup().color
                    })) {
                        append(log.level.name)
                        append("/")
                        append(log.tag)
                        append(" - ")
                        append(log.message)
                    }
                }
            },
            fontFamily = bodyFontFamily(),
            softWrap = false,
            modifier = Modifier
                .fillMaxWidth()

        )
    }
}

@Composable
@Preview
private fun LogEntryPreview() {
    LogEntry(
        log = Log(
            timestamp = LocalDateTime.now(),
            level = Log.LogLevel.ERROR,
            tag = "DeveloperLogsViewModel",
            message = "This is a sample log message for preview purposes."
        )
    )
}

@Preview
@Composable
private fun DeveloperSettingsLogsContentPreview() {
    DeveloperSettingsLogsContent(
        state = DeveloperLogsState(
            logs = List(50) {
                Log(
                    timestamp = LocalDateTime.now(),
                    level = Log.LogLevel.entries[it % Log.LogLevel.entries.size],
                    tag = "SampleTag$it",
                    message = "This is a sample log message number $it for preview purposes."
                )
            }
        ),
        onEvent = {},
        onBack = {}
    )
}