package plus.vplan.app.feature.profile.page.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandHorizontally
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.coerceAtLeast
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.times
import org.jetbrains.compose.resources.painterResource
import androidx.compose.ui.tooling.preview.Preview
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.ui.components.SubjectIcon
import plus.vplan.app.utils.toDp
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.lock
import kotlin.math.roundToInt

/**
 * @param subjects List of subjects to be shown as backdrop icons.
 */
@Composable
fun GradesCard(
    modifier: Modifier = Modifier,
    areGradesLocked: Boolean,
    subjects: Set<String>,
    infiniteTransition: InfiniteTransition = rememberInfiniteTransition(),
    averageGrade: GradesCardFeaturedGrade,
    latestGrade: GradesCardFeaturedGrade,
    onRequestUnlock: () -> Unit,
    onOpenGrades: () -> Unit,
) {
    val colorScheme = MaterialTheme.colorScheme
    val localDensity = LocalDensity.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .clickable {
                if (areGradesLocked) onRequestUnlock()
                else onOpenGrades()
            }
    ) {
        var contentWidth by remember { mutableStateOf(0.dp) }
        var contentHeight by remember { mutableStateOf(0.dp) }

        val subjectBackdropIconSize = 24.dp
        val subjectBackdropIconPadding = 4.dp

        AnimatedVisibility(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .height(contentHeight)
                .clipToBounds(),
            visible = subjects.isNotEmpty() && contentWidth > 0.dp && contentHeight > 0.dp,
            enter = fadeIn(tween(2000)),
            exit = fadeOut(tween(2000)),
        ) subjectIconBackdrop@{
            val rows = (contentHeight / (subjectBackdropIconSize + subjectBackdropIconPadding)).roundToInt()
            val height = rows * (subjectBackdropIconSize + subjectBackdropIconPadding)
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(((contentHeight - height) / 2).coerceAtLeast(0.dp))
                    .drawWithCache {
                        onDrawWithContent {
                            drawContent()
                            drawRect(
                                brush = Brush.horizontalGradient(
                                    0f to colorScheme.surfaceVariant,
                                    .3f to colorScheme.surfaceVariant,
                                    1f to colorScheme.surfaceVariant.copy(alpha = .8f),
                                ),
                                size = size,
                                topLeft = Offset(0f, 0f),
                            )
                        }
                    },
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(subjectBackdropIconPadding)
            ) {
                val subjectMap = remember { mutableMapOf<IntOffset, String>() }
                if (subjects.isNotEmpty()) repeat(rows) { i ->
                    Row(horizontalArrangement = Arrangement.spacedBy(subjectBackdropIconPadding)) {
                        repeat(((contentWidth.value / 1.5) / (subjectBackdropIconSize + subjectBackdropIconPadding).value).roundToInt()) { j ->
                            SubjectIcon(
                                subject = subjectMap.getOrPut(IntOffset(i, j)) { subjects.random() },
                                modifier = Modifier.size(subjectBackdropIconSize),
                                contentColor = MaterialTheme.colorScheme.secondary,
                                containerColor = Color.Transparent,
                            )
                        }
                    }
                }
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { with(localDensity) {
                    contentWidth = it.width.toDp()
                    contentHeight = it.height.toDp()
                } }
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clipToBounds(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(
                        text = "Noten",
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    AnimatedVisibility(
                        visible = areGradesLocked,
                        enter = fadeIn() + expandHorizontally(expandFrom = Alignment.CenterHorizontally) + slideInHorizontally { it },
                        exit = shrinkHorizontally(shrinkTowards = Alignment.CenterHorizontally) + fadeOut() + slideOutHorizontally { it },
                    ) {
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                painter = painterResource(Res.drawable.lock),
                                contentDescription = null,
                                modifier = Modifier.size(12.dp)
                            )
                            Text(
                                text = "Noten gesperrt, tippe zum Entsperren",
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            AnimatedVisibility(
                                visible = averageGrade !is GradesCardFeaturedGrade.NotExisting,
                                enter = fadeIn() + slideInHorizontally { -it / 2 },
                                exit = slideOutHorizontally { -it / 2 } + fadeOut(),
                            ) {
                                Text(
                                    text = "∅ ",
                                    style = MaterialTheme.typography.headlineLarge,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            AnimatedContent(
                                targetState = averageGrade,
                            ) { average ->
                                when (average) {
                                    is GradesCardFeaturedGrade.NotExisting -> FeaturedGradeText("-")
                                    else -> {
                                        if (average is GradesCardFeaturedGrade.Loading || areGradesLocked) FeaturedGradeShimmer(infiniteTransition)
                                        else FeaturedGradeText((average as? GradesCardFeaturedGrade.Value)?.displayValue ?: "?")
                                    }
                                }
                            }
                        }
                        Text(
                            text = "Gesamtdurchschnitt",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                    VerticalDivider(Modifier.height(48.dp))
                    Column(
                        modifier = Modifier.weight(1f),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedContent(
                            targetState = latestGrade,
                        ) { latestGrade ->
                            when (latestGrade) {
                                is GradesCardFeaturedGrade.NotExisting -> FeaturedGradeText("-")
                                else -> {
                                    if (latestGrade is GradesCardFeaturedGrade.Loading || areGradesLocked) FeaturedGradeShimmer(infiniteTransition)
                                    else FeaturedGradeText((latestGrade as? GradesCardFeaturedGrade.Value)?.displayValue ?: "?")
                                }
                            }
                        }
                        Text(
                            text = "Neueste Note",
                            style = MaterialTheme.typography.labelMedium,
                            color = MaterialTheme.colorScheme.onSurface,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun FeaturedGradeText(value: String) {
    Text(
        text = value,
        style = MaterialTheme.typography.headlineLarge,
        color = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun FeaturedGradeShimmer(infiniteTransition: InfiniteTransition) {
    ShimmerLoader(
        modifier = Modifier.size(MaterialTheme.typography.headlineLarge.lineHeight.toDp()).clip(RoundedCornerShape(8.dp)),
        infiniteTransition = infiniteTransition
    )
}

@Preview
@Composable
private fun LockedGradesCardPreview() {
    GradesCard(
        areGradesLocked = true,
        subjects = setOf("DE", "MA", "EN", "BI", "CH", "GE", "PO"),
        averageGrade = GradesCardFeaturedGrade.NotExisting,
        latestGrade = GradesCardFeaturedGrade.NotExisting,
        onRequestUnlock = {},
        onOpenGrades = {}
    )
}

@Preview
@Composable
private fun GradesLoadingPreview() {
    GradesCard(
        areGradesLocked = false,
        subjects = setOf("DE", "MA", "EN", "BI", "CH", "GE", "PO"),
        averageGrade = GradesCardFeaturedGrade.Loading,
        latestGrade = GradesCardFeaturedGrade.Loading,
        onRequestUnlock = {},
        onOpenGrades = {}
    )
}

@Preview
@Composable
private fun GradesWithValuesPreview() {
    GradesCard(
        areGradesLocked = false,
        subjects = setOf("DE", "MA", "EN", "BI", "CH", "GE", "PO"),
        averageGrade = GradesCardFeaturedGrade.Value("2,3"),
        latestGrade = GradesCardFeaturedGrade.Value("1-"),
        onRequestUnlock = {},
        onOpenGrades = {}
    )
}

sealed class GradesCardFeaturedGrade {
    object Loading : GradesCardFeaturedGrade()
    object NotExisting : GradesCardFeaturedGrade()
    data class Value(val displayValue: String) : GradesCardFeaturedGrade()
}