package com.example.ui.theme

import androidx.compose.ui.graphics.Color

// Yawar Hamdard Health Consulting Services Brand Palette
val YawarNavy = Color(0xFF3F78B5)       // Soft brand blue for headings and icons
val YawarNavyDark = Color(0xFF5B91C8)   // Secondary blue accent
val YawarBlue = Color(0xFF286AA8)       // Main blue accent
val YawarBlueLight = Color(0xFF5B9AD2)  // Light blue accent
val ClinicalGreen = YawarBlue          // Blue replacement for the old green accent
val DeepGreen = YawarNavy               // Blue replacement for the old dark green accent
val PaleBlue = Color(0xFFF3F8FE)        // Very light blue decorative surface
val PaleGreen = PaleBlue                // Keep legacy call sites on the white-and-blue palette
val Ink = Color(0xFF43546A)             // Readable body text without pure black
val Slate = Color(0xFF687B91)           // Secondary text
val SlateLight = Color(0xFF94A3B8)      // Subtle Placeholder
val BorderColor = Color(0xFFE2E8F0)     // Clean Border
val CardBackground = Color(0xFFFFFFFF)  // Pure White Surface
val SurfaceBackground = Color.White       // White app background
val WarningAmber = Color(0xFFF59E0B)    // Pending / Warning
val WarningAmberBg = Color(0xFFFEF3C7)  // Soft Amber
val EmergencyRed = Color(0xFFD32F2F)    // Emergency Alert Red
val EmergencyRedBg = Color(0xFFFEE2E2)  // Soft Emergency Red
val VerifiedBlue = Color(0xFF0284C7)    // Verified Badge Blue

// Text resting on the navy banner / top bar.
val OnNavyMuted = Color(0xFF687B91)     // Secondary text on white surfaces
val OnNavyFaint = Color(0x26FFFFFF)     // 15% white overlay chips on navy
val OnNavyWarning = Color(0xFFFFD166)   // Low-bandwidth alert on the navy bar

// Single semantic danger pair, replacing the four reds that had drifted in.
val DangerText = Color(0xFFD92D20)      // Destructive label / icon on light
val DangerBg = Color(0xFFFEECEB)        // Cancelled / declined pill fill

// Neutral chip + row fills.
val ChipBg = Color(0xFFF1F5F9)          // Inactive segmented control, meta strip
val ChipText = Color(0xFF475467)        // Label on ChipBg
val DividerSoft = Color(0xFFCBD5E1)     // Placeholder and skeleton strokes

// Amber foregrounds used on WarningAmberBg.
val AmberText = Color(0xFF92400E)       // Readable amber-brown label
val AmberIcon = Color(0xFFD97706)       // Leading icon in a warning strip

// Soft tints that pair with the accent hues used for specialty avatars.
val SkySoft = Color(0xFFE0F2FE)         // Pairs with YawarBlueLight
val GreenSoft = PaleBlue                // Blue tint for legacy green accents
val PurpleSoft = PaleBlue               // Blue tint for legacy purple accents
val PurpleAccent = YawarBlueLight       // Blue replacement for the legacy purple accent
val SlateSoft = Color(0xFFEDF2F7)       // Auth segmented-control track

// WhatsApp-style conversation canvas. Deliberately off-brand: the chat surface
// keeps a familiar messaging identity, so it is named here rather than scattered
// as raw hexes through MessagesScreen.
val ChatCanvas = Color.White            // White conversation background
val ChatBubbleIn = Color(0xFFFFFFFF)    // Received message
val ChatBubbleOut = PaleBlue            // Sent message with a light blue tint
val ChatMeta = Color(0xFF66778A)        // Timestamps, unread counters
val ChatTickRead = Color(0xFF1565C0)    // Blue double tick
val ChatSystemBg = Color(0xFFFFF7ED)    // Encryption notice pill
val ChatSystemBorder = Color(0xFFFED7AA)
val ChatSystemText = Color(0xFFC2410C)
val ChatSystemTextDeep = Color(0xFF7C2D12)
val ChatOnlineDot = YawarBlueLight      // Blue presence indicator
