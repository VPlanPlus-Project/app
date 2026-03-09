package plus.vplan.app.core.ui

import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.FontResource
import vplanplus.core.ui.generated.resources.PlayfairDisplay_Black
import vplanplus.core.ui.generated.resources.PlayfairDisplay_BlackItalic
import vplanplus.core.ui.generated.resources.PlayfairDisplay_Bold
import vplanplus.core.ui.generated.resources.PlayfairDisplay_BoldItalic
import vplanplus.core.ui.generated.resources.PlayfairDisplay_ExtraBold
import vplanplus.core.ui.generated.resources.PlayfairDisplay_ExtraBoldItalic
import vplanplus.core.ui.generated.resources.PlayfairDisplay_Medium
import vplanplus.core.ui.generated.resources.PlayfairDisplay_MediumItalic
import vplanplus.core.ui.generated.resources.PlayfairDisplay_Regular
import vplanplus.core.ui.generated.resources.PlayfairDisplay_SemiBold
import vplanplus.core.ui.generated.resources.PlayfairDisplay_SemiBoldItalic
import vplanplus.core.ui.generated.resources.Res
import vplanplus.core.ui.generated.resources.arrow_left
import vplanplus.core.ui.generated.resources.arrow_left_right
import vplanplus.core.ui.generated.resources.arrow_right
import vplanplus.core.ui.generated.resources.arrow_up
import vplanplus.core.ui.generated.resources.atom
import vplanplus.core.ui.generated.resources.badge_check
import vplanplus.core.ui.generated.resources.badge_plus
import vplanplus.core.ui.generated.resources.ban
import vplanplus.core.ui.generated.resources.bell_ring
import vplanplus.core.ui.generated.resources.book_marked
import vplanplus.core.ui.generated.resources.book_open
import vplanplus.core.ui.generated.resources.braces
import vplanplus.core.ui.generated.resources.brush
import vplanplus.core.ui.generated.resources.bug
import vplanplus.core.ui.generated.resources.bug_play
import vplanplus.core.ui.generated.resources.calculator
import vplanplus.core.ui.generated.resources.calendar
import vplanplus.core.ui.generated.resources.calendar_cog
import vplanplus.core.ui.generated.resources.chart_no_axes_combined
import vplanplus.core.ui.generated.resources.chart_no_axes_gantt
import vplanplus.core.ui.generated.resources.check
import vplanplus.core.ui.generated.resources.chevron_down
import vplanplus.core.ui.generated.resources.chevron_right
import vplanplus.core.ui.generated.resources.church
import vplanplus.core.ui.generated.resources.circle_slash_2
import vplanplus.core.ui.generated.resources.circle_user_round
import vplanplus.core.ui.generated.resources.clipboard_paste
import vplanplus.core.ui.generated.resources.clock_fading
import vplanplus.core.ui.generated.resources.cloud_alert
import vplanplus.core.ui.generated.resources.cloud_download
import vplanplus.core.ui.generated.resources.copy
import vplanplus.core.ui.generated.resources.croissant
import vplanplus.core.ui.generated.resources.door_closed
import vplanplus.core.ui.generated.resources.door_open
import vplanplus.core.ui.generated.resources.download
import vplanplus.core.ui.generated.resources.dumbbell
import vplanplus.core.ui.generated.resources.earth
import vplanplus.core.ui.generated.resources.ellipsis_vertical
import vplanplus.core.ui.generated.resources.eye
import vplanplus.core.ui.generated.resources.eye_off
import vplanplus.core.ui.generated.resources.file
import vplanplus.core.ui.generated.resources.file_badge
import vplanplus.core.ui.generated.resources.file_text
import vplanplus.core.ui.generated.resources.filter
import vplanplus.core.ui.generated.resources.fingerprint_pattern
import vplanplus.core.ui.generated.resources.flag
import vplanplus.core.ui.generated.resources.flask_conical
import vplanplus.core.ui.generated.resources.github
import vplanplus.core.ui.generated.resources.globe
import vplanplus.core.ui.generated.resources.google_play
import vplanplus.core.ui.generated.resources.graduation_cap
import vplanplus.core.ui.generated.resources.hand_coins
import vplanplus.core.ui.generated.resources.handshake
import vplanplus.core.ui.generated.resources.heart_handshake
import vplanplus.core.ui.generated.resources.house
import vplanplus.core.ui.generated.resources.image
import vplanplus.core.ui.generated.resources.info
import vplanplus.core.ui.generated.resources.instagram
import vplanplus.core.ui.generated.resources.inter_black
import vplanplus.core.ui.generated.resources.inter_black_italic
import vplanplus.core.ui.generated.resources.inter_bold
import vplanplus.core.ui.generated.resources.inter_bold_italic
import vplanplus.core.ui.generated.resources.inter_extrabold
import vplanplus.core.ui.generated.resources.inter_extrabold_italic
import vplanplus.core.ui.generated.resources.inter_extralight
import vplanplus.core.ui.generated.resources.inter_extralight_italic
import vplanplus.core.ui.generated.resources.inter_light
import vplanplus.core.ui.generated.resources.inter_light_italic
import vplanplus.core.ui.generated.resources.inter_medium
import vplanplus.core.ui.generated.resources.inter_medium_italic
import vplanplus.core.ui.generated.resources.inter_regular
import vplanplus.core.ui.generated.resources.inter_regular_italic
import vplanplus.core.ui.generated.resources.jetbrains_mono_bold
import vplanplus.core.ui.generated.resources.jetbrains_mono_bold_italic
import vplanplus.core.ui.generated.resources.jetbrains_mono_extrabold
import vplanplus.core.ui.generated.resources.jetbrains_mono_extrabold_italic
import vplanplus.core.ui.generated.resources.jetbrains_mono_extralight
import vplanplus.core.ui.generated.resources.jetbrains_mono_extralight_italic
import vplanplus.core.ui.generated.resources.jetbrains_mono_italic
import vplanplus.core.ui.generated.resources.jetbrains_mono_light
import vplanplus.core.ui.generated.resources.jetbrains_mono_light_italic
import vplanplus.core.ui.generated.resources.jetbrains_mono_medium
import vplanplus.core.ui.generated.resources.jetbrains_mono_medium_italic
import vplanplus.core.ui.generated.resources.jetbrains_mono_regular
import vplanplus.core.ui.generated.resources.jetbrains_mono_semibold
import vplanplus.core.ui.generated.resources.jetbrains_mono_semibold_italic
import vplanplus.core.ui.generated.resources.jetbrains_mono_thin
import vplanplus.core.ui.generated.resources.jetbrains_mono_thin_italic
import vplanplus.core.ui.generated.resources.key_round
import vplanplus.core.ui.generated.resources.lightbulb
import vplanplus.core.ui.generated.resources.list_ordered
import vplanplus.core.ui.generated.resources.lock
import vplanplus.core.ui.generated.resources.lock_open
import vplanplus.core.ui.generated.resources.log_in
import vplanplus.core.ui.generated.resources.logo
import vplanplus.core.ui.generated.resources.logo_black
import vplanplus.core.ui.generated.resources.logo_dark
import vplanplus.core.ui.generated.resources.logo_light
import vplanplus.core.ui.generated.resources.logo_white
import vplanplus.core.ui.generated.resources.logout
import vplanplus.core.ui.generated.resources.logs
import vplanplus.core.ui.generated.resources.mastodon
import vplanplus.core.ui.generated.resources.megaphone
import vplanplus.core.ui.generated.resources.message_circle_heart
import vplanplus.core.ui.generated.resources.message_circle_warning
import vplanplus.core.ui.generated.resources.message_square
import vplanplus.core.ui.generated.resources.microscope
import vplanplus.core.ui.generated.resources.minus
import vplanplus.core.ui.generated.resources.music
import vplanplus.core.ui.generated.resources.notebook_text
import vplanplus.core.ui.generated.resources.pencil
import vplanplus.core.ui.generated.resources.pi
import vplanplus.core.ui.generated.resources.plus
import vplanplus.core.ui.generated.resources.rectangle_ellipsis
import vplanplus.core.ui.generated.resources.rotate_cw
import vplanplus.core.ui.generated.resources.scale
import vplanplus.core.ui.generated.resources.school
import vplanplus.core.ui.generated.resources.scroll_text
import vplanplus.core.ui.generated.resources.search
import vplanplus.core.ui.generated.resources.search_x
import vplanplus.core.ui.generated.resources.send_horizontal
import vplanplus.core.ui.generated.resources.server_cog
import vplanplus.core.ui.generated.resources.settings
import vplanplus.core.ui.generated.resources.shapes
import vplanplus.core.ui.generated.resources.sheet
import vplanplus.core.ui.generated.resources.shield_user
import vplanplus.core.ui.generated.resources.smartphone
import vplanplus.core.ui.generated.resources.square_arrow_out_up_right
import vplanplus.core.ui.generated.resources.square_user_round
import vplanplus.core.ui.generated.resources.square_x
import vplanplus.core.ui.generated.resources.table_properties
import vplanplus.core.ui.generated.resources.telescope
import vplanplus.core.ui.generated.resources.threads
import vplanplus.core.ui.generated.resources.trash_2
import vplanplus.core.ui.generated.resources.triangle_alert
import vplanplus.core.ui.generated.resources.undo_2
import vplanplus.core.ui.generated.resources.undraw_profile
import vplanplus.core.ui.generated.resources.undraw_relaxing_at_home_black
import vplanplus.core.ui.generated.resources.undraw_relaxing_at_home_white
import vplanplus.core.ui.generated.resources.union_jack
import vplanplus.core.ui.generated.resources.user
import vplanplus.core.ui.generated.resources.user_pen
import vplanplus.core.ui.generated.resources.users
import vplanplus.core.ui.generated.resources.whatsapp
import vplanplus.core.ui.generated.resources.x
import vplanplus.core.ui.generated.resources.zap

