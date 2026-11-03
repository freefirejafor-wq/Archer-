package com.example.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Alarm
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.credentials.CredentialManager
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.R
import com.example.data.model.DeviceActionCommand
import com.example.data.model.MeetingAlert
import com.example.data.model.MobileActionType
import com.example.data.model.NewsHeadline
import com.example.data.model.OrbState
import com.example.data.model.TaskItem
import com.example.data.repository.ArcherRepository
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
import com.example.util.MobileActionExecutor
import com.example.util.VoiceAudioEngine
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

enum class ActiveOverlayScreen {
    NONE,
    MEMORY,
    CHAT,
    SOUL,
    TASKS_AND_MEETINGS,
    MOBILE_CONTROL_CENTER,
    SETTINGS
}

@Composable
fun ArcherMainHudScreen(
    currentUserId: String
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val credentialManager = remember { CredentialManager.create(context) }

    val viewModel: ArcherViewModel = viewModel(
        key = currentUserId,
        factory = viewModelFactory {
            initializer {
                val app = checkNotNull(this[APPLICATION_KEY]) {
                    "APPLICATION_KEY missing from CreationExtras"
                }
                val databaseId = app.getString(R.string.firestore_database_id)
                val db = FirebaseFirestore.getInstance(databaseId)
                ArcherViewModel(ArcherRepository(db), currentUserId)
            }
        }
    )

    val tasksState by viewModel.tasksState.collectAsStateWithLifecycle()
    val meetingsState by viewModel.meetingsState.collectAsStateWithLifecycle()
    val memoriesState by viewModel.memoriesState.collectAsStateWithLifecycle()
    val moodsState by viewModel.moodsState.collectAsStateWithLifecycle()

    val orbState by viewModel.orbState.collectAsStateWithLifecycle()
    val archerTranscript by viewModel.archerTranscript.collectAsStateWithLifecycle()
    val userTranscript by viewModel.userTranscript.collectAsStateWithLifecycle()
    val detectedEmotion by viewModel.detectedEmotion.collectAsStateWithLifecycle()
    val selectedSoul by viewModel.selectedSoul.collectAsStateWithLifecycle()
    val voiceMuted by viewModel.voiceMuted.collectAsStateWithLifecycle()
    val preferredLanguage by viewModel.preferredLanguage.collectAsStateWithLifecycle()
    val aiErrorMessage by viewModel.aiErrorMessage.collectAsStateWithLifecycle()
    val statusBanner by viewModel.statusBanner.collectAsStateWithLifecycle()
    val chatHistory by viewModel.chatHistory.collectAsStateWithLifecycle()
    val headlines by viewModel.headlines.collectAsStateWithLifecycle()
    val focusSecondsRemaining by viewModel.focusSecondsRemaining.collectAsStateWithLifecycle()
    val isFocusTimerRunning by viewModel.isFocusTimerRunning.collectAsStateWithLifecycle()
    val deviceTelemetry by viewModel.deviceTelemetry.collectAsStateWithLifecycle()

    var activeOverlay by remember { mutableStateOf(ActiveOverlayScreen.NONE) }
    var quickInputText by remember { mutableStateOf("") }
    var pendingCameraVideoMode by remember { mutableStateOf(false) }
    var isTorchOnState by remember { mutableStateOf(false) }
    var brightnessPercentState by remember { mutableIntStateOf(80) }

    // Live microphone recording feedback state
    var liveMicAmplitude by remember { mutableFloatStateOf(0.1f) }
    var liveRecordingSeconds by remember { mutableIntStateOf(0) }
    var speechRecognizerHandledTurn by remember { mutableStateOf(false) }

    val installedApps = remember(context) {
        MobileActionExecutor.getInstalledLaunchableApps(context)
    }

    val refreshDeviceTelemetry = {
        viewModel.updateTelemetry(
            MobileActionExecutor.readLiveDeviceTelemetry(
                context = context,
                isTorchOn = isTorchOnState,
                brightnessPercent = brightnessPercentState
            )
        )
    }

    LaunchedEffect(isTorchOnState, brightnessPercentState) {
        refreshDeviceTelemetry()
    }

    // --- 1. ANDROID PHOTO / VIDEO PICKERS (ZERO-PERMISSION MEDIA ACCESS) ---

    val tikTokVideoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            MobileActionExecutor.shareVideoToTikTokOrSystem(
                context = context,
                videoUri = uri,
                caption = "#ArcherAI #Creator",
                onStatus = { msg -> viewModel.setActionStatusBanner(msg) }
            )
        } else {
            viewModel.setActionStatusBanner("কোনো ভিডিও নির্বাচন করা হয়নি।")
        }
    }

    val galleryMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            try {
                val viewIntent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, context.contentResolver.getType(uri) ?: "image/*")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(viewIntent)
                viewModel.setActionStatusBanner("গ্যালারি মিডিয়া ওপেন করা হয়েছে।")
            } catch (_: Exception) {
                MobileActionExecutor.shareVideoToTikTokOrSystem(
                    context = context,
                    videoUri = uri,
                    onStatus = { msg -> viewModel.setActionStatusBanner(msg) }
                )
            }
        }
    }

    // --- 2. RUNTIME PERMISSION LAUNCHERS (CAMERA & RECORD_AUDIO) ---

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            MobileActionExecutor.launchCameraIntent(
                context = context,
                videoMode = pendingCameraVideoMode,
                onStatus = { msg -> viewModel.setActionStatusBanner(msg) }
            )
        } else {
            viewModel.setActionStatusBanner("ক্যামেরা ব্যবহারের জন্য পারমিশন প্রয়োজন।")
        }
    }

    // --- 3. RESILIENT BILINGUAL + PHONETIC TEXT-TO-SPEECH & DUAL-ENGINE VOICE CAPTURE ---

    var ttsEngine by remember { mutableStateOf<TextToSpeech?>(null) }
    var ttsInitialized by remember { mutableStateOf(false) }

    DisposableEffect(context) {
        var instance: TextToSpeech? = null
        instance = TextToSpeech(context) { status ->
            if (status == TextToSpeech.SUCCESS) {
                val bnLocale = Locale.forLanguageTag("bn-BD")
                val result = instance?.setLanguage(bnLocale)
                if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                    instance?.setLanguage(Locale.US)
                }
                instance?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                    override fun onStart(utteranceId: String?) {
                        viewModel.setOrbState(OrbState.SPEAKING)
                    }

                    override fun onDone(utteranceId: String?) {
                        viewModel.setOrbState(OrbState.IDLE)
                    }

                    @Deprecated("Deprecated in Java")
                    override fun onError(utteranceId: String?) {
                        viewModel.setOrbState(OrbState.IDLE)
                    }
                })
                ttsInitialized = true
            }
        }
        ttsEngine = instance
        onDispose {
            VoiceAudioEngine.stopMicRecording()
            instance?.stop()
            instance?.shutdown()
        }
    }

    // Speak initial greeting once TTS is ready so user immediately hears ARCHER's voice
    var initialGreetingSpoken by remember { mutableStateOf(false) }
    LaunchedEffect(ttsInitialized) {
        if (ttsInitialized && !initialGreetingSpoken && !voiceMuted) {
            initialGreetingSpoken = true
            VoiceAudioEngine.speakSmartBilingual(
                context = context,
                tts = ttsEngine,
                text = archerTranscript,
                phoneticHint = "Notun jinish shikhchi, tomader shahajjo korchi, dingulo darun katche! Jekono kajer jonno TALK button e tap kore bolo!",
                onFallbackDone = { viewModel.setOrbState(OrbState.IDLE) }
            )
        }
    }

    var lastPartialRecognizedSpeech by remember { mutableStateOf("") }
    var retriedDefaultLocale by remember { mutableStateOf(false) }

    val startHardwareWavFallback: () -> Unit = {
        VoiceAudioEngine.startMicRecording(
            scope = coroutineScope,
            maxDurationMs = 6000L,
            onAmplitudeUpdate = { amp, sec ->
                liveMicAmplitude = amp
                liveRecordingSeconds = sec
            },
            onFinished = { sample ->
                if (!speechRecognizerHandledTurn) {
                    speechRecognizerHandledTurn = true
                    VoiceAudioEngine.playHudChime(coroutineScope, ascending = false)
                    if (sample != null) {
                        viewModel.sendUserVoiceAudio(
                            wavBytes = sample.wavBytes,
                            maxRmsAmplitude = sample.maxRmsAmplitude
                        )
                    } else {
                        viewModel.setOrbState(OrbState.IDLE)
                        viewModel.setActionStatusBanner("মাইক্রোফোন অডিও পাওয়া যায়নি; আবার চেষ্টা করুন।")
                    }
                }
            }
        )
    }

    val speechRecognizer = remember {
        try {
            if (SpeechRecognizer.isRecognitionAvailable(context)) {
                SpeechRecognizer.createSpeechRecognizer(context)
            } else {
                null
            }
        } catch (_: Exception) {
            null
        }
    }

    DisposableEffect(speechRecognizer) {
        speechRecognizer?.setRecognitionListener(object : RecognitionListener {
            override fun onReadyForSpeech(params: Bundle?) {
                viewModel.setOrbState(OrbState.LISTENING)
            }

            override fun onBeginningOfSpeech() {
                viewModel.setOrbState(OrbState.LISTENING)
            }

            override fun onRmsChanged(rmsdB: Float) {
                val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.08f, 1.0f)
                liveMicAmplitude = normalized
            }

            override fun onBufferReceived(buffer: ByteArray?) {}

            override fun onEndOfSpeech() {
                if (lastPartialRecognizedSpeech.isNotBlank()) {
                    viewModel.updateLiveUserSpeech(lastPartialRecognizedSpeech)
                }
            }

            override fun onError(error: Int) {
                if (speechRecognizerHandledTurn) return

                // 1. If partial recognition already captured the user's words before timeout/no-match, use those exact words!
                if (lastPartialRecognizedSpeech.isNotBlank()) {
                    speechRecognizerHandledTurn = true
                    VoiceAudioEngine.playHudChime(coroutineScope, ascending = false)
                    viewModel.sendUserCommandOrSpeech(lastPartialRecognizedSpeech)
                    return
                }

                // 2. If bn-BD pack is missing on device (ERROR_LANGUAGE_NOT_SUPPORTED = 12 / 13), retry with system default locale
                if ((error == 12 || error == 13) && !retriedDefaultLocale) {
                    retriedDefaultLocale = true
                    try {
                        val fallbackIntent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                        }
                        speechRecognizer.startListening(fallbackIntent)
                        return
                    } catch (_: Exception) {}
                }

                // 3. If user simply didn't speak (NO_MATCH = 7 or SPEECH_TIMEOUT = 6), prompt gently
                if (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT) {
                    speechRecognizerHandledTurn = true
                    viewModel.setOrbState(OrbState.IDLE)
                    viewModel.updateLiveUserSpeech("🎙️ (কথা স্পষ্ট বোঝা যায়নি — আবার TALK চাপুন)")
                    return
                }

                // 4. Only if SpeechRecognizer service itself failed (ERROR_CLIENT, ERROR_SERVICE, etc.),
                // release SpeechRecognizer and record via hardware AudioRecord -> Gemini WAV!
                try {
                    speechRecognizer.cancel()
                } catch (_: Exception) {}
                startHardwareWavFallback()
            }

            override fun onResults(results: Bundle?) {
                val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                val spokenText = matches?.firstOrNull().orEmpty().trim()
                    .ifBlank { lastPartialRecognizedSpeech.trim() }
                if (spokenText.isNotBlank() && !speechRecognizerHandledTurn) {
                    speechRecognizerHandledTurn = true
                    VoiceAudioEngine.playHudChime(coroutineScope, ascending = false)
                    viewModel.sendUserCommandOrSpeech(spokenText)
                } else if (!speechRecognizerHandledTurn) {
                    speechRecognizerHandledTurn = true
                    viewModel.setOrbState(OrbState.IDLE)
                }
            }

            override fun onPartialResults(partialResults: Bundle?) {
                val partial = partialResults
                    ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                    ?.firstOrNull()
                    .orEmpty()
                    .trim()
                if (partial.isNotBlank()) {
                    lastPartialRecognizedSpeech = partial
                    viewModel.updateLiveUserSpeech(partial)
                }
            }

            override fun onEvent(eventType: Int, params: Bundle?) {}
        })

        onDispose {
            try {
                speechRecognizer?.destroy()
            } catch (_: Exception) {}
        }
    }

    val startVoiceListening = {
        ttsEngine?.stop()
        VoiceAudioEngine.stopMicRecording()
        speechRecognizerHandledTurn = false
        lastPartialRecognizedSpeech = ""
        retriedDefaultLocale = false
        liveMicAmplitude = 0.25f
        liveRecordingSeconds = 0

        // 1. Play audible cyber-HUD activation chime
        VoiceAudioEngine.playHudChime(coroutineScope, ascending = true)

        // 2. Immediately update TRANSCRIPT ("YOU") so user sees live listening state
        viewModel.updateLiveUserSpeech("🎙️ শুনছি... এখন কথা বলুন")
        viewModel.setOrbState(OrbState.LISTENING)

        // 3. Run SpeechRecognizer EXCLUSIVELY (do NOT start AudioRecord at the same time!)
        if (speechRecognizer != null) {
            try {
                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, preferredLanguage)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, preferredLanguage)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 5)
                }
                speechRecognizer.startListening(intent)
            } catch (_: Exception) {
                startHardwareWavFallback()
            }
        } else {
            // Device/emulator has no SpeechRecognizer -> use hardware AudioRecord WAV -> Gemini Audio
            startHardwareWavFallback()
        }
    }

    val audioPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            startVoiceListening()
        } else {
            viewModel.setActionStatusBanner("ভয়েস কথোপকথনের জন্য মাইক্রোফোন পারমিশন প্রয়োজন। নিচের ভয়েস চিপে ট্যাপ করুন বা লিখুন।")
        }
    }

    val handleListenOrStopToggle: () -> Unit = {
        when (orbState) {
            OrbState.LISTENING -> {
                // User tapped STOP while speaking -> finish recognition & process speech immediately!
                VoiceAudioEngine.stopMicRecording()
                try {
                    speechRecognizer?.stopListening()
                } catch (_: Exception) {}
                if (lastPartialRecognizedSpeech.isNotBlank() && !speechRecognizerHandledTurn) {
                    speechRecognizerHandledTurn = true
                    VoiceAudioEngine.playHudChime(coroutineScope, ascending = false)
                    viewModel.sendUserCommandOrSpeech(lastPartialRecognizedSpeech)
                }
            }
            OrbState.SPEAKING, OrbState.THINKING -> {
                VoiceAudioEngine.stopMicRecording()
                ttsEngine?.stop()
                try {
                    speechRecognizer?.stopListening()
                } catch (_: Exception) {}
                viewModel.setOrbState(OrbState.IDLE)
            }
            OrbState.IDLE -> {
                val hasAudioPerm = ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.RECORD_AUDIO
                ) == PackageManager.PERMISSION_GRANTED
                if (hasAudioPerm) {
                    startVoiceListening()
                } else {
                    audioPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            }
        }
    }

    // Collect TTS speech events from ViewModel and speak using resilient bilingual + phonetic engine
    LaunchedEffect(ttsEngine, voiceMuted) {
        viewModel.speakEvent.collect { payload ->
            if (!voiceMuted && ttsEngine != null) {
                VoiceAudioEngine.playHudChime(coroutineScope, ascending = true)
                VoiceAudioEngine.speakSmartBilingual(
                    context = context,
                    tts = ttsEngine,
                    text = payload.text,
                    phoneticHint = payload.phoneticHint,
                    onFallbackDone = { viewModel.setOrbState(OrbState.IDLE) }
                )
            } else {
                viewModel.setOrbState(OrbState.IDLE)
            }
        }
    }

    // Collect Mobile Action commands from ViewModel
    LaunchedEffect(Unit) {
        viewModel.deviceActionEvent.collect { cmd ->
            MobileActionExecutor.executeAction(
                context = context,
                command = cmd,
                onPickVideoForTikTok = {
                    tikTokVideoPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                    )
                },
                onOpenGalleryPicker = {
                    galleryMediaPickerLauncher.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageAndVideo)
                    )
                },
                onRequestCameraPhoto = {
                    pendingCameraVideoMode = false
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        MobileActionExecutor.launchCameraIntent(context, false) { viewModel.setActionStatusBanner(it) }
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onRequestCameraVideo = {
                    pendingCameraVideoMode = true
                    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        MobileActionExecutor.launchCameraIntent(context, true) { viewModel.setActionStatusBanner(it) }
                    } else {
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                },
                onTorchStateChanged = { enabled ->
                    isTorchOnState = enabled
                    refreshDeviceTelemetry()
                },
                onBrightnessStateChanged = { level ->
                    brightnessPercentState = level
                    refreshDeviceTelemetry()
                },
                onTelemetryChanged = {
                    refreshDeviceTelemetry()
                },
                onActionStatus = { msg -> viewModel.setActionStatusBanner(msg) }
            )
        }
    }

    val tasks = (tasksState as? DataUiState.Success)?.data.orEmpty()
    val meetings = (meetingsState as? DataUiState.Success)?.data.orEmpty()
    val memories = (memoriesState as? DataUiState.Success)?.data.orEmpty()
    val moodLogs = (moodsState as? DataUiState.Success)?.data.orEmpty()

    // --- 4. SUB-SCREEN OVERLAY ROUTING ---

    when (activeOverlay) {
        ActiveOverlayScreen.MEMORY -> {
            MemoryVaultSheet(
                memories = memories,
                onAddMemory = { content, tag -> viewModel.addMemory(content, tag) },
                onDeleteMemory = { id -> viewModel.deleteMemory(id) },
                onClose = { activeOverlay = ActiveOverlayScreen.NONE }
            )
            return
        }
        ActiveOverlayScreen.CHAT -> {
            ChatAndMobileCommanderSheet(
                chatHistory = chatHistory,
                onSendCommand = { text -> viewModel.sendUserCommandOrSpeech(text) },
                onStartVoiceInput = {
                    activeOverlay = ActiveOverlayScreen.NONE
                    handleListenOrStopToggle()
                },
                onQuickAction = { cmd -> viewModel.triggerQuickDeviceAction(cmd) },
                onClose = { activeOverlay = ActiveOverlayScreen.NONE }
            )
            return
        }
        ActiveOverlayScreen.SOUL -> {
            SoulAndEmotionSheet(
                selectedSoul = selectedSoul,
                detectedEmotion = detectedEmotion,
                moodLogs = moodLogs,
                focusSecondsRemaining = focusSecondsRemaining,
                isFocusTimerRunning = isFocusTimerRunning,
                onSelectSoul = { soul -> viewModel.setSoulPersonality(soul) },
                onToggleFocusTimer = { viewModel.toggleFocusTimer() },
                onResetFocusTimer = { viewModel.resetFocusTimer() },
                onClose = { activeOverlay = ActiveOverlayScreen.NONE }
            )
            return
        }
        ActiveOverlayScreen.TASKS_AND_MEETINGS -> {
            TasksAndMeetingsSheet(
                tasks = tasks,
                meetings = meetings,
                onAddTask = { title, cat, time, prio -> viewModel.addTask(title, cat, time, prio) },
                onToggleTask = { task -> viewModel.toggleTask(task) },
                onDeleteTask = { id -> viewModel.deleteTask(id) },
                onAddMeeting = { title, time, mins, urgent, notes ->
                    viewModel.addMeeting(title, time, mins, urgent, notes)
                },
                onDeleteMeeting = { id -> viewModel.deleteMeeting(id) },
                onSpeakMeetingAlert = { meeting ->
                    val alertSpeech = "জরুরি মিটিং রিমাইন্ডার: ${meeting.title}, শুরু হবে ${meeting.startTime} সময়ে। আমাদের হাতে ${meeting.remindBeforeMinutes} মিনিট সময় আছে।"
                    viewModel.setActionStatusBanner(alertSpeech)
                    VoiceAudioEngine.speakSmartBilingual(
                        context = context,
                        tts = ttsEngine,
                        text = alertSpeech,
                        phoneticHint = "Joruri meeting reminder: ${meeting.title}, shuru hobe ${meeting.startTime} shomoye.",
                        onFallbackDone = { viewModel.setOrbState(OrbState.IDLE) }
                    )
                },
                onClose = { activeOverlay = ActiveOverlayScreen.NONE }
            )
            return
        }
        ActiveOverlayScreen.MOBILE_CONTROL_CENTER -> {
            MobileControlCenterSheet(
                telemetry = deviceTelemetry,
                installedApps = installedApps,
                onExecuteCommand = { cmd -> viewModel.triggerQuickDeviceAction(cmd) },
                onMediaVolumeChange = { pct ->
                    MobileActionExecutor.setMediaVolumePercent(context, pct) { viewModel.setActionStatusBanner(it) }
                    refreshDeviceTelemetry()
                },
                onRingVolumeChange = { pct ->
                    MobileActionExecutor.setRingVolumePercent(context, pct) { viewModel.setActionStatusBanner(it) }
                    refreshDeviceTelemetry()
                },
                onBrightnessChange = { pct ->
                    brightnessPercentState = pct
                    MobileActionExecutor.setWindowBrightness(context, pct) { viewModel.setActionStatusBanner(it) }
                },
                onLaunchInstalledApp = { appInfo ->
                    MobileActionExecutor.launchPackageDirectly(context, appInfo.packageName, appInfo.appName) {
                        viewModel.setActionStatusBanner(it)
                    }
                },
                onClose = { activeOverlay = ActiveOverlayScreen.NONE }
            )
            return
        }
        ActiveOverlayScreen.SETTINGS -> {
            SettingsAndVoiceConfigSheet(
                preferredLanguage = preferredLanguage,
                voiceMuted = voiceMuted,
                userEmail = Firebase.auth.currentUser?.email,
                onSelectLanguage = { lang -> viewModel.setPreferredLanguage(lang) },
                onToggleMute = { viewModel.toggleVoiceMute() },
                onSignOut = {
                    signOutArcher(
                        context = context,
                        credentialManager = credentialManager,
                        onSignOutComplete = {},
                        scope = coroutineScope
                    )
                },
                onClose = { activeOverlay = ActiveOverlayScreen.NONE }
            )
            return
        }
        ActiveOverlayScreen.NONE -> Unit
    }

    // --- 5. MAIN ARCHER AI HUD DASHBOARD ---

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        ObsidianDeep,
                        ObsidianSurface,
                        ObsidianDeep
                    )
                )
            )
            .windowInsetsPadding(WindowInsets.safeDrawing),
        contentAlignment = Alignment.TopCenter
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .widthIn(max = 620.dp)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 14.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // A. TOP BRAND HEADER BAR ("ARCHER AI" + Briefing + Settings)
            ArcherHudTopHeader(
                soulLabel = "${selectedSoul.titleEn} • BAT ${deviceTelemetry.batteryPercent}%",
                onOpenSettings = { activeOverlay = ActiveOverlayScreen.SETTINGS },
                onTriggerBriefing = { viewModel.triggerProactiveDailyBriefing() }
            )

            // B. PROACTIVE MEETING EARLY ALERT / STATUS BANNER
            val urgentMeeting = meetings.firstOrNull { it.isUrgent } ?: meetings.firstOrNull()
            if (statusBanner != null || aiErrorMessage != null || urgentMeeting != null) {
                ProactiveAlertBanner(
                    statusMessage = statusBanner,
                    errorMessage = aiErrorMessage,
                    urgentMeeting = urgentMeeting,
                    onClickMeeting = { activeOverlay = ActiveOverlayScreen.TASKS_AND_MEETINGS },
                    onDismiss = {
                        viewModel.clearStatusBanner()
                        viewModel.clearAiError()
                    }
                )
            }

            // C. NEON GREEN TRANSCRIPT PANEL (ARCHER on Left | YOU on Right + Live Voice Controls)
            ArcherNeonTranscriptBox(
                archerText = archerTranscript,
                userText = userTranscript,
                orbState = orbState,
                liveMicAmplitude = liveMicAmplitude,
                liveRecordingSeconds = liveRecordingSeconds,
                onReplayArcherVoice = { viewModel.speakCurrentArcherTranscriptAgain() },
                onStartOrStopMic = handleListenOrStopToggle,
                onClickBox = { activeOverlay = ActiveOverlayScreen.CHAT }
            )

            // D. 1-TAP SPOKEN COMMAND CHIPS (Works immediately even if emulator mic is muted)
            InstantSpokenVoiceChipStrip(
                onSpeakPhrase = { phrase ->
                    VoiceAudioEngine.playHudChime(coroutineScope, ascending = true)
                    viewModel.sendUserCommandOrSpeech(phrase)
                }
            )

            // E. CIRCUIT NODES (MEMORY, CHAT, SOUL, SETTINGS) + 3D PARTICLE VOICE ORB
            ArcherCircuitAndOrbSection(
                orbState = orbState,
                detectedEmotion = detectedEmotion,
                voiceMuted = voiceMuted,
                onNodeClick = { node ->
                    activeOverlay = when (node) {
                        HudNodeType.MEMORY -> ActiveOverlayScreen.MEMORY
                        HudNodeType.CHAT -> ActiveOverlayScreen.CHAT
                        HudNodeType.SOUL -> ActiveOverlayScreen.SOUL
                        HudNodeType.SETTINGS -> ActiveOverlayScreen.SETTINGS
                    }
                },
                onToggleListenOrStop = handleListenOrStopToggle,
                onToggleMute = { viewModel.toggleVoiceMute() }
            )

            // F. FULL PHONE CONTROL CENTER BANNER + QUICK HARDWARE ACTIONS STRIP
            QuickMobileActionBar(
                isTorchOn = deviceTelemetry.isTorchOn,
                isSilentMode = deviceTelemetry.isSilentMode,
                onOpenFullControlCenter = {
                    refreshDeviceTelemetry()
                    activeOverlay = ActiveOverlayScreen.MOBILE_CONTROL_CENTER
                },
                onAction = { cmd -> viewModel.triggerQuickDeviceAction(cmd) }
            )

            // G. BOTTOM TWO-COLUMN SECTION: TODAY HEADLINES (Left) & TODAY TASKS + STREAK (Right)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.Top
            ) {
                TodayHeadlinesColumn(
                    headlines = headlines,
                    onRefreshBriefing = { viewModel.triggerProactiveDailyBriefing() },
                    modifier = Modifier.weight(0.48f)
                )

                TodayTasksCard(
                    tasks = tasks,
                    onToggleTask = { task -> viewModel.toggleTask(task) },
                    onOpenAllTasks = { activeOverlay = ActiveOverlayScreen.TASKS_AND_MEETINGS },
                    modifier = Modifier.weight(0.52f)
                )
            }

            // H. QUICK COMMAND BAR AT BOTTOM (Speak with Mic button or Type Any Command Directly)
            QuickCommandInputRow(
                text = quickInputText,
                isListening = orbState == OrbState.LISTENING,
                onTextChange = { quickInputText = it },
                onMicClick = handleListenOrStopToggle,
                onSend = {
                    if (quickInputText.isNotBlank()) {
                        viewModel.sendUserCommandOrSpeech(quickInputText)
                        quickInputText = ""
                    }
                }
            )
        }
    }
}

