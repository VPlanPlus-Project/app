@file:OptIn(ExperimentalTime::class)

package plus.vplan.app.feature.news.ui

import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import kotlinx.datetime.TimeZone
import kotlinx.datetime.format
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.ui.components.FullscreenDrawer
import plus.vplan.app.utils.regularDateFormat
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.calendar
import vplanplus.composeapp.generated.resources.school
import vplanplus.composeapp.generated.resources.smartphone
import vplanplus.composeapp.generated.resources.x
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewsDrawer(
    newsId: Int,
    onDismissRequest: () -> Unit
) {
    val viewModel = koinViewModel<NewsViewModel>()
    val state = viewModel.state
    LaunchedEffect(newsId) { viewModel.init(newsId) }
    val contentScrollState = rememberScrollState()
    FullscreenDrawer(
        onDismissRequest = onDismissRequest,
        preventClosingByGesture = false,
        topAppBar = { onCloseClicked, modifier, _ ->
            TopAppBar(
                title = {
                    Text("Information")
                },
                navigationIcon = {
                    IconButton(
                        onClick = onCloseClicked
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.x),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            )
        },
        contentScrollState = contentScrollState
    ) {
        AnimatedContent(
            targetState = state.news == null
        ) { newsIsLoading ->
            if (newsIsLoading || state.news == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
                return@AnimatedContent
            }
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(contentScrollState)
            ) {
                Text(
                    text = state.news.title,
                    style = MaterialTheme.typography.headlineLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Text(
                    text = state.news.author + ", am " + state.news.date.toLocalDateTime(TimeZone.currentSystemDefault()).date.format(regularDateFormat),
                    style = MaterialTheme.typography.labelLarge,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                Spacer(Modifier.size(8.dp))
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.calendar),
                            contentDescription = null,
                            modifier = Modifier.size(MaterialTheme.typography.labelLarge.lineHeight.toDp()),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = buildString {
                                append("Gilt für ")
                                append(state.news.dateFrom.toLocalDateTime(TimeZone.currentSystemDefault()).date.format(regularDateFormat))
                                append(" bis ")
                                append(state.news.dateTo.toLocalDateTime(TimeZone.currentSystemDefault()).date.format(regularDateFormat))
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    if (state.news.versionFrom != null || state.news.versionTo != null) Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.smartphone),
                            contentDescription = null,
                            modifier = Modifier.size(MaterialTheme.typography.labelLarge.lineHeight.toDp()),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = buildString {
                                append("App-Version ")
                                if (state.news.versionFrom != null && state.news.versionTo == null) append("ab Version ${state.news.versionFrom}")
                                else if (state.news.versionFrom != null && state.news.versionTo != null) append("Version ${state.news.versionFrom} bis einschließlich ${state.news.versionTo}")
                                else if (state.news.versionFrom == null && state.news.versionTo != null) append("bis einschließlich Version ${state.news.versionTo}")
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                    Row(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Icon(
                            painter = painterResource(Res.drawable.school),
                            contentDescription = null,
                            modifier = Modifier.size(MaterialTheme.typography.labelLarge.lineHeight.toDp()),
                            tint = MaterialTheme.colorScheme.outline
                        )
                        Text(
                            text = buildString {
                                if (state.news.schools.isEmpty()) append("Alle Schulen")
                                else append(state.news.schools.map { it.name }.sorted().joinToString())
                            },
                            style = MaterialTheme.typography.labelLarge,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                }
                Text(
                    text = state.news.content,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}