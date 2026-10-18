package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FilledIconButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.AssistantLanguage
import com.example.data.AvatarState
import com.example.data.ChatMessageEntity
import com.example.data.EngineSource
import com.example.data.PendingAction
import com.example.data.ZiaSettingsState
import com.example.ui.ZiaDestination
import com.example.ui.components.ActionConfirmationModalCard
import com.example.ui.components.AnimeAvatarStage
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ZiaAcousticCyan
import com.example.ui.theme.ZiaAmberWarning
import com.example.ui.theme.ZiaCardSurface
import com.example.ui.theme.ZiaCrimsonError
import com.example.ui.theme.ZiaElevatedBorder
import com.example.ui.theme.ZiaEmeraldOnline
import com.example.ui.theme.ZiaMidnightSurface
import com.example.ui.theme.ZiaNeuralViolet
import com.example.ui.theme.ZiaSapphireBlue
import com.example.ui.theme.ZiaTextMuted
import com.example.ui.theme.ZiaTextPrimary
import com.example.ui.theme.ZiaTextSecondary

@Composable
fun HomeScreen(
    settings: ZiaSettingsState,
    avatarState: AvatarState,
    isListening: Boolean,
    isSpeaking: Boolean,
    isThinking: Boolean,
    partialTranscript: String,
    audioLevelRms: Float,
    isNetworkOnline: Boolean,
    latestMessages: List<ChatMessageEntity>,
    pendingAction: PendingAction?,
    onSelectLanguage: (AssistantLanguage) -> Unit,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onStopSpeaking: () -> Unit,
    onSubmitText: (String) -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit,
    onNavigate: (ZiaDestination) -> Unit,
    onShowBanner: (String) -> Unit
) {
    val context = LocalContext.current
    var textInput by rememberSaveable { mutableStateOf("") }
    var showMicRationaleDialog by rememberSaveable { mutableStateOf(false) }

    val micPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) {
            onStartVoice()
        } else {
            onShowBanner("Microphone permission deny ho gayi. Aap niche Text Input box se ZIA ke saare features use kar sakte hain.")
        }
    }

    fun handleMicTap() {
        if (isListening) {
            onStopVoice()
            return
        }
        val hasPerm = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.RECORD_AUDIO
        ) == PackageManager.PERMISSION_GRANTED

        if (hasPerm) {
            onStartVoice()
        } else {
            showMicRationaleDialog = true
        }
    }

    if (showMicRationaleDialog) {
        AlertDialog(
            onDismissRequest = { showMicRationaleDialog = false },
            title = { Text("Microphone Permission (Push-to-Talk)") },
            text = {
                Text(stringResource(id = R.string.mic_permission_rationale))
            },
            confirmButton = {
                Button(
                    onClick = {
                        showMicRationaleDialog = false
                        micPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO)
                    },
                    modifier = Modifier.testTag("grant_mic_permission_button")
                ) {
                    Text("Allow Microphone")
                }
            },
            dismissButton = {
                TextButton(onClick = { showMicRationaleDialog = false }) {
                    Text("Use Text Only")
                }
            }
        )
    }

    val quickCommandSamples = listOf(
        "7:30 ka alarm lagao",
        "Volume 70% karo",
        "Open YouTube",
        "Kemon acho ZIA?",
        "Kotlin coroutine code dikhao",
        "Device RAM status check"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 600.dp)
                .fillMaxWidth(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // 1. Top Connectivity & Mode Telemetry Row
            item {
                TopTelemetryHeader(
                    settings = settings,
                    isNetworkOnline = isNetworkOnline,
                    onOfflineBadgeClick = { onNavigate(ZiaDestination.OFFLINE) }
                )
            }

            // 2. Multilingual Quick Selector Bar
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistantLanguage.entries.forEach { lang ->
                        val selected = settings.language == lang
                        FilterChip(
                            selected = selected,
                            onClick = { onSelectLanguage(lang) },
                            label = {
                                Text(
                                    text = when (lang) {
                                        AssistantLanguage.AUTO_MIXED -> "Auto (Mixed)"
                                        AssistantLanguage.ENGLISH -> "English"
                                        AssistantLanguage.HINGLISH -> "Hinglish"
                                        AssistantLanguage.BANGLISH -> "Banglish"
                                        AssistantLanguage.BENGALI -> "বাংলা"
                                    },
                                    style = MaterialTheme.typography.labelLarge
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = ZiaSapphireBlue.copy(alpha = 0.25f),
                                selectedLabelColor = ZiaTextPrimary
                            ),
                            modifier = Modifier.testTag("lang_chip_${lang.code.lowercase()}")
                        )
                    }
                }
            }

            // 3. Centerpiece Anime Character Avatar Stage
            item {
                AnimeAvatarStage(
                    avatarState = avatarState,
                    audioLevelRms = audioLevelRms,
                    avatarSize = 212.dp
                )
            }

            // 4. Active Listening / Speaking Controls Banner
            if (isListening || isSpeaking || partialTranscript.isNotBlank()) {
                item {
                    Surface(
                        color = ZiaCardSurface,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ZiaAcousticCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                    ) {
                        Row(
                            modifier = Modifier.padding(12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = if (isListening) "Listening (${settings.language.displayName})..." else "Voice Output Active",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ZiaAcousticCyan
                                )
                                if (partialTranscript.isNotBlank()) {
                                    Text(
                                        text = "\"$partialTranscript\"",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ZiaTextPrimary
                                    )
                                }
                            }
                            if (isListening) {
                                OutlinedButton(
                                    onClick = onStopVoice,
                                    modifier = Modifier.testTag("stop_listening_button")
                                ) {
                                    Icon(Icons.Default.Stop, contentDescription = "Stop listening", tint = ZiaCrimsonError)
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Stop", color = ZiaCrimsonError)
                                }
                            }
                            if (isSpeaking) {
                                OutlinedButton(
                                    onClick = onStopSpeaking,
                                    modifier = Modifier.testTag("stop_speaking_button")
                                ) {
                                    Icon(Icons.Default.VolumeOff, contentDescription = "Mute speech")
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Mute")
                                }
                            }
                        }
                    }
                }
            }

            // 5. Confirmation Preview Gate (if any sensitive phone action is waiting)
            if (pendingAction != null) {
                item {
                    ActionConfirmationModalCard(
                        pendingAction = pendingAction,
                        onConfirm = onConfirmAction,
                        onCancel = onCancelAction,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }

            // 6. Quick Multilingual Voice/Text Sample Chips
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "TRY VOICE OR TAP A COMMAND",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZiaTextMuted
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickCommandSamples.forEachIndexed { idx, sample ->
                            AssistChip(
                                onClick = { onSubmitText(sample) },
                                label = { Text(sample) },
                                colors = AssistChipDefaults.assistChipColors(
                                    containerColor = ZiaMidnightSurface,
                                    labelColor = ZiaTextPrimary
                                ),
                                modifier = Modifier.testTag("sample_command_chip_$idx")
                            )
                        }
                    }
                }
            }

            // 7. Recent Conversation Preview Card
            item {
                val recentTwo = latestMessages.takeLast(2)
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigate(ZiaDestination.CHAT) }
                        .testTag("home_conversation_preview_card"),
                    colors = CardDefaults.cardColors(containerColor = ZiaCardSurface),
                    shape = RoundedCornerShape(18.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "LATEST CONVERSATION",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZiaSapphireBlue
                            )
                            Text(
                                text = "Open Full Chat →",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZiaAcousticCyan
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        recentTwo.forEach { msg ->
                            Row(
                                modifier = Modifier.padding(vertical = 4.dp),
                                verticalAlignment = Alignment.Top
                            ) {
                                Text(
                                    text = if (msg.isUser) "You: " else "ZIA: ",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = if (msg.isUser) ZiaAcousticCyan else ZiaSapphireBlue
                                )
                                Text(
                                    text = msg.text,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ZiaTextPrimary,
                                    maxLines = 3,
                                    overflow = TextOverflow.Ellipsis
                                )
                            }
                        }
                        if (isThinking) {
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                modifier = Modifier.fillMaxWidth(),
                                color = ZiaNeuralViolet
                            )
                        }
                    }
                }
            }
        }

        // Bottom Push-to-Talk & Permanent Text Input Dock
        VoiceAndTextInputDock(
            textInput = textInput,
            onTextChange = { textInput = it },
            isListening = isListening,
            onMicClick = { handleMicTap() },
            onSendClick = {
                if (textInput.isNotBlank()) {
                    onSubmitText(textInput)
                    textInput = ""
                }
            },
            placeholder = "Speak ('Hey ZIA...') or type in EN / Hinglish / Banglish / বাংলা..."
        )
    }
}

