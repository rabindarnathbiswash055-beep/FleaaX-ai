package com.example.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.ziaDataStore: DataStore<Preferences> by preferencesDataStore(name = "zia_assistant_prefs")

data class ZiaSettingsState(
    val language: AssistantLanguage = AssistantLanguage.AUTO_MIXED,
    val personality: PersonalityStyle = PersonalityStyle.FRIENDLY,
    val avatarTheme: AvatarThemePreset = AvatarThemePreset.SAPPHIRE_CYBER,
    val responseLength: ResponseLengthPreference = ResponseLengthPreference.BALANCED,
    val customGreeting: String = "Namaste! Main ZIA AI hoon — Your Voice. Your World. Your ZIA.",
    val voiceInputEnabled: Boolean = true,
    val voiceOutputEnabled: Boolean = true,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.05f,
    val memorySavingEnabled: Boolean = true,
    val cloudAiConsentEnabled: Boolean = true,
    val cloudPersonalContextConsent: Boolean = false,
    val cloudDocumentUploadConsent: Boolean = false,
    val forceOfflineMode: Boolean = false,
    val notificationReaderEnabled: Boolean = false,
    val wakeWordSessionEnabled: Boolean = false,
    val customGeminiApiKey: String = ""
)

interface PreferenceStore {
    val settingsFlow: Flow<ZiaSettingsState>
    suspend fun setLanguage(language: AssistantLanguage)
    suspend fun setPersonality(personality: PersonalityStyle)
    suspend fun setAvatarTheme(theme: AvatarThemePreset)
    suspend fun setResponseLength(length: ResponseLengthPreference)
    suspend fun setCustomGreeting(greeting: String)
    suspend fun setVoiceInputEnabled(enabled: Boolean)
    suspend fun setVoiceOutputEnabled(enabled: Boolean)
    suspend fun setSpeechRate(rate: Float)
    suspend fun setSpeechPitch(pitch: Float)
    suspend fun setMemorySavingEnabled(enabled: Boolean)
    suspend fun setCloudAiConsentEnabled(enabled: Boolean)
    suspend fun setCloudPersonalContextConsent(enabled: Boolean)
    suspend fun setCloudDocumentUploadConsent(enabled: Boolean)
    suspend fun setForceOfflineMode(forced: Boolean)
    suspend fun setNotificationReaderEnabled(enabled: Boolean)
    suspend fun setWakeWordSessionEnabled(enabled: Boolean)
    suspend fun setCustomGeminiApiKey(apiKey: String)
    suspend fun resetSettingsToDefaults()
}

class DataStorePreferenceStore(private val context: Context) : PreferenceStore {

    private object Keys {
        val LANGUAGE = stringPreferencesKey("language_code")
        val PERSONALITY = stringPreferencesKey("personality_style")
        val AVATAR_THEME = stringPreferencesKey("avatar_theme_preset")
        val RESPONSE_LENGTH = stringPreferencesKey("response_length_pref")
        val CUSTOM_GREETING = stringPreferencesKey("custom_greeting_text")
        val VOICE_INPUT = booleanPreferencesKey("voice_input_enabled")
        val VOICE_OUTPUT = booleanPreferencesKey("voice_output_enabled")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val MEMORY_ENABLED = booleanPreferencesKey("memory_saving_enabled")
        val CLOUD_CONSENT = booleanPreferencesKey("cloud_ai_consent_enabled")
        val CLOUD_PERSONAL_CONTEXT = booleanPreferencesKey("cloud_personal_context_consent")
        val CLOUD_DOC_UPLOAD = booleanPreferencesKey("cloud_doc_upload_consent")
        val FORCE_OFFLINE = booleanPreferencesKey("force_offline_mode")
        val NOTIFICATION_READER = booleanPreferencesKey("notification_reader_enabled")
        val WAKE_WORD_SESSION = booleanPreferencesKey("wake_word_session_enabled")
        val CUSTOM_API_KEY = stringPreferencesKey("custom_gemini_api_key")
    }

