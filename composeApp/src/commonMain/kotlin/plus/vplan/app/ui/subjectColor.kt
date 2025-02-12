package plus.vplan.app.ui

import androidx.compose.ui.graphics.Color
import plus.vplan.app.ui.theme.ColorTheme
import plus.vplan.app.ui.theme.CustomColor
import plus.vplan.app.ui.theme.colors

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