@Composable
private fun TopTelemetryHeader(
    settings: ZiaSettingsState,
    isNetworkOnline: Boolean,
    onOfflineBadgeClick: () -> Unit
) {
    val effectiveOnline = isNetworkOnline && !settings.forceOfflineMode && settings.cloudAiConsentEnabled
    val statusColor = when {
        effectiveOnline -> ZiaEmeraldOnline
        !isNetworkOnline || settings.forceOfflineMode -> ZiaAmberWarning
        else -> ZiaSapphireBlue
    }
    val statusText = when {
        settings.forceOfflineMode -> "Offline Mode Forced · Local Only"
        !isNetworkOnline -> "No Internet · Offline Engine Active"
        !settings.cloudAiConsentEnabled -> "Local Privacy Mode · Cloud Opt-Out"
        else -> "Online · Hybrid Local + Gemini AI"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(ZiaMidnightSurface)
            .border(1.dp, ZiaElevatedBorder, RoundedCornerShape(16.dp))
            .clickable { onOfflineBadgeClick() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
            .testTag("top_telemetry_header"),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
            Icon(
                imageVector = if (effectiveOnline) Icons.Default.CloudDone else Icons.Default.CloudOff,
                contentDescription = statusText,
                tint = statusColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column {
                Text(
                    text = "ZIA AI · ${settings.personality.title.uppercase()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZiaTextSecondary
                )
                Text(
                    text = statusText,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = statusColor
                )
            }
        }
        Surface(
            color = ZiaCardSurface,
            shape = RoundedCornerShape(10.dp)
        ) {
            Text(
                text = if (settings.wakeWordSessionEnabled) "Hey ZIA: ON" else "Push-to-Talk",
                style = MaterialTheme.typography.labelSmall,
                color = ZiaAcousticCyan,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun VoiceAndTextInputDock(
    textInput: String,
    onTextChange: (String) -> Unit,
    isListening: Boolean,
    onMicClick: () -> Unit,
    onSendClick: () -> Unit,
    placeholder: String
) {
    Surface(
        color = ZiaMidnightSurface,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .border(1.dp, ZiaElevatedBorder, RoundedCornerShape(24.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Push-to-Talk Microphone Button (48dp minimum touch target)
            FilledIconButton(
                onClick = onMicClick,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("push_to_talk_mic_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = if (isListening) ZiaCrimsonError else ZiaSapphireBlue,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = if (isListening) Icons.Default.Stop else Icons.Default.Mic,
                    contentDescription = if (isListening) "Stop listening" else "Start Push-to-Talk voice input"
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            OutlinedTextField(
                value = textInput,
                onValueChange = onTextChange,
                placeholder = {
                    Text(
                        text = placeholder,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZiaTextMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Send),
                keyboardActions = KeyboardActions(onSend = { onSendClick() }),
                modifier = Modifier
                    .weight(1f)
                    .testTag("assistant_text_input")
            )

            Spacer(modifier = Modifier.width(8.dp))

            FilledIconButton(
                onClick = onSendClick,
                enabled = textInput.isNotBlank(),
                modifier = Modifier
                    .size(48.dp)
                    .testTag("send_message_button"),
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = ZiaNeuralViolet,
                    contentColor = Color.White
                )
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Send message"
                )
            }
        }
    }
}

@Composable
fun ChatScreen(
    messages: List<ChatMessageEntity>,
    settings: ZiaSettingsState,
    isListening: Boolean,
    isThinking: Boolean,
    pendingAction: PendingAction?,
    onStartVoice: () -> Unit,
    onStopVoice: () -> Unit,
    onSubmitText: (String) -> Unit,
    onSpeakMessage: (String) -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit,
    onClearChat: () -> Unit,
    onShowBanner: (String) -> Unit
) {
    val context = LocalContext.current
    var textInput by rememberSaveable { mutableStateOf("") }
    val listState = rememberLazyListState()

    val micPermLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        if (granted) onStartVoice()
        else onShowBanner("Mic permission denied — Text mode active.")
    }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.size - 1)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Chat Header Bar
        Row(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "Multilingual Chat & Coding Helper",
                    style = MaterialTheme.typography.titleLarge,
                    color = ZiaTextPrimary
                )
                Text(
                    text = "Mode: ${settings.language.displayName} · ${settings.personality.title}",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZiaTextSecondary
                )
            }
            IconButton(
                onClick = onClearChat,
                modifier = Modifier.testTag("clear_chat_button")
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteSweep,
                    contentDescription = "Clear chat history",
                    tint = ZiaTextSecondary
                )
            }
        }

        if (pendingAction != null) {
            ActionConfirmationModalCard(
                pendingAction = pendingAction,
                onConfirm = onConfirmAction,
                onCancel = onCancelAction,
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )
        }

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .testTag("chat_messages_list"),
            contentPadding = PaddingValues(vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                ChatMessageBubble(
                    message = msg,
                    onSpeakClick = { onSpeakMessage(msg.text) },
                    onCopyCode = { snippet ->
                        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                        clipboard?.setPrimaryClip(ClipData.newPlainText("ZIA Code Snippet", snippet))
                        onShowBanner("Code snippet clipboard mein copy ho gaya!")
                    }
                )
            }

            if (isThinking) {
                item {
                    Surface(
                        color = ZiaCardSurface,
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(vertical = 4.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "ZIA is thinking...",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZiaNeuralViolet
                            )
                        }
                    }
                }
            }
        }

        VoiceAndTextInputDock(
            textInput = textInput,
            onTextChange = { textInput = it },
            isListening = isListening,
            onMicClick = {
                if (isListening) {
                    onStopVoice()
                } else {
                    val hasPerm = ContextCompat.checkSelfPermission(
                        context,
                        Manifest.permission.RECORD_AUDIO
                    ) == PackageManager.PERMISSION_GRANTED
                    if (hasPerm) onStartVoice()
                    else micPermLauncher.launch(Manifest.permission.RECORD_AUDIO)
                }
            },
            onSendClick = {
                if (textInput.isNotBlank()) {
                    onSubmitText(textInput)
                    textInput = ""
                }
            },
            placeholder = "Ask ZIA anything or request Kotlin/Python code..."
        )
    }
}