@Composable
private fun ArcherHudTopHeader(
    soulLabel: String,
    onOpenSettings: () -> Unit,
    onTriggerBriefing: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Surface(
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable { onTriggerBriefing() }
                .testTag("daily_briefing_button"),
            shape = RoundedCornerShape(12.dp),
            color = ObsidianCard,
            border = BorderStroke(1.2.dp, NeonQuantumCyan.copy(alpha = 0.7f))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = "AI Daily Briefing",
                    tint = NeonQuantumCyan,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "BRIEFING",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonQuantumCyan
                )
            }
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(10.dp),
                    shape = CircleShape,
                    color = NeonMatrixGreen
                ) {}
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "ARCHER AI",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryHud,
                    letterSpacing = 2.4.sp
                )
            }
            Text(
                text = soulLabel.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = NeonMatrixGreen
            )
        }

        Surface(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(12.dp))
                .clickable { onOpenSettings() }
                .testTag("top_settings_button"),
            shape = RoundedCornerShape(12.dp),
            color = NeonMatrixGreen,
            border = BorderStroke(1.5.dp, NeonMatrixGreen)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Settings",
                    tint = ObsidianDeep,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun ProactiveAlertBanner(
    statusMessage: String?,
    errorMessage: String?,
    urgentMeeting: MeetingAlert?,
    onClickMeeting: () -> Unit,
    onDismiss: () -> Unit
) {
    val isError = !errorMessage.isNullOrBlank()
    val borderColor = when {
        isError -> NeonCrimson
        !statusMessage.isNullOrBlank() -> NeonMatrixGreen
        else -> NeonSolarAmber
    }
    val displayText = errorMessage
        ?: statusMessage
        ?: urgentMeeting?.let { "⏰ জরুরি মিটিং অ্যালার্ট: '${it.title}' (${it.startTime}) — ${it.remindBeforeMinutes} মিনিট আগে রিমাইন্ডার সক্রিয়" }
        ?: return

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable { onClickMeeting() }
            .testTag("proactive_alert_banner"),
        shape = RoundedCornerShape(14.dp),
        color = ObsidianCard,
        border = BorderStroke(1.3.dp, borderColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.NotificationsActive,
                contentDescription = null,
                tint = borderColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = displayText,
                style = MaterialTheme.typography.bodyMedium,
                color = TextPrimaryHud,
                modifier = Modifier.weight(1f),
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            if (statusMessage != null || errorMessage != null) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Dismiss Banner",
                        tint = TextSecondaryHud,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun ArcherNeonTranscriptBox(
    archerText: String,
    userText: String,
    orbState: OrbState,
    liveMicAmplitude: Float,
    liveRecordingSeconds: Int,
    onReplayArcherVoice: () -> Unit,
    onStartOrStopMic: () -> Unit,
    onClickBox: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .clickable { onClickBox() }
            .testTag("transcript_panel"),
        shape = RoundedCornerShape(18.dp),
        color = ObsidianSurface.copy(alpha = 0.94f),
        border = BorderStroke(
            width = 2.dp,
            color = if (orbState == OrbState.LISTENING) NeonQuantumCyan else NeonMatrixGreen
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "TRANSCRIPT",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextPrimaryHud,
                    letterSpacing = 1.5.sp
                )

                // Direct action buttons right inside the Transcript header
                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onReplayArcherVoice() }
                            .testTag("replay_archer_voice_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = ObsidianCard,
                        border = BorderStroke(1.dp, NeonMatrixGreen)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak Archer Reply",
                                tint = NeonMatrixGreen,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "শুনুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = NeonMatrixGreen,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    Surface(
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onStartOrStopMic() }
                            .testTag("transcript_start_mic_button"),
                        shape = RoundedCornerShape(10.dp),
                        color = if (orbState == OrbState.LISTENING) NeonCrimson.copy(alpha = 0.2f) else ObsidianCard,
                        border = BorderStroke(1.dp, if (orbState == OrbState.LISTENING) NeonCrimson else NeonQuantumCyan)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = if (orbState == OrbState.LISTENING) Icons.Default.Stop else Icons.Default.Mic,
                                contentDescription = "Voice Input",
                                tint = if (orbState == OrbState.LISTENING) NeonCrimson else NeonQuantumCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = if (orbState == OrbState.LISTENING) "থামুন (${liveRecordingSeconds}s)" else "মুখে বলুন",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (orbState == OrbState.LISTENING) NeonCrimson else NeonQuantumCyan,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            if (orbState == OrbState.LISTENING) {
                Spacer(modifier = Modifier.height(6.dp))
                LinearProgressIndicator(
                    progress = { liveMicAmplitude.coerceIn(0.12f, 1f) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(5.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = NeonMatrixGreen,
                    trackColor = ObsidianCard
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.Top
            ) {
                // Left Column: ARCHER
                Column(modifier = Modifier.weight(0.54f)) {
                    Text(
                        text = if (orbState == OrbState.SPEAKING) "ARCHER • 🔊 কথা বলছে" else "ARCHER",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonMatrixGreen,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = archerText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimaryHud,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // Right Column: YOU
                Column(
                    modifier = Modifier.weight(0.46f),
                    horizontalAlignment = Alignment.End
                ) {
                    Text(
                        text = if (orbState == OrbState.LISTENING) "🎙️ YOU (শুনছি...)" else "YOU",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonQuantumCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = userText,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextPrimaryHud,
                        maxLines = 4,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
private fun InstantSpokenVoiceChipStrip(
    onSpeakPhrase: (String) -> Unit
) {
    val phrases = remember {
        listOf(
            "ফ্ল্যাশলাইট অন করো",
            "টিকটকে ভিডিও আপলোড করো",
            "ক্যামেরা ওপেন করো",
            "গ্যালারি খোলো",
            "ভলিউম ৯০% করো",
            "ফোন সাইলেন্ট করো",
            "ওয়াইফাই প্যানেল খোলো",
            "ইউটিউবে গান চালাও",
            "আজকের কাজ ও মিটিং বলো",
            "আমার ফোনের ব্যাটারি কত?",
            "আমার মন একটু খারাপ"
        )
    }

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            phrases.forEachIndexed { idx, phrase ->
                Surface(
                    modifier = Modifier
                        .height(34.dp)
                        .clip(RoundedCornerShape(17.dp))
                        .clickable { onSpeakPhrase(phrase) }
                        .testTag("instant_voice_chip_$idx"),
                    shape = RoundedCornerShape(17.dp),
                    color = ObsidianCard,
                    border = BorderStroke(1.dp, NeonQuantumCyan.copy(alpha = 0.65f))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.RecordVoiceOver,
                            contentDescription = null,
                            tint = NeonMatrixGreen,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "\"$phrase\"",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimaryHud,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickMobileActionBar(
    isTorchOn: Boolean,
    isSilentMode: Boolean,
    onOpenFullControlCenter: () -> Unit,
    onAction: (DeviceActionCommand) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        MobileActionPill(
            label = "ফুল ফোন কন্ট্রোল (Full Access)",
            icon = Icons.Default.PhoneAndroid,
            accent = NeonMatrixGreen,
            tag = "open_full_control_center_button",
            onClick = onOpenFullControlCenter
        )
        MobileActionPill(
            label = if (isTorchOn) "ফ্ল্যাশলাইট: ON" else "ফ্ল্যাশলাইট",
            icon = Icons.Default.FlashlightOn,
            accent = NeonGold,
            tag = "quick_action_torch",
            onClick = {
                onAction(
                    DeviceActionCommand(
                        type = if (isTorchOn) MobileActionType.TOGGLE_FLASHLIGHT_OFF else MobileActionType.TOGGLE_FLASHLIGHT_ON
                    )
                )
            }
        )
        MobileActionPill(
            label = if (isSilentMode) "সাইলেন্ট: ON" else "সাউন্ড মোড",
            icon = if (isSilentMode) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
            accent = if (isSilentMode) NeonCrimson else NeonQuantumCyan,
            tag = "quick_action_sound_mode",
            onClick = {
                onAction(
                    DeviceActionCommand(
                        type = if (isSilentMode) MobileActionType.UNMUTE_PHONE else MobileActionType.MUTE_PHONE
                    )
                )
            }
        )
        MobileActionPill(
            label = "Wi-Fi প্যানেল",
            icon = Icons.Default.Wifi,
            accent = NeonQuantumCyan,
            tag = "quick_action_wifi",
            onClick = { onAction(DeviceActionCommand(type = MobileActionType.OPEN_WIFI_PANEL)) }
        )
        MobileActionPill(
            label = "TikTok আপলোড",
            icon = Icons.Default.Share,
            accent = NeonCrimson,
            tag = "quick_action_tiktok",
            onClick = { onAction(DeviceActionCommand(type = MobileActionType.UPLOAD_TIKTOK_VIDEO)) }
        )
        MobileActionPill(
            label = "গ্যালারি",
            icon = Icons.Default.PhotoLibrary,
            accent = NeonQuantumCyan,
            tag = "quick_action_gallery",
            onClick = { onAction(DeviceActionCommand(type = MobileActionType.OPEN_GALLERY)) }
        )
        MobileActionPill(
            label = "ক্যামেরা",
            icon = Icons.Default.CameraAlt,
            accent = NeonMatrixGreen,
            tag = "quick_action_camera",
            onClick = { onAction(DeviceActionCommand(type = MobileActionType.OPEN_CAMERA_PHOTO)) }
        )
        MobileActionPill(
            label = "অ্যালার্ম",
            icon = Icons.Default.Alarm,
            accent = NeonSolarAmber,
            tag = "quick_action_alarm",
            onClick = {
                onAction(
                    DeviceActionCommand(
                        type = MobileActionType.SET_ALARM,
                        payload = "ARCHER AI Routine",
                        hour = 7,
                        minute = 0
                    )
                )
            }
        )
        MobileActionPill(
            label = "ফোন কল",
            icon = Icons.Default.Phone,
            accent = NeonMatrixGreen,
            tag = "quick_action_call",
            onClick = { onAction(DeviceActionCommand(type = MobileActionType.MAKE_CALL)) }
        )
    }
}

@Composable
private fun MobileActionPill(
    label: String,
    icon: ImageVector,
    accent: Color,
    tag: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .height(38.dp)
            .clip(RoundedCornerShape(19.dp))
            .clickable(onClick = onClick)
            .testTag(tag),
        shape = RoundedCornerShape(19.dp),
        color = ObsidianCard,
        border = BorderStroke(1.2.dp, accent.copy(alpha = 0.8f))
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accent,
                modifier = Modifier.size(15.dp)
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
private fun TodayHeadlinesColumn(
    headlines: List<NewsHeadline>,
    onRefreshBriefing: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .clickable { onRefreshBriefing() }
            .padding(vertical = 4.dp)
            .testTag("today_headlines_section")
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "TODAY HEADLINES",
                style = MaterialTheme.typography.labelLarge,
                color = TextPrimaryHud
            )
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Refresh Briefing",
                tint = NeonQuantumCyan,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        headlines.take(4).forEach { item ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .width(3.dp)
                        .height(20.dp)
                        .clip(RoundedCornerShape(2.dp))
                        .background(NeonQuantumCyan)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "${item.source} | ${item.title}",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextPrimaryHud,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "+1 MORE (AI BRIEFING)",
            style = MaterialTheme.typography.labelSmall,
            color = NeonQuantumCyan,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
private fun TodayTasksCard(
    tasks: List<TaskItem>,
    onToggleTask: (TaskItem) -> Unit,
    onOpenAllTasks: () -> Unit,
    modifier: Modifier = Modifier
) {
    val remainingCount = tasks.count { !it.completed }
    val completedCount = tasks.count { it.completed }
    val totalCount = tasks.size.coerceAtLeast(1)
    val progress = (completedCount.toFloat() / totalCount.toFloat()).coerceIn(0.18f, 1f)

    Card(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .clickable { onOpenAllTasks() }
            .testTag("today_tasks_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = ObsidianSurface),
        border = BorderStroke(1.5.dp, NeonSoulWhite.copy(alpha = 0.65f))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "TODAY TASKS",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextPrimaryHud,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "$remainingCount left",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondaryHud
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocalFireDepartment,
                        contentDescription = "Streak",
                        tint = NeonSolarAmber,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "${completedCount + 1}",
                        style = MaterialTheme.typography.labelSmall,
                        color = NeonSolarAmber,
                        fontWeight = FontWeight.Bold
                    )
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = "Open Tasks",
                        tint = TextSecondaryHud,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(5.dp)
                    .clip(RoundedCornerShape(3.dp)),
                color = NeonQuantumCyan,
                trackColor = ObsidianCard
            )

            Spacer(modifier = Modifier.height(10.dp))

            tasks.take(3).forEach { task ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onToggleTask(task) }
                        .padding(vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (task.completed) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = null,
                        tint = if (task.completed) NeonMatrixGreen else NeonQuantumCyan,
                        modifier = Modifier.size(15.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.bodyMedium,
                        color = if (task.completed) TextSecondaryHud else TextPrimaryHud,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun QuickCommandInputRow(
    text: String,
    isListening: Boolean,
    onTextChange: (String) -> Unit,
    onMicClick: () -> Unit,
    onSend: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onMicClick() }
                .testTag("hud_bottom_mic_button"),
            shape = RoundedCornerShape(16.dp),
            color = if (isListening) NeonCrimson else ObsidianCard,
            border = BorderStroke(1.5.dp, if (isListening) NeonCrimson else NeonQuantumCyan)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = "Microphone Voice Input",
                    tint = if (isListening) TextPrimaryHud else NeonQuantumCyan
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        OutlinedTextField(
            value = text,
            onValueChange = onTextChange,
            placeholder = {
                Text(
                    "বলুন বা লিখুন: 'ফ্ল্যাশলাইট অন করো', 'TikTok ভিডিও আপলোড'...",
                    color = TextSecondaryHud,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            },
            singleLine = true,
            modifier = Modifier
                .weight(1f)
                .testTag("hud_quick_command_input"),
            shape = RoundedCornerShape(16.dp),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NeonMatrixGreen,
                unfocusedBorderColor = BorderHudGreen,
                focusedTextColor = TextPrimaryHud,
                unfocusedTextColor = TextPrimaryHud,
                cursorColor = NeonMatrixGreen,
                focusedContainerColor = ObsidianCard,
                unfocusedContainerColor = ObsidianCard
            )
        )
        Spacer(modifier = Modifier.width(8.dp))
        Surface(
            modifier = Modifier
                .size(50.dp)
                .clip(RoundedCornerShape(16.dp))
                .clickable { onSend() }
                .testTag("hud_quick_command_send"),
            shape = RoundedCornerShape(16.dp),
            color = NeonMatrixGreen
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send to Archer AI",
                    tint = ObsidianDeep
                )
            }
        }
    }
}
