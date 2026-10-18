package com.example.ui.screens

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.IosShare
import androidx.compose.material.icons.filled.Lock
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.example.R
import com.example.data.AssistantLanguage
import com.example.data.AvatarThemePreset
import com.example.data.MemoryFactEntity
import com.example.data.PendingAction
import com.example.data.PerformanceTelemetry
import com.example.data.PersonalityStyle
import com.example.data.ResponseLengthPreference
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
import com.example.ui.theme.ZiaSapphireBlue
import com.example.ui.theme.ZiaTextMuted
import com.example.ui.theme.ZiaTextPrimary
import com.example.ui.theme.ZiaTextSecondary
import java.util.Locale

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
    onCancelAction: () -> Unit,
    onExportMemoryText: () -> String,
    onShowBanner: (String) -> Unit
) {
    val context = LocalContext.current
    var searchFilter by rememberSaveable { mutableStateOf("") }
    var newKey by rememberSaveable { mutableStateOf("") }
    var newValue by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf("Preference") }
    var editingFact by remember { mutableStateOf<MemoryFactEntity?>(null) }
    var editKeyText by rememberSaveable { mutableStateOf("") }
    var editValueText by rememberSaveable { mutableStateOf("") }

    val categories = listOf("Preference", "Favourite Command", "Project Note")

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

    val filteredFacts = remember(memoryFacts, searchFilter) {
        val q = searchFilter.trim().lowercase(Locale.ROOT)
        if (q.isEmpty()) memoryFacts
        else memoryFacts.filter {
            it.key.lowercase(Locale.ROOT).contains(q) ||
                it.value.lowercase(Locale.ROOT).contains(q) ||
                it.category.lowercase(Locale.ROOT).contains(q)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
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
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Text(
                    text = "Smart Personal Memory",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ZiaTextPrimary
                )
                Text(
                    text = stringResource(id = R.string.privacy_notice_summary),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZiaTextSecondary
                )
            }

            // Master Memory Switch
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
                                text = "Master Memory Switch",
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

            // Add New Preference / Favourite Command / Project Note
            item {
                ControlSectionCard(
                    title = "Save Preference, Command or Project Note",
                    subtitle = "PrivacyGuard automatically blocks passwords, OTPs, PINs, API secrets, and card numbers."
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        categories.forEach { cat ->
                            FilterChip(
                                selected = selectedCategory == cat,
                                onClick = { selectedCategory = cat },
                                label = { Text(cat) }
                            )
                        }
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newKey,
                        onValueChange = { newKey = it },
                        label = { Text("Title / Key (e.g. User Name, Morning Command)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_key_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = newValue,
                        onValueChange = { newValue = it },
                        label = { Text("Value / Details") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("memory_value_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Button(
                        onClick = {
                            if (newKey.isNotBlank() && newValue.isNotBlank()) {
                                onAddMemoryFact(newKey, newValue, selectedCategory)
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

            // Search & Export Bar
            item {
                OutlinedTextField(
                    value = searchFilter,
                    onValueChange = { searchFilter = it },
                    label = { Text("Search saved memory & commands...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("memory_search_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedButton(
                        onClick = {
                            val exported = onExportMemoryText()
                            val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                            cb?.setPrimaryClip(ClipData.newPlainText("ZIA Memory Export", exported))
                            onShowBanner("Memory & Vault export clipboard mein copy ho gaya!")
                            try {
                                val sendIntent = Intent(Intent.ACTION_SEND).apply {
                                    type = "text/plain"
                                    putExtra(Intent.EXTRA_TEXT, exported)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }
                                context.startActivity(Intent.createChooser(sendIntent, "Export ZIA Memory").apply {
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                })
                            } catch (_: Exception) {
                            }
                        },
                        modifier = Modifier.testTag("export_memory_button")
                    ) {
                        Icon(Icons.Default.IosShare, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Export Memory")
                    }

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

            items(filteredFacts, key = { it.id }) { fact ->
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
fun SettingsScreen(
    settings: ZiaSettingsState,
    notificationAccessGranted: Boolean,
    hardwareAudit: DeviceHardwareAudit?,
    performanceTelemetry: PerformanceTelemetry,
    pendingAction: PendingAction?,
    onSelectLanguage: (AssistantLanguage) -> Unit,
    onSelectPersonality: (PersonalityStyle) -> Unit,
    onSelectAvatarTheme: (AvatarThemePreset) -> Unit,
    onSelectResponseLength: (ResponseLengthPreference) -> Unit,
    onSaveCustomGreeting: (String) -> Unit,
    onToggleVoiceInput: (Boolean) -> Unit,
    onToggleVoiceOutput: (Boolean) -> Unit,
    onSpeechRateChange: (Float) -> Unit,
    onSpeechPitchChange: (Float) -> Unit,
    onTestVoiceClick: () -> Unit,
    onToggleMemorySaving: (Boolean) -> Unit,
    onToggleCloudConsent: (Boolean) -> Unit,
    onToggleCloudPersonalContext: (Boolean) -> Unit,
    onToggleCloudDocUpload: (Boolean) -> Unit,
    onToggleForceOffline: (Boolean) -> Unit,
    onToggleWakeWordSession: (Boolean) -> Unit,
    onToggleNotificationReader: (Boolean) -> Unit,
    onOpenNotificationAccessSettings: () -> Unit,
    onOpenAppPermissionSettings: () -> Unit,
    onRequestWipeAllData: () -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit,
    onSaveCustomApiKey: (String) -> Unit
) {
    val context = LocalContext.current
    val micPermissionGranted = ContextCompat.checkSelfPermission(
        context,
        Manifest.permission.RECORD_AUDIO
    ) == PackageManager.PERMISSION_GRANTED

    var apiKeyDraft by rememberSaveable(settings.customGeminiApiKey) {
        mutableStateOf(settings.customGeminiApiKey)
    }
    var greetingDraft by rememberSaveable(settings.customGreeting) {
        mutableStateOf(settings.customGreeting)
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

        item {
            Text(
                text = "Privacy Control Center & Settings",
                style = MaterialTheme.typography.headlineMedium,
                color = ZiaTextPrimary
            )
            Text(
                text = "Dedicated privacy dashboard, adaptive personality & avatar customizer, and Redmi 9 hardware monitor.",
                style = MaterialTheme.typography.bodyMedium,
                color = ZiaTextSecondary
            )
        }

        // 1. Dedicated Privacy Control Center
        item {
            ControlSectionCard(
                title = "1. Privacy Control Center",
                subtitle = "Live Android OS permission status + granular data & cloud consent switches"
            ) {
                // OS Permission Badges
                Surface(
                    color = ZiaMidnightSurface,
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ZiaElevatedBorder, RoundedCornerShape(14.dp))
                ) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Text(
                            text = "LIVE ANDROID OS PERMISSION STATUS",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZiaAcousticCyan
                        )
                        Text(
                            text = "• Microphone (RECORD_AUDIO): ${if (micPermissionGranted) "GRANTED" else "NOT GRANTED"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (micPermissionGranted) ZiaEmeraldOnline else ZiaAmberWarning
                        )
                        Text(
                            text = "• Notification Access (Listener): ${if (notificationAccessGranted) "GRANTED" else "NOT GRANTED"}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = if (notificationAccessGranted) ZiaEmeraldOnline else ZiaAmberWarning
                        )
                        Text(
                            text = "Important Honesty Note: Niche ke switches ZIA ke اندر feature ko turant disable karte hain, lekin Android OS level par permission revoke karne ke liye 'Open Android App Permissions' button se system settings mein revoke karein.",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZiaTextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                PrivacySwitchRow(
                    title = "Enable Cloud AI Reasoning",
                    subtitle = "Allow online queries to Gemini when connected",
                    checked = settings.cloudAiConsentEnabled,
                    onCheckedChange = onToggleCloudConsent,
                    tag = "cloud_consent_switch"
                )
                PrivacySwitchRow(
                    title = "Share Saved Preferences with Cloud AI",
                    subtitle = "Ask permission before sending personal memory context to cloud",
                    checked = settings.cloudPersonalContextConsent,
                    onCheckedChange = onToggleCloudPersonalContext,
                    tag = "cloud_personal_context_switch"
                )
                PrivacySwitchRow(
                    title = "Allow Document & Screenshot Cloud Upload",
                    subtitle = "Required before analyzing SAF documents or gaming screenshots online",
                    checked = settings.cloudDocumentUploadConsent,
                    onCheckedChange = onToggleCloudDocUpload,
                    tag = "cloud_doc_upload_switch"
                )
                PrivacySwitchRow(
                    title = "Save Local Personal Memory",
                    subtitle = "Store approved preferences & notes in on-device SQLite",
                    checked = settings.memorySavingEnabled,
                    onCheckedChange = onToggleMemorySaving,
                    tag = "privacy_memory_switch"
                )
                PrivacySwitchRow(
                    title = "Allow Push-to-Talk Voice Input",
                    subtitle = "Enable microphone speech recognition when pressing Mic button",
                    checked = settings.voiceInputEnabled,
                    onCheckedChange = onToggleVoiceInput,
                    tag = "voice_input_switch"
                )
                PrivacySwitchRow(
                    title = "Enable Voice Replies (TextToSpeech)",
                    subtitle = "Speak ZIA's answers aloud automatically",
                    checked = settings.voiceOutputEnabled,
                    onCheckedChange = onToggleVoiceOutput,
                    tag = "tts_enabled_switch"
                )
                PrivacySwitchRow(
                    title = "Force 100% Offline Mode",
                    subtitle = "Block all cloud network calls even when Wi-Fi/Data is on",
                    checked = settings.forceOfflineMode,
                    onCheckedChange = onToggleForceOffline,
                    tag = "force_offline_mode_switch"
                )
                PrivacySwitchRow(
                    title = "Enable Optional Notification Reader",
                    subtitle = "Cache & read incoming notifications locally (Never uploaded)",
                    checked = settings.notificationReaderEnabled,
                    onCheckedChange = onToggleNotificationReader,
                    tag = "notification_reader_switch"
                )

                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedButton(
                        onClick = onOpenAppPermissionSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Default.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("OS Permissions")
                    }
                    OutlinedButton(
                        onClick = onOpenNotificationAccessSettings,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Notification Access")
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedButton(
                    onClick = onRequestWipeAllData,
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = ZiaCrimsonError),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.DeleteForever, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Wipe All Saved Memory, Vault, Planner & Chat Data")
                }
            }
        }

        // 2. Adaptive AI Personality & Avatar Appearance Customizer
        item {
            ControlSectionCard(
                title = "2. Adaptive AI Personality & Avatar Customizer",
                subtitle = "Customize ZIA's persona, avatar visor theme, response length & custom greeting. (ZIA never simulates human emotions as factual internal states)."
            ) {
                Text(
                    text = "Select Personality Mode:",
                    style = MaterialTheme.typography.labelLarge,
                    color = ZiaAcousticCyan
                )
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

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Select Anime Avatar Appearance Theme:",
                    style = MaterialTheme.typography.labelLarge,
                    color = ZiaSapphireBlue
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AvatarThemePreset.entries.forEach { theme ->
                        FilterChip(
                            selected = settings.avatarTheme == theme,
                            onClick = { onSelectAvatarTheme(theme) },
                            label = { Text(theme.title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Response Length Preference:",
                    style = MaterialTheme.typography.labelLarge,
                    color = ZiaTextSecondary
                )
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    ResponseLengthPreference.entries.forEach { len ->
                        FilterChip(
                            selected = settings.responseLength == len,
                            onClick = { onSelectResponseLength(len) },
                            label = { Text(len.title) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = greetingDraft,
                    onValueChange = { greetingDraft = it },
                    label = { Text("Custom Greeting Message") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(6.dp))
                OutlinedButton(
                    onClick = { onSaveCustomGreeting(greetingDraft) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Custom Greeting")
                }
            }
        }

        // 3. Multilingual Speech & Voice Tuning
        item {
            ControlSectionCard(
                title = "3. Multilingual Voice & Language Settings",
                subtitle = "Support for English, Hinglish, Banglish, and বাংলা. Accuracy depends on Android Speech Services & downloaded packs."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AssistantLanguage.entries.forEach { lang ->
                        FilterChip(
                            selected = settings.language == lang,
                            onClick = { onSelectLanguage(lang) },
                            label = { Text(lang.displayName) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Speech Rate: ${String.format(Locale.US, "%.2f", settings.speechRate)}x",
                    style = MaterialTheme.typography.labelLarge,
                    color = ZiaTextSecondary
                )
                Slider(
                    value = settings.speechRate,
                    onValueChange = onSpeechRateChange,
                    valueRange = 0.6f..1.6f
                )

                Text(
                    text = "Voice Pitch: ${String.format(Locale.US, "%.2f", settings.speechPitch)}x",
                    style = MaterialTheme.typography.labelLarge,
                    color = ZiaTextSecondary
                )
                Slider(
                    value = settings.speechPitch,
                    onValueChange = onSpeechPitchChange,
                    valueRange = 0.7f..1.4f
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = onTestVoiceClick,
                        modifier = Modifier
                            .weight(1f)
                            .testTag("test_tts_voice_button")
                    ) {
                        Icon(Icons.Default.RecordVoiceOver, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Test Voice")
                    }
                    OutlinedButton(
                        onClick = { onToggleWakeWordSession(!settings.wakeWordSessionEnabled) },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(if (settings.wakeWordSessionEnabled) "Hey ZIA: ON" else "Hey ZIA: OFF")
                    }
                }
            }
        }

        // 4. Performance, Resource Management & Local LLM Evaluation (Redmi 9 · 6 GB RAM)
        item {
            ControlSectionCard(
                title = "4. Performance, Latency & Redmi 9 Hardware Audit",
                subtitle = "Live JVM RAM usage, response latency, and on-device LLM compatibility"
            ) {
                Text(
                    text = "• App JVM Memory Usage: ${performanceTelemetry.appUsedRamMb} MB / ${performanceTelemetry.appMaxRamMb} MB\n" +
                        "• Last Response Latency: ${performanceTelemetry.lastResponseLatencyMs} ms (${performanceTelemetry.lastEngineUsed})",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZiaEmeraldOnline
                )
                if (hardwareAudit != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "• Device: ${hardwareAudit.deviceModel} (${hardwareAudit.androidRelease})\n" +
                            "• System RAM: ${String.format(Locale.US, "%.2f", hardwareAudit.availableRamGb)} GB Free / ${String.format(Locale.US, "%.1f", hardwareAudit.totalRamGb)} GB Total\n" +
                            "• Free Storage: ${String.format(Locale.US, "%.2f", hardwareAudit.freeStorageGb)} GB · CPU Cores: ${hardwareAudit.cpuCores}\n\n" +
                            hardwareAudit.llmRecommendationHinglish,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZiaTextPrimary
                    )
                }
            }
        }

        // 5. Configurable Cloud AI API Key & Security Guidance
        item {
            ControlSectionCard(
                title = "5. Cloud AI Provider & Key Security",
                subtitle = "Provider-independent interface (Gemini 3.5 Flash default)"
            ) {
                Text(
                    text = "Security Notice: An API key entered and stored on-device or in a prototype APK cannot be considered completely secret if the APK is decompiled. For production, always use a trusted backend proxy.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZiaAmberWarning
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = apiKeyDraft,
                    onValueChange = { apiKeyDraft = it },
                    label = { Text("Optional Custom API Key Override (Leave blank for built-in)") },
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
    }
}

@Composable
private fun PrivacySwitchRow(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    tag: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = ZiaTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.labelSmall,
                color = ZiaTextSecondary
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            modifier = Modifier.testTag(tag)
        )
    }
}
