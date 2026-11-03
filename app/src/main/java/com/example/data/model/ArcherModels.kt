package com.example.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.IgnoreExtraProperties
import com.google.firebase.firestore.ServerTimestamp

@IgnoreExtraProperties
data class TaskItem(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val category: String = "Routine",
    val dueTime: String = "09:00 AM",
    val completed: Boolean = false,
    val priority: String = "HIGH",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MeetingAlert(
    @DocumentId val id: String = "",
    val userId: String = "",
    val title: String = "",
    val startTime: String = "11:00 AM",
    val remindBeforeMinutes: Long = 15L,
    val isUrgent: Boolean = true,
    val notes: String = "",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MemoryEntry(
    @DocumentId val id: String = "",
    val userId: String = "",
    val content: String = "",
    val tag: String = "Personal",
    val emotionContext: String = "Calm",
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

@IgnoreExtraProperties
data class MoodLog(
    @DocumentId val id: String = "",
    val userId: String = "",
    val emotion: String = "Calm",
    val soulMode: String = "Empathetic Friend",
    val note: String = "",
    val energyScore: Long = 85L,
    @ServerTimestamp val createdAt: Timestamp? = null,
    @ServerTimestamp val updatedAt: Timestamp? = null
)

data class NewsHeadline(
    val id: String,
    val source: String,
    val title: String,
    val summary: String,
    val category: String
)

enum class OrbState(val label: String) {
    IDLE("STANDBY"),
    LISTENING("LISTENING"),
    THINKING("ANALYZING"),
    SPEAKING("SPEAKING")
}

enum class SoulPersonality(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val descriptionBn: String,
    val systemInstruction: String
) {
    EMPATHETIC(
        id = "Empathetic Friend",
        titleBn = "সহানুভূতিশীল বন্ধু (Empathetic)",
        titleEn = "Empathetic Friend",
        descriptionBn = "আপনার আবেগ, আনন্দ বা ক্লান্তি বুঝে উষ্ণ ও আপনজনের মতো কথা বলবে।",
        systemInstruction = "Speak like a warm, deeply empathetic close companion who notices subtle emotional cues, validates feelings, and supports the user gently in Bengali and English."
    ),
    COACH(
        id = "Productivity Coach",
        titleBn = "প্রোডাক্টিভিটি কোচ (Coach)",
        titleEn = "Productivity Coach",
        descriptionBn = "প্রতিদিনের রুটিন, মিটিং ও টাস্ক সময়মতো শেষ করতে আপনাকে উৎসাহ দেবে।",
        systemInstruction = "Speak like an energetic, motivating productivity strategist who keeps the user focused on daily routines, urgent meetings, and high-impact tasks."
    ),
    MENTOR(
        id = "Calm Mentor",
        titleBn = "শান্ত পরামর্শদাতা (Calm Mentor)",
        titleEn = "Calm Mentor",
        descriptionBn = "চাপ বা ব্যস্ততার সময় ধীরস্থির ও বুদ্ধিদীপ্ত পরামর্শ দেবে।",
        systemInstruction = "Speak like a wise, composed, mindfulness-oriented mentor who brings calm clarity during stressful situations."
    ),
    TACTICAL(
        id = "Cyber Tactical",
        titleBn = "সাইবার কমান্ডার (Tactical HUD)",
        titleEn = "Cyber Tactical",
        descriptionBn = "দ্রুত, সংক্ষিপ্ত এবং প্রিসিশন AI অ্যাসিস্ট্যান্ট মোডে ফোনের কাজ সম্পন্ন করবে।",
        systemInstruction = "Speak like a futuristic, sharp tactical HUD AI assistant (ARCHER AI) delivering crisp, high-precision updates and instant device execution."
    )
}

enum class MobileActionType {
    NONE,
    UPLOAD_TIKTOK_VIDEO,
    OPEN_CAMERA_PHOTO,
    OPEN_CAMERA_VIDEO,
    OPEN_GALLERY,
    SET_ALARM,
    SET_TIMER,
    MAKE_CALL,
    SEND_SMS,
    SEND_WHATSAPP,
    OPEN_APP,
    ADD_TASK,
    ADD_MEETING,
    SAVE_MEMORY,
    // Full Mobile Hardware & System Control Actions
    TOGGLE_FLASHLIGHT_ON,
    TOGGLE_FLASHLIGHT_OFF,
    SET_VOLUME_LEVEL,
    MUTE_PHONE,
    UNMUTE_PHONE,
    SET_BRIGHTNESS,
    OPEN_WIFI_PANEL,
    OPEN_BLUETOOTH_SETTINGS,
    OPEN_SOUND_PANEL,
    OPEN_DISPLAY_SETTINGS,
    OPEN_HOTSPOT_SETTINGS,
    OPEN_LOCATION_SETTINGS,
    NAVIGATE_MAPS,
    SEARCH_YOUTUBE,
    SEARCH_WEB,
    ADD_CALENDAR_EVENT,
    COPY_CLIPBOARD,
    VIBRATE_DEVICE,
    // Voice Screen Navigation & Smart Notification / Macro Actions
    EXIT_CURRENT_SCREEN,
    GO_ANDROID_HOME,
    OPEN_CONTROL_CENTER,
    OPEN_MEMORY_VAULT,
    OPEN_CHAT_SCREEN,
    OPEN_SOUL_SCREEN,
    OPEN_TASKS_SCREEN,
    OPEN_SETTINGS_SCREEN,
    OPEN_VISION_SCANNER,
    OPEN_CREATOR_STUDIO,
    OPEN_EXPENSE_TRACKER,
    READ_NOTIFICATIONS,
    MACRO_GOOD_MORNING,
    MACRO_CREATOR_MODE,
    MACRO_SLEEP_MODE,
    LOG_VOICE_EXPENSE,
    GENERATE_CREATOR_SCRIPT,
    GENERATE_AI_IMAGE,
    SWITCH_CYBER_THEME
}

enum class CyberThemeMode(
    val id: String,
    val titleBn: String,
    val titleEn: String,
    val primaryHex: Long,
    val secondaryHex: Long,
    val accentHex: Long
) {
    MATRIX_GREEN(
        id = "MATRIX_GREEN",
        titleBn = "Matrix Green (ক্লাসিক নিওন গ্রিন)",
        titleEn = "Matrix Green",
        primaryHex = 0xFF00FF66,
        secondaryHex = 0xFF00E5FF,
        accentHex = 0xFFFFB300
    ),
    CYBERPUNK_RED(
        id = "CYBERPUNK_RED",
        titleBn = "Cyberpunk Red (নিওন ক্রিমসন ও গোল্ড)",
        titleEn = "Cyberpunk Red",
        primaryHex = 0xFFFF1744,
        secondaryHex = 0xFFFFEA00,
        accentHex = 0xFF00E5FF
    ),
    IRON_MAN_GOLD(
        id = "IRON_MAN_GOLD",
        titleBn = "Iron-Man Gold/Cyan (আর্ক রিঅ্যাক্টর)",
        titleEn = "Iron-Man Gold/Cyan",
        primaryHex = 0xFF00E5FF,
        secondaryHex = 0xFFFFD54F,
        accentHex = 0xFFFF5252
    ),
    STEALTH_PURPLE(
        id = "STEALTH_PURPLE",
        titleBn = "Stealth Purple (আল্ট্রাভায়োলেট নিওন)",
        titleEn = "Stealth Purple",
        primaryHex = 0xFFD500F9,
        secondaryHex = 0xFF00E5FF,
        accentHex = 0xFF00E676
    )
}

data class ExpenseEntry(
    val id: String,
    val title: String,
    val amountBdt: Int,
    val category: String,
    val timestampLabel: String
)

data class HabitItem(
    val id: String,
    val title: String,
    val streakDays: Int,
    val completedToday: Boolean
)

data class VisionScanResult(
    val summaryBn: String,
    val translationBn: String = "",
    val extractedTasks: List<String> = emptyList(),
    val extractedMeetings: List<String> = emptyList()
)

data class CreatorScriptResult(
    val topic: String,
    val viralHook: String,
    val fullScript: String,
    val caption: String,
    val hashtags: String
)

data class DeviceActionCommand(
    val type: MobileActionType = MobileActionType.NONE,
    val target: String = "",
    val payload: String = "",
    val hour: Int = 8,
    val minute: Int = 0
)

data class DeviceTelemetry(
    val batteryPercent: Int = 85,
    val isCharging: Boolean = false,
    val batteryTempCelsius: Float = 31.0f,
    val availableRamGb: String = "3.8 GB",
    val totalRamGb: String = "8.0 GB",
    val freeStorageGb: String = "42.5 GB",
    val totalStorageGb: String = "128 GB",
    val mediaVolumePercent: Int = 70,
    val ringVolumePercent: Int = 80,
    val isSilentMode: Boolean = false,
    val isTorchOn: Boolean = false,
    val brightnessPercent: Int = 75
)

data class InstalledAppInfo(
    val appName: String,
    val packageName: String
)

data class ChatTurn(
    val id: String,
    val userText: String,
    val archerReply: String,
    val detectedEmotion: String,
    val actionTriggered: String? = null,
    val timestampLabel: String
)
