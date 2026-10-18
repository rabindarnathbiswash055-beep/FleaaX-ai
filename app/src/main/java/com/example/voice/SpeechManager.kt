package com.example.voice

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import com.example.data.AssistantLanguage
import java.util.Locale
import java.util.UUID
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

interface SpeechInput {
    val isListening: StateFlow<Boolean>
    val partialTranscript: StateFlow<String>
    val audioLevelRms: StateFlow<Float>
    val speechServiceAvailable: StateFlow<Boolean>
    fun startListening(
        language: AssistantLanguage,
        onFinalResult: (String) -> Unit,
        onErrorHinglish: (String) -> Unit
    )
    fun stopListening()
    fun release()
}

interface SpeechOutput {
    val isSpeaking: StateFlow<Boolean>
    val ttsReady: StateFlow<Boolean>
    fun speak(
        text: String,
        language: AssistantLanguage,
        rate: Float = 1.0f,
        pitch: Float = 1.05f
    )
    fun stopSpeaking()
    fun release()
}

class AndroidSpeechManager(
    private val context: Context
) : SpeechInput, SpeechOutput {

    private val mainHandler = Handler(Looper.getMainLooper())
    private var speechRecognizer: SpeechRecognizer? = null
    private var textToSpeech: TextToSpeech? = null

    private val _isListening = MutableStateFlow(false)
    override val isListening: StateFlow<Boolean> = _isListening.asStateFlow()

    private val _partialTranscript = MutableStateFlow("")
    override val partialTranscript: StateFlow<String> = _partialTranscript.asStateFlow()

    private val _audioLevelRms = MutableStateFlow(0f)
    override val audioLevelRms: StateFlow<Float> = _audioLevelRms.asStateFlow()

    private val _speechServiceAvailable = MutableStateFlow(
        SpeechRecognizer.isRecognitionAvailable(context)
    )
    override val speechServiceAvailable: StateFlow<Boolean> = _speechServiceAvailable.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    override val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _ttsReady = MutableStateFlow(false)
    override val ttsReady: StateFlow<Boolean> = _ttsReady.asStateFlow()

    init {
        initTts()
    }

    private fun initTts() {
        try {
            textToSpeech = TextToSpeech(context.applicationContext) { status ->
                if (status == TextToSpeech.SUCCESS) {
                    _ttsReady.value = true
                    textToSpeech?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                        override fun onStart(utteranceId: String?) {
                            _isSpeaking.value = true
                        }

                        override fun onDone(utteranceId: String?) {
                            _isSpeaking.value = false
                        }

                        @Deprecated("Deprecated in Java")
                        override fun onError(utteranceId: String?) {
                            _isSpeaking.value = false
                        }

                        override fun onError(utteranceId: String?, errorCode: Int) {
                            _isSpeaking.value = false
                        }
                    })
                } else {
                    _ttsReady.value = false
                }
            }
        } catch (e: Exception) {
            _ttsReady.value = false
        }
    }

    override fun startListening(
        language: AssistantLanguage,
        onFinalResult: (String) -> Unit,
        onErrorHinglish: (String) -> Unit
    ) {
        stopSpeaking()
        mainHandler.post {
            val available = SpeechRecognizer.isRecognitionAvailable(context)
            _speechServiceAvailable.value = available
            if (!available) {
                _isListening.value = false
                onErrorHinglish(
                    "Is device/emulator par Android Speech Recognition service installed ya enabled nahi hai. Aap niche Text Input box se saare commands chala sakte hain."
                )
                return@post
            }

            try {
                speechRecognizer?.destroy()
                speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context.applicationContext)
                speechRecognizer?.setRecognitionListener(object : RecognitionListener {
                    override fun onReadyForSpeech(params: Bundle?) {
                        _isListening.value = true
                        _partialTranscript.value = ""
                    }

                    override fun onBeginningOfSpeech() {
                        _isListening.value = true
                    }

                    override fun onRmsChanged(rmsdB: Float) {
                        val normalized = ((rmsdB + 2f) / 12f).coerceIn(0.05f, 1.0f)
                        _audioLevelRms.value = normalized
                    }

                    override fun onBufferReceived(buffer: ByteArray?) {}

                    override fun onEndOfSpeech() {
                        _isListening.value = false
                        _audioLevelRms.value = 0f
                    }

                    override fun onError(error: Int) {
                        _isListening.value = false
                        _audioLevelRms.value = 0f
                        val partial = _partialTranscript.value.trim()
                        if (partial.isNotEmpty() && (error == SpeechRecognizer.ERROR_NO_MATCH || error == SpeechRecognizer.ERROR_SPEECH_TIMEOUT)) {
                            _partialTranscript.value = ""
                            onFinalResult(partial)
                        } else {
                            onErrorHinglish(mapSpeechErrorHinglish(error))
                        }
                    }

                    override fun onResults(results: Bundle?) {
                        _isListening.value = false
                        _audioLevelRms.value = 0f
                        val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val best = matches?.firstOrNull()?.trim().orEmpty()
                        _partialTranscript.value = ""
                        if (best.isNotEmpty()) {
                            onFinalResult(best)
                        } else {
                            onErrorHinglish("Koi aawaz clear sunai nahi di. Phir se Push-to-Talk dabayein ya text type karein.")
                        }
                    }

                    override fun onPartialResults(partialResults: Bundle?) {
                        val partialList = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                        val text = partialList?.firstOrNull()?.trim().orEmpty()
                        if (text.isNotEmpty()) {
                            _partialTranscript.value = text
                        }
                    }

                    override fun onEvent(eventType: Int, params: Bundle?) {}
                })

                val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
                    putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE, language.speechLocaleTag)
                    putExtra(RecognizerIntent.EXTRA_LANGUAGE_PREFERENCE, language.speechLocaleTag)
                    putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3)
                }

                speechRecognizer?.startListening(intent)
            } catch (e: Exception) {
                _isListening.value = false
                onErrorHinglish("Speech recognizer start nahi ho paya (${e.javaClass.simpleName}). Text fallback use karein.")
            }
        }
    }

    override fun stopListening() {
        mainHandler.post {
            try {
                speechRecognizer?.stopListening()
                speechRecognizer?.cancel()
            } catch (_: Exception) {
            }
            _isListening.value = false
            _audioLevelRms.value = 0f
        }
    }

    override fun speak(
        text: String,
        language: AssistantLanguage,
        rate: Float,
        pitch: Float
    ) {
        val tts = textToSpeech ?: return
        if (!_ttsReady.value) return

        val cleanSpeechText = text
            .replace(Regex("```[\\s\\S]*?```"), " Code snippet screen par diya gaya hai. ")
            .replace(Regex("[•*#_`]"), " ")
            .trim()

        if (cleanSpeechText.isEmpty()) return

        mainHandler.post {
            try {
                val targetLocale = Locale.forLanguageTag(language.ttsLocaleTag)
                val langStatus = tts.isLanguageAvailable(targetLocale)
                if (langStatus >= TextToSpeech.LANG_AVAILABLE) {
                    tts.language = targetLocale
                } else {
                    tts.language = Locale.US
                }
                tts.setSpeechRate(rate.coerceIn(0.6f, 1.6f))
                tts.setPitch(pitch.coerceIn(0.7f, 1.4f))

                val utteranceId = UUID.randomUUID().toString()
                _isSpeaking.value = true
                tts.speak(cleanSpeechText.take(650), TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } catch (e: Exception) {
                _isSpeaking.value = false
            }
        }
    }

    override fun stopSpeaking() {
        mainHandler.post {
            try {
                textToSpeech?.stop()
            } catch (_: Exception) {
            }
            _isSpeaking.value = false
        }
    }

    override fun release() {
        mainHandler.post {
            try {
                speechRecognizer?.destroy()
                speechRecognizer = null
                textToSpeech?.stop()
                textToSpeech?.shutdown()
                textToSpeech = null
            } catch (_: Exception) {
            }
        }
    }

    private fun mapSpeechErrorHinglish(error: Int): String = when (error) {
        SpeechRecognizer.ERROR_AUDIO ->
            "Audio recording error aaya. Check karein ki koi doosri app mic use na kar rahi ho."
        SpeechRecognizer.ERROR_CLIENT ->
            "Speech client stop ho gaya."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            "Microphone permission nahi mili. Push-to-Talk ke liye RECORD_AUDIO allow karein."
        SpeechRecognizer.ERROR_NETWORK, SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            "Speech recognition network timeout — Offline speech pack download karein ya niche Text box use karein."
        SpeechRecognizer.ERROR_NO_MATCH ->
            "Aawaz match nahi hui. Thoda saaf boliye ya language setting (EN / Hinglish / Banglish / বাংলা) check karein."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
            "Speech recognizer abhi busy hai — ek second baad try karein."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
            "Koi speech input detect nahi hua. Push-to-Talk dabane ke turant baad boliye."
        else ->
            "Speech service abhi unavailable hai — Text input hamesha active hai."
    }
}
