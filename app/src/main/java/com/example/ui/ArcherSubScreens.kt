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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Event
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.ChatTurn
import com.example.data.model.DeviceActionCommand
import com.example.data.model.MeetingAlert
import com.example.data.model.MemoryEntry
import com.example.data.model.MobileActionType
import com.example.data.model.MoodLog
import com.example.data.model.SoulPersonality
import com.example.data.model.TaskItem
import com.example.ui.theme.BorderHudGreen
import com.example.ui.theme.NeonCrimson
import com.example.ui.theme.NeonGold
import com.example.ui.theme.NeonMatrixGreen
import com.example.ui.theme.NeonQuantumCyan
import com.example.ui.theme.NeonSolarAmber
import com.example.ui.theme.NeonSoulWhite
import com.example.ui.theme.ObsidianCard
import com.example.ui.theme.ObsidianDeep
import com.example.ui.theme.ObsidianSurface
import com.example.ui.theme.TextPrimaryHud
import com.example.ui.theme.TextSecondaryHud

// --- 1. MEMORY VAULT SUB-SCREEN ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MemoryVaultSheet(
    memories: List<MemoryEntry>,
    onAddMemory: (content: String, tag: String) -> Unit,
    onDeleteMemory: (String) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var newContent by remember { mutableStateOf("") }
    var selectedTag by remember { mutableStateOf("Personal") }
    val tags = listOf("Personal", "Preference", "Important", "VoiceNote")

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ObsidianDeep
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SubScreenTopBar(
                title = "SMART MEMORY VAULT",
                subtitle = "আপনার ব্যক্তিগত তথ্য, পছন্দ ও ভয়েস নোট",
                accentColor = NeonQuantumCyan,
                onBack = onClose
            )

            Spacer(modifier = Modifier.height(12.dp))

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.2.dp, NeonQuantumCyan.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    OutlinedTextField(
                        value = newContent,
                        onValueChange = { newContent = it },
                        placeholder = {
                            Text(
                                "নতুন তথ্য বা ভয়েস নোট লিখুন (যেমন: আমার সকাল ৯টায় কফি খেতে ভালো লাগে)...",
                                color = TextSecondaryHud
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_input_field"),
                        colors = hudTextFieldColors(NeonQuantumCyan)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    FlowRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        tags.forEach { tag ->
                            FilterChip(
                                selected = selectedTag == tag,
                                onClick = { selectedTag = tag },
                                label = { Text(tag) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = NeonQuantumCyan.copy(alpha = 0.25f),
                                    selectedLabelColor = NeonQuantumCyan
                                )
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Button(
                        onClick = {
                            if (newContent.isNotBlank()) {
                                onAddMemory(newContent.trim(), selectedTag)
                                newContent = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(48.dp)
                            .testTag("save_memory_button"),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonQuantumCyan,
                            contentColor = ObsidianDeep
                        ),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("মেমোরিতে সেভ করুন (Save to Memory)", fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(memories, key = { it.id.ifEmpty { it.content } }) { item ->
                    Surface(
                        shape = RoundedCornerShape(16.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.dp, NeonQuantumCyan.copy(alpha = 0.4f))
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.Lightbulb,
                                contentDescription = null,
                                tint = NeonQuantumCyan,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Surface(
                                        color = NeonQuantumCyan.copy(alpha = 0.18f),
                                        shape = RoundedCornerShape(6.dp)
                                    ) {
                                        Text(
                                            text = item.tag.uppercase(),
                                            style = MaterialTheme.typography.labelSmall,
                                            color = NeonQuantumCyan,
                                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text(
                                        text = "Mood: ${item.emotionContext}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonMatrixGreen
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = item.content,
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = TextPrimaryHud
                                )
                            }
                            if (item.id.isNotBlank()) {
                                IconButton(onClick = { onDeleteMemory(item.id) }) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Memory",
                                        tint = NeonCrimson
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 2. CHAT & FULL MOBILE ACTION COMMANDER SUB-SCREEN ---

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ChatAndMobileCommanderSheet(
    chatHistory: List<ChatTurn>,
    onSendCommand: (String) -> Unit,
    onStartVoiceInput: () -> Unit,
    onQuickAction: (DeviceActionCommand) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var inputText by remember { mutableStateOf("") }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ObsidianDeep
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
        ) {
            SubScreenTopBar(
                title = "CHAT & PHONE COMMANDER",
                subtitle = "যেকোনো কথা বলুন বা ফোনের যেকোনো কাজ করান",
                accentColor = NeonSolarAmber,
                onBack = onClose
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Direct Phone Action Grid (TikTok Upload, Camera, Gallery, Alarm, Call, WhatsApp, Apps)
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, NeonSolarAmber.copy(alpha = 0.6f))
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = "DIRECT PHONE ACCESS (ওয়ান-ট্যাপ ও ভয়েস অ্যাকশন)",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonSolarAmber
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        QuickActionChip(
                            label = "TikTok ভিডিও আপলোড",
                            icon = Icons.Default.Share,
                            color = NeonCrimson,
                            onClick = {
                                onQuickAction(DeviceActionCommand(type = MobileActionType.UPLOAD_TIKTOK_VIDEO))
                            }
                        )
                        QuickActionChip(
                            label = "গ্যালারি (Gallery)",
                            icon = Icons.Default.PhotoLibrary,
                            color = NeonQuantumCyan,
                            onClick = {
                                onQuickAction(DeviceActionCommand(type = MobileActionType.OPEN_GALLERY))
                            }
                        )
                        QuickActionChip(
                            label = "ক্যামেরা (Photo)",
                            icon = Icons.Default.CameraAlt,
                            color = NeonMatrixGreen,
                            onClick = {
                                onQuickAction(DeviceActionCommand(type = MobileActionType.OPEN_CAMERA_PHOTO))
                            }
                        )
                        QuickActionChip(
                            label = "ভিডিও রেকর্ড (Video)",
                            icon = Icons.Default.Videocam,
                            color = NeonSolarAmber,
                            onClick = {
                                onQuickAction(DeviceActionCommand(type = MobileActionType.OPEN_CAMERA_VIDEO))
                            }
                        )
                        QuickActionChip(
                            label = "অ্যালার্ম সেট (Alarm)",
                            icon = Icons.Default.Alarm,
                            color = NeonGold,
                            onClick = {
                                onQuickAction(
                                    DeviceActionCommand(
                                        type = MobileActionType.SET_ALARM,
                                        payload = "ARCHER AI Morning Routine",
                                        hour = 7,
                                        minute = 30
                                    )
                                )
                            }
                        )
                        QuickActionChip(
                            label = "ফোন কল (Call)",
                            icon = Icons.Default.Phone,
                            color = NeonMatrixGreen,
                            onClick = {
                                onQuickAction(DeviceActionCommand(type = MobileActionType.MAKE_CALL))
                            }
                        )
                        QuickActionChip(
                            label = "WhatsApp মেসেজ",
                            icon = Icons.AutoMirrored.Filled.Send,
                            color = NeonQuantumCyan,
                            onClick = {
                                onQuickAction(
                                    DeviceActionCommand(
                                        type = MobileActionType.SEND_WHATSAPP,
                                        payload = "হ্যালো! আমি ARCHER AI থেকে মেসেজ পাঠাচ্ছি।"
                                    )
                                )
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Conversation list
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(chatHistory, key = { it.id }) { turn ->
                    Card(
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = ObsidianCard),
                        border = BorderStroke(1.dp, BorderHudGreen)
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = "YOU • ${turn.timestampLabel}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonQuantumCyan
                                )
                                Text(
                                    text = "EMOTION: ${turn.detectedEmotion.uppercase()}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonSolarAmber
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = turn.userText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryHud
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "ARCHER AI",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonMatrixGreen
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = turn.archerReply,
                                style = MaterialTheme.typography.bodyLarge,
                                color = TextPrimaryHud
                            )
                            if (!turn.actionTriggered.isNullOrBlank()) {
                                Spacer(modifier = Modifier.height(6.dp))
                                Surface(
                                    color = NeonMatrixGreen.copy(alpha = 0.16f),
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "⚡ ${turn.actionTriggered}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = NeonMatrixGreen,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Input bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            "যেকোনো নির্দেশ দিন (যেমন: 'টিকটকে ভিডিও আপলোড করো' বা 'মন খারাপ লাগছে')...",
                            color = TextSecondaryHud
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("chat_command_input"),
                    colors = hudTextFieldColors(NeonMatrixGreen)
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(
                    onClick = onStartVoiceInput,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Speak Command",
                        tint = NeonQuantumCyan
                    )
                }
                IconButton(
                    onClick = {
                        if (inputText.isNotBlank()) {
                            onSendCommand(inputText.trim())
                            inputText = ""
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .testTag("send_command_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.Send,
                        contentDescription = "Send Command",
                        tint = NeonMatrixGreen
                    )
                }
            }
        }
    }
}

// --- 3. SOUL, EMOTION ENGINE & FOCUS TIMER SUB-SCREEN ---

@Composable
fun SoulAndEmotionSheet(
    selectedSoul: SoulPersonality,
    detectedEmotion: String,
    moodLogs: List<MoodLog>,
    focusSecondsRemaining: Int,
    isFocusTimerRunning: Boolean,
    onSelectSoul: (SoulPersonality) -> Unit,
    onToggleFocusTimer: () -> Unit,
    onResetFocusTimer: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

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
            item {
                SubScreenTopBar(
                    title = "SOUL, EMOTION & FOCUS ENGINE",
                    subtitle = "আবেগ বিশ্লেষণ, AI পার্সোনালিটি ও ফোকাস টাইমার",
                    accentColor = NeonSoulWhite,
                    onBack = onClose
                )
            }

            // Focus Pomodoro Timer Card
            item {
                val mins = focusSecondsRemaining / 60
                val secs = focusSecondsRemaining % 60
                val formattedTimer = String.format("%02d:%02d", mins, secs)

                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(1.5.dp, NeonMatrixGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text(
                                text = "DEEP FOCUS POMODORO",
                                style = MaterialTheme.typography.labelLarge,
                                color = NeonMatrixGreen
                            )
                            Text(
                                text = formattedTimer,
                                style = MaterialTheme.typography.displayLarge,
                                color = TextPrimaryHud
                            )
                            Text(
                                text = "বর্তমান মানসিক অবস্থা: $detectedEmotion",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonQuantumCyan
                            )
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onToggleFocusTimer,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isFocusTimerRunning) NeonCrimson else NeonMatrixGreen,
                                    contentColor = ObsidianDeep
                                ),
                                shape = RoundedCornerShape(12.dp)
                            ) {
                                Icon(
                                    imageVector = if (isFocusTimerRunning) Icons.Default.Stop else Icons.Default.PlayArrow,
                                    contentDescription = null
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (isFocusTimerRunning) "Pause" else "Start", fontWeight = FontWeight.Bold)
                            }
                            IconButton(onClick = onResetFocusTimer) {
                                Icon(
                                    imageVector = Icons.Default.Refresh,
                                    contentDescription = "Reset Timer",
                                    tint = TextSecondaryHud
                                )
                            }
                        }
                    }
                }
            }

            item {
                Text(
                    text = "AI SOUL PERSONALITY (কথা বলার ধরন নির্বাচন করুন)",
                    style = MaterialTheme.typography.titleSmall,
                    color = NeonQuantumCyan
                )
            }

            items(SoulPersonality.entries) { soul ->
                val isSelected = soul == selectedSoul
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .clickable { onSelectSoul(soul) },
                    shape = RoundedCornerShape(16.dp),
                    color = if (isSelected) ObsidianSurface else ObsidianCard,
                    border = BorderStroke(
                        width = if (isSelected) 2.dp else 1.dp,
                        color = if (isSelected) NeonMatrixGreen else BorderHudGreen
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.SelfImprovement,
                            contentDescription = null,
                            tint = if (isSelected) NeonMatrixGreen else NeonSoulWhite,
                            modifier = Modifier.size(26.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = soul.titleBn,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryHud
                            )
                            Text(
                                text = soul.descriptionBn,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondaryHud
                            )
                        }
                    }
                }
            }

            item {
                Text(
                    text = "RECENT EMOTION & SITUATION LOGS (আবেগের ইতিহাস)",
                    style = MaterialTheme.typography.titleSmall,
                    color = NeonSolarAmber
                )
            }

            items(moodLogs, key = { it.id.ifEmpty { it.note + it.emotion } }) { log ->
                Surface(
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, BorderHudGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "${log.emotion} • Energy ${log.energyScore}%",
                                style = MaterialTheme.typography.titleMedium,
                                color = NeonMatrixGreen
                            )
                            if (log.note.isNotBlank()) {
                                Text(
                                    text = log.note,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondaryHud
                                )
                            }
                        }
                        Text(
                            text = log.soulMode,
                            style = MaterialTheme.typography.labelSmall,
                            color = NeonQuantumCyan
                        )
                    }
                }
            }
        }
    }
}

// --- 4. TASKS, ROUTINES & PROACTIVE MEETING ALERTS SUB-SCREEN ---

@Composable
fun TasksAndMeetingsSheet(
    tasks: List<TaskItem>,
    meetings: List<MeetingAlert>,
    onAddTask: (title: String, category: String, dueTime: String, priority: String) -> Unit,
    onToggleTask: (TaskItem) -> Unit,
    onDeleteTask: (String) -> Unit,
    onAddMeeting: (title: String, startTime: String, remindMinutes: Long, isUrgent: Boolean, notes: String) -> Unit,
    onDeleteMeeting: (String) -> Unit,
    onSpeakMeetingAlert: (MeetingAlert) -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    var newTaskTitle by remember { mutableStateOf("") }
    var newTaskTime by remember { mutableStateOf("06:00 PM") }
    var newMeetingTitle by remember { mutableStateOf("") }
    var newMeetingTime by remember { mutableStateOf("04:30 PM") }
    var remindMinutes by remember { mutableStateOf("15") }
    var isUrgentMeeting by remember { mutableStateOf(true) }

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
            item {
                SubScreenTopBar(
                    title = "ROUTINES & URGENT MEETING ALERTS",
                    subtitle = "প্রতিদিনের টাস্ক এবং মিটিংয়ের আগাম ভয়েস রিমাইন্ডার",
                    accentColor = NeonMatrixGreen,
                    onBack = onClose
                )
            }

            // Add New Urgent Meeting with Early Alert
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(1.4.dp, NeonSolarAmber.copy(alpha = 0.8f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ADD MEETING WITH EARLY ALERT (মিটিংয়ের আগাম অ্যালার্ট যোগ করুন)",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonSolarAmber
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedTextField(
                            value = newMeetingTitle,
                            onValueChange = { newMeetingTitle = it },
                            placeholder = { Text("মিটিংয়ের নাম (যেমন: ক্লায়েন্ট জুম মিটিং)...", color = TextSecondaryHud) },
                            modifier = Modifier.fillMaxWidth(),
                            colors = hudTextFieldColors(NeonSolarAmber)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedTextField(
                                value = newMeetingTime,
                                onValueChange = { newMeetingTime = it },
                                label = { Text("সময় (Time)") },
                                modifier = Modifier.weight(1f),
                                colors = hudTextFieldColors(NeonSolarAmber)
                            )
                            OutlinedTextField(
                                value = remindMinutes,
                                onValueChange = { remindMinutes = it.filter { ch -> ch.isDigit() } },
                                label = { Text("কত মিনিট আগে অ্যালার্ট?") },
                                modifier = Modifier.weight(1f),
                                colors = hudTextFieldColors(NeonSolarAmber)
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text("জরুরি মিটিং (Urgent High-Alert)", color = TextPrimaryHud)
                            Switch(
                                checked = isUrgentMeeting,
                                onCheckedChange = { isUrgentMeeting = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = NeonCrimson)
                            )
                        }
                        Button(
                            onClick = {
                                if (newMeetingTitle.isNotBlank()) {
                                    onAddMeeting(
                                        newMeetingTitle.trim(),
                                        newMeetingTime.trim().ifEmpty { "04:30 PM" },
                                        remindMinutes.toLongOrNull() ?: 15L,
                                        isUrgentMeeting,
                                        "Proactive Early Alert Active"
                                    )
                                    newMeetingTitle = ""
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonSolarAmber,
                                contentColor = ObsidianDeep
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("মিটিং অ্যালার্ট সেট করুন", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            // Upcoming Meetings List
            items(meetings, key = { it.id.ifEmpty { it.title } }) { meeting ->
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = ObsidianCard,
                    border = BorderStroke(
                        1.2.dp,
                        if (meeting.isUrgent) NeonCrimson.copy(alpha = 0.8f) else NeonSolarAmber.copy(alpha = 0.5f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Event,
                            contentDescription = null,
                            tint = if (meeting.isUrgent) NeonCrimson else NeonSolarAmber
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = meeting.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryHud
                            )
                            Text(
                                text = "সময়: ${meeting.startTime} • ${meeting.remindBeforeMinutes} মিনিট আগে ভয়েস অ্যালার্ট",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonQuantumCyan
                            )
                            if (meeting.notes.isNotBlank()) {
                                Text(
                                    text = meeting.notes,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondaryHud
                                )
                            }
                        }
                        IconButton(onClick = { onSpeakMeetingAlert(meeting) }) {
                            Icon(
                                imageVector = Icons.Default.NotificationsActive,
                                contentDescription = "Speak Alert Now",
                                tint = NeonMatrixGreen
                            )
                        }
                        if (meeting.id.isNotBlank()) {
                            IconButton(onClick = { onDeleteMeeting(meeting.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Meeting",
                                    tint = NeonCrimson
                                )
                            }
                        }
                    }
                }
            }

            // Add New Daily Routine / Task
            item {
                Card(
                    shape = RoundedCornerShape(18.dp),
                    colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                    border = BorderStroke(1.2.dp, NeonMatrixGreen.copy(alpha = 0.7f))
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Text(
                            text = "ADD DAILY TASK OR ROUTINE (নতুন টাস্ক বা রুটিন যুক্ত করুন)",
                            style = MaterialTheme.typography.labelLarge,
                            color = NeonMatrixGreen
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = newTaskTitle,
                                onValueChange = { newTaskTitle = it },
                                placeholder = { Text("টাস্কের নাম...", color = TextSecondaryHud) },
                                modifier = Modifier
                                    .weight(0.65f)
                                    .testTag("new_task_title_input"),
                                colors = hudTextFieldColors(NeonMatrixGreen)
                            )
                            OutlinedTextField(
                                value = newTaskTime,
                                onValueChange = { newTaskTime = it },
                                placeholder = { Text("সময়", color = TextSecondaryHud) },
                                modifier = Modifier.weight(0.35f),
                                colors = hudTextFieldColors(NeonMatrixGreen)
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        Button(
                            onClick = {
                                if (newTaskTitle.isNotBlank()) {
                                    onAddTask(newTaskTitle.trim(), "Routine", newTaskTime.trim().ifEmpty { "06:00 PM" }, "HIGH")
                                    newTaskTitle = ""
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("add_task_submit_button"),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonMatrixGreen,
                                contentColor = ObsidianDeep
                            ),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("টাস্ক যোগ করুন (Add Task)", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }

            items(tasks, key = { it.id.ifEmpty { it.title } }) { task ->
                Surface(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(14.dp))
                        .clickable { onToggleTask(task) },
                    shape = RoundedCornerShape(14.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, if (task.completed) BorderHudGreen else NeonMatrixGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Toggle Task",
                            tint = if (task.completed) NeonMatrixGreen else TextSecondaryHud
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = task.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (task.completed) TextSecondaryHud else TextPrimaryHud
                            )
                            Text(
                                text = "${task.category} • ${task.dueTime}",
                                style = MaterialTheme.typography.bodyMedium,
                                color = NeonQuantumCyan
                            )
                        }
                        if (task.id.isNotBlank()) {
                            IconButton(onClick = { onDeleteTask(task.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete Task",
                                    tint = NeonCrimson
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

// --- 5. SETTINGS & ACCOUNT SUB-SCREEN ---

@Composable
fun SettingsAndVoiceConfigSheet(
    preferredLanguage: String,
    voiceMuted: Boolean,
    userEmail: String?,
    onSelectLanguage: (String) -> Unit,
    onToggleMute: () -> Unit,
    onSignOut: () -> Unit,
    onClose: () -> Unit
) {
    BackHandler { onClose() }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = ObsidianDeep
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SubScreenTopBar(
                title = "ARCHER AI SETTINGS",
                subtitle = "ভাষা, ভয়েস ও অ্যাকাউন্ট কনফিগারেশন",
                accentColor = NeonMatrixGreen,
                onBack = onClose
            )

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.2.dp, NeonMatrixGreen.copy(alpha = 0.6f))
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text(
                        text = "VOICE & LANGUAGE (কথা বলার ভাষা)",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonMatrixGreen
                    )
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        FilterChip(
                            selected = preferredLanguage == "bn-BD",
                            onClick = { onSelectLanguage("bn-BD") },
                            label = { Text("বাংলা + English (Bilingual)") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonMatrixGreen.copy(alpha = 0.25f),
                                selectedLabelColor = NeonMatrixGreen
                            )
                        )
                        FilterChip(
                            selected = preferredLanguage == "en-US",
                            onClick = { onSelectLanguage("en-US") },
                            label = { Text("English Primary") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = NeonQuantumCyan.copy(alpha = 0.25f),
                                selectedLabelColor = NeonQuantumCyan
                            )
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text("AI Voice Output (স্পিকার চালু)", color = TextPrimaryHud)
                        Switch(
                            checked = !voiceMuted,
                            onCheckedChange = { onToggleMute() },
                            colors = SwitchDefaults.colors(checkedThumbColor = NeonMatrixGreen)
                        )
                    }
                }
            }

            Card(
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
                border = BorderStroke(1.dp, BorderHudGreen)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text(
                        text = "CLOUD SYNC ACCOUNT",
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonQuantumCyan
                    )
                    Text(
                        text = userEmail ?: "Signed in with Google",
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimaryHud
                    )
                    Button(
                        onClick = onSignOut,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCrimson,
                            contentColor = TextPrimaryHud
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sign_out_button")
                    ) {
                        Icon(Icons.Default.Close, contentDescription = null)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("সাইন আউট (Sign Out)", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun SubScreenTopBar(
    title: String,
    subtitle: String,
    accentColor: Color,
    onBack: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onBack,
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .testTag("subscreen_back_button")
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back to HUD",
                tint = accentColor
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = accentColor
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondaryHud
            )
        }
    }
}

@Composable
private fun QuickActionChip(
    label: String,
    icon: ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(40.dp)
            .clip(RoundedCornerShape(20.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(20.dp),
        color = color.copy(alpha = 0.14f),
        border = BorderStroke(1.2.dp, color)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
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

@Composable
private fun hudTextFieldColors(accent: Color) = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = accent,
    unfocusedBorderColor = BorderHudGreen,
    focusedTextColor = TextPrimaryHud,
    unfocusedTextColor = TextPrimaryHud,
    cursorColor = accent
)
