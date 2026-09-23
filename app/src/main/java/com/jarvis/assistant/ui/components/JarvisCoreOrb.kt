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
import kotlin.math.cos
import kotlin.math.sin

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
    size: Dp = 230.dp
) {
    val infiniteTransition = rememberInfiniteTransition(label = "core_anim")

    val rotationFast by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(
                durationMillis = when (state) {
                    OrbState.THINKING -> 1500
                    OrbState.SPEAKING -> 2400
                    OrbState.LISTENING -> 3000
                    else -> 5000
                },
                easing = LinearEasing
            ),
            repeatMode = RepeatMode.Restart
        ),
        label = "rot_fast"
    )

    val pulse by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "core_pulse"
    )

    val primaryColor = when (state) {
        OrbState.THINKING -> JarvisAmberBright
        OrbState.SPEAKING -> Color(0xFFFF2A85)
        OrbState.ERROR -> JarvisError
        OrbState.EXECUTING -> Color(0xFFBD00FF)
        else -> Color(0xFFFF4081)
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
            val baseRadius = (size.toPx() / 2) * 0.85f

            val dynamicScale = if (state == OrbState.LISTENING || state == OrbState.SPEAKING) {
                1.0f + (audioLevel.coerceIn(0f, 10f) / 25f)
            } else pulse

            val radius = baseRadius * dynamicScale

            // 1. Quantum Holographic Outer Aura
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(glowColor, primaryColor.copy(alpha = 0.08f), Color.Transparent),
                    center = center,
                    radius = radius * 1.15f
                ),
                radius = radius * 1.15f,
                center = center
            )

            // 2. Outer Gyroscopic Compass Arc Ring
            rotate(rotationFast, center) {
                // Segmented radar arcs
                for (i in 0 until 4) {
                    val startAngle = i * 90f + 12f
                    drawArc(
                        color = primaryColor,
                        startAngle = startAngle,
                        sweepAngle = 66f,
                        useCenter = false,
                        topLeft = Offset(center.x - radius, center.y - radius),
                        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
                        style = Stroke(width = 3.2.dp.toPx(), cap = StrokeCap.Round)
                    )
                }

                // 8 Orbital Tech Nodes around the perimeter
                for (i in 0 until 8) {
                    val angle = Math.toRadians((i * 45.0 + 20.0))
                    val nodeX = center.x + (radius * cos(angle)).toFloat()
                    val nodeY = center.y + (radius * sin(angle)).toFloat()
                    drawCircle(
                        color = primaryColor,
                        radius = 3.dp.toPx(),
                        center = Offset(nodeX, nodeY)
                    )
                }
            }

            // 3. Counter-Rotating Inner Planetary Orbital Gyro
            rotate(-rotationFast * 0.75f, center) {
                val midRadius = radius * 0.76f
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.45f),
                    radius = midRadius,
                    center = center,
                    style = Stroke(width = 1.8.dp.toPx())
                )

                for (i in 0 until 6) {
                    val angle = Math.toRadians((i * 60.0))
                    val notchStartX = center.x + (midRadius * 0.94f * cos(angle)).toFloat()
                    val notchStartY = center.y + (midRadius * 0.94f * sin(angle)).toFloat()
                    val notchEndX = center.x + (midRadius * 1.06f * cos(angle)).toFloat()
                    val notchEndY = center.y + (midRadius * 1.06f * sin(angle)).toFloat()
                    drawLine(
                        color = primaryColor,
                        start = Offset(notchStartX, notchStartY),
                        end = Offset(notchEndX, notchEndY),
                        strokeWidth = 2.5.dp.toPx(),
                        cap = StrokeCap.Round
                    )
                }
            }

            // 4. Central Spherical Energy Core
            val coreRadius = radius * 0.48f
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        primaryColor.copy(alpha = 0.9f),
                        primaryColor.copy(alpha = 0.45f),
                        Color(0xFF070C18)
                    ),
                    center = center,
                    radius = coreRadius
                ),
                radius = coreRadius,
                center = center
            )

            // 5. Holographic Triangular / Diamond Quantum Lattice
            rotate(rotationFast * 0.5f, center) {
                val triRadius = coreRadius * 0.65f
                val triPath = Path().apply {
                    val p1X = center.x
                    val p1Y = center.y - triRadius

                    val p2X = center.x + triRadius * 0.866f
                    val p2Y = center.y + triRadius * 0.5f

                    val p3X = center.x - triRadius * 0.866f
                    val p3Y = center.y + triRadius * 0.5f

                    moveTo(p1X, p1Y)
                    lineTo(p2X, p2Y)
                    lineTo(p3X, p3Y)
                    close()
                }

                drawPath(
                    path = triPath,
                    brush = Brush.linearGradient(
                        colors = listOf(primaryColor, Color.White),
                        start = Offset(center.x, center.y - triRadius),
                        end = Offset(center.x, center.y + triRadius)
                    ),
                    style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                )

                // Glowing center singularity dot
                drawCircle(
                    color = Color.White,
                    radius = 4.dp.toPx(),
                    center = center
                )
            }
        }
    }
}
