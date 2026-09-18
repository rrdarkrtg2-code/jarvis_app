package com.jarvis.assistant.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.jarvis.assistant.ui.theme.JarvisAmber
import com.jarvis.assistant.ui.theme.JarvisAmberBright
import com.jarvis.assistant.ui.theme.JarvisBlue
import com.jarvis.assistant.ui.theme.JarvisCyan
import com.jarvis.assistant.ui.theme.JarvisCyanBright
import com.jarvis.assistant.ui.theme.JarvisError
import com.jarvis.assistant.ui.theme.JarvisMagenta
import com.jarvis.assistant.ui.theme.JarvisPurple

enum class OrbState {
    IDLE,
    LISTENING,
    THINKING,
    SPEAKING,
    EXECUTING,
    ERROR
}

@Composable
fun JarvisCoreOrb(
    state: OrbState,
    audioLevel: Float = 0f,
    modifier: Modifier = Modifier,
    size: Dp = 210.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "orb_anim")

    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.THINKING -> 1800
                    OrbState.SPEAKING -> 2800
                    OrbState.LISTENING -> 3500
                    else -> 6000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    val primaryColor = when (state) {
        OrbState.THINKING -> JarvisAmberBright
        OrbState.SPEAKING -> JarvisMagenta
        OrbState.ERROR -> JarvisError
        OrbState.EXECUTING -> JarvisPurple
        else -> JarvisCyan
    }

    val secondaryColor = when (state) {
        OrbState.THINKING -> JarvisAmber
        OrbState.SPEAKING -> JarvisPurple
        OrbState.ERROR -> JarvisError
        else -> JarvisBlue
    }

    val glowColor = primaryColor.copy(alpha = 0.35f)

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(size)) {
            val center = Offset(size.toPx() / 2, size.toPx() / 2)
            val baseRadius = (size.toPx() / 2) * 0.82f

            val dynamicScale = if (state == OrbState.LISTENING || state == OrbState.SPEAKING) {
                1.0f + (audioLevel.coerceIn(0f, 10f) / 30f)
            } else pulse

            val radius = baseRadius * dynamicScale

            // 1. Holographic Pedestal / Base Aura
            drawOval(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, Color.Transparent),
                    center = Offset(center.x, center.y + radius * 0.7f),
                    radius = radius * 0.9f
                ),
                topLeft = Offset(center.x - radius * 0.8f, center.y + radius * 0.45f),
                size = androidx.compose.ui.geometry.Size(radius * 1.6f, radius * 0.5f)
            )

            // 2. Outer Rotating Segmented Sci-Fi Ring
            rotate(rotation, center) {
                for (angle in listOf(0f, 90f, 180f, 270f)) {
                    drawArc(
                        color = primaryColor,
                        startAngle = angle + 10f,
                        sweepAngle = 65f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.5.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 3. Counter-rotating Middle Ring with Angular Notches
            rotate(-rotation * 0.8f, center) {
                for (angle in listOf(45f, 165f, 285f)) {
                    drawArc(
                        color = secondaryColor,
                        startAngle = angle,
                        sweepAngle = 75f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius * 0.76f, center.y - radius * 0.76f),
                        size = androidx.compose.ui.geometry.Size(radius * 1.52f, radius * 1.52f),
                        style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                    )
                }
            }

            // 4. Inner Concentric Luminous Ring
            drawCircle(
                color = primaryColor.copy(alpha = 0.5f),
                radius = radius * 0.55f,
                center = center,
                style = Stroke(width = 2.dp.toPx())
            )

            // 5. Core Disc Glow
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(primaryColor.copy(alpha = 0.5f), Color(0xFF0C1322)),
                    center = center,
                    radius = radius * 0.45f
                ),
                radius = radius * 0.45f,
                center = center
            )

            // 6. Holographic Inverted Triangle / Delta Reactor Core (Matching 9902.png)
            rotate(rotation * 0.5f, center) {
                val triRadius = radius * 0.28f
                val triPath = Path().apply {
                    val angleOffset = -90.0 // Top vertex
                    val p1X = center.x + triRadius * Math.cos(Math.toRadians(angleOffset)).toFloat()
                    val p1Y = center.y + triRadius * Math.sin(Math.toRadians(angleOffset)).toFloat()

                    val p2X = center.x + triRadius * Math.cos(Math.toRadians(angleOffset + 120.0)).toFloat()
                    val p2Y = center.y + triRadius * Math.sin(Math.toRadians(angleOffset + 120.0)).toFloat()

                    val p3X = center.x + triRadius * Math.cos(Math.toRadians(angleOffset + 240.0)).toFloat()
                    val p3Y = center.y + triRadius * Math.sin(Math.toRadians(angleOffset + 240.0)).toFloat()

                    moveTo(p1X, p1Y)
                    lineTo(p2X, p2Y)
                    lineTo(p3X, p3Y)
                    close()
                }

                drawPath(
                    path = triPath,
                    brush = Brush.linearGradient(
                        colors = listOf(primaryColor, JarvisCyanBright),
                        start = Offset(center.x, center.y - triRadius),
                        end = Offset(center.x, center.y + triRadius)
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Inner tiny glowing dot
                drawCircle(
                    color = primaryColor,
                    radius = 3.5.dp.toPx(),
                    center = center
                )
            }
        }
    }
}
