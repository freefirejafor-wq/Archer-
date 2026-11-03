package com.example.ui

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.ChatTurn
import com.example.data.model.DeviceActionCommand
import com.example.data.model.DeviceTelemetry
import com.example.data.model.MeetingAlert
import com.example.data.model.MemoryEntry
import com.example.data.model.MobileActionType
import com.example.data.model.MoodLog
import com.example.data.model.NewsHeadline
import com.example.data.model.OrbState
import com.example.data.model.SoulPersonality
import com.example.data.model.TaskItem
import com.example.data.repository.ArcherRepository
import com.google.firebase.Firebase
import com.google.firebase.ai.ai
import com.google.firebase.ai.type.Schema
import com.google.firebase.ai.type.content
import com.google.firebase.ai.type.generationConfig
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeout
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

sealed interface DataUiState<out T> {
    data object Loading : DataUiState<Nothing>
    data class Success<T>(val data: T) : DataUiState<T>
    data class Error(val message: String) : DataUiState<Nothing>
}

data class SpeakPayload(
    val text: String,
    val phoneticHint: String? = null
)

private data class LocalCommandMatch(
    val spokenReply: String,
    val phoneticReply: String,
    val emotion: String,
    val actionType: MobileActionType,
    val target: String = "",
    val payload: String = "",
    val hour: Int = 8,
    val minute: Int = 0
)