@Composable
private fun ChatMessageBubble(
    message: ChatMessageEntity,
    onSpeakClick: () -> Unit,
    onCopyCode: (String) -> Unit
) {
    val isUser = message.isUser
    val sourceLabel = remember(message.engineSource) {
        EngineSource.entries.find { it.name == message.engineSource }?.label
            ?: "Local Assistant"
    }

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = if (isUser) Alignment.End else Alignment.Start
    ) {
        Surface(
            shape = RoundedCornerShape(
                topStart = 18.dp,
                topEnd = 18.dp,
                bottomStart = if (isUser) 18.dp else 4.dp,
                bottomEnd = if (isUser) 4.dp else 18.dp
            ),
            color = if (isUser) Color(0xFF1D3566) else ZiaCardSurface,
            modifier = Modifier
                .widthIn(max = 520.dp)
                .border(
                    width = 1.dp,
                    color = if (isUser) ZiaSapphireBlue.copy(alpha = 0.5f) else ZiaElevatedBorder,
                    shape = RoundedCornerShape(
                        topStart = 18.dp,
                        topEnd = 18.dp,
                        bottomStart = if (isUser) 18.dp else 4.dp,
                        bottomEnd = if (isUser) 4.dp else 18.dp
                    )
                )
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                if (!isUser) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = if (message.engineSource == EngineSource.SYSTEM_SECURITY.name) {
                                    Icons.Default.Security
                                } else {
                                    Icons.Default.Code
                                },
                                contentDescription = null,
                                tint = ZiaAcousticCyan,
                                modifier = Modifier.size(14.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "ZIA · $sourceLabel",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZiaAcousticCyan
                            )
                        }
                        IconButton(
                            onClick = onSpeakClick,
                            modifier = Modifier.size(28.dp)
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                contentDescription = "Speak reply aloud",
                                tint = ZiaTextSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                Text(
                    text = message.text,
                    style = MaterialTheme.typography.bodyLarge,
                    color = ZiaTextPrimary
                )

                // Render safe copyable Code Snippet Card if present
                AnimatedVisibility(visible = !message.codeSnippet.isNullOrBlank()) {
                    val code = message.codeSnippet.orEmpty()
                    Column(
                        modifier = Modifier
                            .padding(top = 10.dp)
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(Color(0xFF0A0D18))
                            .border(1.dp, ZiaNeuralViolet.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                            .padding(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "SAFE CODE SNIPPET (READ/EDIT ONLY)",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZiaNeuralViolet
                            )
                            TextButton(onClick = { onCopyCode(code) }) {
                                Icon(
                                    imageVector = Icons.Default.ContentCopy,
                                    contentDescription = "Copy code",
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Copy")
                            }
                        }
                        Text(
                            text = code,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFFE2E8F0),
                            modifier = Modifier.horizontalScroll(rememberScrollState())
                        )
                    }
                }
            }
        }
    }
}
