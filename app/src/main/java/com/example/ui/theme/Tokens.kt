package com.example.ui.theme

import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.dp

/**
 * Single source of truth for corner radii. Screens previously inlined over a
 * hundred RoundedCornerShape literals across ten competing values; reach for
 * these instead so a card reads the same everywhere.
 */
val YawarShapes = Shapes(
    extraSmall = RoundedCornerShape(8.dp),   // chips, thumbnails, inline tags
    small = RoundedCornerShape(12.dp),       // buttons, fields, nested panels
    medium = RoundedCornerShape(16.dp),      // the standard card
    large = RoundedCornerShape(20.dp),       // hero cards, banners, dialogs
    extraLarge = RoundedCornerShape(28.dp)   // sheets, full-bleed panels
)

/** Fully rounded pill for status badges and counts. */
val YawarPillShape = CircleShape

/**
 * Vertical rhythm. Values step by 4dp so sibling spacing never drifts by the
 * odd 10-vs-12dp that made lists look uneven.
 */
@Immutable
object YawarSpacing {
    val none = 0.dp
    val hairline = 2.dp
    val xs = 4.dp
    val sm = 8.dp
    val md = 12.dp
    val lg = 16.dp        // default screen gutter and card padding
    val xl = 20.dp
    val xxl = 24.dp
    val xxxl = 32.dp

    /** Clearance a scrollable list must reserve above the bottom navigation bar. */
    val bottomNavClearance = 90.dp
}

@Immutable
object YawarElevation {
    val flat = 0.dp
    val card = 1.dp
    val raised = 3.dp
    val hero = 6.dp
    val overlay = 12.dp
}
