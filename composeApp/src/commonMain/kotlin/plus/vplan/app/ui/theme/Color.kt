package plus.vplan.app.ui.theme

// All color tokens, enums, and maps are now defined in :core:ui.
// This file re-exports everything so existing imports in :composeApp stay valid.

@Suppress("unused")
val primaryLight get() = plus.vplan.app.core.ui.theme.primaryLight
@Suppress("unused")
val onPrimaryLight get() = plus.vplan.app.core.ui.theme.onPrimaryLight
@Suppress("unused")
val primaryContainerLight get() = plus.vplan.app.core.ui.theme.primaryContainerLight
@Suppress("unused")
val onPrimaryContainerLight get() = plus.vplan.app.core.ui.theme.onPrimaryContainerLight

typealias ColorToken = plus.vplan.app.core.ui.theme.ColorToken
typealias CustomColor = plus.vplan.app.core.ui.theme.CustomColor
typealias ColorTokens = plus.vplan.app.core.ui.theme.ColorTokens
typealias ColorGroup = plus.vplan.app.core.ui.theme.ColorGroup
typealias ColorTheme = plus.vplan.app.core.ui.theme.ColorTheme

val customColors get() = plus.vplan.app.core.ui.theme.customColors
val colors get() = plus.vplan.app.core.ui.theme.colors
