package plus.vplan.app.feature.homework.ui.components

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.datetime.LocalDate
import org.jetbrains.compose.resources.painterResource
import plus.vplan.app.ui.components.FullscreenDrawer
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.x

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewHomeworkDrawer(
    selectedDate: LocalDate? = null,
    onDismissRequest: () -> Unit
) {
    val density = LocalDensity.current
    val direction = LocalLayoutDirection.current
    FullscreenDrawer(
        contentScrollState = rememberScrollState(),
        onDismissRequest = onDismissRequest,
        topAppBar = { onCloseClicked, modifier, scrollProgress ->
            TopAppBar(
                modifier = modifier,
                title = { Text("Neue Hausaufgabe") },
                navigationIcon = {
                    IconButton(onClick = { onCloseClicked() }) {
                        Icon(
                            painter = painterResource(Res.drawable.x),
                            contentDescription = null,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                },
                windowInsets = WindowInsets(
                    left = TopAppBarDefaults.windowInsets.getLeft(density, direction).let { with(density) { it.toDp() } },
                    right = TopAppBarDefaults.windowInsets.getRight(density, direction).let { with(density) { it.toDp() } },
                    top = TopAppBarDefaults.windowInsets.getTop(density).let { with(density) { it.toDp() } } * scrollProgress,
                    bottom = TopAppBarDefaults.windowInsets.getBottom(density).let { with(density) { it.toDp() } }
                )
            )
        },
        content = {
            NewHomeworkDrawerContent(
                selectedDate = selectedDate
            )
        }
    )
}