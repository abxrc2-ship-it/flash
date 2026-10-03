package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material.icons.filled.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TorchAmber
import com.example.ui.theme.TorchAmberBright

@Composable
fun StrobeSosControls(
    isStrobeActive: Boolean,
    strobeHz: Float,
    isSosActive: Boolean,
    timerSeconds: Int?,
    onToggleStrobe: () -> Unit,
    onStrobeHzChange: (Float) -> Unit,
    onToggleSos: () -> Unit,
    onOpenTimerDialog: () -> Unit,
    onCancelTimer: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Quick Action Mode Chips
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            ModeChip(
                label = "Strobe",
                icon = Icons.Default.FlashOn,
                isActive = isStrobeActive,
                activeColor = TorchAmber,
                onClick = onToggleStrobe,
                modifier = Modifier.weight(1f).testTag("strobe_mode_chip")
            )

            ModeChip(
                label = "SOS",
                icon = Icons.Default.Warning,
                isActive = isSosActive,
                activeColor = StatusRed,
                onClick = onToggleSos,
                modifier = Modifier.weight(1f).testTag("sos_mode_chip")
            )

            ModeChip(
                label = if (timerSeconds != null) "${timerSeconds / 60}:${(timerSeconds % 60).toString().padStart(2, '0')}" else "Timer",
                icon = Icons.Default.HourglassBottom,
                isActive = timerSeconds != null,
                activeColor = TorchAmberBright,
                onClick = {
                    if (timerSeconds != null) {
                        onCancelTimer()
                    } else {
                        onOpenTimerDialog()
                    }
                },
                modifier = Modifier.weight(1f).testTag("timer_mode_chip")
            )
        }

        // Strobe Slider dropdown when Strobe is active
        AnimatedVisibility(
            visible = isStrobeActive,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 12.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(DarkSurfaceVariant)
                    .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Strobe Speed",
                        color = DarkTextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = String.format("%.1f Hz", strobeHz),
                        color = TorchAmberBright,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                Slider(
                    value = strobeHz,
                    onValueChange = onStrobeHzChange,
                    valueRange = 1f..10f,
                    steps = 8,
                    colors = SliderDefaults.colors(
                        thumbColor = TorchAmberBright,
                        activeTrackColor = TorchAmber,
                        inactiveTrackColor = Color(0xFF333D52)
                    ),
                    modifier = Modifier.testTag("strobe_frequency_slider")
                )
            }
        }
    }
}

@Composable
private fun ModeChip(
    label: String,
    icon: ImageVector,
    isActive: Boolean,
    activeColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bgColor = if (isActive) activeColor.copy(alpha = 0.16f) else DarkSurfaceVariant
    val borderColor = if (isActive) activeColor else DarkBorder
    val contentColor = if (isActive) activeColor else DarkTextSecondary

    Box(
        modifier = modifier
            .height(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(bgColor)
            .border(1.dp, borderColor, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                color = contentColor,
                fontSize = 12.sp,
                fontWeight = if (isActive) FontWeight.Bold else FontWeight.Medium
            )
        }
    }
}