@OptIn(ExperimentalResourceApi::class)
object CoreUiRes {
    object drawable {
        // Logos
        val logo_black: DrawableResource get() = Res.drawable.logo_black
        val logo_white: DrawableResource get() = Res.drawable.logo_white
        val logo: DrawableResource get() = Res.drawable.logo
        val logo_dark: DrawableResource get() = Res.drawable.logo_dark
        val logo_light: DrawableResource get() = Res.drawable.logo_light

        // Arrows / chevrons
        val arrow_left: DrawableResource get() = Res.drawable.arrow_left
        val arrow_left_right: DrawableResource get() = Res.drawable.arrow_left_right
        val arrow_right: DrawableResource get() = Res.drawable.arrow_right
        val arrow_up: DrawableResource get() = Res.drawable.arrow_up
        val chevron_down: DrawableResource get() = Res.drawable.chevron_down
        val chevron_right: DrawableResource get() = Res.drawable.chevron_right

        // Actions
        val ban: DrawableResource get() = Res.drawable.ban
        val check: DrawableResource get() = Res.drawable.check
        val clipboard_paste: DrawableResource get() = Res.drawable.clipboard_paste
        val cloud_alert: DrawableResource get() = Res.drawable.cloud_alert
        val cloud_download: DrawableResource get() = Res.drawable.cloud_download
        val copy: DrawableResource get() = Res.drawable.copy
        val download: DrawableResource get() = Res.drawable.download
        val filter: DrawableResource get() = Res.drawable.filter
        val hand_coins: DrawableResource get() = Res.drawable.hand_coins
        val log_in: DrawableResource get() = Res.drawable.log_in
        val logout: DrawableResource get() = Res.drawable.logout
        val minus: DrawableResource get() = Res.drawable.minus
        val pencil: DrawableResource get() = Res.drawable.pencil
        val plus: DrawableResource get() = Res.drawable.plus
        val rotate_cw: DrawableResource get() = Res.drawable.rotate_cw
        val search: DrawableResource get() = Res.drawable.search
        val search_x: DrawableResource get() = Res.drawable.search_x
        val send_horizontal: DrawableResource get() = Res.drawable.send_horizontal
        val settings: DrawableResource get() = Res.drawable.settings
        val trash_2: DrawableResource get() = Res.drawable.trash_2
        val undo_2: DrawableResource get() = Res.drawable.undo_2
        val x: DrawableResource get() = Res.drawable.x

