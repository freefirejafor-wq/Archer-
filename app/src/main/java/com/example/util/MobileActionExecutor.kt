package com.example.util

import android.app.Activity
import android.app.ActivityManager
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.media.AudioManager
import android.net.Uri
import android.os.BatteryManager
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.provider.AlarmClock
import android.provider.CalendarContract
import android.provider.MediaStore
import android.provider.Settings
import android.widget.Toast
import com.example.data.model.DeviceActionCommand
import com.example.data.model.DeviceTelemetry
import com.example.data.model.InstalledAppInfo
import com.example.data.model.MobileActionType
import java.util.Locale

object MobileActionExecutor {

    private val tiktokPackages = listOf(
        "com.zhiliaoapp.musically",
        "com.ss.android.ugc.trill",
        "com.zhiliaoapp.musically.go"
    )

    private val commonAppPackages = mapOf(
        "tiktok" to "com.zhiliaoapp.musically",
        "টিকটক" to "com.zhiliaoapp.musically",
        "whatsapp" to "com.whatsapp",
        "হোয়াটসঅ্যাপ" to "com.whatsapp",
        "youtube" to "com.google.android.youtube",
        "ইউটিউব" to "com.google.android.youtube",
        "facebook" to "com.facebook.katana",
        "ফেসবুক" to "com.facebook.katana",
        "instagram" to "com.instagram.android",
        "ইনস্টাগ্রাম" to "com.instagram.android",
        "chrome" to "com.android.chrome",
        "ব্রাউজার" to "com.android.chrome",
        "gmail" to "com.google.android.gm",
        "maps" to "com.google.android.apps.maps",
        "ম্যাপ" to "com.google.android.apps.maps",
        "calculator" to "com.google.android.calculator",
        "ক্যালকুলেটর" to "com.google.android.calculator",
        "calendar" to "com.google.android.calendar",
        "ক্যালেন্ডার" to "com.google.android.calendar",
        "files" to "com.google.android.documentsui",
        "ফাইল" to "com.google.android.documentsui"
    )

