package com.example.ui.screens

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Bluetooth
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.InstalledAppItem
import com.example.data.NotificationSummary
import com.example.data.PendingAction
import com.example.data.ReminderEntity
import com.example.data.SelectedFileInfo
import com.example.ui.components.ActionConfirmationModalCard
import com.example.ui.theme.JetBrainsMonoFontFamily
import com.example.ui.theme.ZiaAcousticCyan
import com.example.ui.theme.ZiaCardSurface
import com.example.ui.theme.ZiaElevatedBorder
import com.example.ui.theme.ZiaEmeraldOnline
import com.example.ui.theme.ZiaNeuralViolet
import com.example.ui.theme.ZiaSapphireBlue
import com.example.ui.theme.ZiaTextMuted
import com.example.ui.theme.ZiaTextPrimary
import com.example.ui.theme.ZiaTextSecondary
import java.util.Locale

@Composable
fun QuickActionsScreen(
    installedApps: List<InstalledAppItem>,
    volumeMap: Map<String, Int>,
    reminders: List<ReminderEntity>,
    selectedSafFile: SelectedFileInfo?,
    recentNotifications: List<NotificationSummary>,
    pendingAction: PendingAction?,
    onRequestAction: (PendingAction) -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit,
    onVolumeChange: (String, Int) -> Unit,
    onAddReminder: (String, String) -> Unit,
    onToggleReminder: (Int, Boolean) -> Unit,
    onDeleteReminder: (Int) -> Unit,
    onSafFilePicked: (Uri?) -> Unit,
    onClearSafFile: () -> Unit,
    onReadLatestNotification: () -> Unit,
    onSendCodingPromptToChat: (String) -> Unit
) {
    val safLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        onSafFilePicked(uri)
    }

    var appSearchQuery by rememberSaveable { mutableStateOf("") }
    var dialNumberInput by rememberSaveable { mutableStateOf("") }
    var smsMessageInput by rememberSaveable { mutableStateOf("") }
    var alarmHourInput by rememberSaveable { mutableStateOf("07") }
    var alarmMinuteInput by rememberSaveable { mutableStateOf("30") }
    var reminderTitleInput by rememberSaveable { mutableStateOf("") }
    var reminderTimeInput by rememberSaveable { mutableStateOf("Today · 8:00 PM") }

    var mediaVolSlider by remember(volumeMap["MEDIA"]) {
        mutableFloatStateOf((volumeMap["MEDIA"] ?: 60).toFloat())
    }
    var alarmVolSlider by remember(volumeMap["ALARM"]) {
        mutableFloatStateOf((volumeMap["ALARM"] ?: 80).toFloat())
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
                .testTag("quick_actions_list"),
            contentPadding = PaddingValues(vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "Approved Phone Controls & Shortcuts",
                    style = MaterialTheme.typography.headlineMedium,
                    color = ZiaTextPrimary
                )
                Text(
                    text = "Sabhi actions least-privilege Android APIs aur explicit user confirmation ke saath kaam karte hain.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = ZiaTextSecondary
                )
            }

            // 1. Volume & System Panels Card
            item {
                ControlSectionCard(
                    title = "Settings Panels & Volume Streams",
                    subtitle = "AudioManager volume sliders + Android 10+ System Settings Panels"
                ) {
                    Text(
                        text = "Media Volume: ${mediaVolSlider.toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = ZiaAcousticCyan
                    )
                    Slider(
                        value = mediaVolSlider,
                        onValueChange = { mediaVolSlider = it },
                        onValueChangeFinished = { onVolumeChange("MEDIA", mediaVolSlider.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.testTag("media_volume_slider")
                    )

                    Text(
                        text = "Alarm Volume: ${alarmVolSlider.toInt()}%",
                        style = MaterialTheme.typography.labelLarge,
                        color = ZiaSapphireBlue
                    )
                    Slider(
                        value = alarmVolSlider,
                        onValueChange = { alarmVolSlider = it },
                        onValueChangeFinished = { onVolumeChange("ALARM", alarmVolSlider.toInt()) },
                        valueRange = 0f..100f,
                        modifier = Modifier.testTag("alarm_volume_slider")
                    )

                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = { onRequestAction(PendingAction.OpenSystemSettings("WIFI")) },
                            modifier = Modifier.testTag("open_wifi_panel_button")
                        ) {
                            Icon(Icons.Default.Wifi, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Wi-Fi Panel")
                        }
                        OutlinedButton(
                            onClick = { onRequestAction(PendingAction.OpenSystemSettings("BLUETOOTH")) },
                            modifier = Modifier.testTag("open_bluetooth_panel_button")
                        ) {
                            Icon(Icons.Default.Bluetooth, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Bluetooth")
                        }
                        OutlinedButton(
                            onClick = { onRequestAction(PendingAction.OpenSystemSettings("SOUND")) }
                        ) {
                            Icon(Icons.AutoMirrored.Filled.VolumeUp, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Sound")
                        }
                        OutlinedButton(
                            onClick = { onRequestAction(PendingAction.OpenSystemSettings("DISPLAY")) }
                        ) {
                            Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Display")
                        }
                    }
                }
            }

            // 2. Calls & SMS Composer (Confirmation-Gated)
            item {
                ControlSectionCard(
                    title = "Calls & SMS Composer (Confirmation Gate)",
                    subtitle = "Dialer aur SMS app prefilled details ke saath khulti hai; bina aapke confirmation ke send nahi hota."
                ) {
                    OutlinedTextField(
                        value = dialNumberInput,
                        onValueChange = { dialNumberInput = it },
                        label = { Text("Phone Number (e.g. +919876543210)") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("phone_number_input")
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = smsMessageInput,
                        onValueChange = { smsMessageInput = it },
                        label = { Text("Optional SMS Message Body") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("sms_body_input")
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = {
                                onRequestAction(
                                    PendingAction.DialPhone(
                                        phoneNumber = dialNumberInput.ifBlank { "112" }
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_call_button")
                        ) {
                            Icon(Icons.Default.Call, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Preview Call")
                        }
                        OutlinedButton(
                            onClick = {
                                onRequestAction(
                                    PendingAction.ComposeSms(
                                        phoneNumber = dialNumberInput.ifBlank { "9876543210" },
                                        messageBody = smsMessageInput.ifBlank { "Hello from ZIA AI!" }
                                    )
                                )
                            },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("preview_sms_button")
                        ) {
                            Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Preview SMS")
                        }
                    }
                }
            }

            // 3. Alarms & Local Reminders
            item {
                ControlSectionCard(
                    title = "Alarms & In-App Reminders",
                    subtitle = "System Clock alarm set karein ya local SQLite reminder save karein."
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = alarmHourInput,
                            onValueChange = { alarmHourInput = it.take(2) },
                            label = { Text("Hour (0-23)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        OutlinedTextField(
                            value = alarmMinuteInput,
                            onValueChange = { alarmMinuteInput = it.take(2) },
                            label = { Text("Min (0-59)") },
                            singleLine = true,
                            modifier = Modifier.weight(1f)
                        )
                        Button(
                            onClick = {
                                val h = alarmHourInput.toIntOrNull()?.coerceIn(0, 23) ?: 7
                                val m = alarmMinuteInput.toIntOrNull()?.coerceIn(0, 59) ?: 30
                                onRequestAction(
                                    PendingAction.SetAlarm(
                                        hour24 = h,
                                        minute = m,
                                        label = "ZIA AI Alarm"
                                    )
                                )
                            },
                            modifier = Modifier.testTag("set_alarm_preview_button")
                        ) {
                            Icon(Icons.Default.AccessAlarm, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Set Alarm")
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedTextField(
                            value = reminderTitleInput,
                            onValueChange = { reminderTitleInput = it },
                            label = { Text("New Local Reminder") },
                            singleLine = true,
                            modifier = Modifier
                                .weight(1f)
                                .testTag("reminder_title_input")
                        )
                        Button(
                            onClick = {
                                if (reminderTitleInput.isNotBlank()) {
                                    onAddReminder(reminderTitleInput, reminderTimeInput)
                                    reminderTitleInput = ""
                                }
                            },
                            modifier = Modifier.testTag("add_reminder_button")
                        ) {
                            Text("Add")
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    reminders.forEach { rem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            IconButton(onClick = { onToggleReminder(rem.id, !rem.isCompleted) }) {
                                Icon(
                                    imageVector = if (rem.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = "Toggle reminder",
                                    tint = if (rem.isCompleted) ZiaEmeraldOnline else ZiaTextSecondary
                                )
                            }
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = rem.title,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = if (rem.isCompleted) ZiaTextMuted else ZiaTextPrimary
                                )
                                Text(
                                    text = rem.scheduledTimeText,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZiaTextSecondary
                                )
                            }
                            IconButton(onClick = { onDeleteReminder(rem.id) }) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete reminder",
                                    tint = ZiaTextMuted
                                )
                            }
                        }
                    }
                }
            }

            // 4. Music Playback & Notification Reader Controls
            item {
                ControlSectionCard(
                    title = "Music Session & Notification Reader",
                    subtitle = "Standard Android media key controls + opt-in local notification voice reader."
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly
                    ) {
                        OutlinedButton(onClick = { onRequestAction(PendingAction.MediaTransport("PREVIOUS")) }) {
                            Icon(Icons.Default.SkipPrevious, contentDescription = "Previous track")
                        }
                        Button(
                            onClick = { onRequestAction(PendingAction.MediaTransport("PLAY_PAUSE")) },
                            modifier = Modifier.testTag("media_play_pause_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = "Play or Pause")
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Play / Pause")
                        }
                        OutlinedButton(onClick = { onRequestAction(PendingAction.MediaTransport("NEXT")) }) {
                            Icon(Icons.Default.SkipNext, contentDescription = "Next track")
                        }
                        OutlinedButton(onClick = { onRequestAction(PendingAction.MediaTransport("STOP")) }) {
                            Icon(Icons.Default.Stop, contentDescription = "Stop playback")
                        }
                    }

                    Spacer(modifier = Modifier.height(10.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Local Notification Buffer (${recentNotifications.size})",
                                style = MaterialTheme.typography.labelLarge,
                                color = ZiaNeuralViolet
                            )
                            Text(
                                text = recentNotifications.firstOrNull()?.let { "${it.appTitle}: ${it.contentPreview}" }
                                    ?: "No recent notifications cached locally.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZiaTextSecondary
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = onReadLatestNotification,
                            modifier = Modifier.testTag("read_notification_aloud_button")
                        ) {
                            Icon(Icons.Default.NotificationsActive, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Read Aloud")
                        }
                    }
                }
            }

            // 5. Camera & Storage Access Framework (SAF) File Inspector
            item {
                ControlSectionCard(
                    title = "Camera & Safe File Inspector (SAF)",
                    subtitle = "Zero private file scanning — ZIA sirf wahi file padhti hai jo aap SAF picker se select karte hain."
                ) {
                    Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                        Button(
                            onClick = { safLauncher.launch(arrayOf("*/*")) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("pick_saf_file_button")
                        ) {
                            Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Pick File / Code")
                        }
                        OutlinedButton(
                            onClick = { onRequestAction(PendingAction.LaunchCamera()) },
                            modifier = Modifier
                                .weight(1f)
                                .testTag("launch_camera_button")
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Open Camera")
                        }
                    }

                    if (selectedSafFile != null) {
                        Spacer(modifier = Modifier.height(10.dp))
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(Color(0xFF0A0D18))
                                .border(1.dp, ZiaAcousticCyan.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "${selectedSafFile.displayName} (${selectedSafFile.sizeBytes} bytes)",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = ZiaAcousticCyan
                                )
                                OutlinedButton(onClick = onClearSafFile) {
                                    Text("Close")
                                }
                            }
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = selectedSafFile.previewContent,
                                fontFamily = JetBrainsMonoFontFamily,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZiaTextPrimary
                            )
                        }
                    }
                }
            }

            // 6. Installed Apps Launcher & Quick App Shortcuts
            item {
                ControlSectionCard(
                    title = "Open Apps (${installedApps.size} Installed)",
                    subtitle = "PackageManager intents ke zariye installed apps launch karein."
                ) {
                    val quickApps = listOf("YouTube", "WhatsApp", "Chrome", "Calculator", "Clock", "Settings", "Acode")
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        quickApps.forEach { appName ->
                            AssistChip(
                                onClick = {
                                    onRequestAction(PendingAction.LaunchApp(appNameQuery = appName))
                                },
                                label = { Text("Open $appName") },
                                leadingIcon = {
                                    Icon(Icons.Default.Apps, contentDescription = null, modifier = Modifier.size(16.dp))
                                }
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = appSearchQuery,
                        onValueChange = { appSearchQuery = it },
                        label = { Text("Search installed apps on this device...") },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("search_installed_apps_input")
                    )

                    val filteredApps = remember(installedApps, appSearchQuery) {
                        val q = appSearchQuery.trim().lowercase(Locale.ROOT)
                        if (q.isEmpty()) installedApps.take(6)
                        else installedApps.filter {
                            it.label.lowercase(Locale.ROOT).contains(q) ||
                                it.packageName.lowercase(Locale.ROOT).contains(q)
                        }.take(8)
                    }

                    Spacer(modifier = Modifier.height(8.dp))
                    filteredApps.forEach { app ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = app.label,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.SemiBold,
                                    color = ZiaTextPrimary
                                )
                                Text(
                                    text = app.packageName,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZiaTextMuted
                                )
                            }
                            OutlinedButton(
                                onClick = {
                                    onRequestAction(
                                        PendingAction.LaunchApp(
                                            appNameQuery = app.label,
                                            resolvedPackageName = app.packageName,
                                            resolvedLabel = app.label
                                        )
                                    )
                                }
                            ) {
                                Text("Launch")
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun ControlSectionCard(
    title: String,
    subtitle: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, ZiaElevatedBorder, RoundedCornerShape(20.dp)),
        colors = CardDefaults.cardColors(containerColor = ZiaCardSurface),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                color = ZiaTextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = ZiaTextSecondary
            )
            Spacer(modifier = Modifier.height(12.dp))
            content()
        }
    }
}
