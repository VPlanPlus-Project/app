package plus.vplan.app.ui

import org.jetbrains.compose.resources.DrawableResource
import vplanplus.composeapp.generated.resources.Res
import vplanplus.composeapp.generated.resources.telescope
import vplanplus.composeapp.generated.resources.microscope
import vplanplus.composeapp.generated.resources.flask_conical
import vplanplus.composeapp.generated.resources.book_marked
import vplanplus.composeapp.generated.resources.union_jack
import vplanplus.composeapp.generated.resources.brain_cog
import vplanplus.composeapp.generated.resources.croissant
import vplanplus.composeapp.generated.resources.scroll_text
import vplanplus.composeapp.generated.resources.earth
import vplanplus.composeapp.generated.resources.scale
import vplanplus.composeapp.generated.resources.braces
import vplanplus.composeapp.generated.resources.brush
import vplanplus.composeapp.generated.resources.pi
import vplanplus.composeapp.generated.resources.music
import vplanplus.composeapp.generated.resources.atom
import vplanplus.composeapp.generated.resources.church
import vplanplus.composeapp.generated.resources.dumbbell
import vplanplus.composeapp.generated.resources.square_x
import vplanplus.composeapp.generated.resources.graduation_cap

fun String?.subjectIcon(): DrawableResource {
    return when (this?.lowercase()?.replace("\\d".toRegex(), "")) {
        "ast", "astro", "astronomie" -> Res.drawable.telescope
        "bio", "bia", "biologie" -> Res.drawable.microscope
        "ch", "cha", "chemie" -> Res.drawable.flask_conical
        "daz", "de", "deu", "deutsch" -> Res.drawable.book_marked
        "en", "eng", "englisch" -> Res.drawable.union_jack
        "eth", "ethik" -> Res.drawable.brain_cog
        "fr", "fra", "französisch" -> Res.drawable.croissant
        "ge", "geschichte" -> Res.drawable.scroll_text
        "geo", "geographie", "geografie" -> Res.drawable.earth
        "grw", "wirtschaft" -> Res.drawable.scale
        "inf", "it", "informatik" -> Res.drawable.braces
        "ku", "kunst" -> Res.drawable.brush
        "ma", "maa", "mathematik" -> Res.drawable.pi
        "mu", "musik" -> Res.drawable.music
        "ph", "phy", "lzp", "pha", "physik" -> Res.drawable.atom
        "re", "ree", "religion" -> Res.drawable.church
        "sp", "spo", "spm", "spw", "sport" -> Res.drawable.dumbbell
        null -> Res.drawable.square_x
        else -> Res.drawable.graduation_cap
    }
}