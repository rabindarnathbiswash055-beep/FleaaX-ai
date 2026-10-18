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
    val voiceOutputEnabled: Boolean = true,
    val speechRate: Float = 1.0f,
    val speechPitch: Float = 1.05f,
    val memorySavingEnabled: Boolean = true,
    val cloudAiConsentEnabled: Boolean = true,
    val forceOfflineMode: Boolean = false,
    val notificationReaderEnabled: Boolean = false,
    val wakeWordSessionEnabled: Boolean = false,
    val customGeminiApiKey: String = ""
)

interface PreferenceStore {
    val settingsFlow: Flow<ZiaSettingsState>
    suspend fun setLanguage(language: AssistantLanguage)
    suspend fun setPersonality(personality: PersonalityStyle)
    suspend fun setVoiceOutputEnabled(enabled: Boolean)
    suspend fun setSpeechRate(rate: Float)
    suspend fun setSpeechPitch(pitch: Float)
    suspend fun setMemorySavingEnabled(enabled: Boolean)
    suspend fun setCloudAiConsentEnabled(enabled: Boolean)
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
        val VOICE_OUTPUT = booleanPreferencesKey("voice_output_enabled")
        val SPEECH_RATE = floatPreferencesKey("speech_rate")
        val SPEECH_PITCH = floatPreferencesKey("speech_pitch")
        val MEMORY_ENABLED = booleanPreferencesKey("memory_saving_enabled")
        val CLOUD_CONSENT = booleanPreferencesKey("cloud_ai_consent_enabled")
        val FORCE_OFFLINE = booleanPreferencesKey("force_offline_mode")
        val NOTIFICATION_READER = booleanPreferencesKey("notification_reader_enabled")
        val WAKE_WORD_SESSION = booleanPreferencesKey("wake_word_session_enabled")
        val CUSTOM_API_KEY = stringPreferencesKey("custom_gemini_api_key")
    }

    override val settingsFlow: Flow<ZiaSettingsState> = context.ziaDataStore.data.map { prefs ->
        val langCode = prefs[Keys.LANGUAGE] ?: AssistantLanguage.AUTO_MIXED.name
        val personalityName = prefs[Keys.PERSONALITY] ?: PersonalityStyle.FRIENDLY.name
        ZiaSettingsState(
            language = AssistantLanguage.entries.find { it.name == langCode }
                ?: AssistantLanguage.AUTO_MIXED,
            personality = PersonalityStyle.entries.find { it.name == personalityName }
                ?: PersonalityStyle.FRIENDLY,
            voiceOutputEnabled = prefs[Keys.VOICE_OUTPUT] ?: true,
            speechRate = (prefs[Keys.SPEECH_RATE] ?: 1.0f).coerceIn(0.6f, 1.6f),
            speechPitch = (prefs[Keys.SPEECH_PITCH] ?: 1.05f).coerceIn(0.7f, 1.4f),
            memorySavingEnabled = prefs[Keys.MEMORY_ENABLED] ?: true,
            cloudAiConsentEnabled = prefs[Keys.CLOUD_CONSENT] ?: true,
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
