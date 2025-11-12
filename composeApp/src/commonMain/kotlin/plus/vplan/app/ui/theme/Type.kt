package plus.vplan.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.albertsans_black
import vplanplus.composeapp.generated.resources.albertsans_black_italic
import vplanplus.composeapp.generated.resources.albertsans_bold
import vplanplus.composeapp.generated.resources.albertsans_bold_italic
import vplanplus.composeapp.generated.resources.albertsans_extrabold
import vplanplus.composeapp.generated.resources.albertsans_extrabold_italic
import vplanplus.composeapp.generated.resources.albertsans_extralight
import vplanplus.composeapp.generated.resources.albertsans_extralight_italic
import vplanplus.composeapp.generated.resources.albertsans_light
import vplanplus.composeapp.generated.resources.albertsans_light_italic
import vplanplus.composeapp.generated.resources.albertsans_medium
import vplanplus.composeapp.generated.resources.albertsans_medium_italic
import vplanplus.composeapp.generated.resources.albertsans_regular
import vplanplus.composeapp.generated.resources.albertsans_regular_italic
import vplanplus.composeapp.generated.resources.albertsans_semibold
import vplanplus.composeapp.generated.resources.albertsans_semibold_italic
import vplanplus.composeapp.generated.resources.albertsans_thin
import vplanplus.composeapp.generated.resources.albertsans_thin_italic
import vplanplus.composeapp.generated.resources.inter_black
import vplanplus.composeapp.generated.resources.inter_black_italic
import vplanplus.composeapp.generated.resources.inter_bold
import vplanplus.composeapp.generated.resources.inter_bold_italic
import vplanplus.composeapp.generated.resources.inter_extrabold
import vplanplus.composeapp.generated.resources.inter_extrabold_italic
import vplanplus.composeapp.generated.resources.inter_extralight
import vplanplus.composeapp.generated.resources.inter_extralight_italic
import vplanplus.composeapp.generated.resources.inter_light
import vplanplus.composeapp.generated.resources.inter_light_italic
import vplanplus.composeapp.generated.resources.inter_medium
import vplanplus.composeapp.generated.resources.inter_medium_italic
import vplanplus.composeapp.generated.resources.inter_regular
import vplanplus.composeapp.generated.resources.inter_regular_italic
import vplanplus.composeapp.generated.resources.jetbrains_mono_bold
import vplanplus.composeapp.generated.resources.jetbrains_mono_bold_italic
import vplanplus.composeapp.generated.resources.jetbrains_mono_extrabold
import vplanplus.composeapp.generated.resources.jetbrains_mono_extrabold_italic
import vplanplus.composeapp.generated.resources.jetbrains_mono_extralight
import vplanplus.composeapp.generated.resources.jetbrains_mono_extralight_italic
import vplanplus.composeapp.generated.resources.jetbrains_mono_italic
import vplanplus.composeapp.generated.resources.jetbrains_mono_light
import vplanplus.composeapp.generated.resources.jetbrains_mono_light_italic
import vplanplus.composeapp.generated.resources.jetbrains_mono_medium
import vplanplus.composeapp.generated.resources.jetbrains_mono_medium_italic
import vplanplus.composeapp.generated.resources.jetbrains_mono_regular
import vplanplus.composeapp.generated.resources.jetbrains_mono_semibold
import vplanplus.composeapp.generated.resources.jetbrains_mono_semibold_italic
import vplanplus.composeapp.generated.resources.jetbrains_mono_thin
import vplanplus.composeapp.generated.resources.jetbrains_mono_thin_italic

