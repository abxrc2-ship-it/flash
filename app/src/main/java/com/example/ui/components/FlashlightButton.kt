package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.ripple
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.theme.TorchAmber
import com.example.ui.theme.TorchAmberBright
import com.example.ui.theme.TorchAmberGlow

@Composable
fun FlashlightButton(
    isTorchOn: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }

    // Glow transition
    val glowAlpha by animateFloatAsState(
        targetValue = if (isTorchOn) 1f else 0f,
        animationSpec = tween(durationMillis = 280, easing = FastOutSlowInEasing),
        label = "glowAlpha"
    )

    // Breathing pulse when active
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val buttonScale by animateFloatAsState(
        targetValue = if (isTorchOn) 1.02f else 1.0f,
        animationSpec = tween(250),
        label = "buttonScale"
    )

    // Button surface gradient
    val buttonBgColorStart by animateColorAsState(
        targetValue = if (isTorchOn) Color(0xFF2E2405) else Color(0xFF161A24),
        animationSpec = tween(280),
        label = "btnBgStart"
    )
    val buttonBgColorEnd by animateColorAsState(
        targetValue = if (isTorchOn) Color(0xFF1B1400) else Color(0xFF0F121A),
        animationSpec = tween(280),
        label = "btnBgEnd"
    )

    // Border highlight
    val borderColor by animateColorAsState(
        targetValue = if (isTorchOn) TorchAmberBright else Color(0xFF2D3547),
        animationSpec = tween(280),
        label = "borderColor"
    )

    // Text & icon tint
    val iconColor by animateColorAsState(
        targetValue = if (isTorchOn) TorchAmberBright else Color(0xFF64748B),
        animationSpec = tween(280),
        label = "iconColor"
    )

    val toggleDesc = if (isTorchOn) {
        stringResource(R.string.flashlight_on)
    } else {
        stringResource(R.string.flashlight_off)
    }

    Box(
        modifier = modifier
            .size(260.dp)
            .testTag("flashlight_toggle_button_container"),
        contentAlignment = Alignment.Center
    ) {
        // Outer ambient glow ring
        if (glowAlpha > 0.01f) {
            Box(
                modifier = Modifier
                    .size(260.dp)
                    .scale(pulseScale)
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    TorchAmberGlow.copy(alpha = 0.55f * glowAlpha),
                                    TorchAmberGlow.copy(alpha = 0.25f * glowAlpha),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.minDimension / 1.7f
                            )
                        )
                    }
            )
        }

        // Concentric outer bezel ring
        Box(
            modifier = Modifier
                .size(218.dp)
                .drawBehind {
                    drawCircle(
                        color = if (isTorchOn) Color(0x33FFB800) else Color(0xFF1F2533),
                        style = Stroke(width = 2.dp.toPx())
                    )
                }
        )

        // Main touch button
        Box(
            modifier = Modifier
                .size(200.dp)
                .scale(buttonScale)
                .clip(CircleShape)
                .background(
                    brush = Brush.verticalGradient(
                        colors = listOf(buttonBgColorStart, buttonBgColorEnd)
                    )
                )
                .border(
                    width = if (isTorchOn) 2.5.dp else 1.5.dp,
                    color = borderColor,
                    shape = CircleShape
                )
                .semantics {
                    role = Role.Switch
                    contentDescription = toggleDesc
                }
                .testTag("flashlight_toggle_button")
                .clickable(
                    interactionSource = interactionSource,
                    indication = ripple(bounded = true, radius = 100.dp, color = TorchAmber),
                    enabled = enabled,
                    onClick = onClick
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Vector torch icon drawn directly on Canvas for high aesthetic fidelity
                TorchCanvasIcon(
                    isTorchOn = isTorchOn,
                    tint = iconColor,
                    modifier = Modifier.size(64.dp)
                )

                Spacer(modifier = Modifier.height(10.dp))

                // ON/OFF State indicator
                Text(
                    text = if (isTorchOn) stringResource(R.string.status_on) else stringResource(R.string.status_off),
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isTorchOn) TorchAmberBright else Color(0xFF94A3B8),
                    letterSpacing = 2.sp
                )
            }
        }
    }
}

@Composable
private fun TorchCanvasIcon(
    isTorchOn: Boolean,
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier) {
        val w = size.width
        val h = size.height
        val cx = w / 2f

        // Torch body
        val bodyWidth = w * 0.32f
        val bodyHeight = h * 0.44f
        val bodyTop = h * 0.44f
        val bodyLeft = cx - bodyWidth / 2f

        // Flashlight head (wider trapezoid)
        val headTop = h * 0.22f
        val headBottom = bodyTop
        val headTopWidth = w * 0.46f
        val headBottomWidth = bodyWidth

        val headPath = Path().apply {
            moveTo(cx - headTopWidth / 2f, headTop)
            lineTo(cx + headTopWidth / 2f, headTop)
            lineTo(cx + headBottomWidth / 2f, headBottom)
            lineTo(cx - headBottomWidth / 2f, headBottom)
            close()
        }

        // Draw flashlight head
        drawPath(
            path = headPath,
            color = tint
        )

        // Draw flashlight body
        drawRoundRect(
            color = tint,
            topLeft = Offset(bodyLeft, bodyTop),
            size = androidx.compose.ui.geometry.Size(bodyWidth, bodyHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(6.dp.toPx())
        )

        // Switch button on torch body
        val switchWidth = bodyWidth * 0.4f
        val switchHeight = bodyHeight * 0.22f
        val switchTop = bodyTop + bodyHeight * 0.25f
        drawRoundRect(
            color = if (isTorchOn) Color(0xFF1E1600) else Color(0xFF0F172A),
            topLeft = Offset(cx - switchWidth / 2f, switchTop),
            size = androidx.compose.ui.geometry.Size(switchWidth, switchHeight),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3.dp.toPx())
        )

        // If ON, draw luminous rays emitting upwards!
        if (isTorchOn) {
            val rayColor = TorchAmberBright
            val rayLength = h * 0.18f
            val rayStroke = 2.5.dp.toPx()

            // Center ray
            drawLine(
                color = rayColor,
                start = Offset(cx, headTop - 4.dp.toPx()),
                end = Offset(cx, headTop - 4.dp.toPx() - rayLength),
                strokeWidth = rayStroke,
                cap = StrokeCap.Round
            )
            // Left diagonal ray
            drawLine(
                color = rayColor,
                start = Offset(cx - headTopWidth * 0.32f, headTop - 3.dp.toPx()),
                end = Offset(cx - headTopWidth * 0.55f, headTop - 3.dp.toPx() - rayLength * 0.85f),
                strokeWidth = rayStroke,
                cap = StrokeCap.Round
            )
            // Right diagonal ray
            drawLine(
                color = rayColor,
                start = Offset(cx + headTopWidth * 0.32f, headTop - 3.dp.toPx()),
                end = Offset(cx + headTopWidth * 0.55f, headTop - 3.dp.toPx() - rayLength * 0.85f),
                strokeWidth = rayStroke,
                cap = StrokeCap.Round
            )
        }
    }
}
