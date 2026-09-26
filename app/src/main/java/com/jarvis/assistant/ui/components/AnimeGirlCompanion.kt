package com.jarvis.assistant.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.*
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.*
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlin.math.*

@Composable
fun AnimeGirlCompanion(
    isSpeaking: Boolean,
    isListening: Boolean,
    audioLevel: Float = 0f,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier,
    size: Dp = 220.dp
) {
    val trans = rememberInfiniteTransition(label = "anime")
    val breathe by trans.animateFloat(0.97f, 1.03f, infiniteRepeatable(tween(2200, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "br")
    val blink by trans.animateFloat(1f, 0.05f, infiniteRepeatable(tween(150, delayMillis = 3000, easing = LinearEasing), RepeatMode.Reverse), label = "bl")
    val mouth by trans.animateFloat(0.1f, 1f, infiniteRepeatable(tween(if (isSpeaking) 160 else 1000, easing = FastOutSlowInEasing), RepeatMode.Reverse), label = "mo")
    val rot by trans.animateFloat(0f, 360f, infiniteRepeatable(tween(6000, easing = LinearEasing), RepeatMode.Restart), label = "rot")

    val cyan = Color(0xFF00F0FF)
    val mint = Color(0xFF00FFA3)
    val skin = Color(0xFFFFF1F2)
    val blush = Color(0xFFFF85A1).copy(alpha = 0.6f)

    Box(modifier = modifier.size(size).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.size(size)) {
            val c = Offset(size.toPx() / 2, size.toPx() / 2)
            val w = size.toPx()
            val s = (w / 200f) * breathe
            val dyn = if (isSpeaking || isListening) 1.0f + (audioLevel.coerceIn(0f, 10f) / 25f) else 1.0f

            drawCircle(Brush.radialGradient(listOf(cyan.copy(alpha = 0.35f * dyn), mint.copy(alpha = 0.1f), Color.Transparent), c, (w * 0.48f) * dyn), (w * 0.48f) * dyn, c)
            rotate(rot, c) {
                drawCircle(cyan.copy(alpha = 0.5f), w * 0.44f, c, style = Stroke(2.dp.toPx(), pathEffect = PathEffect.dashPathEffect(floatArrayOf(30f, 20f))))
            }

            drawArc(Color(0xFF1E293B), 180f, 180f, false, Offset(c.x - 46f * s, c.y - 65f * s), Size(92f * s, 80f * s), style = Stroke(6.dp.toPx(), cap = StrokeCap.Round))
            drawOval(Color(0xFF1E293B), Offset(c.x - 52f * s, c.y - 15f * s), Size(14f * s, 32f * s))
            drawOval(cyan, Offset(c.x - 50f * s, c.y - 10f * s), Size(8f * s, 22f * s))
            drawOval(Color(0xFF1E293B), Offset(c.x + 38f * s, c.y - 15f * s), Size(14f * s, 32f * s))
            drawOval(cyan, Offset(c.x + 42f * s, c.y - 10f * s), Size(8f * s, 22f * s))

            drawCircle(cyan, 8.dp.toPx(), Offset(c.x - 30f * s, c.y - 50f * s))
            drawCircle(cyan, 8.dp.toPx(), Offset(c.x + 30f * s, c.y - 50f * s))

            drawOval(skin, Offset(c.x - 36f * s, c.y - 30f * s), Size(72f * s, 80f * s))
            drawOval(blush, Offset(c.x - 28f * s, c.y + 12f * s), Size(12f * s, 6f * s))
            drawOval(blush, Offset(c.x + 16f * s, c.y + 12f * s), Size(12f * s, 6f * s))

            val eh = 12f * s * blink
            if (blink > 0.15f) {
                drawOval(cyan, Offset(c.x - 22f * s, c.y + 2f * s - eh / 2), Size(10f * s, eh))
                drawCircle(Color.White, 2.5.dp.toPx(), Offset(c.x - 19f * s, c.y - 1f * s))
                drawOval(cyan, Offset(c.x + 12f * s, c.y + 2f * s - eh / 2), Size(10f * s, eh))
                drawCircle(Color.White, 2.5.dp.toPx(), Offset(c.x + 15f * s, c.y - 1f * s))
            } else {
                drawLine(Color(0xFF334155), Offset(c.x - 23f * s, c.y + 2f * s), Offset(c.x - 11f * s, c.y + 2f * s), 2.5.dp.toPx(), StrokeCap.Round)
                drawLine(Color(0xFF334155), Offset(c.x + 11f * s, c.y + 2f * s), Offset(c.x + 23f * s, c.y + 2f * s), 2.5.dp.toPx(), StrokeCap.Round)
            }

            val my = c.y + 26f * s
            if (isSpeaking) {
                val mo = 5f * s * mouth
                drawOval(Color(0xFFFF3366), Offset(c.x - 5f * s, my - mo / 2), Size(10f * s, mo + 2f))
            } else {
                drawArc(Color(0xFFFF3366), 20f, 140f, false, Offset(c.x - 6f * s, my - 2f * s), Size(12f * s, 8f * s), style = Stroke(2.dp.toPx(), cap = StrokeCap.Round))
            }
        }
    }
}
