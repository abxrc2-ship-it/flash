package com.example.ui.screens

import androidx.activity.compose.BackHandler
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PowerSettingsNew
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Widgets
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.UserSettings
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceVariant
import com.example.ui.theme.DarkTextPrimary
import com.example.ui.theme.DarkTextSecondary
import com.example.ui.theme.DarkTextTertiary
import com.example.ui.theme.TorchAmber
import com.example.ui.theme.TorchAmberBright

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    userSettings: UserSettings,
    onHapticChange: (Boolean) -> Unit,
    onOledChange: (Boolean) -> Unit,
    onAutoLaunchChange: (Boolean) -> Unit,
    onKeepScreenOnChange: (Boolean) -> Unit,
    onDefaultTimerChange: (Int) -> Unit,
    onBack: () -> Unit
) {
    BackHandler { onBack() }

    var showPrivacyDialog by remember { mutableStateOf(false) }
    var showTileInfoDialog by remember { mutableStateOf(false) }
    var showTimerDropdown by remember { mutableStateOf(false) }

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(horizontal = 20.dp)
            .verticalScroll(scrollState)
    ) {
        // Top Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onBack,
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(DarkSurfaceVariant)
                    .testTag("settings_back_button")
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Back to Flashlight",
                    tint = DarkTextPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column {
                Text(
                    text = "Settings",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = DarkTextPrimary
                )
                Text(
                    text = "Preferences & hardware options",
                    fontSize = 12.sp,
                    color = DarkTextSecondary
                )
            }
        }

        Spacer(modifier = Modifier.height(12.dp))

        // SECTION: Preferences
        SectionHeader(title = "PREFERENCES")

        SettingsCard {
            SettingsSwitchItem(
                icon = Icons.Default.Vibration,
                title = "Vibration on Toggle",
                subtitle = "Subtle tactile haptic feedback when turning ON/OFF",
                checked = userSettings.hapticEnabled,
                onCheckedChange = onHapticChange,
                testTag = "setting_switch_haptic"
            )

            SettingsDivider()

            SettingsSwitchItem(
                icon = Icons.Default.DarkMode,
                title = "Pure Black (OLED) Mode",
                subtitle = "Pitch black UI saves battery on AMOLED displays",
                checked = userSettings.oledPureBlack,
                onCheckedChange = onOledChange,
                testTag = "setting_switch_oled"
            )

            SettingsDivider()

            SettingsSwitchItem(
                icon = Icons.Default.PowerSettingsNew,
                title = "Turn ON at Launch",
                subtitle = "Instantly light up torch when opening FlashLight Pro",
                checked = userSettings.autoTurnOnOnLaunch,
                onCheckedChange = onAutoLaunchChange,
                testTag = "setting_switch_auto_launch"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: Quick Access & Battery
        SectionHeader(title = "QUICK ACCESS & BATTERY")

        SettingsCard {
            SettingsClickItem(
                icon = Icons.Default.Widgets,
                title = "Quick Settings Tile",
                subtitle = "How to add Flashlight tile to Android system tray",
                onClick = { showTileInfoDialog = true },
                testTag = "setting_tile_info"
            )

            SettingsDivider()

            SettingsClickItem(
                icon = Icons.Default.Timer,
                title = "Default Auto-Off Timer",
                subtitle = if (userSettings.autoOffMinutes > 0) "${userSettings.autoOffMinutes} minutes" else "Never (Always On)",
                onClick = { showTimerDropdown = true },
                testTag = "setting_default_timer"
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // SECTION: About & Privacy
        SectionHeader(title = "ABOUT & PRIVACY")

        SettingsCard {
            SettingsClickItem(
                icon = Icons.Default.Security,
                title = "Privacy Policy",
                subtitle = "100% offline, zero trackers, camera not used for capture",
                onClick = { showPrivacyDialog = true },
                testTag = "setting_privacy_policy"
            )

            SettingsDivider()

            SettingsInfoItem(
                icon = Icons.Default.Info,
                title = "About FlashLight Pro",
                subtitle = "Version 1.0.0 • Modern, ultra-fast & battery optimized"
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }

    // Quick Settings Tile Dialog
    if (showTileInfoDialog) {
        AlertDialog(
            onDismissRequest = { showTileInfoDialog = false },
            title = {
                Text(
                    text = "Quick Settings Tile",
                    color = DarkTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "Access FlashLight Pro instantly from any app:",
                        color = DarkTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "1. Swipe down twice from the top of your screen to open Quick Settings.\n" +
                                "2. Tap the Pencil (Edit) icon.\n" +
                                "3. Find \"FlashLight Pro\" in available tiles and drag it into your active tiles.\n" +
                                "4. Tap the tile anytime to instantly toggle your flashlight!",
                        color = DarkTextPrimary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showTileInfoDialog = false },
                    modifier = Modifier.testTag("dismiss_tile_dialog")
                ) {
                    Text("Got it", color = TorchAmberBright)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Privacy Policy Dialog
    if (showPrivacyDialog) {
        AlertDialog(
            onDismissRequest = { showPrivacyDialog = false },
            title = {
                Text(
                    text = "Privacy Guarantee",
                    color = DarkTextPrimary,
                    fontWeight = FontWeight.Bold
                )
            },
            text = {
                Column {
                    Text(
                        text = "FlashLight Pro is built with user privacy as its top priority:\n\n" +
                                "• Zero Personal Data Collected: No names, IDs, location, or usage analytics.\n" +
                                "• No Photo/Video Recording: Camera hardware is accessed solely via CameraManager.setTorchMode() for the flashlight LED.\n" +
                                "• 100% Offline: No internet connection is used or required.\n" +
                                "• No Ads or Background Trackers: Pure lightweight utility that respects your device battery and storage.",
                        color = DarkTextSecondary,
                        fontSize = 13.sp,
                        lineHeight = 20.sp
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = { showPrivacyDialog = false },
                    modifier = Modifier.testTag("dismiss_privacy_dialog")
                ) {
                    Text("Close", color = TorchAmberBright)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }

    // Default Timer Selection Dialog
    if (showTimerDropdown) {
        val options = listOf(0 to "Never (Manual Off)", 1 to "1 minute", 3 to "3 minutes", 5 to "5 minutes", 10 to "10 minutes", 15 to "15 minutes")
        AlertDialog(
            onDismissRequest = { showTimerDropdown = false },
            title = {
                Text("Select Default Timer", color = DarkTextPrimary, fontWeight = FontWeight.Bold)
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for ((mins, label) in options) {
                        val isSelected = userSettings.autoOffMinutes == mins
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(10.dp))
                                .background(if (isSelected) TorchAmber.copy(alpha = 0.2f) else DarkSurfaceVariant)
                                .border(1.dp, if (isSelected) TorchAmber else DarkBorder, RoundedCornerShape(10.dp))
                                .clickable {
                                    onDefaultTimerChange(mins)
                                    showTimerDropdown = false
                                }
                                .padding(14.dp)
                        ) {
                            Text(
                                text = label,
                                color = if (isSelected) TorchAmberBright else DarkTextPrimary,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showTimerDropdown = false }) {
                    Text("Cancel", color = DarkTextSecondary)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(20.dp)
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        color = DarkTextTertiary,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        letterSpacing = 1.2.sp,
        modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
    )
}

@Composable
private fun SettingsCard(content: @Composable () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DarkSurface)
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
    ) {
        content()
    }
}

@Composable
private fun SettingsDivider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(DarkBorder.copy(alpha = 0.6f))
    )
}

@Composable
private fun SettingsSwitchItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TorchAmberBright,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = DarkTextSecondary
            )
        }

        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color(0xFF241500),
                checkedTrackColor = TorchAmber,
                uncheckedThumbColor = DarkTextSecondary,
                uncheckedTrackColor = DarkSurfaceVariant
            ),
            modifier = Modifier.testTag(testTag)
        )
    }
}

@Composable
private fun SettingsClickItem(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    testTag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp)
            .testTag(testTag),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TorchAmberBright,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = DarkTextSecondary
            )
        }
    }
}

@Composable
private fun SettingsInfoItem(
    icon: ImageVector,
    title: String,
    subtitle: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(DarkSurfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = TorchAmberBright,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(14.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = DarkTextPrimary
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = DarkTextSecondary
            )
        }
    }
}
