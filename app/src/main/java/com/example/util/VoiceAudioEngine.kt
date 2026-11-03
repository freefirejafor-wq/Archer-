package com.example.util

import android.annotation.SuppressLint
import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFormat
import android.media.AudioManager
import android.media.AudioRecord
import android.media.AudioTrack
import android.media.MediaRecorder
import android.speech.tts.TextToSpeech
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.sin
import kotlin.math.sqrt

data class RecordedVoiceSample(
    val wavBytes: ByteArray,
    val durationMs: Long,
    val maxRmsAmplitude: Double
)

object VoiceAudioEngine {

    private const val SAMPLE_RATE = 16000
    private var activeRecordingJob: Job? = null

    @Volatile
    private var shouldKeepRecording = false

    /**
     * Plays a short futuristic cyber-HUD acoustic tone via AudioTrack so the user
     * always gets immediate audible confirmation when tapping TALK, STOP, or receiving a reply.
     */
    fun playHudChime(scope: CoroutineScope, ascending: Boolean = true) {
        scope.launch(Dispatchers.Default) {
            try {
                val durationMs = 150
                val numSamples = (SAMPLE_RATE * durationMs) / 1000
                val pcm = ShortArray(numSamples)
                val startFreq = if (ascending) 520.0 else 740.0
                val endFreq = if (ascending) 880.0 else 440.0

                for (i in 0 until numSamples) {
                    val progress = i.toDouble() / numSamples
                    val freq = startFreq + (endFreq - startFreq) * progress
                    val envelope = sin(PI * progress)
                    val sample = (sin(2.0 * PI * i * freq / SAMPLE_RATE) * 0.32 * envelope * Short.MAX_VALUE).toInt()
                    pcm[i] = sample.coerceIn(Short.MIN_VALUE.toInt(), Short.MAX_VALUE.toInt()).toShort()
                }

                val bufferSize = pcm.size * 2
                val audioTrack = AudioTrack.Builder()
                    .setAudioAttributes(
                        AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                            .build()
                    )
                    .setAudioFormat(
                        AudioFormat.Builder()
                            .setEncoding(AudioFormat.ENCODING_PCM_16BIT)
                            .setSampleRate(SAMPLE_RATE)
                            .setChannelMask(AudioFormat.CHANNEL_OUT_MONO)
                            .build()
                    )
                    .setBufferSizeInBytes(bufferSize.coerceAtLeast(AudioTrack.getMinBufferSize(
                        SAMPLE_RATE,
                        AudioFormat.CHANNEL_OUT_MONO,
                        AudioFormat.ENCODING_PCM_16BIT
                    )))
                    .setTransferMode(AudioTrack.MODE_STATIC)
                    .build()

                audioTrack.write(pcm, 0, pcm.size)
                audioTrack.play()
                kotlinx.coroutines.delay(durationMs.toLong() + 40L)
                audioTrack.stop()
                audioTrack.release()
            } catch (_: Exception) {
                // Ignore audio track issues on headless tests
            }
        }
    }

