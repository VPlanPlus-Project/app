package plus.vplan.app.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import org.jetbrains.compose.resources.Font
import vplanplus.composeapp.generated.resources.PlayfairDisplay_Black
import vplanplus.composeapp.generated.resources.PlayfairDisplay_BlackItalic
import vplanplus.composeapp.generated.resources.PlayfairDisplay_Bold
import vplanplus.composeapp.generated.resources.PlayfairDisplay_BoldItalic
import vplanplus.composeapp.generated.resources.PlayfairDisplay_ExtraBold
import vplanplus.composeapp.generated.resources.PlayfairDisplay_ExtraBoldItalic
import vplanplus.composeapp.generated.resources.PlayfairDisplay_Medium
import vplanplus.composeapp.generated.resources.PlayfairDisplay_MediumItalic
import vplanplus.composeapp.generated.resources.PlayfairDisplay_Regular
import vplanplus.composeapp.generated.resources.PlayfairDisplay_SemiBold
import vplanplus.composeapp.generated.resources.PlayfairDisplay_SemiBoldItalic
import vplanplus.composeapp.generated.resources.Res
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
    Font(Res.font.PlayfairDisplay_Black, FontWeight.Black, FontStyle.Normal),
    Font(Res.font.PlayfairDisplay_BlackItalic, FontWeight.Black, FontStyle.Italic),
    Font(Res.font.PlayfairDisplay_Bold, FontWeight.Bold, FontStyle.Normal),
    Font(Res.font.PlayfairDisplay_BoldItalic, FontWeight.Bold, FontStyle.Italic),
    Font(Res.font.PlayfairDisplay_ExtraBold, FontWeight.ExtraBold, FontStyle.Normal),
    Font(Res.font.PlayfairDisplay_ExtraBoldItalic, FontWeight.ExtraBold, FontStyle.Italic),
    Font(Res.font.PlayfairDisplay_Regular, FontWeight.Normal, FontStyle.Normal),
    Font(Res.font.PlayfairDisplay_Medium, FontWeight.Medium, FontStyle.Normal),
    Font(Res.font.PlayfairDisplay_MediumItalic, FontWeight.Medium, FontStyle.Italic),
    Font(Res.font.PlayfairDisplay_SemiBold, FontWeight.SemiBold, FontStyle.Normal),
    Font(Res.font.PlayfairDisplay_SemiBoldItalic, FontWeight.SemiBold, FontStyle.Italic),
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
