package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai.DeterministicLocalAssistantEngine
import com.example.ai.GeminiRemoteAIProvider
import com.example.ai.LocalAssistantEngine
import com.example.ai.RemoteAIProvider
import com.example.ai.RemoteAiResult
import com.example.commands.ActionExecutor
import com.example.commands.DeterministicLocalCommandEngine
import com.example.commands.LocalCommandEngine
import com.example.data.AssistantLanguage
import com.example.data.AvatarState
import com.example.data.ChatMessageEntity
import com.example.data.DataStorePreferenceStore
import com.example.data.EngineSource
import com.example.data.InstalledAppItem
import com.example.data.MemoryFactEntity
import com.example.data.NotificationSummary
import com.example.data.PendingAction
import com.example.data.PersonalityStyle
import com.example.data.PreferenceStore
import com.example.data.ReminderEntity
import com.example.data.SelectedFileInfo
import com.example.data.ZiaDatabase
import com.example.data.ZiaRepository
import com.example.data.ZiaSettingsState
import com.example.media.NotificationVault
import com.example.security.DeviceHardwareAudit
import com.example.security.PrivacyGuard
import com.example.voice.AndroidSpeechManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ZiaDestination(val route: String, val label: String) {
    HOME("home", "Home"),
    CHAT("chat", "Chat"),
    ACTIONS("actions", "Controls"),
    MEMORY("memory", "Memory"),
    OFFLINE("offline", "Offline"),
    SETTINGS("settings", "Settings")
}

data class ZiaTransientUiState(
    val currentDestination: ZiaDestination = ZiaDestination.HOME,
    val isThinking: Boolean = false,
    val statusBannerHinglish: String? = null,
    val pendingConfirmationAction: PendingAction? = null,
    val installedApps: List<InstalledAppItem> = emptyList(),
    val volumeMap: Map<String, Int> = mapOf("MEDIA" to 60, "RING" to 60, "ALARM" to 80, "NOTIFICATION" to 60),
    val selectedSafFile: SelectedFileInfo? = null,
    val hardwareAudit: DeviceHardwareAudit? = null,
    val isNetworkOnline: Boolean = true,
    val notificationPermissionGranted: Boolean = false
)

class ZiaAssistantViewModel(application: Application) : AndroidViewModel(application) {

    private val appContext = application.applicationContext
    private val database = ZiaDatabase.getInstance(appContext)
    private val repository = ZiaRepository(database.ziaDao())
    private val preferenceStore: PreferenceStore = DataStorePreferenceStore(appContext)
    private val localCommandEngine: LocalCommandEngine = DeterministicLocalCommandEngine()
    private val localAssistantEngine: LocalAssistantEngine = DeterministicLocalAssistantEngine()
    private val remoteAiProvider: RemoteAIProvider = GeminiRemoteAIProvider()
    val actionExecutor = ActionExecutor(appContext)
    private val speechManager = AndroidSpeechManager(appContext)

