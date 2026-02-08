package plus.vplan.app.feature.grades.page.view.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.InfiniteTransition
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plus.vplan.app.ui.components.ShimmerLoader
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors
import plus.vplan.app.utils.roundTo
import plus.vplan.app.utils.toDp

@Composable
fun AverageCard(
    modifier: Modifier = Modifier,
    avg: Double?,
    infiniteTransition: InfiniteTransition = rememberInfiniteTransition()
) {
    val colorGroup = colors[CustomColor.Green]!!.getGroup()

    CompositionLocalProvider(LocalContentColor provides colorGroup.onContainer) {
        Column(
            modifier = modifier
                .defaultMinSize(minWidth = 128.dp, minHeight = 100.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(colorGroup.container),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
            ) {
                Text(
                    text = "∅",
                    style = MaterialTheme.typography.displaySmall
                )

                AnimatedContent(
                    targetState = avg
                ) {
                    when (it) {
                        null -> ShimmerLoader(
                            modifier = Modifier.size(MaterialTheme.typography.headlineLarge.lineHeight.toDp()).clip(RoundedCornerShape(8.dp)),
                            infiniteTransition = infiniteTransition
                        )
                        else -> Text(
                            text =
                                if (it.isNaN()) "-"
                                else it.roundTo(2).toString(),
                            style = MaterialTheme.typography.displaySmall
                        )
                    }
                }
            }
            Text(
                text = "Durchschnitt",
                style = MaterialTheme.typography.bodySmall
            )
        }
    }

}

@Preview
@Composable
private fun LoadingPreview() {
    AverageCard(
        modifier = Modifier.size(128.dp),
        avg = null
    )
}

@Preview
@Composable
private fun NotExistingPreview() {
    AverageCard(
        modifier = Modifier.size(128.dp),
        avg = Double.NaN
    )
}

@Preview
@Composable
private fun ValuePreview() {
    AverageCard(
        modifier = Modifier.size(128.dp),
        avg = 2/3.0
    )
}
