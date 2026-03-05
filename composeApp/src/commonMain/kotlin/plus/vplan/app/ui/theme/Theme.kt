package plus.vplan.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.runtime.Composable

// Delegates to :core:ui — all callers in :composeApp continue to resolve AppTheme from this package.
@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) = plus.vplan.app.core.ui.theme.AppTheme(
    darkTheme = darkTheme,
    dynamicColor = dynamicColor,
    content = content
)

@Composable
expect fun dynamicTheme(isDark: Boolean): ColorScheme?