        // UI elements
        val bell_ring: DrawableResource get() = Res.drawable.bell_ring
        val calendar: DrawableResource get() = Res.drawable.calendar
        val calendar_cog: DrawableResource get() = Res.drawable.calendar_cog
        val circle_slash_2: DrawableResource get() = Res.drawable.circle_slash_2
        val circle_user_round: DrawableResource get() = Res.drawable.circle_user_round
        val clock_fading: DrawableResource get() = Res.drawable.clock_fading
        val door_closed: DrawableResource get() = Res.drawable.door_closed
        val door_open: DrawableResource get() = Res.drawable.door_open
        val ellipsis_vertical: DrawableResource get() = Res.drawable.ellipsis_vertical
        val eye: DrawableResource get() = Res.drawable.eye
        val eye_off: DrawableResource get() = Res.drawable.eye_off
        val file: DrawableResource get() = Res.drawable.file
        val file_badge: DrawableResource get() = Res.drawable.file_badge
        val file_text: DrawableResource get() = Res.drawable.file_text
        val fingerprint_pattern: DrawableResource get() = Res.drawable.fingerprint_pattern
        val flag: DrawableResource get() = Res.drawable.flag
        val house: DrawableResource get() = Res.drawable.house
        val image: DrawableResource get() = Res.drawable.image
        val info: DrawableResource get() = Res.drawable.info
        val key_round: DrawableResource get() = Res.drawable.key_round
        val list_ordered: DrawableResource get() = Res.drawable.list_ordered
        val lock: DrawableResource get() = Res.drawable.lock
        val lock_open: DrawableResource get() = Res.drawable.lock_open
        val logs: DrawableResource get() = Res.drawable.logs
        val message_circle_heart: DrawableResource get() = Res.drawable.message_circle_heart
        val message_circle_warning: DrawableResource get() = Res.drawable.message_circle_warning
        val message_square: DrawableResource get() = Res.drawable.message_square
        val rectangle_ellipsis: DrawableResource get() = Res.drawable.rectangle_ellipsis
        val scale: DrawableResource get() = Res.drawable.scale
        val scroll_text: DrawableResource get() = Res.drawable.scroll_text
        val server_cog: DrawableResource get() = Res.drawable.server_cog
        val sheet: DrawableResource get() = Res.drawable.sheet
        val shield_user: DrawableResource get() = Res.drawable.shield_user
        val smartphone: DrawableResource get() = Res.drawable.smartphone
        val square_arrow_out_up_right: DrawableResource get() = Res.drawable.square_arrow_out_up_right
        val square_user_round: DrawableResource get() = Res.drawable.square_user_round
        val square_x: DrawableResource get() = Res.drawable.square_x
        val table_properties: DrawableResource get() = Res.drawable.table_properties
        val triangle_alert: DrawableResource get() = Res.drawable.triangle_alert
        val user: DrawableResource get() = Res.drawable.user
        val user_pen: DrawableResource get() = Res.drawable.user_pen
        val users: DrawableResource get() = Res.drawable.users

