package plus.vplan.app.feature.onboarding.stage.profile_selection.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import plus.vplan.app.core.ui.components.WavySeparator

@Composable
@Preview
fun SomeOptionsHidden(
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier.fillMaxWidth(),
        contentAlignment = Alignment.Center,
    ) {
        WavySeparator(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp),
            frequency = 80f,
            strokeWidth = 2.dp,
        )

        Text(
            text = "Einige Optionen sind ausgeblendet",
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = .95f))
                .padding(horizontal = 4.dp),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.outline,
        )
    }
}