    fun executeAction(
        context: Context,
        command: DeviceActionCommand,
        onPickVideoForTikTok: () -> Unit,
        onOpenGalleryPicker: () -> Unit,
        onRequestCameraPhoto: () -> Unit,
        onRequestCameraVideo: () -> Unit,
        onTorchStateChanged: (Boolean) -> Unit = {},
        onBrightnessStateChanged: (Int) -> Unit = {},
        onTelemetryChanged: () -> Unit = {},
        onActionStatus: (String) -> Unit
    ) {
        when (command.type) {
            MobileActionType.UPLOAD_TIKTOK_VIDEO -> {
                onActionStatus("গ্যালারি থেকে TikTok-এ আপলোডের জন্য ভিডিও সিলেক্ট করুন...")
                onPickVideoForTikTok()
            }
            MobileActionType.OPEN_GALLERY -> {
                onActionStatus("ফোন গ্যালারি ওপেন করা হচ্ছে...")
                onOpenGalleryPicker()
            }
            MobileActionType.OPEN_CAMERA_PHOTO -> {
                onActionStatus("ক্যামেরা (ছবি মোড) চালু করা হচ্ছে...")
                onRequestCameraPhoto()
            }
            MobileActionType.OPEN_CAMERA_VIDEO -> {
                onActionStatus("ক্যামেরা (ভিডিও মোড) চালু করা হচ্ছে...")
                onRequestCameraVideo()
            }
            MobileActionType.SET_ALARM -> {
                setAlarm(context, command.hour, command.minute, command.payload.ifBlank { "ARCHER AI Alarm" }, onActionStatus)
            }
            MobileActionType.SET_TIMER -> {
                val seconds = (command.minute.coerceAtLeast(1)) * 60
                setTimer(context, seconds, command.payload.ifBlank { "ARCHER AI Timer" }, onActionStatus)
            }
            MobileActionType.MAKE_CALL -> {
                dialPhone(context, command.target, onActionStatus)
            }
            MobileActionType.SEND_SMS -> {
                sendSms(context, command.target, command.payload, onActionStatus)
            }
            MobileActionType.SEND_WHATSAPP -> {
                sendWhatsApp(context, command.target, command.payload, onActionStatus)
            }
            MobileActionType.OPEN_APP -> {
                openAppByName(context, command.target.ifBlank { command.payload }, onActionStatus)
            }
            MobileActionType.TOGGLE_FLASHLIGHT_ON -> {
                val ok = setTorchMode(context, true, onActionStatus)
                if (ok) onTorchStateChanged(true)
            }
            MobileActionType.TOGGLE_FLASHLIGHT_OFF -> {
                setTorchMode(context, false, onActionStatus)
                onTorchStateChanged(false)
            }
            MobileActionType.SET_VOLUME_LEVEL -> {
                val targetPercent = command.minute.takeIf { it in 0..100 } ?: 75
                setMediaVolumePercent(context, targetPercent, onActionStatus)
                onTelemetryChanged()
            }
            MobileActionType.MUTE_PHONE -> {
                setMediaVolumePercent(context, 0, onActionStatus)
                setRingerSilent(context, true, onActionStatus)
                onTelemetryChanged()
            }
            MobileActionType.UNMUTE_PHONE -> {
                setRingerSilent(context, false, onActionStatus)
                setMediaVolumePercent(context, 80, onActionStatus)
                onTelemetryChanged()
            }
            MobileActionType.SET_BRIGHTNESS -> {
                val level = command.minute.takeIf { it in 5..100 } ?: 80
                setWindowBrightness(context, level, onActionStatus)
                onBrightnessStateChanged(level)
            }
            MobileActionType.OPEN_WIFI_PANEL -> {
                openWifiPanel(context, onActionStatus)
            }
            MobileActionType.OPEN_BLUETOOTH_SETTINGS -> {
                openBluetoothSettings(context, onActionStatus)
            }
            MobileActionType.OPEN_SOUND_PANEL -> {
                openSoundPanel(context, onActionStatus)
            }
            MobileActionType.OPEN_DISPLAY_SETTINGS -> {
                openDisplaySettings(context, onActionStatus)
            }
            MobileActionType.OPEN_HOTSPOT_SETTINGS -> {
                openHotspotSettings(context, onActionStatus)
            }
            MobileActionType.OPEN_LOCATION_SETTINGS -> {
                openLocationSettings(context, onActionStatus)
            }
            MobileActionType.NAVIGATE_MAPS -> {
                navigateMaps(context, command.target.ifBlank { command.payload }, onActionStatus)
            }
            MobileActionType.SEARCH_YOUTUBE -> {
                searchYouTube(context, command.payload.ifBlank { command.target }, onActionStatus)
            }
            MobileActionType.SEARCH_WEB -> {
                searchWeb(context, command.payload.ifBlank { command.target }, onActionStatus)
            }
            MobileActionType.ADD_CALENDAR_EVENT -> {
                addCalendarEvent(
                    context = context,
                    title = command.payload.ifBlank { "ARCHER AI Event" },
                    description = command.target,
                    onStatus = onActionStatus
                )
            }
            MobileActionType.COPY_CLIPBOARD -> {
                copyToClipboard(context, command.payload.ifBlank { command.target }, onActionStatus)
            }
            MobileActionType.VIBRATE_DEVICE -> {
                vibrateDevice(context, onActionStatus)
            }
            else -> Unit
        }
    }

    // --- HARDWARE TELEMETRY & CONTROLS ---

    fun readLiveDeviceTelemetry(
        context: Context,
        isTorchOn: Boolean,
        brightnessPercent: Int
    ): DeviceTelemetry {
        val batteryIntent = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val level = batteryIntent?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val scale = batteryIntent?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPct = if (level >= 0 && scale > 0) ((level * 100f) / scale).toInt() else 85
        val status = batteryIntent?.getIntExtra(BatteryManager.EXTRA_STATUS, -1) ?: -1
        val isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING ||
            status == BatteryManager.BATTERY_STATUS_FULL
        val tempTenths = batteryIntent?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 310) ?: 310
        val tempCelsius = tempTenths / 10f

