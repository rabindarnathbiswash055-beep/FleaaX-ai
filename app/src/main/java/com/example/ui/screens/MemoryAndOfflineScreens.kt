package com.example.ui.screens

import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.data.AssistantLanguage
import com.example.data.MemoryFactEntity
import com.example.data.PendingAction
import com.example.data.PersonalityStyle
import com.example.data.ZiaSettingsState
import com.example.security.DeviceHardwareAudit
import com.example.ui.components.ActionConfirmationModalCard
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
fun MemoryScreen(
    memoryFacts: List<MemoryFactEntity>,
    settings: ZiaSettingsState,
    pendingAction: PendingAction?,
    onToggleMemorySaving: (Boolean) -> Unit,
    onAddMemoryFact: (String, String, String) -> Unit,
    onEditMemoryFact: (MemoryFactEntity, String, String) -> Unit,
    onDeleteMemoryFact: (Int) -> Unit,
    onRequestWipeAll: () -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit
) {
    var newKey by rememberSaveable { mutableStateOf("") }
    var newValue by rememberSaveable { mutableStateOf("") }
    var editingFact by remember { mutableStateOf<MemoryFactEntity?>(null) }
    var editKeyText by rememberSaveable { mutableStateOf("") }
    var editValueText by rememberSaveable { mutableStateOf("") }

    if (editingFact != null) {
        AlertDialog(
            onDismissRequest = { editingFact = null },
            title = { Text("Edit Saved Preference") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = editKeyText,
                        onValueChange = { editKeyText = it },
                        label = { Text("Preference Key") },
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = editValueText,
                        onValueChange = { editValueText = it },
                        label = { Text("Preference Value") }
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        editingFact?.let { onEditMemoryFact(it, editKeyText, editValueText) }
                        editingFact = null
                    }
                ) {
                    Text("Save Changes")
                }
            },
            dismissButton = {
                TextButton(onClick = { editingFact = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (pendingAction != null) {
            ActionConfirmationModalCard(
                pendingAction = pendingAction,
                onConfirm = onConfirmAction,
                onCancel = onCancelAction,
                modifier = Modifier
                    .widthIn(max = 600.dp)
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
            )
        }

        LazyColumn(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .testTag("memory_vault_list"),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Local Preference Memory Vault",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ZiaTextPrimary
                )
                Text(
                    text = stringResource(id = R.string.privacy_notice_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZiaTextSecondary
                )
            }

            // Opt-In / Opt-Out Memory Switch Card
            item {
                Card(
                    colors = CardDefaults.cardColors(containerColor = ZiaCardSurface),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ZiaElevatedBorder, RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Save Local Preferences",
                                style = MaterialTheme.typography.titleMedium,
                                color = ZiaTextPrimary
                            )
                            Text(
                                text = "Disable karne par ZIA koi naya preference save nahi karegi.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZiaTextSecondary
                            )
                        }
                        Switch(
                            checked = settings.memorySavingEnabled,
                            onCheckedChange = onToggleMemorySaving,
                            modifier = Modifier.testTag("memory_opt_in_switch")
                        )
                    }
                }
            }

            // Add New Preference Card
            item {
                ControlSectionCard(
                    title = "Add Explicit Preference",
                    subtitle = "PrivacyGuard automatically blocks passwords, OTPs, PINs, API keys, and card numbers."
                ) {
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("Key (e.g. User Name, Preferred IDE)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_key_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        label = { Text("Value (e.g. Aarav, Android Studio / Acode)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_value_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (newKey.isNotBlank() && newValue.isNotBlank()) {
                                onAddMemoryFact(newKey, newValue, "Custom")
                                newKey = ""
                                newValue = ""
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("save_memory_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Save to Local Memory")
                    }
                }
            }

            // Saved Items Header
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "SAVED PREFERENCES (${memoryFacts.size})",
                        style = MaterialTheme.typography.labelLarge,
                        color = ZiaSapphireBlue
                    )
                    OutlinedButton(
                        onClick = onRequestWipeAll,
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = ZiaCrimsonError),
                        modifier = Modifier.testTag("wipe_all_memory_button")
                    ) {
                        Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Wipe All Data")
                    }
                }
            }

            items(memoryFacts, key = { it.id }) { fact ->
                Card(
                    colors = CardDefaults.cardColors(containerColor = ZiaMidnightSurface),
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ZiaElevatedBorder, RoundedCornerShape(16.dp))
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = fact.key,
                                style = MaterialTheme.typography.titleMedium,
                                color = ZiaAcousticCyan
                            )
                            Text(
                                text = fact.value,
                                style = MaterialTheme.typography.bodyLarge,
                                color = ZiaTextPrimary
                            )
                            Text(
                                text = "Category: ${fact.category} · Stored Locally",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZiaTextMuted
                            )
                        }
                        IconButton(
                            onClick = {
                                editingFact = fact
                                editKeyText = fact.key
                                editValueText = fact.value
                            }
                        ) {
                            Icon(Icons.Default.Edit, contentDescription = "Edit preference", tint = ZiaSapphireBlue)
                        }
                        IconButton(onClick = { onDeleteMemoryFact(fact.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete preference", tint = ZiaCrimsonError)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun OfflineModeScreen(
    settings: ZiaSettingsState,
    isNetworkOnline: Boolean,
    hardwareAudit: DeviceHardwareAudit?,
    onToggleForceOffline: (Boolean) -> Unit,
    onToggleWakeWordSession: (Boolean) -> Unit
) {
    val offlineCapabilities = listOf(
        "Deterministic Phone Commands (EN / Hinglish / Banglish / বাংলা)" to "100% Offline",
        "Media, Ring, Alarm & Notification Volume Control" to "100% Offline",
        "Set System Alarms & Local SQLite Reminders" to "100% Offline",
        "Launch Installed Apps & System Settings Panels" to "100% Offline",
        "Calls & SMS Composer with Confirmation Preview" to "100% Offline",
        "Local Coding Templates (Compose, Coroutines, Math)" to "100% Offline",
        "View, Edit & Delete Local Preference Memory" to "100% Offline"
    )

    val onlineCapabilities = listOf(
        "Open-Ended Gemini 3.5 Flash Cloud Reasoning" to "Requires Internet + Consent",
        "Complex Multi-File Code Generation & Debugging" to "Requires Internet + Consent",
        "Cloud-Backed Speech Recognition (if offline pack missing)" to "Depends on Android Speech Pack"
    )

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .widthIn(max = 600.dp)
            .testTag("offline_mode_list"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "Offline Capability & Hardware Audit",
                style = MaterialTheme.typography.headlineMedium,
                color = ZiaTextPrimary
            )
            Text(
                text = "Transparent breakdown of what runs 100% on your phone without internet vs. when cloud AI is used.",
                style = MaterialTheme.typography.bodyMedium,
                color = ZiaTextSecondary
            )
        }

        // Force Offline Mode Toggle
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = ZiaCardSurface),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ZiaAmberWarning.copy(alpha = 0.5f), RoundedCornerShape(18.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.CloudOff, contentDescription = null, tint = ZiaAmberWarning)
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Force 100% Offline Mode",
                                style = MaterialTheme.typography.titleMedium,
                                color = ZiaTextPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "ON karne par ZIA kabhi bhi cloud API call nahi karegi — chahe Wi-Fi/Mobile data on ho.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZiaTextSecondary
                        )
                    }
                    Switch(
                        checked = settings.forceOfflineMode,
                        onCheckedChange = onToggleForceOffline,
                        modifier = Modifier.testTag("force_offline_mode_switch")
                    )
                }
            }
        }

        // Real Hardware & On-Device LLM Evaluation Card
        item {
            ControlSectionCard(
                title = "Redmi 9 / Android 10 Hardware & Local LLM Audit",
                subtitle = "Live runtime inspection via ActivityManager.MemoryInfo & StatFs (No fake claims)"
            ) {
                if (hardwareAudit != null) {
                    Text(
                        text = "• Device: ${hardwareAudit.deviceModel} (${hardwareAudit.androidRelease})\n" +
                            "• RAM: ${String.format("%.2f", hardwareAudit.availableRamGb)} GB Free / ${String.format("%.1f", hardwareAudit.totalRamGb)} GB Total\n" +
                            "• Free Internal Storage: ${String.format("%.2f", hardwareAudit.freeStorageGb)} GB\n" +
                            "• CPU Cores: ${hardwareAudit.cpuCores}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZiaAcousticCyan
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = hardwareAudit.llmRecommendationHinglish,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZiaTextPrimary
                    )
                }
            }
        }

        // Wake Word ("Hey ZIA") Research & Foreground Session Card
        item {
            ControlSectionCard(
                title = "Wake Word (“Hey ZIA”) & Battery Architecture",
                subtitle = "Honest assessment of Android 10 speech recognition & background mic rules"
            ) {
                Text(
                    text = "Technical Reality (Hinglish):\n" +
                        "• Ordinary Android SpeechRecognizer continuous 24/7 offline hotword DSP engine nahi hota.\n" +
                        "• Background mein lagataar mic khula rakhne se Android 10 / MIUI battery tezi se drain hoti hai aur OS service kill kar deta hai.\n" +
                        "• Isliye ZIA Push-to-Talk ko primary safe mode rakhti hai, aur jab aap 'Hey ZIA Foreground Session' ON karte hain, tabhi screen khuli rehne par wake-prefix recognize hota hai.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZiaTextSecondary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Enable 'Hey ZIA' Foreground Session Mode",
                        style = MaterialTheme.typography.titleMedium,
                        color = ZiaTextPrimary,
                        modifier = Modifier.weight(1f)
                    )
                    Switch(
                        checked = settings.wakeWordSessionEnabled,
                        onCheckedChange = onToggleWakeWordSession,
                        modifier = Modifier.testTag("wake_word_session_switch")
                    )
                }
            }
        }

        // Offline Capability Matrix
        item {
            ControlSectionCard(
                title = "Works 100% Without Internet",
                subtitle = "LocalCommandEngine + LocalAssistantEngine + Room SQLite"
            ) {
                offlineCapabilities.forEach { (feature, badge) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = ZiaEmeraldOnline,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZiaTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = ZiaEmeraldOnline
                        )
                    }
                }
            }
        }

        // Online Capability Matrix
        item {
            ControlSectionCard(
                title = "Requires Online Connectivity & User Consent",
                subtitle = "Automatically falls back to Local Assistant when offline"
            ) {
                onlineCapabilities.forEach { (feature, badge) ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Memory,
                            contentDescription = null,
                            tint = ZiaNeuralViolet,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = feature,
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZiaTextPrimary,
                            modifier = Modifier.weight(1f)
                        )
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = ZiaNeuralViolet
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SettingsScreen(
    settings: ZiaSettingsState,
    notificationAccessGranted: Boolean,
    onSelectLanguage: (AssistantLanguage) -> Unit,
    onSelectPersonality: (PersonalityStyle) -> Unit,
    onToggleVoiceOutput: (Boolean) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onSpeechPitchChange: (Float) -> Unit,
    onTestVoiceClick: () -> Unit,
    onToggleCloudConsent: (Boolean) -> Unit,
    onToggleNotificationReader: (Boolean) -> Unit,
    onOpenNotificationAccessSettings: () -> Unit,
    onOpenAppPermissionSettings: () -> Unit,
    onSaveCustomApiKey: (String) -> Unit
) {
    var apiKeyDraft by rememberSaveable(settings.customGeminiApiKey) {
        mutableStateOf(settings.customGeminiApiKey)
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp)
            .widthIn(max = 600.dp)
            .testTag("settings_screen_list"),
        contentPadding = PaddingValues(vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        item {
            Text(
                text = "ZIA AI Settings, Privacy & Build Guide",
                style = MaterialTheme.typography.headlineMedium,
                color = ZiaTextPrimary
            )
            Text(
                text = "Configure multilingual speech, personality, cloud AI consent, and permissions.",
                style = MaterialTheme.typography.bodyMedium,
                color = ZiaTextSecondary
            )
        }

        // 1. Language Selection
        item {
            ControlSectionCard(
                title = "1. Multilingual Speech & Text Language",
                subtitle = "Recognition accuracy depends on Android Speech Services, downloaded language packs, accent, and network."
            ) {
                AssistantLanguage.entries.forEach { lang ->
                    val isSelected = settings.language == lang
                    Surface(
                        color = if (isSelected) ZiaSapphireBlue.copy(alpha = 0.2f) else ZiaMidnightSurface,
                        shape = RoundedCornerShape(12.dp),
                        onClick = { onSelectLanguage(lang) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) ZiaSapphireBlue else ZiaElevatedBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = lang.displayName,
                                style = MaterialTheme.typography.titleMedium,
                                color = if (isSelected) ZiaAcousticCyan else ZiaTextPrimary
                            )
                            Text(
                                text = "Example: \"${lang.samplePrompt}\"",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZiaTextSecondary
                            )
                        }
                    }
                }
            }
        }

        // 2. Personality Style
        item {
            ControlSectionCard(
                title = "2. ZIA Personality Style",
                subtitle = "Changes how ZIA structures both offline and online responses."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PersonalityStyle.entries.forEach { style ->
                        FilterChip(
                            selected = settings.personality == style,
                            onClick = { onSelectPersonality(style) },
                            label = { Text("${style.title} (${style.badge})") },
                            modifier = Modifier.testTag("personality_chip_${style.name.lowercase()}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = settings.personality.descriptionHinglish,
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZiaAcousticCyan
                )
            }
        }

        // 3. Voice & TextToSpeech Tuning
        item {
            ControlSectionCard(
                title = "3. Voice Replies & Android TextToSpeech",
                subtitle = "Customize ZIA's speaking speed and voice pitch."
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Speak Replies Aloud (TTS)",
                        style = MaterialTheme.typography.titleMedium,
                        color = ZiaTextPrimary
                    )
                    Switch(
                        checked = settings.voiceOutputEnabled,
                        onCheckedChange = onToggleVoiceOutput,
                        modifier = Modifier.testTag("tts_enabled_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Speech Rate: ${String.format("%.2f", settings.speechRate)}x",
                    style = MaterialTheme.typography.labelLarge,
                    color = ZiaTextSecondary
                )
                Slider(
                    value = settings.speechRate,
                    onValueChange = onSpeechRateChange,
                    valueRange = 0.6f..1.6f
                )

                Text(
                    text = "Voice Pitch: ${String.format("%.2f", settings.speechPitch)}x",
                    style = MaterialTheme.typography.labelLarge,
                    color = ZiaTextSecondary
                )
                Slider(
                    value = settings.speechPitch,
                    onValueChange = onSpeechPitchChange,
                    valueRange = 0.7f..1.4f
                )

                Button(
                    onClick = onTestVoiceClick,
                    modifier = Modifier.testTag("test_tts_voice_button")
                ) {
                    Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Test ZIA Voice Now")
                }
            }
        }

        // 4. Hybrid Cloud AI Provider & Security Notice
        item {
            ControlSectionCard(
                title = "4. Cloud AI Provider & Security Notice",
                subtitle = "Built-in Gemini 3.5 Flash + Optional Custom API Key Override"
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Allow Cloud AI Queries (User Consent)",
                            style = MaterialTheme.typography.titleMedium,
                            color = ZiaTextPrimary
                        )
                        Text(
                            text = "OFF karne par koi bhi text cloud provider ko nahi bheja jayega.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = ZiaTextSecondary
                        )
                    }
                    Switch(
                        checked = settings.cloudAiConsentEnabled,
                        onCheckedChange = onToggleCloudConsent,
                        modifier = Modifier.testTag("cloud_consent_switch")
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Security Warning: Android APKs can be decompiled. Any user-supplied API key stored on-device or via BuildConfig in a prototype APK is extractable and should not be shared publicly. For production deployments, always route requests through a trusted backend proxy.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZiaAmberWarning
                )

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKeyDraft,
                    onValueChange = { apiKeyDraft = it },
                    label = { Text("Optional Custom Gemini API Key Override (Leave blank for built-in)") },
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_api_key_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = { onSaveCustomApiKey(apiKeyDraft) },
                    modifier = Modifier.testTag("save_custom_api_key_button")
                ) {
                    Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save API Key Preference")
                }
            }
        }

        // 5. Optional Notification Reader & Permissions Audit
        item {
            ControlSectionCard(
                title = "5. Optional Notification Reader & Permissions",
                subtitle = "Notifications are never uploaded. OTPs are automatically redacted."
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Enable Local Notification Reader",
                            style = MaterialTheme.typography.titleMedium,
                            color = ZiaTextPrimary
                        )
                        Text(
                            text = if (notificationAccessGranted) {
                                "System Notification Listener Permission: GRANTED"
                            } else {
                                "System Notification Listener Permission: NOT GRANTED"
                            },
                            style = MaterialTheme.typography.labelSmall,
                            color = if (notificationAccessGranted) ZiaEmeraldOnline else ZiaAmberWarning
                        )
                    }
                    Switch(
                        checked = settings.notificationReaderEnabled,
                        onCheckedChange = onToggleNotificationReader,
                        modifier = Modifier.testTag("notification_reader_switch")
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedButton(
                        onClick = onOpenNotificationAccessSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Notification Access")
                    }
                    OutlinedButton(
                        onClick = onOpenAppPermissionSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("App Permissions")
                    }
                }
            }
        }

        // 6. Step-by-Step Phone-Only Build, Installation & Release Signing Guide (Hinglish)
        item {
            ControlSectionCard(
                title = "6. Phone-Only APK Installation & Release Signing Guide",
                subtitle = "Beginner-friendly Hinglish documentation for Redmi 9 (Android 10)"
            ) {
                Text(
                    text = "• Mobile Environment Assessment:\n" +
                        "  Acode phone par code edit karne ke liye accha hai, par usme native Kotlin + Jetpack Compose compile karne wala Android SDK/Gradle toolchain nahi hota. Isliye aapka yeh ZIA AI project Cloud Android Gradle Build System par compile ho raha hai.\n\n" +
                        "• Phone Par APK Install Kaise Karein:\n" +
                        "  1. AI Studio ke top-right Settings / Export menu se 'Download APK' chunein.\n" +
                        "  2. Redmi 9 (Android 10) ke Files app mein downloaded APK par tap karein aur 'Install from this source' allow karein.\n" +
                        "  3. App open karke Push-to-Talk ya Offline Commands test karein.\n\n" +
                        "• Release Signing Guidance (Safe Practice):\n" +
                        "  Play Store ya public release ke liye apna private `.jks` keystore kabhi bhi code repository ya chat mein share na karein. Hamesha environment variables (`KEYSTORE_PATH`, `STORE_PASSWORD`, `KEY_PASSWORD`) ya Android Studio / CI signing secrets use karein.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZiaTextPrimary
                )
            }
        }
    }
}
