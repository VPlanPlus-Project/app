package plus.vplan.app.ui.theme
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val primaryLight = Color(0xFF000000)
val onPrimaryLight = Color(0xFFFFFFFF)
val primaryContainerLight = Color(0xFF262626)
val onPrimaryContainerLight = Color(0xFFA1A1A1)

val secondaryLight = Color(0xFFE2E2E2)
val onSecondaryLight = Color(0xFF4C4C4C)
val secondaryContainerLight = Color(0xFFf2f2f2)
val onSecondaryContainerLight = Color(0xFF494A4B)

val tertiaryLight = Color(0xFF00525C)
val onTertiaryLight = Color(0xFFFFFFFF)
val tertiaryContainerLight = Color(0xFF227884)
val onTertiaryContainerLight = Color(0xFFFFFFFF)
val errorLight = Color(0xFFBA1A1A)
val onErrorLight = Color(0xFFFFFFFF)
val errorContainerLight = Color(0xFFFFDAD6)
val onErrorContainerLight = Color(0xFF410002)
val backgroundLight = Color(0xFFF9F9F9)
val onBackgroundLight = Color(0xFF1B1B1B)
val surfaceLight = Color(0xFFFCF8F8)
val onSurfaceLight = Color(0xFF1C1B1B)
val surfaceVariantLight = Color(0xFFE0E3E4)
val onSurfaceVariantLight = Color(0xFF434748)
val outlineLight = Color(0xFF747879)
val outlineVariantLight = Color(0xFFC3C7C8)
val scrimLight = Color(0xFF000000)
val inverseSurfaceLight = Color(0xFF313030)
val inverseOnSurfaceLight = Color(0xFFF4F0EF)
val inversePrimaryLight = Color(0xFFC6C6C6)
val surfaceDimLight = Color(0xFFDDD9D9)
val surfaceBrightLight = Color(0xFFFCF8F8)
val surfaceContainerLowestLight = Color(0xFFFFFFFF)
val surfaceContainerLowLight = Color(0xFFF6F3F2)
val surfaceContainerLight = Color(0xFFF1EDEC)
val surfaceContainerHighLight = Color(0xFFEBE7E7)
val surfaceContainerHighestLight = Color(0xFFE5E2E1)

val primaryDark = Color(0xFFC6C6C6)
val onPrimaryDark = Color(0xFF303030)
val primaryContainerDark = Color(0xFF000000)
val onPrimaryContainerDark = Color(0xFF969696)
val secondaryDark = Color(0xFFFFFFFF)
val onSecondaryDark = Color(0xFF2F3131)
val secondaryContainerDark = Color(0xFFD4D4D4)
val onSecondaryContainerDark = Color(0xFF3E4040)
val tertiaryDark = Color(0xFF85D2E0)
val onTertiaryDark = Color(0xFF00363D)
val tertiaryContainerDark = Color(0xFF005D68)
val onTertiaryContainerDark = Color(0xFFE3FBFF)
val errorDark = Color(0xFFFFB4AB)
val onErrorDark = Color(0xFF690005)
val errorContainerDark = Color(0xFF93000A)
val onErrorContainerDark = Color(0xFFFFDAD6)
val backgroundDark = Color(0xFF131313)
val onBackgroundDark = Color(0xFFE2E2E2)
val surfaceDark = Color(0xFF141313)
val onSurfaceDark = Color(0xFFE5E2E1)
val surfaceVariantDark = Color(0xFF434748)
val onSurfaceVariantDark = Color(0xFFC3C7C8)
val outlineDark = Color(0xFF8D9192)
val outlineVariantDark = Color(0xFF434748)
val scrimDark = Color(0xFF000000)
val inverseSurfaceDark = Color(0xFFE5E2E1)
val inverseOnSurfaceDark = Color(0xFF313030)
val inversePrimaryDark = Color(0xFF5E5E5E)
val surfaceDimDark = Color(0xFF141313)
val surfaceBrightDark = Color(0xFF3A3939)
val surfaceContainerLowestDark = Color(0xFF0E0E0E)
val surfaceContainerLowDark = Color(0xFF1C1B1B)
val surfaceContainerDark = Color(0xFF201F1F)
val surfaceContainerHighDark = Color(0xFF2A2A2A)
val surfaceContainerHighestDark = Color(0xFF353434)

val greenLight = Color(0xFF006E1C)
val onGreenLight = Color(0xFFFFFFFF)
val greenContainerLight = Color(0xFF71D670)
val onGreenContainerLight = Color(0xFF003B0A)

val greenDark = Color(0xFF89EF86)
val onGreenDark = Color(0xFF00390A)
val greenContainerDark = Color(0xFF60C461)
val onGreenContainerDark = Color(0xFF002B06)

val yellowLight = Color(0xFFFFD600)
val onYellowLight = Color(0xFF000000)
val yellowContainerLight = Color(0xFFFFF176)
val onYellowContainerLight = Color(0xFF402C00)

val yellowDark = Color(0xFFFFE45C)
val onYellowDark = Color(0xFF3F2D00)
val yellowContainerDark = Color(0xFFFFC400)
val onYellowContainerDark = Color(0xFF201C00)


enum class ColorToken {
    Green, OnGreen, GreenContainer, OnGreenContainer,
    Yellow, OnYellow, YellowContainer, OnYellowContainer
}

val customColors = mapOf(
    ColorToken.Green to ColorTokens(
        light = greenLight,
        dark = greenDark,
    ),
    ColorToken.OnGreen to ColorTokens(
        light = onGreenLight,
        dark = onGreenDark,
    ),
    ColorToken.GreenContainer to ColorTokens(
        light = greenContainerLight,
        dark = greenContainerDark,
    ),
    ColorToken.OnGreenContainer to ColorTokens(
        light = onGreenContainerLight,
        dark = onGreenContainerDark,
    ),

    ColorToken.Yellow to ColorTokens(
        light = yellowLight,
        dark = yellowDark,
    ),
    ColorToken.OnYellow to ColorTokens(
        light = onYellowLight,
        dark = onYellowDark,
    ),
    ColorToken.YellowContainer to ColorTokens(
        light = yellowContainerLight,
        dark = yellowContainerDark,
    ),
    ColorToken.OnYellowContainer to ColorTokens(
        light = onYellowContainerLight,
        dark = onYellowContainerDark,
    )
)

data class ColorTokens(
    val light: Color,
    val dark: Color,
) {
    @Composable
    fun get() = when (isSystemInDarkTheme()) {
        true -> dark
        false -> light
    }
}