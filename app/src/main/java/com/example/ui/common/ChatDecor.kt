package com.example.ui.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.BorderColor
import com.example.ui.theme.ChatBubbleIn
import com.example.ui.theme.ChatBubbleOut
import com.example.ui.theme.ChatCanvas
import com.example.ui.theme.Ink

/**
 * Scatters a faint, deterministic doodle over the chat canvas. The wallpaper is
 * the most recognisable part of WhatsApp's look; a flat fill read as a plain grey
 * box instead. Drawn under the messages rather than as an asset so it scales to
 * any thread length and needs no new drawable.
 */
fun Modifier.chatDoodleWallpaper(tint: Color = Color.Black): Modifier = drawBehind {
    val step = 74f
    val mark = tint.copy(alpha = 0.045f)
    var row = 0
    var y = step / 2f
    while (y < size.height + step) {
        // Offset alternate rows so the result does not read as a grid.
        var x = step / 2f + (if (row % 2 == 0) 0f else step / 2f)
        var col = 0
        while (x < size.width + step) {
            when ((row * 7 + col * 3) % 4) {
                0 -> drawCircle(color = mark, radius = 5f, center = Offset(x, y), style = Stroke(width = 1.6f))
                1 -> {
                    drawLine(mark, Offset(x - 5f, y), Offset(x + 5f, y), strokeWidth = 1.6f)
                    drawLine(mark, Offset(x, y - 5f), Offset(x, y + 5f), strokeWidth = 1.6f)
                }
                2 -> {
                    val p = Path().apply {
                        moveTo(x - 5f, y + 4f)
                        lineTo(x, y - 5f)
                        lineTo(x + 5f, y + 4f)
                        close()
                    }
                    drawPath(p, mark, style = Stroke(width = 1.6f))
                }
                else -> {
                    drawLine(mark, Offset(x - 5f, y - 3f), Offset(x + 5f, y - 3f), strokeWidth = 1.6f)
                    drawLine(mark, Offset(x - 5f, y + 3f), Offset(x + 5f, y + 3f), strokeWidth = 1.6f)
                }
            }
            x += step
            col++
        }
        y += step
        row++
    }
}

/**
 * The chat surface keeps its own palette rather than following the app's
 * material roles, because the WhatsApp look is deliberate. These are that
 * look's real dark-theme values, so the beige-and-green canvas does not glare
 * when the system switches to dark.
 */
@Immutable
data class ChatPalette(
    val canvas: Color,
    val bubbleIn: Color,
    val bubbleOut: Color,
    val bubbleBorder: Color,
    val bubbleText: Color,
    val wallpaperTint: Color
)

private val ChatLight = ChatPalette(
    canvas = ChatCanvas,
    bubbleIn = ChatBubbleIn,
    bubbleOut = ChatBubbleOut,
    bubbleBorder = BorderColor,
    bubbleText = Ink,
    wallpaperTint = Color.Black
)

private val ChatDark = ChatPalette(
    canvas = Color(0xFF0B141F),
    bubbleIn = Color(0xFF202C39),
    bubbleOut = Color(0xFF005C5B),
    bubbleBorder = Color(0xFF2A3945),
    bubbleText = Color(0xFFE9EDEF),
    wallpaperTint = Color.White
)

@Composable
fun rememberChatPalette(): ChatPalette =
    if (isSystemInDarkTheme()) ChatDark else ChatLight

/** Floating date pill that separates stretches of a conversation. */
@Composable
fun ChatDateDivider(
    label: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = label.uppercase(),
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier
                .background(MaterialTheme.colorScheme.surface, RoundedCornerShape(8.dp))
                .padding(horizontal = 12.dp, vertical = 5.dp)
        )
    }
}
