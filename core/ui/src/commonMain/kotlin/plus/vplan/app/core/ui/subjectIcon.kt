package plus.vplan.app.core.ui

import org.jetbrains.compose.resources.DrawableResource
import plus.vplan.app.core.ui.theme.ColorTheme
import plus.vplan.app.core.ui.theme.CustomColor
import plus.vplan.app.core.ui.theme.colors

fun String?.subjectIcon(): DrawableResource {
    return when (this?.lowercase()?.replace("\\d".toRegex(), "")) {
        "ast", "astro", "astronomie" -> CoreUiRes.drawable.telescope
        "bio", "bia", "biologie" -> CoreUiRes.drawable.microscope
        "ch", "cha", "chemie" -> CoreUiRes.drawable.flask_conical
        "daz", "de", "deu", "deutsch" -> CoreUiRes.drawable.book_marked
        "en", "eng", "englisch" -> CoreUiRes.drawable.union_jack
        "eth", "ethik" -> CoreUiRes.drawable.heart_handshake
        "fr", "fra", "französisch" -> CoreUiRes.drawable.croissant
        "ge", "geschichte" -> CoreUiRes.drawable.scroll_text
        "geo", "geographie", "geografie" -> CoreUiRes.drawable.earth
        "grw", "wirtschaft" -> CoreUiRes.drawable.scale
        "inf", "it", "informatik" -> CoreUiRes.drawable.braces
        "ku", "kunst" -> CoreUiRes.drawable.brush
        "ma", "maa", "mathematik" -> CoreUiRes.drawable.pi
        "mu", "musik" -> CoreUiRes.drawable.music
        "ph", "phy", "lzp", "pha", "physik" -> CoreUiRes.drawable.atom
        "re", "ree", "religion" -> CoreUiRes.drawable.church
        "sp", "spo", "spm", "spw", "sport" -> CoreUiRes.drawable.dumbbell
        null -> CoreUiRes.drawable.square_x
        else -> CoreUiRes.drawable.graduation_cap
    }
}

fun String?.subjectColor(): ColorTheme {
    return when (this?.lowercase()?.replace("\\d".toRegex(), "")) {
        "ast", "astro", "astronomie" -> colors[CustomColor.Cyan]!!
        "bio", "bia", "biologie" -> colors[CustomColor.Green]!!
        "ch", "cha", "chemie" -> colors[CustomColor.Green]!!
        "daz", "de", "deu", "deutsch" -> colors[CustomColor.Red]!!
        "en", "eng", "englisch" -> colors[CustomColor.Red]!!
        "eth", "ethik" -> colors[CustomColor.GreenGray]!!
        "fr", "fra", "französisch" -> colors[CustomColor.Red]!!
        "ge", "geschichte" -> colors[CustomColor.WineRed]!!
        "geo", "geographie", "geografie" -> colors[CustomColor.Green]!!
        "grw", "wirtschaft" -> colors[CustomColor.DarkPurple]!!
        "inf", "it", "informatik" -> colors[CustomColor.Blue]!!
        "ku", "kunst" -> colors[CustomColor.Violet]!!
        "ma", "maa", "mathematik" -> colors[CustomColor.Blue]!!
        "mu", "musik" -> colors[CustomColor.Violet]!!
        "ph", "phy", "lzp", "pha", "physik" -> colors[CustomColor.Blue]!!
        "re", "ree", "religion" -> colors[CustomColor.GreenGray]!!
        "sp", "spo", "spm", "spw", "sport" -> colors[CustomColor.Yellow]!!
        null -> colors[CustomColor.Red]!!
        else -> colors[CustomColor.GreenGray]!!
    }
}