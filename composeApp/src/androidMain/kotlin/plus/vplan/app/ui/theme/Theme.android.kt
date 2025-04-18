package plus.vplan.app.ui.theme

import android.os.Build
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

@Composable
actual fun dynamicTheme(isDark: Boolean): ColorScheme? = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) run {
    val context = LocalContext.current
    if (isDark) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
} else null