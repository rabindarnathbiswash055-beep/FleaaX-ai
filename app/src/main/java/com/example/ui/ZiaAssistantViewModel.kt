package com.example.ui

import android.app.Application
import android.net.Uri
import android.os.SystemClock
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
import com.example.data.AvatarThemePreset
import com.example.data.ChatMessageEntity
import com.example.data.DataStorePreferenceStore
import com.example.data.EngineSource
import com.example.data.GamingProfileEntity
import com.example.data.InstalledAppItem
import com.example.data.KnowledgeNoteEntity
import com.example.data.MemoryFactEntity
import com.example.data.NoteCategory
import com.example.data.NotificationSummary
import com.example.data.PendingAction
import com.example.data.PerformanceTelemetry
import com.example.data.PersonalityStyle
import com.example.data.PlannerTaskEntity
import com.example.data.PreferenceStore
import com.example.data.ReminderEntity
import com.example.data.ResponseLengthPreference
import com.example.data.SelectedFileInfo
import com.example.data.SmartSearchResult
import com.example.data.TaskRecurrence
import com.example.data.WorkspaceSubTab
import com.example.data.ZiaDatabase
import com.example.data.ZiaRepository
import com.example.data.ZiaSettingsState
import com.example.media.NotificationVault
import com.example.security.DeviceHardwareAudit
import com.example.security.PrivacyGuard
import com.example.voice.AndroidSpeechManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class ZiaDestination(val route: String, val label: String) {
    HOME("home", "Home"),
    CHAT("chat", "Chat"),
    WORKSPACE("workspace", "Workspace"),
    ACTIONS("actions", "Controls"),
    SETTINGS("settings", "Privacy & Set")
}