        // Badges
        val badge_check: DrawableResource get() = Res.drawable.badge_check
        val badge_plus: DrawableResource get() = Res.drawable.badge_plus

        // Subject / education icons
        val atom: DrawableResource get() = Res.drawable.atom
        val book_marked: DrawableResource get() = Res.drawable.book_marked
        val book_open: DrawableResource get() = Res.drawable.book_open
        val braces: DrawableResource get() = Res.drawable.braces
        val brush: DrawableResource get() = Res.drawable.brush
        val bug: DrawableResource get() = Res.drawable.bug
        val bug_play: DrawableResource get() = Res.drawable.bug_play
        val calculator: DrawableResource get() = Res.drawable.calculator
        val chart_no_axes_combined: DrawableResource get() = Res.drawable.chart_no_axes_combined
        val chart_no_axes_gantt: DrawableResource get() = Res.drawable.chart_no_axes_gantt
        val church: DrawableResource get() = Res.drawable.church
        val croissant: DrawableResource get() = Res.drawable.croissant
        val dumbbell: DrawableResource get() = Res.drawable.dumbbell
        val earth: DrawableResource get() = Res.drawable.earth
        val flask_conical: DrawableResource get() = Res.drawable.flask_conical
        val globe: DrawableResource get() = Res.drawable.globe
        val graduation_cap: DrawableResource get() = Res.drawable.graduation_cap
        val handshake: DrawableResource get() = Res.drawable.handshake
        val heart_handshake: DrawableResource get() = Res.drawable.heart_handshake
        val lightbulb: DrawableResource get() = Res.drawable.lightbulb
        val megaphone: DrawableResource get() = Res.drawable.megaphone
        val microscope: DrawableResource get() = Res.drawable.microscope
        val music: DrawableResource get() = Res.drawable.music
        val notebook_text: DrawableResource get() = Res.drawable.notebook_text
        val pi: DrawableResource get() = Res.drawable.pi
        val school: DrawableResource get() = Res.drawable.school
        val shapes: DrawableResource get() = Res.drawable.shapes
        val telescope: DrawableResource get() = Res.drawable.telescope
        val zap: DrawableResource get() = Res.drawable.zap

        // Social / brand
        val github: DrawableResource get() = Res.drawable.github
        val google_play: DrawableResource get() = Res.drawable.google_play
        val instagram: DrawableResource get() = Res.drawable.instagram
        val mastodon: DrawableResource get() = Res.drawable.mastodon
        val threads: DrawableResource get() = Res.drawable.threads
        val union_jack: DrawableResource get() = Res.drawable.union_jack
        val whatsapp: DrawableResource get() = Res.drawable.whatsapp

