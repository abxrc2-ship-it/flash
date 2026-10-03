package com.example.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.ui.FlashlightUiState
import com.example.ui.components.FlashlightButton
import com.example.ui.components.StrobeSosControls
import com.example.ui.components.TimerPickerDialog
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.DarkTextTertiary
import com.example.ui.theme.StatusAmber
import com.example.ui.theme.StatusGreen
import com.example.ui.theme.StatusRed
import com.example.ui.theme.TorchAmber
import com.example.ui.theme.TorchAmberBright

@Composable
fun MainFlashlightScreen(
    uiState: FlashlightUiState,
    onToggleTorch: () -> Unit,
    onToggleStrobe: () -> Unit,
    onStrobeHzChange: (Float) -> Unit,
    onToggleSos: () -> Unit,
    onSelectTimerMinutes: (Int) -> Unit,
    onCancelTimer: () -> Unit,
    onOpenScreenLight: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismissError: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showTimerDialog by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // TOP SECTION: Header & Actions
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Battery indicator if available
                    if (uiState.batteryLevel >= 0) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .clip(RoundedCornerShape(10.dp))
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkBorder, RoundedCornerShape(10.dp))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("battery_indicator")
                        ) {
                            Icon(
                                imageVector = Icons.Default.BatteryFull,
                                contentDescription = "Battery Level",
                                tint = if (uiState.batteryLevel <= 20) StatusRed else TorchAmberBright,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "${uiState.batteryLevel}%",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.SemiBold,
                                color = DarkTextPrimary
                            )
                        }
                    } else {
                        Spacer(modifier = Modifier.size(44.dp))
                    }

                    // Action buttons (Screen Light + Settings)
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        IconButton(
                            onClick = onOpenScreenLight,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkBorder, CircleShape)
                                .testTag("open_screen_light_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = stringResource(R.string.screen_light_mode),
                                tint = TorchAmberBright,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        IconButton(
                            onClick = onOpenSettings,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(DarkSurfaceVariant)
                                .border(1.dp, DarkBorder, CircleShape)
                                .testTag("open_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings),
                                tint = DarkTextPrimary,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // App Title and Subtitle
                Text(
                    text = stringResource(R.string.app_name),
                    fontSize = 28.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = DarkTextPrimary,
                    letterSpacing = 0.5.sp,
                    modifier = Modifier.testTag("app_title")
                )

                Text(
                    text = stringResource(R.string.app_subtitle),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = TorchAmberBright,
                    letterSpacing = 1.sp,
                    modifier = Modifier.padding(top = 2.dp).testTag("app_subtitle")
                )

                // Error / Warning Banner
                if (!uiState.isFlashAvailable) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33F59E0B))
                            .border(1.dp, StatusAmber, RoundedCornerShape(12.dp))
                            .padding(14.dp)
                            .testTag("no_flash_warning")
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = StatusAmber,
                                    modifier = Modifier.size(18.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = stringResource(R.string.no_flashlight_error),
                                    color = DarkTextPrimary,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(TorchAmber)
                                    .clickable(onClick = onOpenScreenLight)
                                    .padding(horizontal = 14.dp, vertical = 6.dp)
                                    .testTag("fallback_screen_light_button")
                            ) {
                                Text(
                                    text = "Use Screen Light Instead",
                                    color = Color(0xFF1E1400),
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                } else if (uiState.errorMessage != null) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 16.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0x33EF4444))
                            .border(1.dp, StatusRed, RoundedCornerShape(12.dp))
                            .padding(horizontal = 14.dp, vertical = 10.dp)
                            .testTag("error_banner")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = uiState.errorMessage,
                                color = DarkTextPrimary,
                                fontSize = 12.sp,
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(
                                onClick = onDismissError,
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Close,
                                    contentDescription = "Dismiss error",
                                    tint = DarkTextPrimary,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
            }

            // CENTER SECTION: Large Circular Flashlight Button & Status
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(vertical = 12.dp)
            ) {
                FlashlightButton(
                    isTorchOn = uiState.isTorchOn || uiState.isStrobeActive || uiState.isSosActive,
                    onClick = onToggleTorch,
                    enabled = uiState.isFlashAvailable
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Status text (below button)
                val statusText = when {
                    uiState.isSosActive -> "SOS Emergency Signal Active"
                    uiState.isStrobeActive -> "Strobe Mode (${String.format("%.1f", uiState.strobeHz)} Hz)"
                    uiState.isTorchOn -> stringResource(R.string.flashlight_on)
                    else -> stringResource(R.string.flashlight_off)
                }

                val statusColor = when {
                    uiState.isSosActive -> StatusRed
                    uiState.isStrobeActive || uiState.isTorchOn -> StatusGreen
                    else -> DarkTextSecondary
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    // Status indicator dot
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(statusColor)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = statusText,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = DarkTextPrimary,
                        modifier = Modifier.testTag("status_text")
                    )
                }

                // Auto-off timer badge if active
                if (uiState.timerRemainingSeconds != null) {
                    val mins = uiState.timerRemainingSeconds / 60
                    val secs = uiState.timerRemainingSeconds % 60
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Auto-off in ${mins}:${secs.toString().padStart(2, '0')}",
                        fontSize = 12.sp,
                        color = TorchAmberBright,
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            // BOTTOM SECTION: Modes, Controls & About Section
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Strobe, SOS, and Timer quick controls
                if (uiState.isFlashAvailable) {
                    StrobeSosControls(
                        isStrobeActive = uiState.isStrobeActive,
                        strobeHz = uiState.strobeHz,
                        isSosActive = uiState.isSosActive,
                        timerSeconds = uiState.timerRemainingSeconds,
                        onToggleStrobe = onToggleStrobe,
                        onStrobeHzChange = onStrobeHzChange,
                        onToggleSos = onToggleSos,
                        onOpenTimerDialog = { showTimerDialog = true },
                        onCancelTimer = onCancelTimer
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                // About section summary footer
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.clickable(onClick = onOpenSettings)
                ) {
                    Icon(
                        imageVector = Icons.Default.Info,
                        contentDescription = "About FlashLight Pro",
                        tint = DarkTextTertiary,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "FlashLight Pro v1.0 • Instant & Battery Safe",
                        color = DarkTextTertiary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Normal
                    )
                }
            }
        }
    }

    // Auto-off Timer Dialog
    if (showTimerDialog) {
        TimerPickerDialog(
            onDismiss = { showTimerDialog = false },
            onSelectMinutes = onSelectTimerMinutes
        )
    }
}