class ArcherViewModel(
    private val repository: ArcherRepository,
    private val currentUserId: String
) : ViewModel() {

    // --- 1. FIRESTORE REAL-TIME STREAMS ---

    val tasksState: StateFlow<DataUiState<List<TaskItem>>> = repository.observeTasks(currentUserId)
        .map<List<TaskItem>, DataUiState<List<TaskItem>>> { DataUiState.Success(it) }
        .catch { emit(DataUiState.Error(it.message ?: "Failed to load tasks")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataUiState.Loading)

    val meetingsState: StateFlow<DataUiState<List<MeetingAlert>>> = repository.observeMeetings(currentUserId)
        .map<List<MeetingAlert>, DataUiState<List<MeetingAlert>>> { DataUiState.Success(it) }
        .catch { emit(DataUiState.Error(it.message ?: "Failed to load meetings")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataUiState.Loading)

    val memoriesState: StateFlow<DataUiState<List<MemoryEntry>>> = repository.observeMemories(currentUserId)
        .map<List<MemoryEntry>, DataUiState<List<MemoryEntry>>> { DataUiState.Success(it) }
        .catch { emit(DataUiState.Error(it.message ?: "Failed to load memories")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataUiState.Loading)

    val moodsState: StateFlow<DataUiState<List<MoodLog>>> = repository.observeMoods(currentUserId)
        .map<List<MoodLog>, DataUiState<List<MoodLog>>> { DataUiState.Success(it) }
        .catch { emit(DataUiState.Error(it.message ?: "Failed to load mood logs")) }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000L), DataUiState.Loading)

    // --- 2. HUD, VOICE & HARDWARE TELEMETRY STATE ---

    private val _orbState = MutableStateFlow(OrbState.IDLE)
    val orbState: StateFlow<OrbState> = _orbState.asStateFlow()

    private val _archerTranscript = MutableStateFlow(
        "নতুন জিনিস শিখছি, তোমাদের সাহায্য করছি, দিনগুলো দারুণ কাটছে! তোমার দিনকাল কেমন যাচ্ছে? যেকোনো কাজের জন্য TALK বাটনে ট্যাপ করে বলো!"
    )
    val archerTranscript: StateFlow<String> = _archerTranscript.asStateFlow()

    private val _userTranscript = MutableStateFlow(
        "দিনকাল কেমন যাচ্ছে? আজ আমার কী কী কাজ বাকি আছে?"
    )
    val userTranscript: StateFlow<String> = _userTranscript.asStateFlow()

    private val _detectedEmotion = MutableStateFlow("Focused")
    val detectedEmotion: StateFlow<String> = _detectedEmotion.asStateFlow()

    private val _selectedSoul = MutableStateFlow(SoulPersonality.EMPATHETIC)
    val selectedSoul: StateFlow<SoulPersonality> = _selectedSoul.asStateFlow()

    private val _voiceMuted = MutableStateFlow(false)
    val voiceMuted: StateFlow<Boolean> = _voiceMuted.asStateFlow()

    private val _preferredLanguage = MutableStateFlow("bn-BD")
    val preferredLanguage: StateFlow<String> = _preferredLanguage.asStateFlow()

    private val _aiErrorMessage = MutableStateFlow<String?>(null)
    val aiErrorMessage: StateFlow<String?> = _aiErrorMessage.asStateFlow()

    private val _statusBanner = MutableStateFlow<String?>(null)
    val statusBanner: StateFlow<String?> = _statusBanner.asStateFlow()

    private val _deviceTelemetry = MutableStateFlow(DeviceTelemetry())
    val deviceTelemetry: StateFlow<DeviceTelemetry> = _deviceTelemetry.asStateFlow()

    private val _chatHistory = MutableStateFlow<List<ChatTurn>>(
        listOf(
            ChatTurn(
                id = "welcome_1",
                userText = "দিনকাল কেমন যাচ্ছে? আজ আমার কী কী কাজ বাকি আছে?",
                archerReply = "নতুন জিনিস শিখছি, তোমাদের সাহায্য করছি, দিনগুলো দারুণ কাটছে! তোমার ফোনের ফুল কন্ট্রোল (Torch, Volume, Wi-Fi, Camera, TikTok, Gallery, Apps) এখন আমার সাথে যুক্ত।",
                detectedEmotion = "Happy",
                actionTriggered = "FULL MOBILE CONTROL READY",
                timestampLabel = "Just now"
            )
        )
    )
    val chatHistory: StateFlow<List<ChatTurn>> = _chatHistory.asStateFlow()

    private val _headlines = MutableStateFlow<List<NewsHeadline>>(
        listOf(
            NewsHeadline(
                id = "h1",
                source = "AI & Tech Daily",
                title = "AI Assistants Now Understand Human Emotion & Execute Mobile Workflows",
                summary = "Next-gen voice companions combine empathetic conversation with direct smartphone automation.",
                category = "AI News"
            ),
            NewsHeadline(
                id = "h2",
                source = "Creator Economy",
                title = "Short-Form Video Creators Boost Engagement with Consistent Daily Schedules",
                summary = "Uploading high-retention clips on TikTok during peak evening hours drives 2.4x reach.",
                category = "Creator"
            ),
            NewsHeadline(
                id = "h3",
                source = "World & Business",
                title = "Global Tech Markets Rally as Mobile Productivity Tools Lead Growth",
                summary = "Smart scheduling and proactive meeting alerts reduce daily workplace stress by 38%.",
                category = "Business"
            ),
            NewsHeadline(
                id = "h4",
                source = "Bangladesh & South Asia",
                title = "বাংলা ভয়েস প্রযুক্তির নতুন দিগন্ত: মোবাইলের সব কাজ এখন মুখের কথায়",
                summary = "বাংলা ও ইংরেজি উভয় ভাষায় আবেগ বুঝে কথা বলা AI সহকারীর জনপ্রিয়তা দ্রুত বাড়ছে।",
                category = "Tech BD"
            )
        )
    )
    val headlines: StateFlow<List<NewsHeadline>> = _headlines.asStateFlow()

    // Focus Pomodoro Timer State
    private val _focusSecondsRemaining = MutableStateFlow(25 * 60)
    val focusSecondsRemaining: StateFlow<Int> = _focusSecondsRemaining.asStateFlow()

    private val _isFocusTimerRunning = MutableStateFlow(false)
    val isFocusTimerRunning: StateFlow<Boolean> = _isFocusTimerRunning.asStateFlow()
    private var focusTimerJob: Job? = null

    // One-shot events for TTS speech and Mobile Device Actions
    private val _speakEvent = MutableSharedFlow<SpeakPayload>(extraBufferCapacity = 6)
    val speakEvent: SharedFlow<SpeakPayload> = _speakEvent.asSharedFlow()

    private val _deviceActionEvent = MutableSharedFlow<DeviceActionCommand>(extraBufferCapacity = 6)
    val deviceActionEvent: SharedFlow<DeviceActionCommand> = _deviceActionEvent.asSharedFlow()

    // --- 3. FIREBASE AI LOGIC (GEMINI 3.8 FLASH) WITH STREAMLINED SCHEMA + PLAIN MODEL FALLBACK ---

    private val assistantResponseSchema = Schema.obj(
        properties = mapOf(
            "userSpokenTranscript" to Schema.string(
                description = "Exact transcription of what the user said in Bengali or English."
            ),
            "spokenReply" to Schema.string(
                description = "Unique, specific, emotion-aware spoken response in Bengali (or English) directly answering what the user said."
            ),
            "spokenReplyPhonetic" to Schema.string(
                description = "Romanized phonetic version of spokenReply so an English TTS voice can pronounce it naturally."
            ),
            "detectedEmotion" to Schema.enumeration(
                values = listOf("Calm", "Happy", "Focused", "Stressed", "Tired", "Excited"),
                description = "Detected emotional state of the user."
            ),
            "energyScore" to Schema.integer(
                description = "Estimated user energy score between 20 and 100."
            ),
            "actionType" to Schema.string(
                description = "Action code: NONE, UPLOAD_TIKTOK_VIDEO, OPEN_CAMERA_PHOTO, OPEN_CAMERA_VIDEO, OPEN_GALLERY, SET_ALARM, SET_TIMER, MAKE_CALL, SEND_SMS, SEND_WHATSAPP, OPEN_APP, ADD_TASK, ADD_MEETING, SAVE_MEMORY, TOGGLE_FLASHLIGHT_ON, TOGGLE_FLASHLIGHT_OFF, SET_VOLUME_LEVEL, MUTE_PHONE, UNMUTE_PHONE, SET_BRIGHTNESS, OPEN_WIFI_PANEL, OPEN_BLUETOOTH_SETTINGS, OPEN_SOUND_PANEL, OPEN_DISPLAY_SETTINGS, OPEN_HOTSPOT_SETTINGS, OPEN_LOCATION_SETTINGS, NAVIGATE_MAPS, SEARCH_YOUTUBE, SEARCH_WEB, ADD_CALENDAR_EVENT, COPY_CLIPBOARD, or VIBRATE_DEVICE"
            ),
            "actionTarget" to Schema.string(
                description = "Optional target app name, phone number, map destination, or time string."
            ),
            "actionPayload" to Schema.string(
                description = "Optional task title, meeting title, memory note, search query, or message text."
            ),
            "alarmHour" to Schema.integer(
                description = "Optional hour in 24h format (0-23)."
            ),
            "alarmMinute" to Schema.integer(
                description = "Optional minute (0-59), duration, or percentage (0-100)."
            )
        ),
        optionalProperties = listOf(
            "spokenReplyPhonetic",
            "energyScore",
            "actionTarget",
            "actionPayload",
            "alarmHour",
            "alarmMinute"
        )
    )

    private val briefingSchema = Schema.obj(
        properties = mapOf(
            "briefingSpeech" to Schema.string(
                description = "Spoken morning/daily briefing in Bengali summarizing tasks, meetings, and top news."
            ),
            "briefingPhonetic" to Schema.string(
                description = "Phonetic Romanized pronunciation of briefingSpeech."
            ),
            "headlines" to Schema.array(
                items = Schema.obj(
                    properties = mapOf(
                        "source" to Schema.string(description = "News source or category label"),
                        "title" to Schema.string(description = "Headline title"),
                        "summary" to Schema.string(description = "1-sentence summary"),
                        "category" to Schema.string(description = "Short tag")
                    )
                ),
                description = "4 concise daily news & productivity headlines"
            )
        ),
        optionalProperties = listOf("briefingPhonetic")
    )

    private val assistantModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = assistantResponseSchema
            }
        )
    }

    // Unconstrained conversational model fallback so Gemini can always answer even if schema decoding fails
    private val plainAssistantModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash"
        )
    }

    private val briefingModel by lazy {
        Firebase.ai.generativeModel(
            modelName = "gemini-3.8-flash",
            generationConfig = generationConfig {
                responseMimeType = "application/json"
                responseSchema = briefingSchema
            }
        )
    }

    init {
        viewModelScope.launch {
            repository.ensureStarterDataIfEmpty(currentUserId)
        }
    }

    // --- 4. TELEMETRY & STATE UPDATES ---

    fun updateTelemetry(telemetry: DeviceTelemetry) {
        _deviceTelemetry.value = telemetry
    }

    fun setOrbState(state: OrbState) {
        _orbState.value = state
    }

    fun updateLiveUserSpeech(partialOrFinalText: String) {
        if (partialOrFinalText.isNotBlank()) {
            _userTranscript.value = partialOrFinalText
        }
    }

    fun toggleVoiceMute() {
        _voiceMuted.value = !_voiceMuted.value
        if (!_voiceMuted.value) {
            speakCurrentArcherTranscriptAgain()
        }
    }

    fun speakCurrentArcherTranscriptAgain() {
        val currentText = _archerTranscript.value
        if (currentText.isNotBlank() && !_voiceMuted.value) {
            _orbState.value = OrbState.SPEAKING
            _speakEvent.tryEmit(SpeakPayload(text = currentText))
        }
    }

    fun setSoulPersonality(soul: SoulPersonality) {
        _selectedSoul.value = soul
        val msg = "সোল মোড পরিবর্তন করা হয়েছে: ${soul.titleBn}"
        _statusBanner.value = msg
        _archerTranscript.value = msg
        if (!_voiceMuted.value) {
            _speakEvent.tryEmit(SpeakPayload(text = msg))
        }
    }

    fun setPreferredLanguage(langCode: String) {
        _preferredLanguage.value = langCode
    }

    fun clearStatusBanner() {
        _statusBanner.value = null
    }

    fun setActionStatusBanner(message: String) {
        _statusBanner.value = message
    }

    fun clearAiError() {
        _aiErrorMessage.value = null
    }

    // --- 5. MULTIMODAL VOICE AUDIO PROCESSING (WHEN SPEECHRECOGNIZER IS UNAVAILABLE) ---

    fun sendUserVoiceAudio(wavBytes: ByteArray, maxRmsAmplitude: Double) {
        _aiErrorMessage.value = null

        if (maxRmsAmplitude < 60.0) {
            val helpReply = "মাইক্রোফোনে কোনো শব্দ শোনা যায়নি। অনুগ্রহ করে মাইকের কাছে কথা বলুন অথবা নিচের ভয়েস কমান্ড চিপে ট্যাপ করুন।"
            _userTranscript.value = "🎙️ (শব্দ শোনা যায়নি — আবার বলুন)"
            _archerTranscript.value = helpReply
            _orbState.value = OrbState.IDLE
            return
        }

        _userTranscript.value = "🎙️ আপনার কথা লেখা হচ্ছে..."
        _orbState.value = OrbState.THINKING

        viewModelScope.launch {
            try {
                val promptText = buildSystemPrompt(
                    userMessage = "Transcribe the exact Bengali or English words spoken by the user in this audio into 'userSpokenTranscript'. Then answer their exact question or command in 'spokenReply' (never give a generic response) and set 'actionType' if they asked for a phone action."
                )

                val multimodalInput = content {
                    inlineData(wavBytes, "audio/wav")
                    text(promptText)
                }

                val response = withTimeout(60_000L) {
                    assistantModel.generateContent(multimodalInput)
                }

                val jsonString = response.text
                if (jsonString.isNullOrBlank()) {
                    throw IllegalStateException("Empty multimodal voice response")
                }

                handleParsedAiResponse(jsonString, fallbackUserText = "")
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Structured audio AI failed, trying plain multimodal transcription", e)
                try {
                    val plainInput = content {
                        inlineData(wavBytes, "audio/wav")
                        text(
                            """
                            Listen to this audio.
                            Line 1: Write ONLY the exact words the user spoke (in Bengali or English).
                            Line 2: Write a warm, specific reply in Bengali answering what the user said.
                            """.trimIndent()
                        )
                    }
                    val plainResp = withTimeout(45_000L) {
                        plainAssistantModel.generateContent(plainInput)
                    }
                    val lines = plainResp.text.orEmpty().lines().map { it.trim() }.filter { it.isNotBlank() }
                    val transcribed = lines.firstOrNull()?.removePrefix("Line 1:")?.trim()
                        ?: "🎙️ ভয়েস মেসেজ"
                    val reply = lines.drop(1).joinToString(" ").removePrefix("Line 2:").trim()
                        .ifBlank { generateContextualReplyForInput(transcribed).spokenReply }

                    _userTranscript.value = transcribed
                    // Check if the transcribed text triggers a local mobile command!
                    val instantCmd = matchInstantMobileCommand(transcribed)
                    if (instantCmd != null) {
                        executeMatchedActionAndRecordHistory(
                            userText = transcribed,
                            spokenReply = instantCmd.spokenReply,
                            phoneticReply = instantCmd.phoneticReply,
                            emotion = instantCmd.emotion,
                            energyScore = 88L,
                            actionType = instantCmd.actionType,
                            actionTarget = instantCmd.target,
                            actionPayload = instantCmd.payload,
                            alarmHour = instantCmd.hour,
                            alarmMinute = instantCmd.minute
                        )
                    } else {
                        executeMatchedActionAndRecordHistory(
                            userText = transcribed,
                            spokenReply = reply,
                            phoneticReply = "",
                            emotion = "Focused",
                            energyScore = 85L,
                            actionType = MobileActionType.NONE,
                            actionTarget = "",
                            actionPayload = "",
                            alarmHour = 8,
                            alarmMinute = 0
                        )
                    }
                } catch (innerEx: CancellationException) {
                    throw innerEx
                } catch (innerEx: Exception) {
                    Log.e(TAG, "Plain multimodal audio fallback also failed", innerEx)
                    _orbState.value = OrbState.IDLE
                    _aiErrorMessage.value = "ভয়েস ট্রান্সক্রিপশন সার্ভিসে সংযোগ পাওয়া যায়নি (${innerEx.localizedMessage ?: "Network/AI Error"})।"
                }
            }
        }
    }

    // --- 6. VOICE & TEXT CONVERSATION ORCHESTRATION ---

    fun sendUserCommandOrSpeech(userInput: String) {
        val cleanInput = userInput.removePrefix("🎙️").trim()
        if (cleanInput.isEmpty()) return

        // 1. Immediately display the exact words the user spoke in the "YOU" transcript box!
        _userTranscript.value = cleanInput
        _aiErrorMessage.value = null

        // 2. Check if the user's speech/text matches a direct mobile hardware/app command
        val instantMatch = matchInstantMobileCommand(cleanInput)
        if (instantMatch != null) {
            _archerTranscript.value = instantMatch.spokenReply
            _detectedEmotion.value = instantMatch.emotion

            viewModelScope.launch {
                executeMatchedActionAndRecordHistory(
                    userText = cleanInput,
                    spokenReply = instantMatch.spokenReply,
                    phoneticReply = instantMatch.phoneticReply,
                    emotion = instantMatch.emotion,
                    energyScore = 88L,
                    actionType = instantMatch.actionType,
                    actionTarget = instantMatch.target,
                    actionPayload = instantMatch.payload,
                    alarmHour = instantMatch.hour,
                    alarmMinute = instantMatch.minute
                )
            }
            return
        }

        // 3. Call Gemini 3.8 Flash for natural, dynamic conversation answering the exact user speech
        _orbState.value = OrbState.THINKING

        viewModelScope.launch {
            try {
                val prompt = buildSystemPrompt(cleanInput)
                val response = withTimeout(60_000L) {
                    assistantModel.generateContent(prompt)
                }

                val jsonString = response.text
                if (jsonString.isNullOrBlank()) {
                    throw IllegalStateException("AI returned an empty response.")
                }

                handleParsedAiResponse(jsonString, fallbackUserText = cleanInput)
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.w(TAG, "Structured AI call failed, trying plain Gemini conversational call", e)
                try {
                    val plainPrompt = """
                        You are ARCHER AI, a warm, empathetic Bengali & English personal AI voice assistant.
                        The user just said: "$cleanInput"
                        Reply directly and specifically to what the user said in natural Bengali (2-3 sentences max). Do not repeat generic greetings.
                    """.trimIndent()
                    val plainResponse = withTimeout(45_000L) {
                        plainAssistantModel.generateContent(plainPrompt)
                    }
                    val replyText = plainResponse.text?.trim().orEmpty()
                    if (replyText.isNotBlank()) {
                        executeMatchedActionAndRecordHistory(
                            userText = cleanInput,
                            spokenReply = replyText,
                            phoneticReply = "",
                            emotion = "Happy",
                            energyScore = 85L,
                            actionType = MobileActionType.NONE,
                            actionTarget = "",
                            actionPayload = "",
                            alarmHour = 8,
                            alarmMinute = 0
                        )
                    } else {
                        throw IllegalStateException("Empty plain response")
                    }
                } catch (innerEx: CancellationException) {
                    throw innerEx
                } catch (innerEx: Exception) {
                    Log.e(TAG, "Plain AI call also failed, using contextual analyzer", innerEx)
                    val contextual = generateContextualReplyForInput(cleanInput)
                    executeMatchedActionAndRecordHistory(
                        userText = cleanInput,
                        spokenReply = contextual.spokenReply,
                        phoneticReply = contextual.phoneticReply,
                        emotion = contextual.emotion,
                        energyScore = 82L,
                        actionType = contextual.actionType,
                        actionTarget = contextual.target,
                        actionPayload = contextual.payload,
                        alarmHour = contextual.hour,
                        alarmMinute = contextual.minute
                    )
                }
            }
        }
    }

    private fun buildSystemPrompt(userMessage: String): String {
        val currentTasks = (tasksState.value as? DataUiState.Success)?.data.orEmpty()
        val currentMeetings = (meetingsState.value as? DataUiState.Success)?.data.orEmpty()
        val currentMemories = (memoriesState.value as? DataUiState.Success)?.data.orEmpty()
        val tel = _deviceTelemetry.value
        val soul = _selectedSoul.value
        val recentTurns = _chatHistory.value.take(3).joinToString("\n") {
            "User: ${it.userText} -> Archer: ${it.archerReply}"
        }

        val tasksSummary = currentTasks.take(5).joinToString("; ") {
            "${it.title} (${if (it.completed) "Done" else "Pending at ${it.dueTime}"})"
        }.ifEmpty { "No tasks yet" }

        val meetingsSummary = currentMeetings.take(4).joinToString("; ") {
            "${it.title} at ${it.startTime} (Alert ${it.remindBeforeMinutes}m before)"
        }.ifEmpty { "No meetings scheduled" }

        val memoriesSummary = currentMemories.take(5).joinToString("; ") {
            "[${it.tag}] ${it.content}"
        }.ifEmpty { "No saved memories" }

        return """
            You are ARCHER AI, an emotion-aware bilingual (Bengali & English) personal AI assistant and FULL MOBILE CONTROL COMMANDER on Android.
            Current Personality Mode: ${soul.id} — ${soul.systemInstruction}
            Preferred Language: ${if (_preferredLanguage.value == "bn-BD") "Bengali (বাংলা) primarily, with natural English terms" else "English & Bengali"}
            
            IMPORTANT: Give a SPECIFIC, UNIQUE answer to the user's exact words below. NEVER repeat generic boilerplate sentences.
            Recent Conversation Context:
            $recentTurns
            
            Live Phone Hardware Telemetry:
            - Battery: ${tel.batteryPercent}% (${if (tel.isCharging) "Charging" else "On Battery"}, ${tel.batteryTempCelsius}°C)
            - Available RAM: ${tel.availableRamGb} / ${tel.totalRamGb}
            - Free Storage: ${tel.freeStorageGb} / ${tel.totalStorageGb}
            - Media Volume: ${tel.mediaVolumePercent}%, Ring Volume: ${tel.ringVolumePercent}%, Silent Mode: ${tel.isSilentMode}
            - Flashlight Torch: ${if (tel.isTorchOn) "ON" else "OFF"}, Screen Brightness: ${tel.brightnessPercent}%
            
            User's Current Tasks: $tasksSummary
            User's Upcoming Meetings: $meetingsSummary
            User's Smart Memory Vault: $memoriesSummary
            
            Action codes for 'actionType':
            - Upload/share video to TikTok -> "UPLOAD_TIKTOK_VIDEO"
            - Take photo / open camera -> "OPEN_CAMERA_PHOTO"
            - Record video -> "OPEN_CAMERA_VIDEO"
            - Open gallery / photos -> "OPEN_GALLERY"
            - Turn flashlight / torch ON -> "TOGGLE_FLASHLIGHT_ON"
            - Turn flashlight / torch OFF -> "TOGGLE_FLASHLIGHT_OFF"
            - Set volume level -> "SET_VOLUME_LEVEL", alarmMinute = percentage (0-100)
            - Mute / silent phone -> "MUTE_PHONE"
            - Unmute / normal sound -> "UNMUTE_PHONE"
            - Change screen brightness -> "SET_BRIGHTNESS", alarmMinute = percentage (5-100)
            - Open Wi-Fi / Internet panel -> "OPEN_WIFI_PANEL"
            - Open Bluetooth settings -> "OPEN_BLUETOOTH_SETTINGS"
            - Open Sound / Volume panel -> "OPEN_SOUND_PANEL"
            - Open Display settings -> "OPEN_DISPLAY_SETTINGS"
            - Open Hotspot / Wireless settings -> "OPEN_HOTSPOT_SETTINGS"
            - Open Location / GPS settings -> "OPEN_LOCATION_SETTINGS"
            - Navigate on Google Maps -> "NAVIGATE_MAPS", actionTarget = destination
            - Search or play on YouTube -> "SEARCH_YOUTUBE", actionPayload = search query
            - Search Google / Web -> "SEARCH_WEB", actionPayload = search query
            - Add event to phone Calendar -> "ADD_CALENDAR_EVENT", actionPayload = event title
            - Copy text to clipboard -> "COPY_CLIPBOARD", actionPayload = text to copy
            - Vibrate phone -> "VIBRATE_DEVICE"
            - Set alarm -> "SET_ALARM", alarmHour (0-23), alarmMinute (0-59)
            - Set timer -> "SET_TIMER", alarmMinute = duration in minutes
            - Call phone number -> "MAKE_CALL", actionTarget = phone number
            - Send SMS -> "SEND_SMS", actionTarget = number, actionPayload = message
            - Send WhatsApp -> "SEND_WHATSAPP", actionPayload = message
            - Open any app -> "OPEN_APP", actionTarget = app name
            - Add task/routine -> "ADD_TASK", actionPayload = task title, actionTarget = time
            - Add urgent meeting reminder -> "ADD_MEETING", actionPayload = meeting title, actionTarget = start time
            - Save personal note/memory -> "SAVE_MEMORY", actionPayload = memory fact
            - Otherwise -> "NONE"
            
            User input: "$userMessage"
        """.trimIndent()
    }

    private suspend fun handleParsedAiResponse(jsonString: String, fallbackUserText: String) {
        val json = JSONObject(jsonString)
        val rawTranscribed = json.optString("userSpokenTranscript", "").trim()
        val transcribedUser = when {
            fallbackUserText.isNotBlank() -> fallbackUserText
            rawTranscribed.isNotBlank() -> rawTranscribed
            else -> _userTranscript.value
        }
        val spokenReply = json.optString("spokenReply", "").trim()
            .ifBlank { generateContextualReplyForInput(transcribedUser).spokenReply }
        val phoneticReply = json.optString("spokenReplyPhonetic", "")
        val emotion = json.optString("detectedEmotion", "Focused")
        val energyScore = json.optLong("energyScore", 85L)
        val actionTypeStr = json.optString("actionType", "NONE").trim().uppercase(Locale.US)
        val actionTarget = json.optString("actionTarget", "")
        val actionPayload = json.optString("actionPayload", "")
        val alarmHour = json.optInt("alarmHour", 8)
        val alarmMinute = json.optInt("alarmMinute", 0)

        _userTranscript.value = transcribedUser

        val parsedActionType = try {
            MobileActionType.valueOf(actionTypeStr)
        } catch (_: Exception) {
            MobileActionType.NONE
        }

        executeMatchedActionAndRecordHistory(
            userText = transcribedUser,
            spokenReply = spokenReply,
            phoneticReply = phoneticReply,
            emotion = emotion,
            energyScore = energyScore,
            actionType = parsedActionType,
            actionTarget = actionTarget,
            actionPayload = actionPayload,
            alarmHour = alarmHour,
            alarmMinute = alarmMinute
        )
    }

    private suspend fun executeMatchedActionAndRecordHistory(
        userText: String,
        spokenReply: String,
        phoneticReply: String,
        emotion: String,
        energyScore: Long,
        actionType: MobileActionType,
        actionTarget: String,
        actionPayload: String,
        alarmHour: Int,
        alarmMinute: Int
    ) {
        _userTranscript.value = userText
        _archerTranscript.value = spokenReply
        _detectedEmotion.value = emotion

        try {
            repository.logMood(
                emotion = emotion,
                soulMode = _selectedSoul.value.id,
                note = userText.take(150),
                energyScore = energyScore
            )
        } catch (_: Exception) {}

        var actionLabel: String? = null
        when (actionType) {
            MobileActionType.ADD_TASK -> {
                val title = actionPayload.ifBlank { userText }
                val time = actionTarget.ifBlank { "06:00 PM" }
                try {
                    repository.createTask(title = title, category = "Routine", dueTime = time, priority = "HIGH")
                } catch (_: Exception) {}
                actionLabel = "টাস্ক যুক্ত হয়েছে: $title"
                _statusBanner.value = actionLabel
            }
            MobileActionType.ADD_MEETING -> {
                val title = actionPayload.ifBlank { userText }
                val time = actionTarget.ifBlank { "05:00 PM" }
                try {
                    repository.createMeeting(
                        title = title,
                        startTime = time,
                        remindBeforeMinutes = 15L,
                        isUrgent = true,
                        notes = "Added via Voice AI"
                    )
                } catch (_: Exception) {}
                actionLabel = "জরুরি মিটিং অ্যালার্ট সেট হয়েছে: $title ($time)"
                _statusBanner.value = actionLabel
            }
            MobileActionType.SAVE_MEMORY -> {
                val content = actionPayload.ifBlank { userText }
                try {
                    repository.createMemory(
                        content = content,
                        tag = "VoiceNote",
                        emotionContext = emotion
                    )
                } catch (_: Exception) {}
                actionLabel = "মেমোরি ভল্টে সংরক্ষিত হয়েছে"
                _statusBanner.value = actionLabel
            }
            MobileActionType.NONE -> Unit
            else -> {
                actionLabel = "মোবাইল কন্ট্রোল: ${actionType.name}"
                _deviceActionEvent.tryEmit(
                    DeviceActionCommand(
                        type = actionType,
                        target = actionTarget,
                        payload = actionPayload,
                        hour = alarmHour,
                        minute = alarmMinute
                    )
                )
            }
        }

        val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
        _chatHistory.value = listOf(
            ChatTurn(
                id = UUID.randomUUID().toString(),
                userText = userText,
                archerReply = spokenReply,
                detectedEmotion = emotion,
                actionTriggered = actionLabel,
                timestampLabel = timeStr
            )
        ) + _chatHistory.value

        if (!_voiceMuted.value) {
            _orbState.value = OrbState.SPEAKING
            _speakEvent.tryEmit(SpeakPayload(text = spokenReply, phoneticHint = phoneticReply))
        } else {
            _orbState.value = OrbState.IDLE
        }
    }

    /**
     * Instant zero-latency bilingual voice/text command matcher for mobile hardware & app actions.
     */
    private fun matchInstantMobileCommand(input: String): LocalCommandMatch? {
        val lower = input.lowercase(Locale.getDefault()).trim()

        // 1. Flashlight / Torch
        if (lower.contains("ফ্ল্যাশ") || lower.contains("টর্চ") || lower.contains("flashlight") || lower.contains("torch") || lower.contains("লাইট জ্বালাও")) {
            val turnOff = lower.contains("অফ") || lower.contains("বন্ধ") || lower.contains("off")
            return if (turnOff) {
                LocalCommandMatch(
                    spokenReply = "ঠিক আছে, আপনার ফোনের ফ্ল্যাশলাইট (Torch) এখনই অফ করে দিচ্ছি।",
                    phoneticReply = "Thik ache, apnar phoner flashlight ekhoni off kore dichchi.",
                    emotion = "Calm",
                    actionType = MobileActionType.TOGGLE_FLASHLIGHT_OFF
                )
            } else {
                LocalCommandMatch(
                    spokenReply = "এখনই আপনার ফোনের ফ্ল্যাশলাইট (Torch) অন করে দিচ্ছি!",
                    phoneticReply = "Ekhoni apnar phoner flashlight on kore dichchi!",
                    emotion = "Excited",
                    actionType = MobileActionType.TOGGLE_FLASHLIGHT_ON
                )
            }
        }

        // 2. TikTok Video Upload
        if (lower.contains("টিকটক") || lower.contains("টিক টক") || lower.contains("tiktok") || lower.contains("ভিডিও আপলোড")) {
            return LocalCommandMatch(
                spokenReply = "দারুণ! আপনার ভিডিও সিলেক্ট করার জন্য গ্যালারি ওপেন করছি, এখান থেকে সরাসরি TikTok-এ আপলোড করতে পারবেন।",
                phoneticReply = "Darun! Apnar video select korar jonno gallery open korchi, ekhan theke shorashori TikTok e upload korte parben.",
                emotion = "Excited",
                actionType = MobileActionType.UPLOAD_TIKTOK_VIDEO
            )
        }

        // 3. Camera (Photo & Video)
        if (lower.contains("ভিডিও রেকর্ড") || lower.contains("video record") || lower.contains("ভিডিও ক্যামেরা")) {
            return LocalCommandMatch(
                spokenReply = "এখনই ভিডিও রেকর্ডার ক্যামেরা চালু করে দিচ্ছি!",
                phoneticReply = "Ekhoni video recorder camera chalu kore dichchi!",
                emotion = "Happy",
                actionType = MobileActionType.OPEN_CAMERA_VIDEO
            )
        }
        if (lower.contains("ক্যামেরা") || lower.contains("ক্যমেরা") || lower.contains("ছবি তোল") || lower.contains("সেলফি") || lower.contains("camera") || lower.contains("photo")) {
            return LocalCommandMatch(
                spokenReply = "এখনই আপনার ফোনের ক্যামেরা ওপেন করে দিচ্ছি!",
                phoneticReply = "Ekhoni apnar phoner camera open kore dichchi!",
                emotion = "Happy",
                actionType = MobileActionType.OPEN_CAMERA_PHOTO
            )
        }

        // 4. Gallery / Photos
        if (lower.contains("গ্যালারি") || lower.contains("গ্যলারি") || lower.contains("ফটো") || lower.contains("gallery") || lower.contains("photos")) {
            return LocalCommandMatch(
                spokenReply = "আপনার ফোনের গ্যালারি ও মিডিয়া ওপেন করে দিচ্ছি।",
                phoneticReply = "Apnar phoner gallery o media open kore dichchi.",
                emotion = "Focused",
                actionType = MobileActionType.OPEN_GALLERY
            )
        }

        // 5. Silent / Mute / Unmute / Volume
        if (lower.contains("সাইলেন্ট") || lower.contains("মিউট") || lower.contains("silent") || lower.contains("mute")) {
            return LocalCommandMatch(
                spokenReply = "আপনার ফোন এখন সাইলেন্ট (ভাইব্রেট) মোডে করে দিয়েছি।",
                phoneticReply = "Apnar phone ekhon silent mode e kore diyechi.",
                emotion = "Calm",
                actionType = MobileActionType.MUTE_PHONE
            )
        }
        if (lower.contains("আনমিউট") || lower.contains("নরমাল মোড") || lower.contains("সাউন্ড অন") || lower.contains("unmute")) {
            return LocalCommandMatch(
                spokenReply = "ফোনের সাউন্ড আবার নরমাল মোডে চালু করে দিয়েছি!",
                phoneticReply = "Phoner sound abar normal mode e chalu kore diyechi!",
                emotion = "Happy",
                actionType = MobileActionType.UNMUTE_PHONE
            )
        }
        if (lower.contains("ভলিউম") || lower.contains("সাউন্ড বাড়াও") || lower.contains("সাউন্ড কমাও") || lower.contains("volume")) {
            val numberMatch = Regex("(\\d{1,3})").find(lower)?.groupValues?.getOrNull(1)?.toIntOrNull()
            val targetVol = when {
                numberMatch != null -> numberMatch.coerceIn(0, 100)
                lower.contains("কমাও") || lower.contains("down") || lower.contains("low") -> 25
                else -> 90
            }
            return LocalCommandMatch(
                spokenReply = "ফোনের মিডিয়া ভলিউম $targetVol% সেট করে দিয়েছি।",
                phoneticReply = "Phoner media volume $targetVol percent set kore diyechi.",
                emotion = "Focused",
                actionType = MobileActionType.SET_VOLUME_LEVEL,
                minute = targetVol
            )
        }

        // 6. Screen Brightness
        if (lower.contains("ব্রাইটনেস") || lower.contains("আলো বাড়াও") || lower.contains("আলো কমাও") || lower.contains("brightness")) {
            val numberMatch = Regex("(\\d{1,3})").find(lower)?.groupValues?.getOrNull(1)?.toIntOrNull()
            val targetBrt = when {
                numberMatch != null -> numberMatch.coerceIn(10, 100)
                lower.contains("কমাও") || lower.contains("down") || lower.contains("low") -> 25
                else -> 95
            }
            return LocalCommandMatch(
                spokenReply = "স্ক্রিনের ব্রাইটনেস $targetBrt% করে দিয়েছি।",
                phoneticReply = "Screen er brightness $targetBrt percent kore diyechi.",
                emotion = "Focused",
                actionType = MobileActionType.SET_BRIGHTNESS,
                minute = targetBrt
            )
        }

        // 7. Wi-Fi, Bluetooth, Hotspot, Location
        if (lower.contains("ওয়াইফাই") || lower.contains("ওয়াইফাই") || lower.contains("ইন্টারনেট") || lower.contains("wifi") || lower.contains("wi-fi")) {
            return LocalCommandMatch(
                spokenReply = "ওয়াইফাই এবং ইন্টারনেট কন্ট্রোল প্যানেল ওপেন করে দিচ্ছি।",
                phoneticReply = "Wi-Fi ebong internet control panel open kore dichchi.",
                emotion = "Focused",
                actionType = MobileActionType.OPEN_WIFI_PANEL
            )
        }
        if (lower.contains("ব্লুটুথ") || lower.contains("bluetooth")) {
            return LocalCommandMatch(
                spokenReply = "ব্লুটুথ সেটিংস এখনই ওপেন করে দিচ্ছি।",
                phoneticReply = "Bluetooth settings ekhoni open kore dichchi.",
                emotion = "Focused",
                actionType = MobileActionType.OPEN_BLUETOOTH_SETTINGS
            )
        }
        if (lower.contains("হটস্পট") || lower.contains("hotspot")) {
            return LocalCommandMatch(
                spokenReply = "ওয়াইফাই হটস্পট ও ওয়্যারলেস সেটিংস ওপেন করে দিচ্ছি।",
                phoneticReply = "Wi-Fi hotspot settings open kore dichchi.",
                emotion = "Focused",
                actionType = MobileActionType.OPEN_HOTSPOT_SETTINGS
            )
        }
        if (lower.contains("লোকেশন") || lower.contains("জিপিএস") || lower.contains("gps") || lower.contains("location")) {
            return LocalCommandMatch(
                spokenReply = "ফোনের জিপিএস ও লোকেশন সেটিংস ওপেন করে দিচ্ছি।",
                phoneticReply = "Phoner GPS o location settings open kore dichchi.",
                emotion = "Focused",
                actionType = MobileActionType.OPEN_LOCATION_SETTINGS
            )
        }

        // 8. YouTube, Maps, Google Search
        if (lower.contains("ইউটিউব") || lower.contains("youtube") || lower.contains("গান চালাও")) {
            val query = input.replace(Regex("(?i)(ইউটিউবে|ইউটিউব|youtube|চালাও|খোলো|প্লে করো|play|open)"), "").trim()
                .ifBlank { "Bangla & Lofi Hits" }
            return LocalCommandMatch(
                spokenReply = "এখনই ইউটিউবে '$query' ওপেন করে দিচ্ছি!",
                phoneticReply = "Ekhoni YouTube e $query open kore dichchi!",
                emotion = "Excited",
                actionType = MobileActionType.SEARCH_YOUTUBE,
                payload = query
            )
        }
        if (lower.contains("ম্যাপ") || lower.contains("নেভিগেট") || lower.contains("map") || lower.contains("navigate")) {
            val dest = input.replace(Regex("(?i)(ম্যাপে|ম্যাপ|নেভিগেট করো|map|navigate to|open)"), "").trim()
                .ifBlank { "Dhaka" }
            return LocalCommandMatch(
                spokenReply = "গুগল ম্যাপসে '$dest' এর নেভিগেশন চালু করছি।",
                phoneticReply = "Google Maps e $dest er navigation chalu korchi.",
                emotion = "Focused",
                actionType = MobileActionType.NAVIGATE_MAPS,
                target = dest
            )
        }
        if (lower.contains("গুগল সার্চ") || lower.contains("সার্চ করো") || lower.contains("google search") || lower.startsWith("search ")) {
            val query = input.replace(Regex("(?i)(গুগল সার্চ|সার্চ করো|google search|search)"), "").trim()
                .ifBlank { input }
            return LocalCommandMatch(
                spokenReply = "গুগলে '$query' লিখে সার্চ করে দেখাচ্ছি।",
                phoneticReply = "Google e $query likhe search kore dekhachchi.",
                emotion = "Focused",
                actionType = MobileActionType.SEARCH_WEB,
                payload = query
            )
        }

        // 9. Alarm, Timer, Call, WhatsApp, Vibrate
        if (lower.contains("অ্যালার্ম") || lower.contains("এলার্ম") || lower.contains("alarm")) {
            return LocalCommandMatch(
                spokenReply = "আপনার ফোনে অ্যালার্ম সেট করে দিচ্ছি!",
                phoneticReply = "Apnar phone e alarm set kore dichchi!",
                emotion = "Focused",
                actionType = MobileActionType.SET_ALARM,
                payload = "ARCHER AI Alarm",
                hour = 7,
                minute = 0
            )
        }
        if (lower.contains("টাইমার") || lower.contains("timer")) {
            return LocalCommandMatch(
                spokenReply = "১০ মিনিটের কাউন্টডাউন টাইমার চালু করে দিচ্ছি!",
                phoneticReply = "Dosh miniter countdown timer chalu kore dichchi!",
                emotion = "Focused",
                actionType = MobileActionType.SET_TIMER,
                payload = "ARCHER Focus Timer",
                minute = 10
            )
        }
        if (lower.contains("কল দাও") || lower.contains("ফোন দাও") || lower.contains("কল করো") || lower.contains("call ")) {
            val digits = Regex("[+\\d][\\d\\s-]{3,}").find(input)?.value?.trim().orEmpty()
            return LocalCommandMatch(
                spokenReply = "এখনই ফোন ডায়ালার ওপেন করে দিচ্ছি।",
                phoneticReply = "Ekhoni phone dialer open kore dichchi.",
                emotion = "Focused",
                actionType = MobileActionType.MAKE_CALL,
                target = digits
            )
        }
        if (lower.contains("হোয়াটসঅ্যাপ") || lower.contains("হোয়াটসঅ্যাপ") || lower.contains("whatsapp")) {
            return LocalCommandMatch(
                spokenReply = "হোয়াটসঅ্যাপ ওপেন করে মেসেজ পাঠানোর ব্যবস্থা করছি।",
                phoneticReply = "WhatsApp open kore message pathanor bebostha korchi.",
                emotion = "Happy",
                actionType = MobileActionType.SEND_WHATSAPP,
                payload = "Sent from ARCHER AI"
            )
        }
        if (lower.contains("ভাইব্রেট") || lower.contains("vibrate")) {
            return LocalCommandMatch(
                spokenReply = "ডিভাইস হ্যাপটিক ভাইব্রেশন পালস চালু করা হয়েছে!",
                phoneticReply = "Device haptic vibration pulse chalu kora hoyeche!",
                emotion = "Excited",
                actionType = MobileActionType.VIBRATE_DEVICE
            )
        }

        // 10. Open any specific app by name ("ওপেন করো ...", "open ...")
        if (lower.contains("অ্যাপ খোলো") || lower.contains("ওপেন করো") || lower.startsWith("open ")) {
            val targetApp = input.replace(Regex("(?i)(অ্যাপ খোলো|ওপেন করো|খোলো|open|app)"), "").trim()
            if (targetApp.isNotBlank()) {
                return LocalCommandMatch(
                    spokenReply = "এখনই আপনার ফোনে '$targetApp' অ্যাপটি ওপেন করে দিচ্ছি!",
                    phoneticReply = "Ekhoni apnar phone e $targetApp app ti open kore dichchi!",
                    emotion = "Focused",
                    actionType = MobileActionType.OPEN_APP,
                    target = targetApp
                )
            }
        }

        return null
    }

    /**
     * Context-aware dynamic responder that directly addresses the user's spoken words
     * if offline/unreachable, so it NEVER repeats a static boilerplate sentence.
     */
    private fun generateContextualReplyForInput(userInput: String): LocalCommandMatch {
        val lower = userInput.lowercase(Locale.getDefault())
        val tel = _deviceTelemetry.value
        val pendingTasks = ((tasksState.value as? DataUiState.Success)?.data.orEmpty()).filter { !it.completed }
        val meetings = (meetingsState.value as? DataUiState.Success)?.data.orEmpty()
        val nowTime = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())

        return when {
            lower.contains("ব্যাটারি") || lower.contains("চার্জ") || lower.contains("স্টোরেজ") || lower.contains("র‍্যাম") || lower.contains("battery") || lower.contains("storage") || lower.contains("ram") -> {
                LocalCommandMatch(
                    spokenReply = "আপনার ফোনের ব্যাটারি এখন ${tel.batteryPercent}% (${if (tel.isCharging) "চার্জ হচ্ছে" else "ব্যাটারিতে চলছে"}), ফ্রি র‍্যাম ${tel.availableRamGb} এবং ফ্রি স্টোরেজ ${tel.freeStorageGb} খালি আছে।",
                    phoneticReply = "Apnar phoner battery ekhon ${tel.batteryPercent} percent, free RAM ${tel.availableRamGb} ebong free storage ${tel.freeStorageGb} khali ache.",
                    emotion = "Focused",
                    actionType = MobileActionType.NONE
                )
            }
            lower.contains("সময়") || lower.contains("কয়টা বাজে") || lower.contains("time") -> {
                LocalCommandMatch(
                    spokenReply = "এখন ঘড়িতে সময় $nowTime। আপনার আজকের বাকি কাজ রয়েছে ${pendingTasks.size}টি।",
                    phoneticReply = "Ekhon ghorite shomoy $nowTime. Apnar ajker baki kaj royeche ${pendingTasks.size} ti.",
                    emotion = "Calm",
                    actionType = MobileActionType.NONE
                )
            }
            lower.contains("কেমন আছ") || lower.contains("কি খবর") || lower.contains("হ্যালো") || lower.contains("হাই") || lower.contains("সালাম") || lower.contains("how are you") || lower.contains("hello") || lower.contains("hi") -> {
                LocalCommandMatch(
                    spokenReply = "আমি দারুণ আছি বন্ধু! আপনি বললেন '$userInput'—শুনে খুব ভালো লাগল। আপনার দিনকাল কেমন যাচ্ছে এবং আজ আপনাকে কোন কাজে সাহায্য করতে পারি?",
                    phoneticReply = "Ami darun achi bondhu! Apnar dinkal kemon jachche ebong aj apnake kon kaje shahajjo korte pari?",
                    emotion = "Happy",
                    actionType = MobileActionType.NONE
                )
            }
            lower.contains("মন খারাপ") || lower.contains("ক্লান্ত") || lower.contains("টেনশন") || lower.contains("অস্থির") || lower.contains("ভালো লাগছে না") || lower.contains("sad") || lower.contains("tired") || lower.contains("stressed") -> {
                LocalCommandMatch(
                    spokenReply = "মন খারাপ বা অতিরিক্ত চিন্তা কোরো না বন্ধু, আমি সবসময় তোমার পাশে আছি। একটু গভীর শ্বাস নাও, আজকের কাজগুলো আমরা একসাথে খুব সহজে গুছিয়ে নেব।",
                    phoneticReply = "Mon kharap ba otirikto chinta koro na bondhu, ami shobshomoy tomar pashe achi. Ektu gobhir shash nao, ajker kajgulo amra ekshathe khub shohoje guchiye nebo.",
                    emotion = "Calm",
                    actionType = MobileActionType.NONE
                )
            }
            lower.contains("কাজ") || lower.contains("টাস্ক") || lower.contains("রুটিন") || lower.contains("মিটিং") || lower.contains("task") || lower.contains("routine") || lower.contains("meeting") -> {
                val taskNames = pendingTasks.take(2).joinToString(" এবং ") { it.title }.ifEmpty { "কোনো পেন্ডিং কাজ নেই" }
                val nextMeeting = meetings.firstOrNull()?.let { "'${it.title}' (${it.startTime})" } ?: "কোনো জরুরি মিটিং নেই"
                LocalCommandMatch(
                    spokenReply = "আজ আপনার ${pendingTasks.size}টি কাজ বাকি আছে ($taskNames)। আর পরবর্তী মিটিং: $nextMeeting। আমি সময়মতো আপনাকে অ্যালার্ট দিয়ে দেব!",
                    phoneticReply = "Aj apnar ${pendingTasks.size} ti kaj baki ache. Ar poroborti meeting $nextMeeting. Ami shomoymoto apnake alert diye debo!",
                    emotion = "Focused",
                    actionType = MobileActionType.NONE
                )
            }
            lower.contains("মনে রাখ") || lower.contains("সেভ কর") || lower.contains("নোট কর") || lower.contains("remember") -> {
                LocalCommandMatch(
                    spokenReply = "ঠিক আছে! আপনার কথাটি ('${userInput.take(60)}') আমি আমার স্মার্ট মেমোরি ভল্টে সংরক্ষণ করে রেখেছি।",
                    phoneticReply = "Thik ache! Apnar kothati ami amar smart memory vault e shongrokkhon kore rekhechi.",
                    emotion = "Focused",
                    actionType = MobileActionType.SAVE_MEMORY,
                    payload = userInput
                )
            }
            else -> {
                LocalCommandMatch(
                    spokenReply = "আপনি বলেছেন: \"$userInput\"। আমি বিষয়টি নোট করে নিয়েছি! এটি কি আপনার আজকের টাস্ক রুটিনে যোগ করব নাকি গুগলে খুঁজে দেখাব?",
                    phoneticReply = "Apni bolechen $userInput. Ami bishoyti note kore niyechi! Eti ki apnar ajker task routine e jog korbo naki Google e khuje dekhabo?",
                    emotion = "Focused",
                    actionType = MobileActionType.NONE
                )
            }
        }
    }

    // --- 7. PROACTIVE DAILY BRIEFING & HEADLINES GENERATOR ---

    fun triggerProactiveDailyBriefing() {
        _orbState.value = OrbState.THINKING
        _aiErrorMessage.value = null

        viewModelScope.launch {
            try {
                val pendingTasks = ((tasksState.value as? DataUiState.Success)?.data.orEmpty())
                    .filter { !it.completed }
                val urgentMeetings = (meetingsState.value as? DataUiState.Success)?.data.orEmpty()
                val tel = _deviceTelemetry.value

                val prompt = """
                    Generate a warm, energetic proactive daily briefing in Bengali (with natural English tech terms) for the user from ARCHER AI.
                    Also provide 'briefingPhonetic' (Romanized Banglish/English pronunciation).
                    Phone Battery: ${tel.batteryPercent}%, Free Storage: ${tel.freeStorageGb}
                    Pending Tasks (${pendingTasks.size}): ${pendingTasks.joinToString { "${it.title} at ${it.dueTime}" }}
                    Upcoming Meetings (${urgentMeetings.size}): ${urgentMeetings.joinToString { "${it.title} at ${it.startTime} (Alert ${it.remindBeforeMinutes} mins early)" }}
                    Also generate 4 fresh, realistic daily headlines covering AI innovation, Creator/TikTok trends, Global Business, and Tech news.
                """.trimIndent()

                val response = withTimeout(60_000L) {
                    briefingModel.generateContent(prompt)
                }
                val jsonString = response.text ?: throw IllegalStateException("Empty briefing response")
                val json = JSONObject(jsonString)
                val speech = json.optString("briefingSpeech", "শুভ দিন! আপনার আজকের রুটিন এবং জরুরি মিটিংগুলোর আপডেট প্রস্তুত।")
                val phonetic = json.optString("briefingPhonetic", "")
                val headlinesArray = json.optJSONArray("headlines")
                if (headlinesArray != null && headlinesArray.length() > 0) {
                    val parsedHeadlines = (0 until headlinesArray.length()).map { idx ->
                        val item = headlinesArray.getJSONObject(idx)
                        NewsHeadline(
                            id = "hl_${System.currentTimeMillis()}_$idx",
                            source = item.optString("source", "AI Briefing"),
                            title = item.optString("title", "Daily Update"),
                            summary = item.optString("summary", ""),
                            category = item.optString("category", "News")
                        )
                    }
                    _headlines.value = parsedHeadlines
                }

                _archerTranscript.value = speech
                _statusBanner.value = "আজকের ডেইলি ব্রিফিং ও মিটিং অ্যালার্ট আপডেট হয়েছে"
                if (!_voiceMuted.value) {
                    _orbState.value = OrbState.SPEAKING
                    _speakEvent.tryEmit(SpeakPayload(text = speech, phoneticHint = phonetic))
                } else {
                    _orbState.value = OrbState.IDLE
                }
            } catch (e: CancellationException) {
                throw e
            } catch (e: Exception) {
                Log.e(TAG, "Briefing fallback used", e)
                val fallbackSpeech = "শুভ দিন! আজ আপনার ফোনের ব্যাটারি ${_deviceTelemetry.value.batteryPercent}% আছে এবং আপনার প্রতিদিনের রুটিন ও জরুরি মিটিংগুলোর অ্যালার্ট সক্রিয় রয়েছে।"
                _archerTranscript.value = fallbackSpeech
                _statusBanner.value = "ডেইলি ব্রিফিং প্রস্তুত"
                if (!_voiceMuted.value) {
                    _orbState.value = OrbState.SPEAKING
                    _speakEvent.tryEmit(
                        SpeakPayload(
                            text = fallbackSpeech,
                            phoneticHint = "Shubho din! Aj apnar phoner battery ${_deviceTelemetry.value.batteryPercent} percent ache ebong apnar protidiner routine o joruri meeting gulor alert shokriyo royeche."
                        )
                    )
                } else {
                    _orbState.value = OrbState.IDLE
                }
            }
        }
    }

    // --- 8. CRUD HELPERS FOR UI ---

    fun addTask(title: String, category: String, dueTime: String, priority: String) {
        viewModelScope.launch {
            repository.createTask(title, category, dueTime, priority)
            _statusBanner.value = "নতুন টাস্ক যুক্ত হয়েছে: $title"
        }
    }

    fun toggleTask(task: TaskItem) {
        viewModelScope.launch {
            repository.toggleTaskCompleted(task)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            repository.deleteTask(taskId)
        }
    }

    fun addMeeting(title: String, startTime: String, remindBeforeMinutes: Long, isUrgent: Boolean, notes: String) {
        viewModelScope.launch {
            repository.createMeeting(title, startTime, remindBeforeMinutes, isUrgent, notes)
            _statusBanner.value = "মিটিং রিমাইন্ডার সেট হয়েছে ($remindBeforeMinutes মিনিট আগে অ্যালার্ট)"
        }
    }

    fun deleteMeeting(meetingId: String) {
        viewModelScope.launch {
            repository.deleteMeeting(meetingId)
        }
    }

    fun addMemory(content: String, tag: String) {
        viewModelScope.launch {
            repository.createMemory(content, tag, _detectedEmotion.value)
            _statusBanner.value = "মেমোরি ভল্টে সংরক্ষিত হয়েছে"
        }
    }

    fun deleteMemory(memoryId: String) {
        viewModelScope.launch {
            repository.deleteMemory(memoryId)
        }
    }

    fun triggerQuickDeviceAction(command: DeviceActionCommand) {
        _deviceActionEvent.tryEmit(command)
    }

    // --- 9. FOCUS POMODORO TIMER ---

    fun toggleFocusTimer() {
        if (_isFocusTimerRunning.value) {
            focusTimerJob?.cancel()
            _isFocusTimerRunning.value = false
        } else {
            _isFocusTimerRunning.value = true
            focusTimerJob = viewModelScope.launch {
                while (_focusSecondsRemaining.value > 0 && _isFocusTimerRunning.value) {
                    delay(1000L)
                    _focusSecondsRemaining.value -= 1
                }
                if (_focusSecondsRemaining.value <= 0) {
                    _isFocusTimerRunning.value = false
                    _focusSecondsRemaining.value = 25 * 60
                    val doneMsg = "অভিনন্দন! আপনার ২৫ মিনিটের ফোকাস সেশন সম্পন্ন হয়েছে। এবার একটু বিশ্রাম নিন।"
                    _archerTranscript.value = doneMsg
                    if (!_voiceMuted.value) {
                        _speakEvent.tryEmit(SpeakPayload(text = doneMsg))
                    }
                }
            }
        }
    }

    fun resetFocusTimer() {
        focusTimerJob?.cancel()
        _isFocusTimerRunning.value = false
        _focusSecondsRemaining.value = 25 * 60
    }

    companion object {
        private const val TAG = "ArcherViewModel"
    }
}
