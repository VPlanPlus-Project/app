package plus.vplan.app.feature.search.ui.main.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.unit.dp
import org.jetbrains.compose.resources.painterResource
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.search

@Composable
fun SearchBar(
    value: String,
    onQueryChange: (to: String) -> Unit
) {
    val searchObjects = remember { listOf("Räumen", "Lehrern", "Klassen", "Nutzern", "Hausaufgaben", "Leistungserhebungen").shuffled() }
    val infiniteTransition = rememberInfiniteTransition(label = "infinite placeholder")
    val index by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = searchObjects.lastIndex.toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(1500*searchObjects.size, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        )
    )

    val focusRequester = remember { FocusRequester() }

    TextField(
        value = value,
        onValueChange = onQueryChange,
        placeholder = {
            Row {
                Text("Suche nach ")
                AnimatedContent(
                    targetState = searchObjects[index.toInt()],
                    modifier = Modifier.fillMaxWidth(),
                    transitionSpec = { fadeIn(tween(durationMillis = 200, delayMillis = 200)) togetherWith fadeOut() }
                ) {
                    Text(it)
                }
            }
        },
        leadingIcon = {
            Icon(
                painter = painterResource(Res.drawable.search),
                contentDescription = null,
                modifier = Modifier.size(24.dp)
            )
        },
        modifier = Modifier
            .fillMaxWidth()
            .focusRequester(focusRequester)
    )

    LaunchedEffect(Unit) { focusRequester.requestFocus() }
}