    override val settingsFlow: Flow<ZiaSettingsState> = context.ziaDataStore.data.map { prefs ->
        val langCode = prefs[Keys.LANGUAGE] ?: AssistantLanguage.AUTO_MIXED.name
        val personalityName = prefs[Keys.PERSONALITY] ?: PersonalityStyle.FRIENDLY.name
        val themeName = prefs[Keys.AVATAR_THEME] ?: AvatarThemePreset.SAPPHIRE_CYBER.name
        val lengthName = prefs[Keys.RESPONSE_LENGTH] ?: ResponseLengthPreference.BALANCED.name

        ZiaSettingsState(
            language = AssistantLanguage.entries.find { it.name == langCode }
                ?: AssistantLanguage.AUTO_MIXED,
            personality = PersonalityStyle.entries.find { it.name == personalityName }
                ?: PersonalityStyle.FRIENDLY,
            avatarTheme = AvatarThemePreset.entries.find { it.name == themeName }
                ?: AvatarThemePreset.SAPPHIRE_CYBER,
            responseLength = ResponseLengthPreference.entries.find { it.name == lengthName }
                ?: ResponseLengthPreference.BALANCED,
            customGreeting = prefs[Keys.CUSTOM_GREETING]
                ?: "Namaste! Main ZIA AI hoon — Your Voice. Your World. Your ZIA.",
            voiceInputEnabled = prefs[Keys.VOICE_INPUT] ?: true,
            voiceOutputEnabled = prefs[Keys.VOICE_OUTPUT] ?: true,
            speechRate = (prefs[Keys.SPEECH_RATE] ?: 1.0f).coerceIn(0.6f, 1.6f),
            speechPitch = (prefs[Keys.SPEECH_PITCH] ?: 1.05f).coerceIn(0.7f, 1.4f),
            memorySavingEnabled = prefs[Keys.MEMORY_ENABLED] ?: true,
            cloudAiConsentEnabled = prefs[Keys.CLOUD_CONSENT] ?: true,
            cloudPersonalContextConsent = prefs[Keys.CLOUD_PERSONAL_CONTEXT] ?: false,
            cloudDocumentUploadConsent = prefs[Keys.CLOUD_DOC_UPLOAD] ?: false,
            forceOfflineMode = prefs[Keys.FORCE_OFFLINE] ?: false,
            notificationReaderEnabled = prefs[Keys.NOTIFICATION_READER] ?: false,
            wakeWordSessionEnabled = prefs[Keys.WAKE_WORD_SESSION] ?: false,
            customGeminiApiKey = prefs[Keys.CUSTOM_API_KEY] ?: ""
        )
    }

    override suspend fun setLanguage(language: AssistantLanguage) {
        context.ziaDataStore.edit { it[Keys.LANGUAGE] = language.name }
    }

    override suspend fun setPersonality(personality: PersonalityStyle) {
        context.ziaDataStore.edit { it[Keys.PERSONALITY] = personality.name }
    }

    override suspend fun setAvatarTheme(theme: AvatarThemePreset) {
        context.ziaDataStore.edit { it[Keys.AVATAR_THEME] = theme.name }
    }

    override suspend fun setResponseLength(length: ResponseLengthPreference) {
        context.ziaDataStore.edit { it[Keys.RESPONSE_LENGTH] = length.name }
    }

    override suspend fun setCustomGreeting(greeting: String) {
        context.ziaDataStore.edit {
            it[Keys.CUSTOM_GREETING] = greeting.trim().take(160).ifEmpty {
                "Namaste! Main ZIA AI hoon — Your Voice. Your World. Your ZIA."
            }
        }
    }

    override suspend fun setVoiceInputEnabled(enabled: Boolean) {
        context.ziaDataStore.edit { it[Keys.VOICE_INPUT] = enabled }
    }

    override suspend fun setVoiceOutputEnabled(enabled: Boolean) {
        context.ziaDataStore.edit { it[Keys.VOICE_OUTPUT] = enabled }
    }

    override suspend fun setSpeechRate(rate: Float) {
        context.ziaDataStore.edit { it[Keys.SPEECH_RATE] = rate.coerceIn(0.6f, 1.6f) }
    }

    override suspend fun setSpeechPitch(pitch: Float) {
        context.ziaDataStore.edit { it[Keys.SPEECH_PITCH] = pitch.coerceIn(0.7f, 1.4f) }
    }

    override suspend fun setMemorySavingEnabled(enabled: Boolean) {
        context.ziaDataStore.edit { it[Keys.MEMORY_ENABLED] = enabled }
    }

    override suspend fun setCloudAiConsentEnabled(enabled: Boolean) {
        context.ziaDataStore.edit { it[Keys.CLOUD_CONSENT] = enabled }
    }

    override suspend fun setCloudPersonalContextConsent(enabled: Boolean) {
        context.ziaDataStore.edit { it[Keys.CLOUD_PERSONAL_CONTEXT] = enabled }
    }

    override suspend fun setCloudDocumentUploadConsent(enabled: Boolean) {
        context.ziaDataStore.edit { it[Keys.CLOUD_DOC_UPLOAD] = enabled }
    }

    override suspend fun setForceOfflineMode(forced: Boolean) {
        context.ziaDataStore.edit { it[Keys.FORCE_OFFLINE] = forced }
    }

    override suspend fun setNotificationReaderEnabled(enabled: Boolean) {
        context.ziaDataStore.edit { it[Keys.NOTIFICATION_READER] = enabled }
    }

    override suspend fun setWakeWordSessionEnabled(enabled: Boolean) {
        context.ziaDataStore.edit { it[Keys.WAKE_WORD_SESSION] = enabled }
    }

    override suspend fun setCustomGeminiApiKey(apiKey: String) {
        context.ziaDataStore.edit { it[Keys.CUSTOM_API_KEY] = apiKey.trim() }
    }

    override suspend fun resetSettingsToDefaults() {
        context.ziaDataStore.edit { it.clear() }
    }
}