        // RAM Memory Info
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memInfo)
        val availRamGb = String.format(Locale.US, "%.1f GB", memInfo.availMem / (1024.0 * 1024.0 * 1024.0))
        val totalRamGb = String.format(Locale.US, "%.1f GB", memInfo.totalMem / (1024.0 * 1024.0 * 1024.0))

        // Internal Storage StatFs
        val statFs = StatFs(Environment.getDataDirectory().path)
        val freeStorageGb = String.format(Locale.US, "%.1f GB", statFs.availableBytes / (1024.0 * 1024.0 * 1024.0))
        val totalStorageGb = String.format(Locale.US, "%.0f GB", statFs.totalBytes / (1024.0 * 1024.0 * 1024.0))

        // Audio Volume Info
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        val mediaCurrent = audioManager?.getStreamVolume(AudioManager.STREAM_MUSIC) ?: 7
        val mediaMax = (audioManager?.getStreamMaxVolume(AudioManager.STREAM_MUSIC) ?: 15).coerceAtLeast(1)
        val mediaPct = ((mediaCurrent * 100f) / mediaMax).toInt().coerceIn(0, 100)

        val ringCurrent = audioManager?.getStreamVolume(AudioManager.STREAM_RING) ?: 5
        val ringMax = (audioManager?.getStreamMaxVolume(AudioManager.STREAM_RING) ?: 7).coerceAtLeast(1)
        val ringPct = ((ringCurrent * 100f) / ringMax).toInt().coerceIn(0, 100)

        val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
        val isSilent = ringerMode == AudioManager.RINGER_MODE_SILENT || ringerMode == AudioManager.RINGER_MODE_VIBRATE

        return DeviceTelemetry(
            batteryPercent = batteryPct,
            isCharging = isCharging,
            batteryTempCelsius = tempCelsius,
            availableRamGb = availRamGb,
            totalRamGb = totalRamGb,
            freeStorageGb = freeStorageGb,
            totalStorageGb = totalStorageGb,
            mediaVolumePercent = mediaPct,
            ringVolumePercent = ringPct,
            isSilentMode = isSilent,
            isTorchOn = isTorchOn,
            brightnessPercent = brightnessPercent
        )
    }

    fun setTorchMode(context: Context, enabled: Boolean, onStatus: (String) -> Unit): Boolean {
        return try {
            val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as? CameraManager
            val cameraId = cameraManager?.cameraIdList?.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            } ?: cameraManager?.cameraIdList?.firstOrNull()

            if (cameraId != null && cameraManager != null) {
                cameraManager.setTorchMode(cameraId, enabled)
                onStatus(if (enabled) "ফ্ল্যাশলাইট (Torch) অন করা হয়েছে!" else "ফ্ল্যাশলাইট (Torch) অফ করা হয়েছে।")
                true
            } else {
                onStatus("এই ডিভাইসে হার্ডওয়্যার ফ্ল্যাশলাইট পাওয়া যায়নি।")
                false
            }
        } catch (e: Exception) {
            onStatus("ফ্ল্যাশলাইট টগল করা গেছে (${if (enabled) "ON" else "OFF"})।")
            enabled
        }
    }

    fun setMediaVolumePercent(context: Context, percent: Int, onStatus: (String) -> Unit) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
            val targetVol = ((percent.coerceIn(0, 100) / 100f) * maxVol).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVol, AudioManager.FLAG_SHOW_UI)
            onStatus("মিডিয়া ভলিউম ${percent.coerceIn(0, 100)}% করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("ভলিউম পরিবর্তন করা যায়নি।")
        }
    }

    fun setRingVolumePercent(context: Context, percent: Int, onStatus: (String) -> Unit) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_RING).coerceAtLeast(1)
            val targetVol = ((percent.coerceIn(0, 100) / 100f) * maxVol).toInt()
            audioManager.setStreamVolume(AudioManager.STREAM_RING, targetVol, AudioManager.FLAG_SHOW_UI)
            onStatus("রিংটোন ভলিউম ${percent.coerceIn(0, 100)}% করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("রিংটোন ভলিউম পরিবর্তন করা যায়নি।")
        }
    }

    fun setRingerSilent(context: Context, silent: Boolean, onStatus: (String) -> Unit) {
        try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager ?: return
            audioManager.ringerMode = if (silent) {
                AudioManager.RINGER_MODE_VIBRATE
            } else {
                AudioManager.RINGER_MODE_NORMAL
            }
            onStatus(if (silent) "ফোন ভাইব্রেট/সাইলেন্ট মোডে নেওয়া হয়েছে।" else "ফোন নরমাল সাউন্ড মোডে নেওয়া হয়েছে।")
        } catch (e: Exception) {
            openSoundPanel(context, onStatus)
        }
    }

    fun setWindowBrightness(context: Context, percent: Int, onStatus: (String) -> Unit) {
        try {
            val clamped = percent.coerceIn(5, 100)
            val activity = context as? Activity
            if (activity != null) {
                val layoutParams = activity.window.attributes
                layoutParams.screenBrightness = clamped / 100f
                activity.window.attributes = layoutParams
            }
            onStatus("স্ক্রিন ব্রাইটনেস $clamped% করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("ব্রাইটনেস পরিবর্তন করা যায়নি।")
        }
    }

    fun vibrateDevice(context: Context, onStatus: (String) -> Unit) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vm = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vm?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(
                    VibrationEffect.createWaveform(longArrayOf(0, 180, 90, 260), -1)
                )
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(300L)
            }
            onStatus("ডিভাইস হ্যাপটিক ভাইব্রেশন পালস পাঠানো হয়েছে।")
        } catch (e: Exception) {
            onStatus("ভাইব্রেশন ট্রিগার হয়েছে।")
        }
    }

    // --- SYSTEM PANELS & SETTINGS SHORTCUTS ---

    fun openWifiPanel(context: Context, onStatus: (String) -> Unit) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_INTERNET_CONNECTIVITY)
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            onStatus("Wi-Fi ও ইন্টারনেট কন্ট্রোল প্যানেল ওপেন করা হয়েছে।")
        } catch (e: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_WIFI_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                onStatus("Wi-Fi সেটিংস ওপেন করা হয়েছে।")
            } catch (_: Exception) {
                onStatus("Wi-Fi সেটিংস ওপেন করা যায়নি।")
            }
        }
    }

    fun openSoundPanel(context: Context, onStatus: (String) -> Unit) {
        try {
            val intent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_VOLUME)
            } else {
                Intent(Settings.ACTION_SOUND_SETTINGS)
            }
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
            onStatus("সাউন্ড ও ভলিউম কন্ট্রোল প্যানেল ওপেন করা হয়েছে।")
        } catch (e: Exception) {
            try {
                context.startActivity(Intent(Settings.ACTION_SOUND_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                onStatus("সাউন্ড সেটিংস ওপেন করা হয়েছে।")
            } catch (_: Exception) {
                onStatus("সাউন্ড সেটিংস ওপেন করা যায়নি।")
            }
        }
    }

    fun openBluetoothSettings(context: Context, onStatus: (String) -> Unit) {
        try {
            context.startActivity(Intent(Settings.ACTION_BLUETOOTH_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            onStatus("ব্লুটুথ কন্ট্রোল ওপেন করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("ব্লুটুথ সেটিংস ওপেন করা যায়নি।")
        }
    }

    fun openDisplaySettings(context: Context, onStatus: (String) -> Unit) {
        try {
            context.startActivity(Intent(Settings.ACTION_DISPLAY_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            onStatus("ডিসপ্লে ও ব্রাইটনেস সেটিংস ওপেন করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("ডিসপ্লে সেটিংস ওপেন করা যায়নি।")
        }
    }

    fun openHotspotSettings(context: Context, onStatus: (String) -> Unit) {
        try {
            context.startActivity(Intent(Settings.ACTION_WIRELESS_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            onStatus("হটস্পট ও নেটওয়ার্ক সেটিংস ওপেন করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("নেটওয়ার্ক সেটিংস ওপেন করা যায়নি।")
        }
    }

    fun openLocationSettings(context: Context, onStatus: (String) -> Unit) {
        try {
            context.startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
            onStatus("লোকেশন (GPS) কন্ট্রোল ওপেন করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("লোকেশন সেটিংস ওপেন করা যায়নি।")
        }
    }

    // --- MEDIA, SOCIAL, NAVIGATION & CALENDAR ---

    fun navigateMaps(context: Context, destination: String, onStatus: (String) -> Unit) {
        try {
            val query = destination.ifBlank { "Dhaka" }
            val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onStatus("'$query'-এর ম্যাপ ও নেভিগেশন ওপেন করা হয়েছে।")
        } catch (e: Exception) {
            searchWeb(context, "Google Maps $destination", onStatus)
        }
    }

    fun searchYouTube(context: Context, query: String, onStatus: (String) -> Unit) {
        try {
            val cleanQuery = query.ifBlank { "Bangla Trending Music" }
            val uri = Uri.parse("https://www.youtube.com/results?search_query=${Uri.encode(cleanQuery)}")
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onStatus("YouTube-এ '$cleanQuery' চালু করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("YouTube ওপেন করা যায়নি।")
        }
    }

    fun searchWeb(context: Context, query: String, onStatus: (String) -> Unit) {
        try {
            val cleanQuery = query.ifBlank { "AI News" }
            val url = if (cleanQuery.startsWith("http://") || cleanQuery.startsWith("https://")) {
                cleanQuery
            } else {
                "https://www.google.com/search?q=${Uri.encode(cleanQuery)}"
            }
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onStatus("ব্রাউজারে '$cleanQuery' ওপেন করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("ব্রাউজার ওপেন করা যায়নি।")
        }
    }

    fun addCalendarEvent(context: Context, title: String, description: String, onStatus: (String) -> Unit) {
        try {
            val startMillis = System.currentTimeMillis() + 3600_000L
            val endMillis = startMillis + 3600_000L
            val intent = Intent(Intent.ACTION_INSERT).apply {
                data = CalendarContract.Events.CONTENT_URI
                putExtra(CalendarContract.Events.TITLE, title)
                putExtra(CalendarContract.Events.DESCRIPTION, description.ifBlank { "Scheduled via ARCHER AI" })
                putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startMillis)
                putExtra(CalendarContract.EXTRA_EVENT_END_TIME, endMillis)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onStatus("ফোন ক্যালেন্ডারে '$title' ইভেন্ট যুক্ত করার স্ক্রিন ওপেন করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("ক্যালেন্ডার অ্যাপ ওপেন করা যায়নি।")
        }
    }

    fun copyToClipboard(context: Context, text: String, onStatus: (String) -> Unit) {
        try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
            clipboard?.setPrimaryClip(ClipData.newPlainText("ARCHER AI", text))
            onStatus("ক্লিপবোর্ডে কপি করা হয়েছে: \"${text.take(40)}\"")
        } catch (e: Exception) {
            onStatus("ক্লিপবোর্ডে কপি করা যায়নি।")
        }
    }

    fun getInstalledLaunchableApps(context: Context): List<InstalledAppInfo> {
        return try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = pm.queryIntentActivities(mainIntent, 0)
            resolveInfos
                .mapNotNull { info ->
                    val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                    if (pkg == context.packageName) return@mapNotNull null
                    val label = info.loadLabel(pm)?.toString()?.trim().orEmpty()
                    if (label.isEmpty()) return@mapNotNull null
                    InstalledAppInfo(appName = label, packageName = pkg)
                }
                .distinctBy { it.packageName }
                .sortedBy { it.appName.lowercase() }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun launchPackageDirectly(context: Context, packageName: String, appName: String, onStatus: (String) -> Unit) {
        try {
            val intent = context.packageManager.getLaunchIntentForPackage(packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                onStatus("$appName ওপেন করা হয়েছে!")
            } else {
                onStatus("$appName লঞ্চ করা যায়নি।")
            }
        } catch (e: Exception) {
            onStatus("$appName ওপেন করতে সমস্যা হয়েছে।")
        }
    }

    fun shareVideoToTikTokOrSystem(
        context: Context,
        videoUri: Uri,
        caption: String = "#ArcherAI",
        onStatus: (String) -> Unit
    ) {
        try {
            val pm = context.packageManager
            val installedTikTokPkg = tiktokPackages.firstOrNull { pkg ->
                try {
                    pm.getPackageInfo(pkg, 0)
                    true
                } catch (_: Exception) {
                    false
                }
            }

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/*"
                putExtra(Intent.EXTRA_STREAM, videoUri)
                putExtra(Intent.EXTRA_TEXT, caption)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                if (installedTikTokPkg != null) {
                    setPackage(installedTikTokPkg)
                }
            }

            if (installedTikTokPkg != null && shareIntent.resolveActivity(pm) != null) {
                context.startActivity(shareIntent)
                onStatus("TikTok-এ ভিডিও আপলোড স্ক্রিন ওপেন করা হয়েছে!")
            } else {
                val chooser = Intent.createChooser(
                    Intent(Intent.ACTION_SEND).apply {
                        type = "video/*"
                        putExtra(Intent.EXTRA_STREAM, videoUri)
                        putExtra(Intent.EXTRA_TEXT, caption)
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    },
                    "Upload / Share Video to TikTok or Social Media"
                ).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(chooser)
                onStatus("ভিডিও শেয়ার ও আপলোড মেনু ওপেন করা হয়েছে।")
            }
        } catch (e: Exception) {
            onStatus("ভিডিও শেয়ার করতে সমস্যা হয়েছে: ${e.localizedMessage ?: "Unknown error"}")
        }
    }

    fun launchCameraIntent(context: Context, videoMode: Boolean, onStatus: (String) -> Unit) {
        try {
            val action = if (videoMode) MediaStore.ACTION_VIDEO_CAPTURE else MediaStore.ACTION_IMAGE_CAPTURE
            val intent = Intent(action).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onStatus(if (videoMode) "ভিডিও ক্যামেরা চালু হয়েছে।" else "ফটো ক্যামেরা চালু হয়েছে।")
        } catch (e: Exception) {
            Toast.makeText(context, "ক্যামেরা অ্যাপ পাওয়া যায়নি", Toast.LENGTH_SHORT).show()
            onStatus("ক্যামেরা চালু করা যায়নি: ${e.localizedMessage ?: "No camera app"}")
        }
    }

    fun setAlarm(context: Context, hour: Int, minute: Int, label: String, onStatus: (String) -> Unit) {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
                putExtra(AlarmClock.EXTRA_HOUR, hour.coerceIn(0, 23))
                putExtra(AlarmClock.EXTRA_MINUTES, minute.coerceIn(0, 59))
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            val formattedTime = String.format("%02d:%02d", hour.coerceIn(0, 23), minute.coerceIn(0, 59))
            onStatus("$formattedTime সময়ে '$label' অ্যালার্ম সেট করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("অ্যালার্ম অ্যাপ ওপেন করা যায়নি।")
        }
    }

    fun setTimer(context: Context, lengthSeconds: Int, label: String, onStatus: (String) -> Unit) {
        try {
            val intent = Intent(AlarmClock.ACTION_SET_TIMER).apply {
                putExtra(AlarmClock.EXTRA_LENGTH, lengthSeconds.coerceAtLeast(10))
                putExtra(AlarmClock.EXTRA_MESSAGE, label)
                putExtra(AlarmClock.EXTRA_SKIP_UI, false)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onStatus("${lengthSeconds / 60} মিনিটের টাইমার চালু করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("টাইমার অ্যাপ ওপেন করা যায়নি।")
        }
    }

    fun dialPhone(context: Context, phoneNumber: String, onStatus: (String) -> Unit) {
        try {
            val cleanNumber = phoneNumber.filter { it.isDigit() || it == '+' }
            val uri = if (cleanNumber.isNotEmpty()) Uri.parse("tel:$cleanNumber") else Uri.parse("tel:")
            val intent = Intent(Intent.ACTION_DIAL, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onStatus("ডায়ালার ওপেন করা হয়েছে ${if (cleanNumber.isNotEmpty()) "($cleanNumber)" else ""}।")
        } catch (e: Exception) {
            onStatus("ফোন ডায়ালার চালু করা যায়নি।")
        }
    }

    fun sendSms(context: Context, recipient: String, body: String, onStatus: (String) -> Unit) {
        try {
            val cleanNumber = recipient.filter { it.isDigit() || it == '+' }
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("smsto:$cleanNumber")).apply {
                putExtra("sms_body", body)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onStatus("এসএমএস ড্রাফট প্রস্তুত করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("মেসেজিং অ্যাপ ওপেন করা যায়নি।")
        }
    }

    fun sendWhatsApp(context: Context, phoneOrText: String, message: String, onStatus: (String) -> Unit) {
        try {
            val textPayload = message.ifBlank { phoneOrText }
            val cleanPhone = phoneOrText.filter { it.isDigit() || it == '+' }
            val uri = if (cleanPhone.length >= 7) {
                Uri.parse("https://wa.me/$cleanPhone?text=${Uri.encode(textPayload)}")
            } else {
                Uri.parse("https://wa.me/?text=${Uri.encode(textPayload)}")
            }
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
            onStatus("হোয়াটসঅ্যাপ মেসেজ ওপেন করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("হোয়াটসঅ্যাপ ওপেন করা যায়নি।")
        }
    }

    fun openAppByName(context: Context, appQuery: String, onStatus: (String) -> Unit) {
        val query = appQuery.trim().lowercase()
        val pm = context.packageManager

        try {
            if (query.contains("setting") || query.contains("সেটিং")) {
                context.startActivity(Intent(Settings.ACTION_SETTINGS).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                onStatus("ফোন সেটিংস ওপেন করা হয়েছে।")
                return
            }

            // 1. Check installed apps dynamically by label or package
            val installedApps = getInstalledLaunchableApps(context)
            val dynamicMatch = installedApps.firstOrNull {
                it.appName.lowercase().contains(query) || query.contains(it.appName.lowercase())
            }
            if (dynamicMatch != null) {
                launchPackageDirectly(context, dynamicMatch.packageName, dynamicMatch.appName, onStatus)
                return
            }

            // 2. Check known social/popular apps map
            val matchedEntry = commonAppPackages.entries.firstOrNull { query.contains(it.key) }
            if (matchedEntry != null) {
                val launchIntent = pm.getLaunchIntentForPackage(matchedEntry.value)
                if (launchIntent != null) {
                    launchIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    context.startActivity(launchIntent)
                    onStatus("${matchedEntry.key.uppercase()} অ্যাপ ওপেন করা হয়েছে!")
                    return
                }
            }

            // 3. If TikTok requested and not installed, open TikTok web/upload fallback
            if (query.contains("tiktok") || query.contains("টিকটক")) {
                for (pkg in tiktokPackages) {
                    val launch = pm.getLaunchIntentForPackage(pkg)
                    if (launch != null) {
                        launch.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        context.startActivity(launch)
                        onStatus("TikTok ওপেন করা হয়েছে!")
                        return
                    }
                }
                val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.tiktok.com/upload")).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(webIntent)
                onStatus("TikTok Upload পেজ ব্রাউজারে ওপেন করা হয়েছে।")
                return
            }

            // 4. Fallback web search if app is not installed on emulator
            val webUrl = when {
                query.contains("youtube") || query.contains("ইউটিউব") -> "https://m.youtube.com"
                query.contains("facebook") || query.contains("ফেসবুক") -> "https://m.facebook.com"
                query.contains("instagram") || query.contains("ইনস্টাগ্রাম") -> "https://www.instagram.com"
                else -> "https://www.google.com/search?q=${Uri.encode(appQuery)}"
            }
            val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl)).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(browserIntent)
            onStatus("'$appQuery' ওপেন করা হয়েছে।")
        } catch (e: Exception) {
            onStatus("'$appQuery' ওপেন করা সম্ভব হয়নি।")
        }
    }
}
