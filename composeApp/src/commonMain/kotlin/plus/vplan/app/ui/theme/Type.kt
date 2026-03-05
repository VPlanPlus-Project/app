package plus.vplan.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily

// Delegates to :core:ui. Existing imports of displayFontFamily / appTypography in :composeApp
// continue to resolve from this package without any change.

@Composable
fun bodyFontFamily(): FontFamily = plus.vplan.app.core.ui.theme.bodyFontFamily()

@Composable
fun displayFontFamily(): FontFamily = plus.vplan.app.core.ui.theme.displayFontFamily()

@Composable
fun monospaceFontFamily(): FontFamily = plus.vplan.app.core.ui.theme.monospaceFontFamily()

@Composable
fun appTypography(): Typography = plus.vplan.app.core.ui.theme.appTypography()
