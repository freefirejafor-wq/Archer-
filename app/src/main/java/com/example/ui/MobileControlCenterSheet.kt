package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.BrightnessHigh
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Navigation
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.WifiTethering
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.DeviceActionCommand
import com.example.data.model.DeviceTelemetry
import com.example.data.model.InstalledAppInfo
import com.example.data.model.MobileActionType
import com.example.ui.theme.BorderHudGreen
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonMatrixGreen
import com.example.ui.theme.NeonQuantumCyan
import com.example.ui.theme.NeonSolarAmber
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimaryHud
import com.example.ui.theme.TextSecondaryHud
import com.example.util.MobileActionExecutor

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MobileControlCenterSheet(
    telemetry: DeviceTelemetry,
    installedApps: List<InstalledAppInfo>,
    onExecuteCommand: (DeviceActionCommand) -> Unit,
    onMediaVolumeChange: (Int) -> Unit,
    onRingVolumeChange: (Int) -> Unit,
    onBrightnessChange: (Int) -> Unit,
    onLaunchInstalledApp: (InstalledAppInfo) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var mediaVolSlider by remember(telemetry.mediaVolumePercent) {
        mutableFloatStateOf(telemetry.mediaVolumePercent.toFloat())
    }
    var ringVolSlider by remember(telemetry.ringVolumePercent) {
        mutableFloatStateOf(telemetry.ringVolumePercent.toFloat())
    }
    var brightnessSlider by remember(telemetry.brightnessPercent) {
        mutableFloatStateOf(telemetry.brightnessPercent.toFloat())
    }
    var searchOrMapQuery by remember { mutableStateOf("") }
    var appFilterQuery by remember { mutableStateOf("") }

    val filteredApps = remember(installedApps, appFilterQuery) {
        if (appFilterQuery.isBlank()) {
            installedApps
        } else {
            installedApps.filter {
                it.appName.contains(appFilterQuery, ignoreCase = true) ||
                    it.packageName.contains(appFilterQuery, ignoreCase = true)
            }
        }
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ObsidianDeep
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(
                        onClick = onClose,
                        modifier = Modifier
                            .size(48.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .testTag("control_center_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = NeonMatrixGreen
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = "FULL MOBILE CONTROL CENTER",
                            style = MaterialTheme.typography.titleLarge,
                            color = NeonMatrixGreen
                        )
                        Text(
                            text = "ফোনের সম্পূর্ণ হার্ডওয়্যার, সেটিংস, মিডিয়া ও অ্যাপ কন্ট্রোল হাব",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondaryHud
                        )
                    }
                }
            }

            // 1. Live Phone Telemetry Row (Battery, RAM, Storage)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TelemetryMiniCard(
                        title = "BATTERY",
                        value = "${telemetry.batteryPercent}%",
                        subtitle = "${if (telemetry.isCharging) "Charging" else "Active"} • ${telemetry.batteryTempCelsius}°C",
                        icon = Icons.Default.BatteryChargingFull,
                        accent = NeonMatrixGreen,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniCard(
                        title = "RAM FREE",
                        value = telemetry.availableRamGb,
                        subtitle = "of ${telemetry.totalRamGb}",
                        icon = Icons.Default.Memory,
                        accent = NeonQuantumCyan,
                        modifier = Modifier.weight(1f)
                    )
                    TelemetryMiniCard(
                        title = "STORAGE",
                        value = telemetry.freeStorageGb,
                        subtitle = "of ${telemetry.totalStorageGb}",
                        icon = Icons.Default.Storage,
                        accent = NeonSolarAmber,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            // 2. Primary Hardware Switches (Flashlight Torch, Sound Mode, Vibration, Wi-Fi, Bluetooth)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(1.4.dp, NeonMatrixGreen.copy(alpha = 0.7f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text(
                            text = "HARDWARE & SYSTEM QUICK TOGGLES (হার্ডওয়্যার সুইচ)",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonMatrixGreen
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ControlToggleTile(
                                label = if (telemetry.isTorchOn) "ফ্ল্যাশলাইট: ON" else "ফ্ল্যাশলাইট: OFF",
                                icon = if (telemetry.isTorchOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                                active = telemetry.isTorchOn,
                                color = NeonGold,
                                tag = "toggle_flashlight_button",
                                onClick = {
                                    onExecuteCommand(
                                        DeviceActionCommand(
                                            type = if (telemetry.isTorchOn) {
                                                MobileActionType.TOGGLE_FLASHLIGHT_OFF
                                            } else {
                                                MobileActionType.TOGGLE_FLASHLIGHT_ON
                                            }
                                        )
                                    )
                                }
                            )
                            ControlToggleTile(
                                label = if (telemetry.isSilentMode) "সাইলেন্ট মোড: ON" else "সাউন্ড: NORMAL",
                                icon = if (telemetry.isSilentMode) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                                active = !telemetry.isSilentMode,
                                color = if (telemetry.isSilentMode) NeonCrimson else NeonMatrixGreen,
                                tag = "toggle_silent_mode_button",
                                onClick = {
                                    onExecuteCommand(
                                        DeviceActionCommand(
                                            type = if (telemetry.isSilentMode) {
                                                MobileActionType.UNMUTE_PHONE
                                            } else {
                                                MobileActionType.MUTE_PHONE
                                            }
                                        )
                                    )
                                }
                            )
                            ControlToggleTile(
                                label = "Wi-Fi প্যানেল",
                                icon = Icons.Default.Wifi,
                                active = true,
                                color = NeonQuantumCyan,
                                tag = "open_wifi_panel_button",
                                onClick = {
                                    onExecuteCommand(DeviceActionCommand(type = MobileActionType.OPEN_WIFI_PANEL))
                                }
                            )
                            ControlToggleTile(
                                label = "Bluetooth",
                                icon = Icons.Default.Bluetooth,
                                active = true,
                                color = NeonQuantumCyan,
                                tag = "open_bluetooth_button",
                                onClick = {
                                    onExecuteCommand(DeviceActionCommand(type = MobileActionType.OPEN_BLUETOOTH_SETTINGS))
                                }
                            )
                            ControlToggleTile(
                                label = "হটস্পট ও নেটওয়ার্ক",
                                icon = Icons.Default.WifiTethering,
                                active = true,
                                color = NeonSolarAmber,
                                tag = "open_hotspot_button",
                                onClick = {
                                    onExecuteCommand(DeviceActionCommand(type = MobileActionType.OPEN_HOTSPOT_SETTINGS))
                                }
                            )
                            ControlToggleTile(
                                label = "লোকেশন (GPS)",
                                icon = Icons.Default.LocationOn,
                                active = true,
                                color = NeonMatrixGreen,
                                tag = "open_location_button",
                                onClick = {
                                    onExecuteCommand(DeviceActionCommand(type = MobileActionType.OPEN_LOCATION_SETTINGS))
                                }
                            )
                            ControlToggleTile(
                                label = "ভাইব্রেট পালস",
                                icon = Icons.Default.Vibration,
                                active = true,
                                color = NeonGold,
                                tag = "vibrate_phone_button",
                                onClick = {
                                    onExecuteCommand(DeviceActionCommand(type = MobileActionType.VIBRATE_DEVICE))
                                }
                            )
                        }
                    }
                }
            }

            // 3. Real-time Sliders (Media Volume, Ringtone Volume, Screen Brightness)
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(1.2.dp, NeonQuantumCyan.copy(alpha = 0.6f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "LIVE VOLUME & BRIGHTNESS SLIDERS",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonQuantumCyan
                        )

                        // Media Volume
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, tint = NeonMatrixGreen)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "মিডিয়া সাউন্ড (${mediaVolSlider.toInt()}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimaryHud,
                                modifier = Modifier.width(130.dp)
                            )
                            Slider(
                                value = mediaVolSlider,
                                onValueChange = {
                                    mediaVolSlider = it
                                    onMediaVolumeChange(it.toInt())
                                },
                                valueRange = 0f..100f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonMatrixGreen,
                                    activeTrackColor = NeonMatrixGreen
                                )
                            )
                        }

                        // Ring Volume
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.Phone, contentDescription = null, tint = NeonQuantumCyan)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "রিংটোন সাউন্ড (${ringVolSlider.toInt()}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimaryHud,
                                modifier = Modifier.width(130.dp)
                            )
                            Slider(
                                value = ringVolSlider,
                                onValueChange = {
                                    ringVolSlider = it
                                    onRingVolumeChange(it.toInt())
                                },
                                valueRange = 0f..100f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonQuantumCyan,
                                    activeTrackColor = NeonQuantumCyan
                                )
                            )
                        }

                        // Screen Brightness
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(Icons.Default.BrightnessHigh, contentDescription = null, tint = NeonGold)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "ব্রাইটনেস (${brightnessSlider.toInt()}%)",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextPrimaryHud,
                                modifier = Modifier.width(130.dp)
                            )
                            Slider(
                                value = brightnessSlider,
                                onValueChange = {
                                    brightnessSlider = it
                                    onBrightnessChange(it.toInt())
                                },
                                valueRange = 5f..100f,
                                modifier = Modifier.weight(1f),
                                colors = SliderDefaults.colors(
                                    thumbColor = NeonGold,
                                    activeTrackColor = NeonGold
                                )
                            )
                        }
                    }
                }
            }

            // 4. Media, Social, YouTube, Maps & Phone Actions
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(1.2.dp, NeonSolarAmber.copy(alpha = 0.65f))
                ) {
                    Column(
                        modifier = Modifier.padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "MEDIA, TIKTOK, CAMERA, YOUTUBE & MAPS",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonSolarAmber
                        )

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            ControlToggleTile(
                                label = "TikTok ভিডিও আপলোড",
                                icon = Icons.Default.Share,
                                active = true,
                                color = NeonCrimson,
                                tag = "cc_tiktok_upload",
                                onClick = {
                                    onExecuteCommand(DeviceActionCommand(type = MobileActionType.UPLOAD_TIKTOK_VIDEO))
                                }
                            )
                            ControlToggleTile(
                                label = "ফোন গ্যালারি",
                                icon = Icons.Default.PhotoLibrary,
                                active = true,
                                color = NeonQuantumCyan,
                                tag = "cc_open_gallery",
                                onClick = {
                                    onExecuteCommand(DeviceActionCommand(type = MobileActionType.OPEN_GALLERY))
                                }
                            )
                            ControlToggleTile(
                                label = "ফটো ক্যামেরা",
                                icon = Icons.Default.CameraAlt,
                                active = true,
                                color = NeonMatrixGreen,
                                tag = "cc_photo_camera",
                                onClick = {
                                    onExecuteCommand(DeviceActionCommand(type = MobileActionType.OPEN_CAMERA_PHOTO))
                                }
                            )
                            ControlToggleTile(
                                label = "ভিডিও ক্যামেরা",
                                icon = Icons.Default.Videocam,
                                active = true,
                                color = NeonSolarAmber,
                                tag = "cc_video_camera",
                                onClick = {
                                    onExecuteCommand(DeviceActionCommand(type = MobileActionType.OPEN_CAMERA_VIDEO))
                                }
                            )
                            ControlToggleTile(
                                label = "অ্যালার্ম সেট",
                                icon = Icons.Default.Alarm,
                                active = true,
                                color = NeonGold,
                                tag = "cc_set_alarm",
                                onClick = {
                                    onExecuteCommand(
                                        DeviceActionCommand(
                                            type = MobileActionType.SET_ALARM,
                                            payload = "ARCHER AI Alarm",
                                            hour = 7,
                                            minute = 0
                                        )
                                    )
                                }
                            )
                            ControlToggleTile(
                                label = "ক্যালেন্ডার ইভেন্ট",
                                icon = Icons.Default.Event,
                                active = true,
                                color = NeonQuantumCyan,
                                tag = "cc_calendar_event",
                                onClick = {
                                    onExecuteCommand(
                                        DeviceActionCommand(
                                            type = MobileActionType.ADD_CALENDAR_EVENT,
                                            payload = "Important Meeting (ARCHER AI)"
                                        )
                                    )
                                }
                            )
                            ControlToggleTile(
                                label = "ফোন ডায়ালার",
                                icon = Icons.Default.Phone,
                                active = true,
                                color = NeonMatrixGreen,
                                tag = "cc_phone_dial",
                                onClick = {
                                    onExecuteCommand(DeviceActionCommand(type = MobileActionType.MAKE_CALL))
                                }
                            )
                            ControlToggleTile(
                                label = "WhatsApp মেসেজ",
                                icon = Icons.AutoMirrored.Filled.Send,
                                active = true,
                                color = NeonMatrixGreen,
                                tag = "cc_whatsapp",
                                onClick = {
                                    onExecuteCommand(
                                        DeviceActionCommand(
                                            type = MobileActionType.SEND_WHATSAPP,
                                            payload = "হ্যালো! ARCHER AI থেকে মেসেজ পাঠাচ্ছি।"
                                        )
                                    )
                                }
                            )
                        }

                        // Direct YouTube / Google Maps / Web launcher row
                        OutlinedTextField(
                            value = searchOrMapQuery,
                            onValueChange = { searchOrMapQuery = it },
                            placeholder = {
                                Text("গান, লোকেশন বা ওয়েব সার্চ লিখুন...", color = TextSecondaryHud)
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth(),
                            colors = OutlinedTextFieldDefaults.colors(
                                focusedBorderColor = NeonSolarAmber,
                                unfocusedBorderColor = BorderHudGreen,
                                focusedTextColor = TextPrimaryHud,
                                unfocusedTextColor = TextPrimaryHud
                            )
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Button(
                                onClick = {
                                    onExecuteCommand(
                                        DeviceActionCommand(
                                            type = MobileActionType.SEARCH_YOUTUBE,
                                            payload = searchOrMapQuery.ifBlank { "Bangla Hit Songs" }
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCrimson,
                                    contentColor = TextPrimaryHud
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.PlayCircleFilled, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("YouTube", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    onExecuteCommand(
                                        DeviceActionCommand(
                                            type = MobileActionType.NAVIGATE_MAPS,
                                            target = searchOrMapQuery.ifBlank { "Dhaka" }
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonMatrixGreen,
                                    contentColor = ObsidianDeep
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Navigation, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Maps", fontWeight = FontWeight.Bold)
                            }
                            Button(
                                onClick = {
                                    onExecuteCommand(
                                        DeviceActionCommand(
                                            type = MobileActionType.SEARCH_WEB,
                                            payload = searchOrMapQuery.ifBlank { "Latest AI News" }
                                        )
                                    )
                                },
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonQuantumCyan,
                                    contentColor = ObsidianDeep
                                ),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Google", fontWeight = FontWeight.Bold)
                            }
                        }
                    }
                }
            }

            // 5. Installed Apps Drawer (Launch Any App on the Phone)
            item {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = "ALL INSTALLED PHONE APPS (${installedApps.size} অ্যাপ পাওয়া গেছে)",
                        style = MaterialTheme.typography.titleSmall,
                        color = NeonMatrixGreen
                    )
                    OutlinedTextField(
                        value = appFilterQuery,
                        onValueChange = { appFilterQuery = it },
                        placeholder = { Text("ফোনের যেকোনো অ্যাপ খুঁজুন ও ওপেন করুন...", color = TextSecondaryHud) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonMatrixGreen,
                            unfocusedBorderColor = BorderHudGreen,
                            focusedTextColor = TextPrimaryHud,
                            unfocusedTextColor = TextPrimaryHud
                        )
                    )
                }
            }

            items(filteredApps, key = { it.packageName }) { appInfo ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onLaunchInstalledApp(appInfo) },
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, BorderHudGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Surface(
                                modifier = Modifier.size(36.dp),
                                shape = CircleShape,
                                color = NeonMatrixGreen.copy(alpha = 0.16f),
                                border = BorderStroke(1.dp, NeonMatrixGreen)
                            ) {
                                Box(contentAlignment = Alignment.Center) {
                                    Icon(
                                        imageVector = Icons.Default.Apps,
                                        contentDescription = null,
                                        tint = NeonMatrixGreen,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = appInfo.appName,
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimaryHud
                                )
                                Text(
                                    text = appInfo.packageName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondaryHud
                                )
                            }
                        }
                        Text(
                            text = "OPEN",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonQuantumCyan
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TelemetryMiniCard(
    title: String,
    value: String,
    subtitle: String,
    icon: ImageVector,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = ObsidianCard,
        border = BorderStroke(1.2.dp, accent.copy(alpha = 0.65f))
    ) {
        Column(modifier = Modifier.padding(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = title,
                    tint = accent,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = accent
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.titleLarge,
                color = TextPrimaryHud
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryHud,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun ControlToggleTile(
    label: String,
    icon: ImageVector,
    active: Boolean,
    color: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(21.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(21.dp),
        color = if (active) color.copy(alpha = 0.16f) else ObsidianCard,
        border = BorderStroke(1.3.dp, if (active) color else BorderHudGreen)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = color,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimaryHud,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