    val settingsState: StateFlow<ZiaSettingsState> = preferenceStore.settingsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ZiaSettingsState()
    )

    val chatMessages: StateFlow<List<ChatMessageEntity>> = repository.chatMessages.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val memoryFacts: StateFlow<List<MemoryFactEntity>> = repository.memoryFacts.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val reminders: StateFlow<List<ReminderEntity>> = repository.reminders.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val recentNotifications: StateFlow<List<NotificationSummary>> = NotificationVault.recentNotifications

    val isListening: StateFlow<Boolean> = speechManager.isListening
    val partialTranscript: StateFlow<String> = speechManager.partialTranscript
    val audioLevelRms: StateFlow<Float> = speechManager.audioLevelRms
    val isSpeaking: StateFlow<Boolean> = speechManager.isSpeaking
    val speechServiceAvailable: StateFlow<Boolean> = speechManager.speechServiceAvailable

    private val _transientState = MutableStateFlow(ZiaTransientUiState())
    val transientState: StateFlow<ZiaTransientUiState> = _transientState.asStateFlow()

    val avatarState: StateFlow<AvatarState> = combine(
        isListening,
        _transientState,
        isSpeaking,
        speechServiceAvailable
    ) { listening, transient, speaking, speechAvail ->
        when {
            listening -> AvatarState.LISTENING
            transient.isThinking -> AvatarState.THINKING
            speaking -> AvatarState.SPEAKING
            !speechAvail -> AvatarState.UNAVAILABLE
            else -> AvatarState.IDLE
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = AvatarState.IDLE
    )

    init {
        viewModelScope.launch {
            repository.ensureStarterDataSeeded()
            refreshSystemSnapshot()
        }
    }

    fun refreshSystemSnapshot() {
        viewModelScope.launch {
            val apps = actionExecutor.getLaunchableInstalledApps()
            val volumes = actionExecutor.getCurrentVolumePercentages()
            val audit = PrivacyGuard.inspectDeviceHardware(appContext)
            val online = remoteAiProvider.isNetworkOnline(appContext)
            val notifGranted = NotificationVault.isNotificationAccessGranted(appContext)
            _transientState.update {
                it.copy(
                    installedApps = apps,
                    volumeMap = volumes,
                    hardwareAudit = audit,
                    isNetworkOnline = online,
                    notificationPermissionGranted = notifGranted
                )
            }
        }
    }

    fun navigateTo(destination: ZiaDestination) {
        _transientState.update { it.copy(currentDestination = destination) }
        refreshSystemSnapshot()
    }

    fun dismissBanner() {
        _transientState.update { it.copy(statusBannerHinglish = null) }
    }

    fun showBanner(message: String) {
        _transientState.update { it.copy(statusBannerHinglish = message) }
    }

    fun startVoicePushToTalk() {
        val currentSettings = settingsState.value
        speechManager.startListening(
            language = currentSettings.language,
            onFinalResult = { recognizedText ->
                val trimmed = recognizedText.trim()
                if (trimmed.isNotEmpty()) {
                    submitUserMessage(trimmed)
                }
            },
            onErrorHinglish = { errorMsg ->
                showBanner(errorMsg)
            }
        )
    }

    fun stopVoiceListening() {
        speechManager.stopListening()
    }

    fun stopSpeaking() {
        speechManager.stopSpeaking()
    }

    fun speakMessageAloud(text: String) {
        val currentSettings = settingsState.value
        speechManager.speak(
            text = text,
            language = currentSettings.language,
            rate = currentSettings.speechRate,
            pitch = currentSettings.speechPitch
        )
    }

    fun submitUserMessage(rawInput: String) {
        val cleaned = rawInput.trim()
        if (cleaned.isEmpty()) return

        viewModelScope.launch {
            val currentSettings = settingsState.value
            val currentMemories = memoryFacts.value

            // Save user message to chat history
            repository.addChatMessage(
                isUser = true,
                text = cleaned,
                languageCode = currentSettings.language.code,
                engineSource = EngineSource.LOCAL_COMMAND
            )

            _transientState.update { it.copy(isThinking = true, statusBannerHinglish = null) }

            // Step 1: Try Deterministic Local Command Engine first (0ms offline phone controls)
            val commandMatch = localCommandEngine.evaluateCommand(cleaned, appContext)
            if (commandMatch.matched) {
                commandMatch.reminderToSave?.let { (title, timeText) ->
                    repository.addReminder(title, timeText)
                }
                commandMatch.memoryToSave?.let { (key, value) ->
                    val memResult = repository.saveMemoryFactSafely(
                        key = key,
                        value = value,
                        category = "Voice Note",
                        memoryEnabled = currentSettings.memorySavingEnabled
                    )
                    if (!memResult.isSafe && memResult.blockedReasonHinglish != null) {
                        _transientState.update {
                            it.copy(isThinking = false, statusBannerHinglish = memResult.blockedReasonHinglish)
                        }
                        repository.addChatMessage(
                            isUser = false,
                            text = memResult.blockedReasonHinglish,
                            languageCode = currentSettings.language.code,
                            engineSource = EngineSource.SYSTEM_SECURITY
                        )
                        return@launch
                    }
                }

                val pending = commandMatch.pendingAction
                var finalReply = commandMatch.replyText

                if (pending != null) {
                    if (pending.requiresConfirmation) {
                        _transientState.update { it.copy(pendingConfirmationAction = pending) }
                    } else {
                        val outcome = actionExecutor.execute(pending)
                        finalReply = outcome.messageHinglish
                        refreshSystemSnapshot()
                    }
                }

                repository.addChatMessage(
                    isUser = false,
                    text = finalReply,
                    languageCode = currentSettings.language.code,
                    engineSource = EngineSource.LOCAL_COMMAND
                )

                _transientState.update { it.copy(isThinking = false) }
                if (currentSettings.voiceOutputEnabled) {
                    speakMessageAloud(finalReply)
                }
                return@launch
            }

            // Step 2: Check whether Remote Cloud AI is permitted & online
            val online = remoteAiProvider.isNetworkOnline(appContext)
            _transientState.update { it.copy(isNetworkOnline = online) }

            if (!currentSettings.forceOfflineMode && currentSettings.cloudAiConsentEnabled && online) {
                when (
                    val cloudResult = remoteAiProvider.generateCloudResponse(
                        context = appContext,
                        userPrompt = cleaned,
                        language = currentSettings.language,
                        personality = currentSettings.personality,
                        savedMemories = if (currentSettings.memorySavingEnabled) currentMemories else emptyList(),
                        customApiKeyOverride = currentSettings.customGeminiApiKey
                    )
                ) {
                    is RemoteAiResult.Success -> {
                        repository.addChatMessage(
                            isUser = false,
                            text = cloudResult.reply.replyText,
                            languageCode = currentSettings.language.code,
                            engineSource = EngineSource.GEMINI_CLOUD,
                            codeSnippet = cloudResult.reply.codeSnippet
                        )
                        _transientState.update { it.copy(isThinking = false) }
                        if (currentSettings.voiceOutputEnabled) {
                            speakMessageAloud(cloudResult.reply.replyText)
                        }
                        return@launch
                    }
                    is RemoteAiResult.Unavailable -> {
                        val localFallback = localAssistantEngine.generateLocalReply(
                            input = cleaned,
                            language = currentSettings.language,
                            personality = currentSettings.personality,
                            savedMemories = currentMemories,
                            offlineReasonNote = cloudResult.reasonHinglish
                        )
                        repository.addChatMessage(
                            isUser = false,
                            text = localFallback.replyText,
                            languageCode = currentSettings.language.code,
                            engineSource = EngineSource.LOCAL_ASSISTANT,
                            codeSnippet = localFallback.codeSnippet
                        )
                        _transientState.update { it.copy(isThinking = false) }
                        if (currentSettings.voiceOutputEnabled) {
                            speakMessageAloud(localFallback.replyText)
                        }
                        return@launch
                    }
                }
            } else {
                // Step 3: Pure Offline / Opt-Out Mode -> Route to LocalAssistantEngine
                val reason = when {
                    currentSettings.forceOfflineMode -> "Force Offline Mode active hai — 100% On-Device response."
                    !currentSettings.cloudAiConsentEnabled -> "Cloud AI Consent OFF hai — Privacy-First Local Engine active."
                    else -> "Internet offline hai — ZIA Local Assistant Engine jawab de rahi hai."
                }
                val localReply = localAssistantEngine.generateLocalReply(
                    input = cleaned,
                    language = currentSettings.language,
                    personality = currentSettings.personality,
                    savedMemories = currentMemories,
                    offlineReasonNote = reason
                )
                repository.addChatMessage(
                    isUser = false,
                    text = localReply.replyText,
                    languageCode = currentSettings.language.code,
                    engineSource = EngineSource.LOCAL_ASSISTANT,
                    codeSnippet = localReply.codeSnippet
                )
                _transientState.update { it.copy(isThinking = false) }
                if (currentSettings.voiceOutputEnabled) {
                    speakMessageAloud(localReply.replyText)
                }
            }
        }
    }

    // Confirmation Gate Actions
    fun requestActionWithConfirmationGate(action: PendingAction) {
        if (action.requiresConfirmation) {
            _transientState.update { it.copy(pendingConfirmationAction = action) }
        } else {
            val outcome = actionExecutor.execute(action)
            showBanner(outcome.messageHinglish)
            refreshSystemSnapshot()
        }
    }

    fun confirmAndExecutePendingAction() {
        val action = _transientState.value.pendingConfirmationAction ?: return
        _transientState.update { it.copy(pendingConfirmationAction = null) }
        viewModelScope.launch {
            if (action is PendingAction.ClearAllMemory) {
                repository.wipeAllLocalData()
                repository.ensureStarterDataSeeded()
                showBanner("Saari local memory, reminders aur chat history safely wipe ho gayi.")
            } else {
                val outcome = actionExecutor.execute(action)
                showBanner(outcome.messageHinglish)
                repository.addChatMessage(
                    isUser = false,
                    text = "[Confirmed Action] ${outcome.messageHinglish}",
                    languageCode = settingsState.value.language.code,
                    engineSource = EngineSource.LOCAL_COMMAND
                )
                refreshSystemSnapshot()
            }
        }
    }

    fun cancelPendingAction() {
        _transientState.update {
            it.copy(
                pendingConfirmationAction = null,
                statusBannerHinglish = "Action cancel kar diya gaya."
            )
        }
    }

    // Quick Controls & SAF File Picker
    fun updateStreamVolume(streamType: String, percent: Int) {
        val outcome = actionExecutor.execute(
            PendingAction.AdjustVolume(streamType = streamType, targetPercent = percent)
        )
        refreshSystemSnapshot()
        showBanner(outcome.messageHinglish)
    }

    fun onSafFileSelected(uri: Uri?) {
        if (uri == null) return
        viewModelScope.launch {
            val info = actionExecutor.inspectSafFile(uri)
            if (info != null) {
                _transientState.update {
                    it.copy(
                        selectedSafFile = info,
                        statusBannerHinglish = "SAF File '${info.displayName}' safely load ho gayi (No private folder scan)."
                    )
                }
            } else {
                showBanner("File read nahi ho saki.")
            }
        }
    }

    fun clearSelectedSafFile() {
        _transientState.update { it.copy(selectedSafFile = null) }
    }

    // Memory & Reminders CRUD
    fun addOrUpdateMemoryFact(key: String, value: String, category: String) {
        viewModelScope.launch {
            val check = repository.saveMemoryFactSafely(
                key = key,
                value = value,
                category = category,
                memoryEnabled = settingsState.value.memorySavingEnabled
            )
            if (!check.isSafe) {
                showBanner(check.blockedReasonHinglish ?: "Memory entry blocked by PrivacyGuard.")
            } else {
                showBanner("Memory saved locally: $key")
            }
        }
    }

    fun editExistingMemoryFact(fact: MemoryFactEntity, newKey: String, newValue: String) {
        viewModelScope.launch {
            val check = repository.updateMemoryFact(fact.copy(key = newKey, value = newValue))
            if (!check.isSafe) {
                showBanner(check.blockedReasonHinglish ?: "Update blocked by PrivacyGuard.")
            } else {
                showBanner("Preference updated locally.")
            }
        }
    }

    fun deleteMemoryFact(id: Int) {
        viewModelScope.launch {
            repository.deleteMemoryFact(id)
            showBanner("Preference deleted from local database.")
        }
    }

    fun addReminder(title: String, timeText: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addReminder(title, timeText.ifBlank { "Anytime" })
            showBanner("Reminder added: $title")
        }
    }

    fun toggleReminder(id: Int, completed: Boolean) {
        viewModelScope.launch {
            repository.toggleReminder(id, completed)
        }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch {
            repository.deleteReminder(id)
        }
    }

    fun clearChatHistoryOnly() {
        viewModelScope.launch {
            repository.clearChatHistory()
            repository.ensureStarterDataSeeded()
            showBanner("Chat history cleared.")
        }
    }

    // Notification Reader Actions
    fun readLatestNotificationAloud() {
        val currentSettings = settingsState.value
        if (!currentSettings.notificationReaderEnabled) {
            showBanner("Notification Reader abhi OFF hai. Settings mein jaakar enable karein.")
            return
        }
        val latest = recentNotifications.value.firstOrNull()
        if (latest == null) {
            showBanner("Abhi koi naya notification local buffer mein nahi hai.")
            return
        }
        val speechText = "Notification from ${latest.appTitle}: ${latest.contentPreview}"
        speakMessageAloud(speechText)
        showBanner("Reading aloud: ${latest.appTitle}")
    }

    // Settings Mutations
    fun setLanguage(language: AssistantLanguage) {
        viewModelScope.launch { preferenceStore.setLanguage(language) }
    }

    fun setPersonality(personality: PersonalityStyle) {
        viewModelScope.launch { preferenceStore.setPersonality(personality) }
    }

    fun setVoiceOutputEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (!enabled) speechManager.stopSpeaking()
            preferenceStore.setVoiceOutputEnabled(enabled)
        }
    }

    fun setSpeechRate(rate: Float) {
        viewModelScope.launch { preferenceStore.setSpeechRate(rate) }
    }

    fun setSpeechPitch(pitch: Float) {
        viewModelScope.launch { preferenceStore.setSpeechPitch(pitch) }
    }

    fun setMemorySavingEnabled(enabled: Boolean) {
        viewModelScope.launch { preferenceStore.setMemorySavingEnabled(enabled) }
    }

    fun setCloudAiConsentEnabled(enabled: Boolean) {
        viewModelScope.launch { preferenceStore.setCloudAiConsentEnabled(enabled) }
    }

    fun setForceOfflineMode(forced: Boolean) {
        viewModelScope.launch { preferenceStore.setForceOfflineMode(forced) }
    }

    fun setNotificationReaderEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (!enabled) NotificationVault.clearNotifications()
            preferenceStore.setNotificationReaderEnabled(enabled)
        }
    }

    fun setWakeWordSessionEnabled(enabled: Boolean) {
        viewModelScope.launch { preferenceStore.setWakeWordSessionEnabled(enabled) }
    }

    fun setCustomGeminiApiKey(apiKey: String) {
        viewModelScope.launch {
            preferenceStore.setCustomGeminiApiKey(apiKey)
            showBanner(
                if (apiKey.isBlank()) "Built-in Gemini API Key active."
                else "Custom Gemini API Key locally saved."
            )
        }
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.release()
    }
}
