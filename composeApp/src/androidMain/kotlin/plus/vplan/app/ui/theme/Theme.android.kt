package plus.vplan.app.ui.theme

import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

@Composable
actual fun dynamicTheme(isDark: Boolean): ColorScheme? =
    plus.vplan.app.core.ui.theme.dynamicTheme(isDark)