        // Illustrations
        val undraw_profile: DrawableResource get() = Res.drawable.undraw_profile
        val undraw_relaxing_at_home_black: DrawableResource get() = Res.drawable.undraw_relaxing_at_home_black
        val undraw_relaxing_at_home_white: DrawableResource get() = Res.drawable.undraw_relaxing_at_home_white
    }

    object font {
        // Inter
        val inter_black: FontResource get() = Res.font.inter_black
        val inter_black_italic: FontResource get() = Res.font.inter_black_italic
        val inter_bold: FontResource get() = Res.font.inter_bold
        val inter_bold_italic: FontResource get() = Res.font.inter_bold_italic
        val inter_extrabold: FontResource get() = Res.font.inter_extrabold
        val inter_extrabold_italic: FontResource get() = Res.font.inter_extrabold_italic
        val inter_extralight: FontResource get() = Res.font.inter_extralight
        val inter_extralight_italic: FontResource get() = Res.font.inter_extralight_italic
        val inter_light: FontResource get() = Res.font.inter_light
        val inter_light_italic: FontResource get() = Res.font.inter_light_italic
        val inter_medium: FontResource get() = Res.font.inter_medium
        val inter_medium_italic: FontResource get() = Res.font.inter_medium_italic
        val inter_regular: FontResource get() = Res.font.inter_regular
        val inter_regular_italic: FontResource get() = Res.font.inter_regular_italic

        // PlayfairDisplay
        val PlayfairDisplay_Black: FontResource get() = Res.font.PlayfairDisplay_Black
        val PlayfairDisplay_BlackItalic: FontResource get() = Res.font.PlayfairDisplay_BlackItalic
        val PlayfairDisplay_Bold: FontResource get() = Res.font.PlayfairDisplay_Bold
        val PlayfairDisplay_BoldItalic: FontResource get() = Res.font.PlayfairDisplay_BoldItalic
        val PlayfairDisplay_ExtraBold: FontResource get() = Res.font.PlayfairDisplay_ExtraBold
        val PlayfairDisplay_ExtraBoldItalic: FontResource get() = Res.font.PlayfairDisplay_ExtraBoldItalic
        val PlayfairDisplay_Medium: FontResource get() = Res.font.PlayfairDisplay_Medium
        val PlayfairDisplay_MediumItalic: FontResource get() = Res.font.PlayfairDisplay_MediumItalic
        val PlayfairDisplay_Regular: FontResource get() = Res.font.PlayfairDisplay_Regular
        val PlayfairDisplay_SemiBold: FontResource get() = Res.font.PlayfairDisplay_SemiBold
        val PlayfairDisplay_SemiBoldItalic: FontResource get() = Res.font.PlayfairDisplay_SemiBoldItalic

        // JetBrains Mono
        val jetbrains_mono_extralight: FontResource get() = Res.font.jetbrains_mono_extralight
        val jetbrains_mono_extralight_italic: FontResource get() = Res.font.jetbrains_mono_extralight_italic
        val jetbrains_mono_light: FontResource get() = Res.font.jetbrains_mono_light
        val jetbrains_mono_light_italic: FontResource get() = Res.font.jetbrains_mono_light_italic
        val jetbrains_mono_regular: FontResource get() = Res.font.jetbrains_mono_regular
        val jetbrains_mono_italic: FontResource get() = Res.font.jetbrains_mono_italic
        val jetbrains_mono_medium: FontResource get() = Res.font.jetbrains_mono_medium
        val jetbrains_mono_medium_italic: FontResource get() = Res.font.jetbrains_mono_medium_italic
        val jetbrains_mono_semibold: FontResource get() = Res.font.jetbrains_mono_semibold
        val jetbrains_mono_semibold_italic: FontResource get() = Res.font.jetbrains_mono_semibold_italic
        val jetbrains_mono_bold: FontResource get() = Res.font.jetbrains_mono_bold
        val jetbrains_mono_bold_italic: FontResource get() = Res.font.jetbrains_mono_bold_italic
        val jetbrains_mono_extrabold: FontResource get() = Res.font.jetbrains_mono_extrabold
        val jetbrains_mono_extrabold_italic: FontResource get() = Res.font.jetbrains_mono_extrabold_italic
        val jetbrains_mono_thin: FontResource get() = Res.font.jetbrains_mono_thin
        val jetbrains_mono_thin_italic: FontResource get() = Res.font.jetbrains_mono_thin_italic
    }
}
