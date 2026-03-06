package plus.vplan.app.ui

import org.jetbrains.compose.resources.DrawableResource
import plus.vplan.app.core.ui.CoreUiRes

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