data class ZiaTransientUiState(
    val currentDestination: ZiaDestination = ZiaDestination.HOME,
    val activeWorkspaceTab: WorkspaceSubTab = WorkspaceSubTab.PLANNER,
    val isThinking: Boolean = false,
    val statusBannerHinglish: String? = null,
    val pendingConfirmationAction: PendingAction? = null,
    val installedApps: List<InstalledAppItem> = emptyList(),
    val volumeMap: Map<String, Int> = mapOf("MEDIA" to 60, "RING" to 60, "ALARM" to 80, "NOTIFICATION" to 60),
    val selectedSafFile: SelectedFileInfo? = null,
    val hardwareAudit: DeviceHardwareAudit? = null,
    val performanceTelemetry: PerformanceTelemetry = PerformanceTelemetry(),
    val isNetworkOnline: Boolean = true,
    val notificationPermissionGranted: Boolean = false,
    // Focus Timer State
    val focusTimerSecondsRemaining: Int = 25 * 60,
    val focusTimerRunning: Boolean = false,
    val focusSessionLabel: String = "25m Pomodoro Study Session",
    // Smart Search State
    val smartSearchResult: SmartSearchResult? = null,
    // Gaming Companion State
    val selectedGameplayScreenshotUri: Uri? = null,
    val screenshotAnalysisResult: String? = null,
    val generatedGamingPackText: String? = null
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
    private var focusTimerJob: Job? = null

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

    val plannerTasks: StateFlow<List<PlannerTaskEntity>> = repository.plannerTasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val knowledgeNotes: StateFlow<List<KnowledgeNoteEntity>> = repository.knowledgeNotes.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val gamingProfiles: StateFlow<List<GamingProfileEntity>> = repository.gamingProfiles.stateIn(
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

    private fun readAppRamTelemetry(latencyMs: Long, engineLabel: String): PerformanceTelemetry {
        val rt = Runtime.getRuntime()
        val usedMb = ((rt.totalMemory() - rt.freeMemory()) / (1024L * 1024L)).coerceAtLeast(12L)
        val maxMb = (rt.maxMemory() / (1024L * 1024L)).coerceAtLeast(128L)
        return PerformanceTelemetry(
            appUsedRamMb = usedMb,
            appMaxRamMb = maxMb,
            lastResponseLatencyMs = latencyMs,
            lastEngineUsed = engineLabel
        )
    }

    fun refreshSystemSnapshot() {
        viewModelScope.launch {
            val apps = actionExecutor.getLaunchableInstalledApps()
            val volumes = actionExecutor.getCurrentVolumePercentages()
            val audit = PrivacyGuard.inspectDeviceHardware(appContext)
            val online = remoteAiProvider.isNetworkOnline(appContext)
            val notifGranted = NotificationVault.isNotificationAccessGranted(appContext)
            val currentPerf = _transientState.value.performanceTelemetry
            val updatedPerf = readAppRamTelemetry(
                latencyMs = currentPerf.lastResponseLatencyMs,
                engineLabel = currentPerf.lastEngineUsed
            )
            _transientState.update {
                it.copy(
                    installedApps = apps,
                    volumeMap = volumes,
                    hardwareAudit = audit,
                    performanceTelemetry = updatedPerf,
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

    fun selectWorkspaceTab(subTab: WorkspaceSubTab) {
        _transientState.update {
            it.copy(currentDestination = ZiaDestination.WORKSPACE, activeWorkspaceTab = subTab)
        }
    }

    fun dismissBanner() {
        _transientState.update { it.copy(statusBannerHinglish = null) }
    }

    fun showBanner(message: String) {
        _transientState.update { it.copy(statusBannerHinglish = message) }
    }

    fun startVoicePushToTalk() {
        val currentSettings = settingsState.value
        if (!currentSettings.voiceInputEnabled) {
            showBanner("Voice Input abhi Privacy Center mein disabled hai. Enable karke Push-to-Talk use karein.")
            return
        }
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
            val startTime = SystemClock.elapsedRealtime()
            val currentSettings = settingsState.value
            val currentMemories = memoryFacts.value

            repository.addChatMessage(
                isUser = true,
                text = cleaned,
                languageCode = currentSettings.language.code,
                engineSource = EngineSource.LOCAL_COMMAND
            )

            _transientState.update { it.copy(isThinking = true, statusBannerHinglish = null) }

            // Step 1: Deterministic Local Command Engine (Offline Phone Controls)
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
                        val elapsed = (SystemClock.elapsedRealtime() - startTime).coerceAtLeast(1L)
                        _transientState.update {
                            it.copy(
                                isThinking = false,
                                statusBannerHinglish = memResult.blockedReasonHinglish,
                                performanceTelemetry = readAppRamTelemetry(elapsed, "PrivacyGuard Blocker")
                            )
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

                val elapsed = (SystemClock.elapsedRealtime() - startTime).coerceAtLeast(1L)
                _transientState.update {
                    it.copy(
                        isThinking = false,
                        performanceTelemetry = readAppRamTelemetry(elapsed, "LocalCommandEngine")
                    )
                }
                if (currentSettings.voiceOutputEnabled) {
                    speakMessageAloud(finalReply)
                }
                return@launch
            }

            // Step 2: Check Remote Cloud AI availability & consent
            val online = remoteAiProvider.isNetworkOnline(appContext)
            _transientState.update { it.copy(isNetworkOnline = online) }

            val memoriesForCloud = if (currentSettings.memorySavingEnabled && currentSettings.cloudPersonalContextConsent) {
                currentMemories
            } else {
                emptyList()
            }

            if (!currentSettings.forceOfflineMode && currentSettings.cloudAiConsentEnabled && online) {
                when (
                    val cloudResult = remoteAiProvider.generateCloudResponse(
                        context = appContext,
                        userPrompt = cleaned,
                        language = currentSettings.language,
                        personality = currentSettings.personality,
                        responseLength = currentSettings.responseLength,
                        savedMemories = memoriesForCloud,
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
                        val elapsed = (SystemClock.elapsedRealtime() - startTime).coerceAtLeast(1L)
                        _transientState.update {
                            it.copy(
                                isThinking = false,
                                performanceTelemetry = readAppRamTelemetry(elapsed, "Gemini Cloud AI")
                            )
                        }
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
                            offlineReasonNote = cloudResult.reasonHinglish,
                            responseLength = currentSettings.responseLength,
                            customGreeting = currentSettings.customGreeting
                        )
                        repository.addChatMessage(
                            isUser = false,
                            text = localFallback.replyText,
                            languageCode = currentSettings.language.code,
                            engineSource = EngineSource.LOCAL_ASSISTANT,
                            codeSnippet = localFallback.codeSnippet
                        )
                        val elapsed = (SystemClock.elapsedRealtime() - startTime).coerceAtLeast(1L)
                        _transientState.update {
                            it.copy(
                                isThinking = false,
                                performanceTelemetry = readAppRamTelemetry(elapsed, "LocalAssistant Fallback")
                            )
                        }
                        if (currentSettings.voiceOutputEnabled) {
                            speakMessageAloud(localFallback.replyText)
                        }
                        return@launch
                    }
                }
            } else {
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
                    offlineReasonNote = reason,
                    responseLength = currentSettings.responseLength,
                    customGreeting = currentSettings.customGreeting
                )
                repository.addChatMessage(
                    isUser = false,
                    text = localReply.replyText,
                    languageCode = currentSettings.language.code,
                    engineSource = EngineSource.LOCAL_ASSISTANT,
                    codeSnippet = localReply.codeSnippet
                )
                val elapsed = (SystemClock.elapsedRealtime() - startTime).coerceAtLeast(1L)
                _transientState.update {
                    it.copy(
                        isThinking = false,
                        performanceTelemetry = readAppRamTelemetry(elapsed, "LocalAssistant (Offline)")
                    )
                }
                if (currentSettings.voiceOutputEnabled) {
                    speakMessageAloud(localReply.replyText)
                }
            }
        }
    }

    // =========================================================================
    // Daily Planner & Focus Timer Actions
    // =========================================================================
    fun addPlannerTask(
        title: String,
        subjectOrTag: String,
        dayOffset: Int,
        timeSlot: String,
        recurrence: TaskRecurrence,
        durationMinutes: Int
    ) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addPlannerTask(title, subjectOrTag, dayOffset, timeSlot, recurrence, durationMinutes)
            showBanner("Study task '${title.take(28)}' Planner mein add ho gaya!")
        }
    }

    fun togglePlannerTask(id: Int, completed: Boolean) {
        viewModelScope.launch { repository.togglePlannerTask(id, completed) }
    }

    fun deletePlannerTask(id: Int) {
        viewModelScope.launch { repository.deletePlannerTask(id) }
    }

    fun startFocusTimer(minutes: Int, sessionLabel: String) {
        focusTimerJob?.cancel()
        val totalSec = (minutes.coerceIn(1, 180)) * 60
        _transientState.update {
            it.copy(
                focusTimerSecondsRemaining = totalSec,
                focusTimerRunning = true,
                focusSessionLabel = sessionLabel
            )
        }
        focusTimerJob = viewModelScope.launch {
            while (_transientState.value.focusTimerSecondsRemaining > 0 && _transientState.value.focusTimerRunning) {
                delay(1000L)
                _transientState.update { state ->
                    val next = (state.focusTimerSecondsRemaining - 1).coerceAtLeast(0)
                    state.copy(focusTimerSecondsRemaining = next)
                }
            }
            if (_transientState.value.focusTimerSecondsRemaining == 0) {
                _transientState.update { it.copy(focusTimerRunning = false) }
                val doneMsg = "Focus session '$sessionLabel' complete ho gaya! 5 minute ka break lein."
                showBanner(doneMsg)
                if (settingsState.value.voiceOutputEnabled) {
                    speakMessageAloud(doneMsg)
                }
            }
        }
    }

    fun pauseOrResumeFocusTimer() {
        val current = _transientState.value
        if (current.focusTimerRunning) {
            focusTimerJob?.cancel()
            _transientState.update { it.copy(focusTimerRunning = false) }
        } else {
            val remainingMin = (current.focusTimerSecondsRemaining / 60).coerceAtLeast(1)
            startFocusTimer(remainingMin, current.focusSessionLabel)
        }
    }

    fun resetFocusTimer(defaultMinutes: Int = 25) {
        focusTimerJob?.cancel()
        _transientState.update {
            it.copy(
                focusTimerSecondsRemaining = defaultMinutes * 60,
                focusTimerRunning = false,
                focusSessionLabel = "${defaultMinutes}m Focus Session"
            )
        }
    }

    // =========================================================================
    // Coding Assistant & Local Code-Notes Library
    // =========================================================================
    fun generateAndSaveCodeSnippet(codingLanguage: String, promptOrError: String) {
        viewModelScope.launch {
            val reply = localAssistantEngine.generateCodeSnippetByLanguage(codingLanguage, promptOrError)
            val snippet = reply.codeSnippet ?: reply.replyText
            repository.saveKnowledgeNote(
                title = "$codingLanguage: ${promptOrError.ifBlank { "Template Snippet" }.take(40)}",
                content = snippet,
                category = NoteCategory.CODE_SNIPPET,
                languageOrTag = codingLanguage
            )
            showBanner("$codingLanguage snippet generate hokar Code-Notes Library mein save ho gaya!")
        }
    }

    // =========================================================================
    // Personal Knowledge Vault & Smart Search
    // =========================================================================
    fun saveNoteToKnowledgeVault(
        title: String,
        content: String,
        category: NoteCategory,
        languageOrTag: String
    ) {
        if (title.isBlank() || content.isBlank()) return
        viewModelScope.launch {
            val result = repository.saveKnowledgeNote(title, content, category, languageOrTag)
            if (!result.isSafe) {
                showBanner(result.blockedReasonHinglish ?: "Blocked by PrivacyGuard.")
            } else {
                showBanner("Saved to Knowledge Vault (${category.label})!")
            }
        }
    }

    fun deleteKnowledgeNote(id: Int) {
        viewModelScope.launch {
            repository.deleteKnowledgeNote(id)
            showBanner("Vault item deleted.")
        }
    }

    fun summarizeSafFileToVault(useCloudIfPermitted: Boolean) {
        val safFile = _transientState.value.selectedSafFile
        if (safFile == null) {
            showBanner("Pehle 'Pick File / Document' se koi text ya code file select karein.")
            return
        }
        viewModelScope.launch {
            val currentSettings = settingsState.value
            if (useCloudIfPermitted && (!currentSettings.cloudAiConsentEnabled || !currentSettings.cloudDocumentUploadConsent)) {
                showBanner("Document cloud upload ke liye Privacy Center mein 'Cloud Document Upload Consent' enable karein. Offline summary generate ki gayi hai.")
            }

            val summaryText = if (useCloudIfPermitted &&
                currentSettings.cloudAiConsentEnabled &&
                currentSettings.cloudDocumentUploadConsent &&
                !currentSettings.forceOfflineMode &&
                remoteAiProvider.isNetworkOnline(appContext)
            ) {
                val res = remoteAiProvider.generateCloudResponse(
                    context = appContext,
                    userPrompt = "Summarize this user-selected file (${safFile.displayName}) in concise bullet points:\n\n${safFile.previewContent.take(1200)}",
                    language = currentSettings.language,
                    personality = currentSettings.personality,
                    responseLength = ResponseLengthPreference.SHORT,
                    savedMemories = emptyList(),
                    customApiKeyOverride = currentSettings.customGeminiApiKey
                )
                when (res) {
                    is RemoteAiResult.Success -> res.reply.replyText
                    is RemoteAiResult.Unavailable -> localAssistantEngine.summarizeTextLocally(
                        safFile.displayName,
                        safFile.previewContent
                    )
                }
            } else {
                localAssistantEngine.summarizeTextLocally(safFile.displayName, safFile.previewContent)
            }

            repository.saveKnowledgeNote(
                title = "Summary: ${safFile.displayName}",
                content = summaryText,
                category = NoteCategory.DOC_SUMMARY,
                languageOrTag = safFile.mimeType
            )
            showBanner("'${safFile.displayName}' ka summary Knowledge Vault mein save ho gaya!")
        }
    }

    fun runSmartSearch(query: String, includeOnlineWebSearch: Boolean) {
        val q = query.trim()
        if (q.isEmpty()) return
        viewModelScope.launch {
            val qLower = q.lowercase()
            val localHits = mutableListOf<String>()

            knowledgeNotes.value.forEach { note ->
                if (note.title.lowercase().contains(qLower) || note.content.lowercase().contains(qLower)) {
                    localHits.add("[Vault · ${note.category}] ${note.title}: ${note.content.take(100)}")
                }
            }
            memoryFacts.value.forEach { mem ->
                if (mem.key.lowercase().contains(qLower) || mem.value.lowercase().contains(qLower)) {
                    localHits.add("[Memory · ${mem.category}] ${mem.key}: ${mem.value}")
                }
            }
            plannerTasks.value.forEach { task ->
                if (task.title.lowercase().contains(qLower) || task.subjectOrTag.lowercase().contains(qLower)) {
                    localHits.add("[Planner] ${task.title} (${task.timeSlot})")
                }
            }

            val currentSettings = settingsState.value
            if (!includeOnlineWebSearch || currentSettings.forceOfflineMode || !currentSettings.cloudAiConsentEnabled) {
                _transientState.update {
                    it.copy(
                        smartSearchResult = SmartSearchResult(
                            query = q,
                            localMatches = localHits,
                            retrievedWebFacts = emptyList(),
                            citedSources = listOf("On-Device SQLite Knowledge Vault"),
                            aiSynthesis = "100% Offline Search complete: Found ${localHits.size} local matches across your Vault, Memory, and Planner.",
                            isOnlineResult = false
                        )
                    )
                }
            } else {
                _transientState.update { it.copy(isThinking = true) }
                val onlineRes = remoteAiProvider.performSmartWebSearch(
                    context = appContext,
                    query = q,
                    localMatches = localHits,
                    customApiKeyOverride = currentSettings.customGeminiApiKey
                )
                _transientState.update {
                    it.copy(isThinking = false, smartSearchResult = onlineRes)
                }
            }
        }
    }

    // =========================================================================
    // Gaming Companion Actions
    // =========================================================================
    fun saveGamingProfile(
        gameTitle: String,
        sensitivityNotes: String,
        matchChecklist: String,
        ytIdeaNotes: String
    ) {
        if (gameTitle.isBlank()) return
        viewModelScope.launch {
            repository.saveGamingProfile(
                gameTitle = gameTitle,
                sensitivityNotes = sensitivityNotes,
                matchChecklist = matchChecklist,
                ytIdeaNotes = ytIdeaNotes
            )
            showBanner("Gaming profile '$gameTitle' saved locally!")
        }
    }

    fun deleteGamingProfile(id: Int) {
        viewModelScope.launch { repository.deleteGamingProfile(id) }
    }

    fun generateYouTubeGamingPack(gameName: String, focusTopic: String) {
        val pack = localAssistantEngine.generateGamingCreatorPack(gameName, focusTopic)
        _transientState.update { it.copy(generatedGamingPackText = pack) }
    }

    fun onGameplayScreenshotSelected(uri: Uri?) {
        _transientState.update {
            it.copy(
                selectedGameplayScreenshotUri = uri,
                screenshotAnalysisResult = if (uri != null) {
                    "Screenshot selected locally. Tap 'Confirm Cloud Vision Analysis' to analyze HUD/stats via Gemini, or inspect locally."
                } else null
            )
        }
    }

    fun analyzeSelectedScreenshotWithCloudConsent(question: String) {
        val uri = _transientState.value.selectedGameplayScreenshotUri
        if (uri == null) {
            showBanner("Pehle Photo Picker se koi gameplay screenshot select karein.")
            return
        }
        val currentSettings = settingsState.value
        if (!currentSettings.cloudAiConsentEnabled || !currentSettings.cloudDocumentUploadConsent) {
            showBanner("Screenshot cloud par bhejne ke liye Privacy Center mein 'Cloud AI' aur 'Document/Image Upload Consent' ON hona zaroori hai.")
            return
        }

        viewModelScope.launch {
            _transientState.update { it.copy(isThinking = true) }
            val result = remoteAiProvider.analyzeGameplayScreenshot(
                context = appContext,
                imageUri = uri,
                userQuestion = question,
                customApiKeyOverride = currentSettings.customGeminiApiKey
            )
            val outputText = when (result) {
                is RemoteAiResult.Success -> result.reply.replyText
                is RemoteAiResult.Unavailable -> result.reasonHinglish
            }
            _transientState.update {
                it.copy(isThinking = false, screenshotAnalysisResult = outputText)
            }
        }
    }

    // =========================================================================
    // Confirmation Gate & Device Actions
    // =========================================================================
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
                showBanner("Saari local memory, vault notes, planner tasks aur chat history safely wipe ho gayi.")
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

    // Memory & Export
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

    fun exportMemoryAndVaultAsText(): String {
        val sb = StringBuilder()
        sb.appendLine("=== ZIA AI LOCAL MEMORY & VAULT EXPORT ===")
        sb.appendLine("Preferences (${memoryFacts.value.size}):")
        memoryFacts.value.forEach {
            sb.appendLine("- [${it.category}] ${it.key}: ${it.value}")
        }
        sb.appendLine("\nKnowledge Vault (${knowledgeNotes.value.size}):")
        knowledgeNotes.value.forEach {
            sb.appendLine("- [${it.category} / ${it.languageOrTag}] ${it.title}: ${it.content}")
        }
        return sb.toString()
    }

    fun addReminder(title: String, timeText: String) {
        if (title.isBlank()) return
        viewModelScope.launch {
            repository.addReminder(title, timeText.ifBlank { "Anytime" })
            showBanner("Reminder added: $title")
        }
    }

    fun toggleReminder(id: Int, completed: Boolean) {
        viewModelScope.launch { repository.toggleReminder(id, completed) }
    }

    fun deleteReminder(id: Int) {
        viewModelScope.launch { repository.deleteReminder(id) }
    }

    fun clearChatHistoryOnly() {
        viewModelScope.launch {
            repository.clearChatHistory()
            repository.ensureStarterDataSeeded()
            showBanner("Chat history cleared.")
        }
    }

    fun readLatestNotificationAloud() {
        val currentSettings = settingsState.value
        if (!currentSettings.notificationReaderEnabled) {
            showBanner("Notification Reader abhi OFF hai. Privacy & Settings mein jaakar enable karein.")
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

    // Settings & Privacy Mutations
    fun setLanguage(language: AssistantLanguage) {
        viewModelScope.launch { preferenceStore.setLanguage(language) }
    }

    fun setPersonality(personality: PersonalityStyle) {
        viewModelScope.launch { preferenceStore.setPersonality(personality) }
    }

    fun setAvatarTheme(theme: AvatarThemePreset) {
        viewModelScope.launch { preferenceStore.setAvatarTheme(theme) }
    }

    fun setResponseLength(length: ResponseLengthPreference) {
        viewModelScope.launch { preferenceStore.setResponseLength(length) }
    }

    fun setCustomGreeting(greeting: String) {
        viewModelScope.launch {
            preferenceStore.setCustomGreeting(greeting)
            showBanner("Custom greeting updated!")
        }
    }

    fun setVoiceInputEnabled(enabled: Boolean) {
        viewModelScope.launch {
            if (!enabled) speechManager.stopListening()
            preferenceStore.setVoiceInputEnabled(enabled)
        }
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

    fun setCloudPersonalContextConsent(enabled: Boolean) {
        viewModelScope.launch { preferenceStore.setCloudPersonalContextConsent(enabled) }
    }

    fun setCloudDocumentUploadConsent(enabled: Boolean) {
        viewModelScope.launch { preferenceStore.setCloudDocumentUploadConsent(enabled) }
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
        focusTimerJob?.cancel()
        speechManager.release()
    }
}
