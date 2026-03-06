package plus.vplan.app.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import plus.vplan.app.core.ui.CoreUiRes

@Composable
fun bodyFontFamily() = FontFamily(
    Font(CoreUiRes.font.inter_black, FontWeight.Black, FontStyle.Normal),
    Font(CoreUiRes.font.inter_black_italic, FontWeight.Black, FontStyle.Italic),
    Font(CoreUiRes.font.inter_bold, FontWeight.Bold, FontStyle.Normal),
    Font(CoreUiRes.font.inter_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(CoreUiRes.font.inter_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(CoreUiRes.font.inter_extrabold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(CoreUiRes.font.inter_extralight, FontWeight.ExtraLight, FontStyle.Normal),
    Font(CoreUiRes.font.inter_extralight_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(CoreUiRes.font.inter_regular_italic, FontWeight.Normal, FontStyle.Italic),
    Font(CoreUiRes.font.inter_light, FontWeight.Light, FontStyle.Normal),
    Font(CoreUiRes.font.inter_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(CoreUiRes.font.inter_medium, FontWeight.Medium, FontStyle.Normal),
    Font(CoreUiRes.font.inter_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(CoreUiRes.font.inter_regular, FontWeight.Normal, FontStyle.Normal)
)

@Composable
fun displayFontFamily() = FontFamily(
    Font(CoreUiRes.font.PlayfairDisplay_Black, FontWeight.Black, FontStyle.Normal),
    Font(CoreUiRes.font.PlayfairDisplay_BlackItalic, FontWeight.Black, FontStyle.Italic),
    Font(CoreUiRes.font.PlayfairDisplay_Bold, FontWeight.Bold, FontStyle.Normal),
    Font(CoreUiRes.font.PlayfairDisplay_BoldItalic, FontWeight.Bold, FontStyle.Italic),
    Font(CoreUiRes.font.PlayfairDisplay_ExtraBold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(CoreUiRes.font.PlayfairDisplay_ExtraBoldItalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(CoreUiRes.font.PlayfairDisplay_Regular, FontWeight.Normal, FontStyle.Normal),
    Font(CoreUiRes.font.PlayfairDisplay_Medium, FontWeight.Medium, FontStyle.Normal),
    Font(CoreUiRes.font.PlayfairDisplay_MediumItalic, FontWeight.Medium, FontStyle.Italic),
    Font(CoreUiRes.font.PlayfairDisplay_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
    Font(CoreUiRes.font.PlayfairDisplay_SemiBoldItalic, FontWeight.SemiBold, FontStyle.Italic),
)

@Composable
fun monospaceFontFamily() = FontFamily(
    Font(CoreUiRes.font.jetbrains_mono_extralight, FontWeight.ExtraLight, FontStyle.Normal),
    Font(CoreUiRes.font.jetbrains_mono_extralight_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(CoreUiRes.font.jetbrains_mono_light, FontWeight.Light, FontStyle.Normal),
    Font(CoreUiRes.font.jetbrains_mono_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(CoreUiRes.font.jetbrains_mono_regular, FontWeight.Normal, FontStyle.Normal),
    Font(CoreUiRes.font.jetbrains_mono_italic, FontWeight.Normal, FontStyle.Italic),
    Font(CoreUiRes.font.jetbrains_mono_medium, FontWeight.Medium, FontStyle.Normal),
    Font(CoreUiRes.font.jetbrains_mono_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(CoreUiRes.font.jetbrains_mono_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(CoreUiRes.font.jetbrains_mono_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(CoreUiRes.font.jetbrains_mono_bold, FontWeight.Bold, FontStyle.Normal),
    Font(CoreUiRes.font.jetbrains_mono_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(CoreUiRes.font.jetbrains_mono_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(CoreUiRes.font.jetbrains_mono_extrabold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(CoreUiRes.font.jetbrains_mono_thin, FontWeight.Thin, FontStyle.Normal),
    Font(CoreUiRes.font.jetbrains_mono_thin_italic, FontWeight.Thin, FontStyle.Italic)
)

val baseline = Typography()

@Composable
fun appTypography() = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = bodyFontFamily()),
    displayMedium = baseline.displayMedium.copy(fontFamily = bodyFontFamily()),
    displaySmall = baseline.displaySmall.copy(fontFamily = bodyFontFamily()),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = bodyFontFamily()),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = bodyFontFamily()),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = bodyFontFamily()),
    titleLarge = baseline.titleLarge.copy(fontFamily = bodyFontFamily()),
    titleMedium = baseline.titleMedium.copy(fontFamily = bodyFontFamily()),
    titleSmall = baseline.titleSmall.copy(fontFamily = bodyFontFamily()),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily()),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily()),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily()),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily()),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily()),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily()),
)
