package com.example.ava.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * Ava's palette. Two greens carry the identity:
 *  - Moss, a deep saturated green, is the brand colour in light mode.
 *  - Sprout, a bright mint, is the same idea after dark, where Moss goes muddy.
 * Gold is reserved for exactly one thing: the Premium badge. Nothing else uses it.
 *
 * No screen ever writes Color(0xFF...) — every colour is read from MaterialTheme.colorScheme.
 */

// Brand greens
val Moss900 = Color(0xFF03291B)
val Moss800 = Color(0xFF07402B)
val Moss700 = Color(0xFF0B5B3D)
val Moss600 = Color(0xFF0E7A54)   // light primary
val Moss500 = Color(0xFF149468)
val Sprout400 = Color(0xFF2BC183)
val Sprout300 = Color(0xFF4EDD9A) // dark primary
val Sprout200 = Color(0xFF8CEEC0)
val Sprout100 = Color(0xFFB8F0D4)
val Sprout050 = Color(0xFFE3F8EC)

// Neutrals, tinted very slightly green so nothing looks grey next to the brand
val Bone     = Color(0xFFF5FAF6)
val Paper    = Color(0xFFFFFFFF)
val Fog      = Color(0xFFE2EFE7)
val Slate    = Color(0xFF52655C)
val Ink      = Color(0xFF0B1A14)

val NightBg      = Color(0xFF071410)
val NightSurface = Color(0xFF0C1E17)
val NightRaised  = Color(0xFF163024)
val NightOutline = Color(0xFF2A473A)
val Mist         = Color(0xFFA9C2B6)

// Semantics
val PremiumGold = Color(0xFFE8B931)
val Ruby        = Color(0xFFC5414F)
val RubyDark    = Color(0xFFFF8A93)
