package plus.vplan.app.feature.search.subfeature.room_search.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import androidx.navigation.NavHostController
import kotlinx.datetime.LocalTime
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.feature.search.subfeature.room_search.domain.usecase.Occupancy
import plus.vplan.app.utils.minusWithCapAtMidnight
import plus.vplan.app.utils.plusWithCapAtMidnight
import plus.vplan.app.utils.until
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import kotlin.time.Duration.Companion.hours

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RoomSearch(
    navHostController: NavHostController
) {
    val viewModel = koinViewModel<RoomSearchViewModel>()
    val state = viewModel.state

    val scrollBehaviour = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Freien Raum finden") },
                navigationIcon = {
                    IconButton(onClick = { navHostController.navigateUp() }) {
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
        ) {
            val roomNameColumnWidth = 48.dp
            val lineHeight = 32.dp
            val horizontalScrollState = rememberScrollState()
            val hourWidth = 64.dp

            val uiStart = state.startTime?.time?.minusWithCapAtMidnight(1.hours) ?: LocalTime(0, 0)
            val uiEnd = state.endTime?.time?.plusWithCapAtMidnight(1.hours) ?: LocalTime(23, 59)

            Row {
                Column {
                    state.rooms.keys.forEach {
                        Column(Modifier.width(roomNameColumnWidth)) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(lineHeight),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = it.name,
                                    style = MaterialTheme.typography.labelSmall
                                )
                            }
                            HorizontalDivider()
                        }
                    }
                }
                Column (
                    modifier = Modifier
                        .fillMaxSize()
                        .horizontalScroll(horizontalScrollState)
                ) {
                    state.rooms.values.forEach { occupancies ->
                        Box(
                            modifier = Modifier
                                .width(uiStart.until(uiEnd).inWholeMinutes.toFloat() * hourWidth / 60)
                                .height(lineHeight)
                        ) {
                            occupancies.forEach { occupancy ->
                                val startOffset = uiStart.until(occupancy.start.time).inWholeMinutes.toFloat() * hourWidth / 60
                                val width = occupancy.start.time.until(occupancy.end.time).inWholeMinutes.toFloat() * hourWidth / 60
                                Box(
                                    modifier = Modifier
                                        .offset(x = startOffset, y = 2.dp)
                                        .height(lineHeight - 4.dp)
                                        .width(width)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    CompositionLocalProvider(LocalContentColor provides MaterialTheme.colorScheme.onSurfaceVariant) {
                                        when (occupancy) {
                                            is Occupancy.Lesson -> Text(occupancy.lesson.groupItems!!.joinToString { it.name })
                                        }
                                    }
                                }
                            }
                        }
                        HorizontalDivider(Modifier.width(uiStart.until(uiEnd).inWholeMinutes.toFloat() * hourWidth / 60))
                    }
                }
            }
        }
    }
}