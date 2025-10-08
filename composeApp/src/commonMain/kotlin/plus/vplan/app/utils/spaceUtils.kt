package plus.vplan.app.utils

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.systemBars
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

@Composable
fun safeBottomPadding() = WindowInsets.systemBars.asPaddingValues().calculateBottomPadding().let { bottomPadding ->
    if (bottomPadding == 0.dp) 16.dp else bottomPadding
}