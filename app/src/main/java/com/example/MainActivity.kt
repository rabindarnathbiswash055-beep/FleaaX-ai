package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DashboardCustomize
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.PendingAction
import com.example.ui.ZiaAssistantViewModel
import com.example.ui.ZiaDestination
import com.example.ui.screens.ChatScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.QuickActionsScreen
import com.example.ui.screens.SettingsScreen
import com.example.ui.screens.WorkspaceHubScreen
import com.example.ui.theme.ZiaAITheme
import com.example.ui.theme.ZiaAcousticCyan
import com.example.ui.theme.ZiaCardSurface
import com.example.ui.theme.ZiaElevatedBorder
import com.example.ui.theme.ZiaMidnightSurface
import com.example.ui.theme.ZiaObsidianBg
import com.example.ui.theme.ZiaSapphireBlue
import com.example.ui.theme.ZiaTextPrimary
import com.example.ui.theme.ZiaTextSecondary

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ZiaAITheme {
                ZiaMainApp()
            }
        }
    }
}

@Composable
fun ZiaMainApp(
    viewModel: ZiaAssistantViewModel = viewModel()
) {
    val settings by viewModel.settingsState.collectAsStateWithLifecycle()
    val transient by viewModel.transientState.collectAsStateWithLifecycle()
    val avatarState by viewModel.avatarState.collectAsStateWithLifecycle()
    val chatMessages by viewModel.chatMessages.collectAsStateWithLifecycle()
    val memoryFacts by viewModel.memoryFacts.collectAsStateWithLifecycle()
    val reminders by viewModel.reminders.collectAsStateWithLifecycle()
    val plannerTasks by viewModel.plannerTasks.collectAsStateWithLifecycle()
    val knowledgeNotes by viewModel.knowledgeNotes.collectAsStateWithLifecycle()
    val gamingProfiles by viewModel.gamingProfiles.collectAsStateWithLifecycle()
    val recentNotifications by viewModel.recentNotifications.collectAsStateWithLifecycle()
    val isListening by viewModel.isListening.collectAsStateWithLifecycle()
    val isSpeaking by viewModel.isSpeaking.collectAsStateWithLifecycle()
    val partialTranscript by viewModel.partialTranscript.collectAsStateWithLifecycle()
    val audioLevelRms by viewModel.audioLevelRms.collectAsStateWithLifecycle()

    BackHandler(enabled = transient.currentDestination != ZiaDestination.HOME) {
        viewModel.navigateTo(ZiaDestination.HOME)
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(ZiaObsidianBg)
    ) {
        val useNavigationRail = maxWidth >= 640.dp

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = ZiaObsidianBg,
            contentWindowInsets = WindowInsets.safeDrawing,
            bottomBar = {
                if (!useNavigationRail) {
                    ZiaBottomNavigationBar(
                        currentDestination = transient.currentDestination,
                        onNavigate = viewModel::navigateTo
                    )
                }
            }
        ) { innerPadding ->
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                if (useNavigationRail) {
                    ZiaSideNavigationRail(
                        currentDestination = transient.currentDestination,
                        onNavigate = viewModel::navigateTo
                    )
                }

                Column(modifier = Modifier.fillMaxSize()) {
                    AnimatedVisibility(visible = !transient.statusBannerHinglish.isNullOrBlank()) {
                        Surface(
                            color = ZiaCardSurface,
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 16.dp, vertical = 6.dp)
                                .border(1.dp, ZiaAcousticCyan.copy(alpha = 0.6f), RoundedCornerShape(14.dp))
                                .testTag("status_feedback_banner")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = transient.statusBannerHinglish.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ZiaTextPrimary,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(
                                    onClick = viewModel::dismissBanner,
                                    modifier = Modifier.testTag("dismiss_banner_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Dismiss notification banner",
                                        tint = ZiaTextSecondary
                                    )
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.weight(1f)) {
                        when (transient.currentDestination) {
                            ZiaDestination.HOME -> HomeScreen(
                                settings = settings,
                                avatarState = avatarState,
                                isListening = isListening,
                                isSpeaking = isSpeaking,
                                isThinking = transient.isThinking,
                                partialTranscript = partialTranscript,
                                audioLevelRms = audioLevelRms,
                                isNetworkOnline = transient.isNetworkOnline,
                                performanceTelemetry = transient.performanceTelemetry,
                                latestMessages = chatMessages,
                                pendingAction = transient.pendingConfirmationAction,
                                onSelectLanguage = viewModel::setLanguage,
                                onStartVoice = viewModel::startVoicePushToTalk,
                                onStopVoice = viewModel::stopVoiceListening,
                                onStopSpeaking = viewModel::stopSpeaking,
                                onSubmitText = viewModel::submitUserMessage,
                                onConfirmAction = viewModel::confirmAndExecutePendingAction,
                                onCancelAction = viewModel::cancelPendingAction,
                                onNavigate = viewModel::navigateTo,
                                onOpenWorkspaceSubTab = viewModel::selectWorkspaceTab,
                                onShowBanner = viewModel::showBanner
                            )

                            ZiaDestination.CHAT -> ChatScreen(
                                messages = chatMessages,
                                settings = settings,
                                isListening = isListening,
                                isThinking = transient.isThinking,
                                pendingAction = transient.pendingConfirmationAction,
                                onStartVoice = viewModel::startVoicePushToTalk,
                                onStopVoice = viewModel::stopVoiceListening,
                                onSubmitText = viewModel::submitUserMessage,
                                onSpeakMessage = viewModel::speakMessageAloud,
                                onSaveMessageToVault = viewModel::saveNoteToKnowledgeVault,
                                onConfirmAction = viewModel::confirmAndExecutePendingAction,
                                onCancelAction = viewModel::cancelPendingAction,
                                onClearChat = viewModel::clearChatHistoryOnly,
                                onShowBanner = viewModel::showBanner
                            )

                            ZiaDestination.WORKSPACE -> WorkspaceHubScreen(
                                activeSubTab = transient.activeWorkspaceTab,
                                onSelectSubTab = viewModel::selectWorkspaceTab,
                                settings = settings,
                                plannerTasks = plannerTasks,
                                knowledgeNotes = knowledgeNotes,
                                gamingProfiles = gamingProfiles,
                                memoryFacts = memoryFacts,
                                pendingAction = transient.pendingConfirmationAction,
                                focusTimerSeconds = transient.focusTimerSecondsRemaining,
                                focusTimerRunning = transient.focusTimerRunning,
                                focusSessionLabel = transient.focusSessionLabel,
                                selectedSafFile = transient.selectedSafFile,
                                smartSearchResult = transient.smartSearchResult,
                                selectedScreenshotUri = transient.selectedGameplayScreenshotUri,
                                screenshotAnalysisResult = transient.screenshotAnalysisResult,
                                generatedGamingPackText = transient.generatedGamingPackText,
                                onStartFocusTimer = viewModel::startFocusTimer,
                                onPauseResumeFocusTimer = viewModel::pauseOrResumeFocusTimer,
                                onResetFocusTimer = viewModel::resetFocusTimer,
                                onAddPlannerTask = viewModel::addPlannerTask,
                                onTogglePlannerTask = viewModel::togglePlannerTask,
                                onDeletePlannerTask = viewModel::deletePlannerTask,
                                onRequestAction = viewModel::requestActionWithConfirmationGate,
                                onConfirmAction = viewModel::confirmAndExecutePendingAction,
                                onCancelAction = viewModel::cancelPendingAction,
                                onGenerateAndSaveCodeSnippet = viewModel::generateAndSaveCodeSnippet,
                                onSendPromptToChat = { prompt ->
                                    viewModel.navigateTo(ZiaDestination.CHAT)
                                    viewModel.submitUserMessage(prompt)
                                },
                                onSafFilePicked = viewModel::onSafFileSelected,
                                onSummarizeSafFile = viewModel::summarizeSafFileToVault,
                                onSaveVaultNote = viewModel::saveNoteToKnowledgeVault,
                                onDeleteVaultNote = viewModel::deleteKnowledgeNote,
                                onRunSmartSearch = viewModel::runSmartSearch,
                                onSaveGamingProfile = viewModel::saveGamingProfile,
                                onDeleteGamingProfile = viewModel::deleteGamingProfile,
                                onGenerateGamingPack = viewModel::generateYouTubeGamingPack,
                                onScreenshotPicked = viewModel::onGameplayScreenshotSelected,
                                onAnalyzeScreenshot = viewModel::analyzeSelectedScreenshotWithCloudConsent,
                                onToggleMemorySaving = viewModel::setMemorySavingEnabled,
                                onAddMemoryFact = viewModel::addOrUpdateMemoryFact,
                                onEditMemoryFact = viewModel::editExistingMemoryFact,
                                onDeleteMemoryFact = viewModel::deleteMemoryFact,
                                onExportMemoryText = viewModel::exportMemoryAndVaultAsText,
                                onShowBanner = viewModel::showBanner
                            )

                            ZiaDestination.ACTIONS -> QuickActionsScreen(
                                installedApps = transient.installedApps,
                                volumeMap = transient.volumeMap,
                                reminders = reminders,
                                selectedSafFile = transient.selectedSafFile,
                                recentNotifications = recentNotifications,
                                pendingAction = transient.pendingConfirmationAction,
                                onRequestAction = viewModel::requestActionWithConfirmationGate,
                                onConfirmAction = viewModel::confirmAndExecutePendingAction,
                                onCancelAction = viewModel::cancelPendingAction,
                                onVolumeChange = viewModel::updateStreamVolume,
                                onAddReminder = viewModel::addReminder,
                                onToggleReminder = viewModel::toggleReminder,
                                onDeleteReminder = viewModel::deleteReminder,
                                onSafFilePicked = viewModel::onSafFileSelected,
                                onClearSafFile = viewModel::clearSelectedSafFile,
                                onReadLatestNotification = viewModel::readLatestNotificationAloud,
                                onSendCodingPromptToChat = { prompt ->
                                    viewModel.navigateTo(ZiaDestination.CHAT)
                                    viewModel.submitUserMessage(prompt)
                                }
                            )

                            ZiaDestination.SETTINGS -> SettingsScreen(
                                settings = settings,
                                notificationAccessGranted = transient.notificationPermissionGranted,
                                hardwareAudit = transient.hardwareAudit,
                                performanceTelemetry = transient.performanceTelemetry,
                                pendingAction = transient.pendingConfirmationAction,
                                onSelectLanguage = viewModel::setLanguage,
                                onSelectPersonality = viewModel::setPersonality,
                                onSelectAvatarTheme = viewModel::setAvatarTheme,
                                onSelectResponseLength = viewModel::setResponseLength,
                                onSaveCustomGreeting = viewModel::setCustomGreeting,
                                onToggleVoiceInput = viewModel::setVoiceInputEnabled,
                                onToggleVoiceOutput = viewModel::setVoiceOutputEnabled,
                                onSpeechRateChange = viewModel::setSpeechRate,
                                onSpeechPitchChange = viewModel::setSpeechPitch,
                                onTestVoiceClick = {
                                    viewModel.speakMessageAloud(settings.customGreeting)
                                },
                                onToggleMemorySaving = viewModel::setMemorySavingEnabled,
                                onToggleCloudConsent = viewModel::setCloudAiConsentEnabled,
                                onToggleCloudPersonalContext = viewModel::setCloudPersonalContextConsent,
                                onToggleCloudDocUpload = viewModel::setCloudDocumentUploadConsent,
                                onToggleForceOffline = viewModel::setForceOfflineMode,
                                onToggleWakeWordSession = viewModel::setWakeWordSessionEnabled,
                                onToggleNotificationReader = viewModel::setNotificationReaderEnabled,
                                onOpenNotificationAccessSettings = {
                                    viewModel.requestActionWithConfirmationGate(
                                        PendingAction.OpenSystemSettings("NOTIFICATION_LISTENER")
                                    )
                                },
                                onOpenAppPermissionSettings = {
                                    viewModel.requestActionWithConfirmationGate(
                                        PendingAction.OpenSystemSettings("APP_DETAILS")
                                    )
                                },
                                onRequestWipeAllData = {
                                    viewModel.requestActionWithConfirmationGate(
                                        PendingAction.ClearAllMemory()
                                    )
                                },
                                onConfirmAction = viewModel::confirmAndExecutePendingAction,
                                onCancelAction = viewModel::cancelPendingAction,
                                onSaveCustomApiKey = viewModel::setCustomGeminiApiKey
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun destinationIcon(dest: ZiaDestination): ImageVector = when (dest) {
    ZiaDestination.HOME -> Icons.Default.Home
    ZiaDestination.CHAT -> Icons.AutoMirrored.Filled.Chat
    ZiaDestination.WORKSPACE -> Icons.Default.DashboardCustomize
    ZiaDestination.ACTIONS -> Icons.Default.Bolt
    ZiaDestination.SETTINGS -> Icons.Default.Security
}

@Composable
private fun ZiaBottomNavigationBar(
    currentDestination: ZiaDestination,
    onNavigate: (ZiaDestination) -> Unit
) {
    NavigationBar(
        containerColor = ZiaMidnightSurface,
        modifier = Modifier
            .border(1.dp, ZiaElevatedBorder, RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp))
            .testTag("bottom_navigation_bar")
    ) {
        ZiaDestination.entries.forEach { dest ->
            val selected = currentDestination == dest
            NavigationBarItem(
                selected = selected,
                onClick = { onNavigate(dest) },
                icon = {
                    Icon(
                        imageVector = destinationIcon(dest),
                        contentDescription = dest.label
                    )
                },
                label = {
                    Text(
                        text = dest.label,
                        style = MaterialTheme.typography.labelSmall
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = ZiaAcousticCyan,
                    selectedTextColor = ZiaAcousticCyan,
                    indicatorColor = ZiaSapphireBlue.copy(alpha = 0.22f),
                    unselectedIconColor = ZiaTextSecondary,
                    unselectedTextColor = ZiaTextSecondary
                ),
                modifier = Modifier.testTag("nav_tab_${dest.route}")
            )
        }
    }
}

@Composable
private fun ZiaSideNavigationRail(
    currentDestination: ZiaDestination,
    onNavigate: (ZiaDestination) -> Unit
) {
    NavigationRail(
        containerColor = ZiaMidnightSurface,
        modifier = Modifier
            .fillMaxHeight()
            .testTag("side_navigation_rail")
    ) {
        ZiaDestination.entries.forEach { dest ->
            val selected = currentDestination == dest
            NavigationRailItem(
                selected = selected,
                onClick = { onNavigate(dest) },
                icon = {
                    Icon(
                        imageVector = destinationIcon(dest),
                        contentDescription = dest.label
                    )
                },
                label = { Text(dest.label) },
                modifier = Modifier.testTag("rail_tab_${dest.route}")
            )
        }
        Spacer(modifier = Modifier.width(4.dp))
    }
}
