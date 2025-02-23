package plus.vplan.app.feature.grades.page.analytics.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AssistChip
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import org.jetbrains.compose.resources.painterResource
import org.koin.compose.viewmodel.koinViewModel
import plus.vplan.app.domain.model.schulverwalter.Interval
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.arrow_left
import vplanplus.composeapp.generated.resources.chevron_down
import vplanplus.composeapp.generated.resources.filter

@Composable
fun AnalyticsScreen(
    navHostController: NavHostController,
    vppIdId: Int
) {
    val viewModel = koinViewModel<AnalyticsViewModel>()
    val state = viewModel.state

    LaunchedEffect(vppIdId) { viewModel.init(vppIdId) }

    AnalyticsContent(
        state = state,
        onBack = navHostController::navigateUp
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AnalyticsContent(
    state: AnalyticsState,
    onBack: () -> Unit
) {
    val topScrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = "Notenanalyse") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            painter = painterResource(Res.drawable.arrow_left),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                scrollBehavior = topScrollBehavior
            )
        },
    ) { contentPadding ->
        Column(
            modifier = Modifier
                .padding(contentPadding)
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            Column gradesByGrade@{
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Notenverteilung",
                        style = MaterialTheme.typography.titleMedium
                    )
                    AssistChip(
                        onClick = {},
                        label = { Text("Filter") },
                        leadingIcon = { Icon(painter = painterResource(Res.drawable.filter), contentDescription = null, modifier = Modifier.size(18.dp)) },
                        trailingIcon = { Icon(painter = painterResource(Res.drawable.chevron_down), contentDescription = null, modifier = Modifier.size(18.dp)) },
                    )
                }
                if (state.interval == null) return@gradesByGrade
                val map = remember(state.grades) {
                    var max = 0
                    when (state.interval.type) {
                        Interval.Type.SEK1 -> (1..6).toList().associateWith { grade -> state.grades.count { it.numericValue == grade } }
                        Interval.Type.SEK2 -> (0..15).toList().associateWith { grade -> state.grades.count { it.numericValue == grade } }
                    }
                        .also { max = it.maxOf { gradeByGrade -> gradeByGrade.value } }
                        .map {
                            GradesByGrade(
                                grades = it.value,
                                grade = it.key,
                                percentage = (it.value.toFloat() / max).let { percentage -> if (percentage.isNaN()) 0f else percentage }
                            )
                        }
                    }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth()
                        .height(256.dp),
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    map.toList().forEachIndexed { i, category ->
                        val percentageAnimation by animateFloatAsState(
                            targetValue = category.percentage,
                            animationSpec = tween(durationMillis = 200, delayMillis = 75*i)
                        )
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight(percentageAnimation)
                                .clip(RoundedCornerShape(8.dp, 8.dp, 0.dp, 0.dp))
                                .background(MaterialTheme.colorScheme.primaryContainer)
                        )
                    }
                }
                Row(
                    modifier = Modifier
                        .padding(horizontal = 16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.Top,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    map.toList().forEach { category ->
                        Column(
                            modifier = Modifier.weight(1f),
                            verticalArrangement = Arrangement.Center
                        ) {
                            Text(
                                text = category.grade.toString(),
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                text = category.grades.toString() + "x",
                                style = MaterialTheme.typography.labelMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }
        }
    }
}

data class GradesByGrade(
    val grades: Int,
    val grade: Int,
    val percentage: Float
)