@Composable
fun bodyFontFamily() = FontFamily(
    Font(Res.font.inter_black, FontWeight.Black, FontStyle.Normal),
    Font(Res.font.inter_black_italic, FontWeight.Black, FontStyle.Italic),
    Font(Res.font.inter_bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.inter_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(Res.font.inter_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(Res.font.inter_extrabold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(Res.font.inter_extralight, FontWeight.ExtraLight, FontStyle.Normal),
    Font(Res.font.inter_extralight_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(Res.font.inter_regular_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.inter_light, FontWeight.Light, FontStyle.Normal),
    Font(Res.font.inter_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(Res.font.inter_medium, FontWeight.Medium, FontStyle.Normal),
    Font(Res.font.inter_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.inter_regular, FontWeight.Normal, FontStyle.Normal)
)

@Composable
fun displayFontFamily() = FontFamily(
    Font(Res.font.albertsans_black, FontWeight.Black, FontStyle.Normal),
    Font(Res.font.albertsans_black_italic, FontWeight.Black, FontStyle.Italic),
    Font(Res.font.albertsans_bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.albertsans_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(Res.font.albertsans_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(Res.font.albertsans_extrabold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(Res.font.albertsans_extralight, FontWeight.ExtraLight, FontStyle.Normal),
    Font(Res.font.albertsans_extralight_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(Res.font.albertsans_regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.albertsans_regular_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.albertsans_light, FontWeight.Light, FontStyle.Normal),
    Font(Res.font.albertsans_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(Res.font.albertsans_medium, FontWeight.Medium, FontStyle.Normal),
    Font(Res.font.albertsans_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.albertsans_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(Res.font.albertsans_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(Res.font.albertsans_thin, FontWeight.Thin, FontStyle.Normal),
    Font(Res.font.albertsans_thin_italic, FontWeight.Thin, FontStyle.Italic)
)

@Composable
fun monospaceFontFamily() = FontFamily(
    Font(Res.font.jetbrains_mono_extralight, FontWeight.ExtraLight, FontStyle.Normal),
    Font(Res.font.jetbrains_mono_extralight_italic, FontWeight.ExtraLight, FontStyle.Italic),
    Font(Res.font.jetbrains_mono_light, FontWeight.Light, FontStyle.Normal),
    Font(Res.font.jetbrains_mono_light_italic, FontWeight.Light, FontStyle.Italic),
    Font(Res.font.jetbrains_mono_regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.jetbrains_mono_italic, FontWeight.Normal, FontStyle.Italic),
    Font(Res.font.jetbrains_mono_medium, FontWeight.Medium, FontStyle.Normal),
    Font(Res.font.jetbrains_mono_medium_italic, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.jetbrains_mono_semibold, FontWeight.SemiBold, FontStyle.Normal),
    Font(Res.font.jetbrains_mono_semibold_italic, FontWeight.SemiBold, FontStyle.Italic),
    Font(Res.font.jetbrains_mono_bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.jetbrains_mono_bold_italic, FontWeight.Bold, FontStyle.Italic),
    Font(Res.font.jetbrains_mono_extrabold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(Res.font.jetbrains_mono_extrabold_italic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(Res.font.jetbrains_mono_thin, FontWeight.Thin, FontStyle.Normal),
    Font(Res.font.jetbrains_mono_thin_italic, FontWeight.Thin, FontStyle.Italic)
)

val baseline = Typography()

@Composable
fun appTypography() = Typography(
    displayLarge = baseline.displayLarge.copy(fontFamily = displayFontFamily()),
    displayMedium = baseline.displayMedium.copy(fontFamily = displayFontFamily()),
    displaySmall = baseline.displaySmall.copy(fontFamily = displayFontFamily()),
    headlineLarge = baseline.headlineLarge.copy(fontFamily = displayFontFamily()),
    headlineMedium = baseline.headlineMedium.copy(fontFamily = displayFontFamily()),
    headlineSmall = baseline.headlineSmall.copy(fontFamily = displayFontFamily()),
    titleLarge = baseline.titleLarge.copy(fontFamily = displayFontFamily()),
    titleMedium = baseline.titleMedium.copy(fontFamily = displayFontFamily()),
    titleSmall = baseline.titleSmall.copy(fontFamily = displayFontFamily()),
    bodyLarge = baseline.bodyLarge.copy(fontFamily = bodyFontFamily()),
    bodyMedium = baseline.bodyMedium.copy(fontFamily = bodyFontFamily()),
    bodySmall = baseline.bodySmall.copy(fontFamily = bodyFontFamily()),
    labelLarge = baseline.labelLarge.copy(fontFamily = bodyFontFamily()),
    labelMedium = baseline.labelMedium.copy(fontFamily = bodyFontFamily()),
    labelSmall = baseline.labelSmall.copy(fontFamily = bodyFontFamily()),
)