    /**
     * Starts direct hardware microphone capture (16kHz 16-bit mono PCM) with real-time
     * voice activity detection (VAD) and live amplitude callbacks.
     */
    @SuppressLint("MissingPermission")
    fun startMicRecording(
        scope: CoroutineScope,
        maxDurationMs: Long = 6500L,
        onAmplitudeUpdate: (normalizedLevel: Float, elapsedSeconds: Int) -> Unit,
        onFinished: (RecordedVoiceSample?) -> Unit
    ) {
        stopMicRecording()
        shouldKeepRecording = true

        activeRecordingJob = scope.launch(Dispatchers.IO) {
            var audioRecord: AudioRecord? = null
            val pcmOut = ByteArrayOutputStream()
            val startTime = System.currentTimeMillis()
            var maxRms = 0.0
            var detectedSpeech = false
            var lastSpeechTime = startTime

            try {
                val minBuf = AudioRecord.getMinBufferSize(
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT
                )
                val bufferSize = (minBuf * 2).coerceAtLeast(3200)

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    SAMPLE_RATE,
                    AudioFormat.CHANNEL_IN_MONO,
                    AudioFormat.ENCODING_PCM_16BIT,
                    bufferSize
                )

                if (audioRecord.state != AudioRecord.STATE_INITIALIZED) {
                    withContext(Dispatchers.Main) {
                        onFinished(null)
                    }
                    return@launch
                }

                audioRecord.startRecording()
                val shortBuffer = ShortArray(1024)

                while (isActive && shouldKeepRecording) {
                    val readCount = audioRecord.read(shortBuffer, 0, shortBuffer.size)
                    val now = System.currentTimeMillis()
                    val elapsedMs = now - startTime

                    if (readCount > 0) {
                        var sumSquares = 0.0
                        val byteBuf = ByteBuffer.allocate(readCount * 2).order(ByteOrder.LITTLE_ENDIAN)
                        for (i in 0 until readCount) {
                            val sample = shortBuffer[i]
                            byteBuf.putShort(sample)
                            val d = sample.toDouble()
                            sumSquares += d * d
                        }
                        pcmOut.write(byteBuf.array())

                        val rms = sqrt(sumSquares / readCount)
                        if (rms > maxRms) {
                            maxRms = rms
                        }

                        // Speech threshold (~350 RMS on 16-bit PCM)
                        if (rms > 350.0) {
                            detectedSpeech = true
                            lastSpeechTime = now
                        }

                        val normalized = (rms / 3500.0).toFloat().coerceIn(0.05f, 1.0f)
                        val elapsedSec = (elapsedMs / 1000L).toInt()
                        withContext(Dispatchers.Main) {
                            onAmplitudeUpdate(normalized, elapsedSec)
                        }

                        // Auto-stop after 1.8s of silence once speech has already been spoken
                        if (detectedSpeech && (now - lastSpeechTime) > 1800L && elapsedMs > 1600L) {
                            break
                        }
                    }

                    if (elapsedMs >= maxDurationMs) {
                        break
                    }
                }
            } catch (_: Exception) {
                // Handled below
            } finally {
                shouldKeepRecording = false
                try {
                    audioRecord?.stop()
                } catch (_: Exception) {}
                try {
                    audioRecord?.release()
                } catch (_: Exception) {}
            }

            val totalDuration = (System.currentTimeMillis() - startTime).coerceAtLeast(100L)
            val rawPcm = pcmOut.toByteArray()
            if (rawPcm.isNotEmpty()) {
                val wavBytes = encodePcmToWav(rawPcm, SAMPLE_RATE, 1, 16)
                val sample = RecordedVoiceSample(
                    wavBytes = wavBytes,
                    durationMs = totalDuration,
                    maxRmsAmplitude = maxRms
                )
                withContext(Dispatchers.Main) {
                    onFinished(sample)
                }
            } else {
                withContext(Dispatchers.Main) {
                    onFinished(null)
                }
            }
        }
    }

    fun stopMicRecording() {
        shouldKeepRecording = false
    }

    fun isRecording(): Boolean = shouldKeepRecording

    /**
     * Speaks Bengali and English text reliably across all Android devices & emulators.
     * - If the device TTS engine supports Bengali (`bn-BD` or `bn-IN`), speaks Bengali script directly.
     * - If the device TTS engine only has English (`en-US`) installed (common in emulators),
     *   automatically transliterates Bengali Unicode into natural phonetic pronunciation so TTS
     *   ALWAYS produces clear audible voice output instead of silence!
     */
    fun speakSmartBilingual(
        context: Context,
        tts: TextToSpeech?,
        text: String,
        phoneticHint: String? = null,
        onFallbackDone: () -> Unit
    ) {
        if (tts == null || text.isBlank()) {
            onFallbackDone()
            return
        }

        try {
            // Ensure media stream volume is audible (at least 40% if currently 0)
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager != null) {
                val currentVol = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
                val maxVol = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).coerceAtLeast(1)
                if (currentVol == 0) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, (maxVol * 0.55f).toInt().coerceAtLeast(1), 0)
                }
            }

            val hasBengaliScript = text.any { it in '\u0980'..'\u09FF' }
            val bnBdLocale = Locale.forLanguageTag("bn-BD")
            val bnInLocale = Locale.forLanguageTag("bn-IN")
            val hiInLocale = Locale.forLanguageTag("hi-IN")
            val enInLocale = Locale.forLanguageTag("en-IN")

            val bnBdAvail = tts.isLanguageAvailable(bnBdLocale)
            val bnInAvail = tts.isLanguageAvailable(bnInLocale)

            val speechToSpeak: String
            when {
                hasBengaliScript && bnBdAvail >= TextToSpeech.LANG_AVAILABLE -> {
                    tts.language = bnBdLocale
                    speechToSpeak = text
                }
                hasBengaliScript && bnInAvail >= TextToSpeech.LANG_AVAILABLE -> {
                    tts.language = bnInLocale
                    speechToSpeak = text
                }
                hasBengaliScript -> {
                    // Fallback for emulators/devices without a Bengali TTS voice pack:
                    // Use phonetic Latin transliteration so the English/Indic TTS engine speaks every word aloud!
                    if (tts.isLanguageAvailable(enInLocale) >= TextToSpeech.LANG_AVAILABLE) {
                        tts.language = enInLocale
                    } else if (tts.isLanguageAvailable(hiInLocale) >= TextToSpeech.LANG_AVAILABLE) {
                        tts.language = hiInLocale
                    } else {
                        tts.language = Locale.US
                    }
                    speechToSpeak = if (!phoneticHint.isNullOrBlank()) {
                        phoneticHint
                    } else {
                        transliterateBengaliToPhonetic(text)
                    }
                }
                else -> {
                    tts.language = Locale.US
                    speechToSpeak = text
                }
            }

            tts.setPitch(1.0f)
            tts.setSpeechRate(0.96f)

            val utteranceId = "archer_utt_${System.currentTimeMillis()}"
            val speakResult = tts.speak(speechToSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            if (speakResult == TextToSpeech.ERROR) {
                onFallbackDone()
            }
        } catch (_: Exception) {
            onFallbackDone()
        }
    }

    /**
     * Converts Bengali Unicode text into natural phonetic Romanized text so that even if a device
     * only has an English TTS voice installed, it speaks the Bengali response clearly out loud.
     */
    fun transliterateBengaliToPhonetic(input: String): String {
        // Common phrase dictionary for ultra-natural pronunciation
        var text = input
        val phraseReplacements = listOf(
            "নতুন জিনিস শিখছি" to "Notun jinish shikhchi",
            "তোমাদের সাহায্য করছি" to "tomader shahajjo korchi",
            "দিনগুলো দারুণ কাটছে" to "dingulo darun katche",
            "আমি আপনার কথা বুঝতে পেরেছি" to "Ami apnar kotha bujhte perechi",
            "ফ্ল্যাশলাইট" to "flashlight",
            "অন করা হয়েছে" to "on kora hoyeche",
            "অফ করা হয়েছে" to "off kora hoyeche",
            "ক্যামেরা" to "camera",
            "গ্যালারি" to "gallery",
            "ভলিউম" to "volume",
            "ব্রাইটনেস" to "brightness",
            "সাইলেন্ট" to "silent",
            "টিকটক" to "TikTok",
            "ভিডিও" to "video",
            "আপলোড" to "upload",
            "মিটিং" to "meeting",
            "অ্যালার্ট" to "alert",
            "টাস্ক" to "task",
            "রুটিন" to "routine",
            "ব্যাটারি" to "battery",
            "স্টোরেজ" to "storage",
            "ওয়াইফাই" to "Wi-Fi",
            "ব্লুটুথ" to "Bluetooth",
            "ইউটিউব" to "YouTube",
            "ম্যাপ" to "Map",
            "অ্যালার্ম" to "alarm",
            "টাইমার" to "timer",
            "এখনই করে দিচ্ছি" to "ekhoni kore dichchi",
            "বলুন কী করতে হবে" to "bolun ki korte hobe"
        )
        for ((bn, en) in phraseReplacements) {
            text = text.replace(bn, en)
        }

        val sb = StringBuilder()
        for (ch in text) {
            val mapped = when (ch) {
                'অ' -> "o"
                'আ', 'া' -> "a"
                'ই', 'ঈ', 'ি', 'ী' -> "i"
                'উ', 'ঊ', 'ু', 'ূ' -> "u"
                'ঋ', 'ৃ' -> "ri"
                'এ', 'ে' -> "e"
                'ঐ', 'ৈ' -> "oi"
                'ও', 'ো' -> "o"
                'ঔ', 'ৌ' -> "ou"
                'ক' -> "k"
                'খ' -> "kh"
                'গ' -> "g"
                'ঘ' -> "gh"
                'ঙ', 'ং' -> "ng"
                'চ' -> "ch"
                'ছ' -> "chh"
                'জ', 'য' -> "j"
                'ঝ' -> "jh"
                'ঞ' -> "n"
                'ট', 'ত', 'ৎ' -> "t"
                'ঠ', 'থ' -> "th"
                'ড', 'দ' -> "d"
                'ঢ', 'ধ' -> "dh"
                'ণ', 'ন' -> "n"
                'প' -> "p"
                'ফ' -> "f"
                'ব' -> "b"
                'ভ' -> "bh"
                'ম' -> "m"
                'র', 'ড়', 'ঢ়' -> "r"
                'ল' -> "l"
                'শ', 'ষ' -> "sh"
                'স' -> "s"
                'হ' -> "h"
                'য়' -> "y"
                'ঁ', '্', 'ঃ' -> ""
                '।' -> ". "
                '০' -> "0"
                '১' -> "1"
                '২' -> "2"
                '৩' -> "3"
                '৪' -> "4"
                '৫' -> "5"
                '৬' -> "6"
                '৭' -> "7"
                '৮' -> "8"
                '৯' -> "9"
                else -> ch.toString()
            }
            sb.append(mapped)
        }
        return sb.toString().replace(Regex("\\s+"), " ").trim()
    }

    private fun encodePcmToWav(
        pcmData: ByteArray,
        sampleRate: Int,
        channels: Int,
        bitsPerSample: Int
    ): ByteArray {
        val byteRate = sampleRate * channels * bitsPerSample / 8
        val blockAlign = (channels * bitsPerSample / 8).toShort()
        val dataSize = pcmData.size
        val chunkSize = 36 + dataSize

        val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN)
        header.put("RIFF".toByteArray(Charsets.US_ASCII))
        header.putInt(chunkSize)
        header.put("WAVE".toByteArray(Charsets.US_ASCII))
        header.put("fmt ".toByteArray(Charsets.US_ASCII))
        header.putInt(16) // PCM subchunk size
        header.putShort(1) // AudioFormat = 1 (PCM)
        header.putShort(channels.toShort())
        header.putInt(sampleRate)
        header.putInt(byteRate)
        header.putShort(blockAlign)
        header.putShort(bitsPerSample.toShort())
        header.put("data".toByteArray(Charsets.US_ASCII))
        header.putInt(dataSize)

        val out = ByteArrayOutputStream(44 + dataSize)
        out.write(header.array())
        out.write(pcmData)
        return out.toByteArray()
    }
}
