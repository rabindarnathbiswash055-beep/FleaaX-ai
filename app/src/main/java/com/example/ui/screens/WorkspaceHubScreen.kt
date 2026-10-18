package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessAlarm
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material.icons.filled.SkipPrevious
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.GamingProfileEntity
import com.example.data.KnowledgeNoteEntity
import com.example.data.MemoryFactEntity
import com.example.data.NoteCategory
import com.example.data.PendingAction
import com.example.data.PlannerTaskEntity
import com.example.data.SelectedFileInfo
import com.example.data.SmartSearchResult
import com.example.data.TaskRecurrence
import com.example.data.WorkspaceSubTab
import com.example.data.ZiaSettingsState
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
import java.util.Locale

@Composable
fun WorkspaceHubScreen(
    activeSubTab: WorkspaceSubTab,
    onSelectSubTab: (WorkspaceSubTab) -> Unit,
    settings: ZiaSettingsState,
    plannerTasks: List<PlannerTaskEntity>,
    knowledgeNotes: List<KnowledgeNoteEntity>,
    gamingProfiles: List<GamingProfileEntity>,
    memoryFacts: List<MemoryFactEntity>,
    pendingAction: PendingAction?,
    focusTimerSeconds: Int,
    focusTimerRunning: Boolean,
    focusSessionLabel: String,
    selectedSafFile: SelectedFileInfo?,
    smartSearchResult: SmartSearchResult?,
    selectedScreenshotUri: Uri?,
    screenshotAnalysisResult: String?,
    generatedGamingPackText: String?,
    onStartFocusTimer: (Int, String) -> Unit,
    onPauseResumeFocusTimer: () -> Unit,
    onResetFocusTimer: (Int) -> Unit,
    onAddPlannerTask: (String, String, Int, String, TaskRecurrence, Int) -> Unit,
    onTogglePlannerTask: (Int, Boolean) -> Unit,
    onDeletePlannerTask: (Int) -> Unit,
    onRequestAction: (PendingAction) -> Unit,
    onConfirmAction: () -> Unit,
    onCancelAction: () -> Unit,
    onGenerateAndSaveCodeSnippet: (String, String) -> Unit,
    onSendPromptToChat: (String) -> Unit,
    onSafFilePicked: (Uri?) -> Unit,
    onSummarizeSafFile: (Boolean) -> Unit,
    onSaveVaultNote: (String, String, NoteCategory, String) -> Unit,
    onDeleteVaultNote: (Int) -> Unit,
    onRunSmartSearch: (String, Boolean) -> Unit,
    onSaveGamingProfile: (String, String, String, String) -> Unit,
    onDeleteGamingProfile: (Int) -> Unit,
    onGenerateGamingPack: (String, String) -> Unit,
    onScreenshotPicked: (Uri?) -> Unit,
    onAnalyzeScreenshot: (String) -> Unit,
    onToggleMemorySaving: (Boolean) -> Unit,
    onAddMemoryFact: (String, String, String) -> Unit,
    onEditMemoryFact: (MemoryFactEntity, String, String) -> Unit,
    onDeleteMemoryFact: (Int) -> Unit,
    onExportMemoryText: () -> String,
    onShowBanner: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Sub-Module Switcher Bar
        Row(
            modifier = Modifier
                .widthIn(max = 600.dp)
                .fillMaxWidth()
                .padding(vertical = 8.dp)
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            WorkspaceSubTab.entries.forEach { tab ->
                val selected = activeSubTab == tab
                FilterChip(
                    selected = selected,
                    onClick = { onSelectSubTab(tab) },
                    label = {
                        Text(
                            text = tab.title,
                            style = MaterialTheme.typography.labelLarge
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = ZiaSapphireBlue.copy(alpha = 0.28f),
                        selectedLabelColor = ZiaAcousticCyan
                    ),
                    modifier = Modifier.testTag("workspace_subtab_${tab.name.lowercase()}")
                )
            }
        }

        when (activeSubTab) {
            WorkspaceSubTab.PLANNER -> PlannerAndFocusSubScreen(
                plannerTasks = plannerTasks,
                focusTimerSeconds = focusTimerSeconds,
                focusTimerRunning = focusTimerRunning,
                focusSessionLabel = focusSessionLabel,
                onStartFocusTimer = onStartFocusTimer,
                onPauseResumeFocusTimer = onPauseResumeFocusTimer,
                onResetFocusTimer = onResetFocusTimer,
                onAddPlannerTask = onAddPlannerTask,
                onTogglePlannerTask = onTogglePlannerTask,
                onDeletePlannerTask = onDeletePlannerTask,
                onRequestAction = onRequestAction
            )

            WorkspaceSubTab.CODING -> CodingLabSubScreen(
                codeNotes = knowledgeNotes.filter { it.category == NoteCategory.CODE_SNIPPET.name },
                selectedSafFile = selectedSafFile,
                onGenerateAndSaveCodeSnippet = onGenerateAndSaveCodeSnippet,
                onSendPromptToChat = onSendPromptToChat,
                onSafFilePicked = onSafFilePicked,
                onSaveVaultNote = onSaveVaultNote,
                onDeleteVaultNote = onDeleteVaultNote,
                onShowBanner = onShowBanner
            )

            WorkspaceSubTab.GAMING -> GamingCompanionSubScreen(
                gamingProfiles = gamingProfiles,
                settings = settings,
                selectedScreenshotUri = selectedScreenshotUri,
                screenshotAnalysisResult = screenshotAnalysisResult,
                generatedGamingPackText = generatedGamingPackText,
                onSaveGamingProfile = onSaveGamingProfile,
                onDeleteGamingProfile = onDeleteGamingProfile,
                onGenerateGamingPack = onGenerateGamingPack,
                onScreenshotPicked = onScreenshotPicked,
                onAnalyzeScreenshot = onAnalyzeScreenshot,
                onSaveVaultNote = onSaveVaultNote
            )

            WorkspaceSubTab.VAULT -> KnowledgeVaultAndSearchSubScreen(
                knowledgeNotes = knowledgeNotes,
                selectedSafFile = selectedSafFile,
                smartSearchResult = smartSearchResult,
                settings = settings,
                onSafFilePicked = onSafFilePicked,
                onSummarizeSafFile = onSummarizeSafFile,
                onSaveVaultNote = onSaveVaultNote,
                onDeleteVaultNote = onDeleteVaultNote,
                onRunSmartSearch = onRunSmartSearch,
                onShowBanner = onShowBanner
            )

            WorkspaceSubTab.MEMORY -> MemoryScreen(
                memoryFacts = memoryFacts,
                settings = settings,
                pendingAction = pendingAction,
                onToggleMemorySaving = onToggleMemorySaving,
                onAddMemoryFact = onAddMemoryFact,
                onEditMemoryFact = onEditMemoryFact,
                onDeleteMemoryFact = onDeleteMemoryFact,
                onRequestWipeAll = {
                    onRequestAction(PendingAction.ClearAllMemory())
                },
                onConfirmAction = onConfirmAction,
                onCancelAction = onCancelAction,
                onExportMemoryText = onExportMemoryText,
                onShowBanner = onShowBanner
            )
        }
    }
}

@Composable
private fun PlannerAndFocusSubScreen(
    plannerTasks: List<PlannerTaskEntity>,
    focusTimerSeconds: Int,
    focusTimerRunning: Boolean,
    focusSessionLabel: String,
    onStartFocusTimer: (Int, String) -> Unit,
    onPauseResumeFocusTimer: () -> Unit,
    onResetFocusTimer: (Int) -> Unit,
    onAddPlannerTask: (String, String, Int, String, TaskRecurrence, Int) -> Unit,
    onTogglePlannerTask: (Int, Boolean) -> Unit,
    onDeletePlannerTask: (Int) -> Unit,
    onRequestAction: (PendingAction) -> Unit
) {
    var selectedDayOffset by rememberSaveable { mutableIntStateOf(0) }
    var taskTitle by rememberSaveable { mutableStateOf("") }
    var taskSubject by rememberSaveable { mutableStateOf("Study / Exam") }
    var taskTimeSlot by rememberSaveable { mutableStateOf("07:00 PM") }
    var selectedRecurrence by rememberSaveable { mutableStateOf(TaskRecurrence.DAILY) }

    val dayLabels = listOf("Today", "Tomorrow", "Day +2", "Day +3", "Day +4", "Day +5", "Day +6")
    val mm = focusTimerSeconds / 60
    val ss = focusTimerSeconds % 60
    val timerFormatted = String.format(Locale.US, "%02d:%02d", mm, ss)

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .testTag("planner_focus_list"),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Study Focus Timer + Music Playback Integration
        item {
            ControlSectionCard(
                title = "Music & Study Focus Mode Timer",
                subtitle = "Pomodoro countdown + active media session controls (No background wake-locks)"
            ) {
                Surface(
                    color = ZiaMidnightSurface,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ZiaAcousticCyan.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = focusSessionLabel.uppercase(),
                            style = MaterialTheme.typography.labelSmall,
                            color = ZiaAcousticCyan
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = timerFormatted,
                            fontFamily = JetBrainsMonoFontFamily,
                            style = MaterialTheme.typography.headlineLarge,
                            color = ZiaTextPrimary,
                            modifier = Modifier.testTag("focus_timer_display")
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            Button(
                                onClick = onPauseResumeFocusTimer,
                                modifier = Modifier.testTag("focus_timer_start_pause_button")
                            ) {
                                Icon(
                                    imageVector = if (focusTimerRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(if (focusTimerRunning) "Pause" else "Start Focus")
                            }
                            OutlinedButton(onClick = { onResetFocusTimer(25) }) {
                                Icon(Icons.Default.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Reset")
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.horizontalScroll(rememberScrollState()),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            AssistChip(
                                onClick = { onStartFocusTimer(25, "25m Study Pomodoro") },
                                label = { Text("25m Study") }
                            )
                            AssistChip(
                                onClick = { onStartFocusTimer(5, "5m Eye & Mind Break") },
                                label = { Text("5m Break") }
                            )
                            AssistChip(
                                onClick = { onStartFocusTimer(45, "45m Deep Coding Session") },
                                label = { Text("45m Deep Work") }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "Focus Music Session Controls (Compatible Android Media Sessions):",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZiaTextSecondary
                )
                Spacer(modifier = Modifier.height(6.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    OutlinedButton(onClick = { onRequestAction(PendingAction.MediaTransport("PREVIOUS")) }) {
                        Icon(Icons.Default.SkipPrevious, contentDescription = "Previous track")
                    }
                    OutlinedButton(onClick = { onRequestAction(PendingAction.MediaTransport("PLAY_PAUSE")) }) {
                        Icon(Icons.Default.PlayArrow, contentDescription = "Play/Pause")
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Play / Pause")
                    }
                    OutlinedButton(onClick = { onRequestAction(PendingAction.MediaTransport("NEXT")) }) {
                        Icon(Icons.Default.SkipNext, contentDescription = "Next track")
                    }
                }
                Text(
                    text = "Note: Kuch third-party music apps external media keys restrict karti hain. ZIA copyrighted music download nahi karti.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZiaTextMuted
                )
            }
        }

        // 2. 7-Day Interactive Calendar Strip
        item {
            ControlSectionCard(
                title = "Daily Planner & 7-Day Study Schedule",
                subtitle = "Select a day to view or schedule tasks. System alarms require Android Clock confirmation."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    dayLabels.forEachIndexed { idx, label ->
                        FilterChip(
                            selected = selectedDayOffset == idx,
                            onClick = { selectedDayOffset = idx },
                            label = { Text(label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = taskTitle,
                    onValueChange = { taskTitle = it },
                    label = { Text("Task / Study Session Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("planner_task_title_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value = taskSubject,
                        onValueChange = { taskSubject = it },
                        label = { Text("Subject / Tag") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    OutlinedTextField(
                        value = taskTimeSlot,
                        onValueChange = { taskTimeSlot = it },
                        label = { Text("Time (e.g. 07:30 PM)") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TaskRecurrence.entries.forEach { rec ->
                        FilterChip(
                            selected = selectedRecurrence == rec,
                            onClick = { selectedRecurrence = rec },
                            label = { Text(rec.label) }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            if (taskTitle.isNotBlank()) {
                                onAddPlannerTask(
                                    taskTitle,
                                    taskSubject,
                                    selectedDayOffset,
                                    taskTimeSlot,
                                    selectedRecurrence,
                                    25
                                )
                                taskTitle = ""
                            }
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("add_planner_task_button")
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add to Schedule")
                    }
                    OutlinedButton(
                        onClick = {
                            onRequestAction(
                                PendingAction.SetAlarm(
                                    hour24 = 7,
                                    minute = 30,
                                    label = taskTitle.ifBlank { "ZIA Study Session" }
                                )
                            )
                        }
                    ) {
                        Icon(Icons.Default.AccessAlarm, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Clock Alarm")
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Android 10 Alarm & Notification Honesty Note: Background exact-alarms aur reminders tabhi reliably bajte hain jab System Clock alarm confirm kiya jaye aur MIUI/Android battery saver ZIA ko restrict na kare.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZiaAmberWarning
                )
            }
        }

        // 3. Scheduled Tasks List
        val filteredTasks = plannerTasks.filter { it.dayOffset == selectedDayOffset }
        items(filteredTasks.ifEmpty { plannerTasks }, key = { it.id }) { task ->
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
                        .padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onTogglePlannerTask(task.id, !task.isCompleted) }) {
                        Icon(
                            imageVector = if (task.isCompleted) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = "Complete task",
                            tint = if (task.isCompleted) ZiaEmeraldOnline else ZiaAcousticCyan
                        )
                    }
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = task.title,
                            style = MaterialTheme.typography.titleMedium,
                            color = if (task.isCompleted) ZiaTextMuted else ZiaTextPrimary
                        )
                        Text(
                            text = "${task.subjectOrTag} · ${task.timeSlot} · ${task.recurrence} (${dayLabels.getOrElse(task.dayOffset) { "Scheduled" }})",
                            style = MaterialTheme.typography.labelSmall,
                            color = ZiaTextSecondary
                        )
                    }
                    IconButton(onClick = { onDeletePlannerTask(task.id) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete task", tint = ZiaCrimsonError)
                    }
                }
            }
        }
    }
}

@Composable
private fun CodingLabSubScreen(
    codeNotes: List<KnowledgeNoteEntity>,
    selectedSafFile: SelectedFileInfo?,
    onGenerateAndSaveCodeSnippet: (String, String) -> Unit,
    onSendPromptToChat: (String) -> Unit,
    onSafFilePicked: (Uri?) -> Unit,
    onSaveVaultNote: (String, String, NoteCategory, String) -> Unit,
    onDeleteVaultNote: (Int) -> Unit,
    onShowBanner: (String) -> Unit
) {
    val context = LocalContext.current
    val safPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> onSafFilePicked(uri) }

    val languages = listOf("Kotlin Compose", "Kotlin Coroutines", "JavaScript", "HTML/CSS", "Python")
    var selectedLang by rememberSaveable { mutableStateOf("Kotlin Compose") }
    var conceptOrErrorInput by rememberSaveable { mutableStateOf("") }
    var editableFileBuffer by rememberSaveable(selectedSafFile?.previewContent) {
        mutableStateOf(selectedSafFile?.previewContent.orEmpty())
    }

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .testTag("coding_lab_list"),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            ControlSectionCard(
                title = "Coding Assistant & Error Debugger",
                subtitle = "Generate Kotlin, JavaScript, HTML, CSS & Python snippets. Never runs arbitrary shell commands."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    languages.forEach { lang ->
                        FilterChip(
                            selected = selectedLang == lang,
                            onClick = { selectedLang = lang },
                            label = { Text(lang) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = conceptOrErrorInput,
                    onValueChange = { conceptOrErrorInput = it },
                    label = { Text("Concept or Compiler Error Message (e.g. Unresolved reference)") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("coding_prompt_input")
                )
                Spacer(modifier = Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onGenerateAndSaveCodeSnippet(selectedLang, conceptOrErrorInput)
                            conceptOrErrorInput = ""
                        },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("generate_code_snippet_button")
                    ) {
                        Icon(Icons.Default.Code, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Generate & Save")
                    }
                    OutlinedButton(
                        onClick = {
                            val q = "Explain in Hinglish & give $selectedLang code for: ${conceptOrErrorInput.ifBlank { selectedLang }}"
                            onSendPromptToChat(q)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Deep Explain in Chat")
                    }
                }
            }
        }

        // Optional File Editing with Explicit SAF Selection
        item {
            ControlSectionCard(
                title = "Safe File Editor (Explicit SAF Selection Only)",
                subtitle = "Select a local code/text file to inspect, edit in buffer, and save to your Code Library."
            ) {
                OutlinedButton(
                    onClick = { safPicker.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Select File via Android Storage Access Framework")
                }

                if (selectedSafFile != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Editing Buffer: ${selectedSafFile.displayName}",
                        style = MaterialTheme.typography.labelLarge,
                        color = ZiaAcousticCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = editableFileBuffer,
                        onValueChange = { editableFileBuffer = it },
                        modifier = Modifier.fillMaxWidth(),
                        textStyle = MaterialTheme.typography.bodyMedium.copy(fontFamily = JetBrainsMonoFontFamily)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = {
                            onSaveVaultNote(
                                "Edited: ${selectedSafFile.displayName}",
                                editableFileBuffer,
                                NoteCategory.CODE_SNIPPET,
                                selectedLang
                            )
                        }
                    ) {
                        Text("Save Edited Code to Library")
                    }
                }
            }
        }

        // Saved Code-Notes Library
        item {
            Text(
                text = "SAVED CODE-NOTES LIBRARY (${codeNotes.size})",
                style = MaterialTheme.typography.labelLarge,
                color = ZiaSapphireBlue
            )
        }

        items(codeNotes, key = { it.id }) { snippetNote ->
            Card(
                colors = CardDefaults.cardColors(containerColor = Color(0xFF0C1020)),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ZiaNeuralViolet.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${snippetNote.title} [${snippetNote.languageOrTag}]",
                            style = MaterialTheme.typography.titleMedium,
                            color = ZiaAcousticCyan,
                            modifier = Modifier.weight(1f)
                        )
                        Row {
                            IconButton(
                                onClick = {
                                    val cb = context.getSystemService(Context.CLIPBOARD_SERVICE) as? ClipboardManager
                                    cb?.setPrimaryClip(ClipData.newPlainText("ZIA Snippet", snippetNote.content))
                                    onShowBanner("Snippet copied to clipboard!")
                                }
                            ) {
                                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = ZiaTextPrimary)
                            }
                            IconButton(onClick = { onDeleteVaultNote(snippetNote.id) }) {
                                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = ZiaCrimsonError)
                            }
                        }
                    }
                    Text(
                        text = snippetNote.content,
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

@Composable
private fun GamingCompanionSubScreen(
    gamingProfiles: List<GamingProfileEntity>,
    settings: ZiaSettingsState,
    selectedScreenshotUri: Uri?,
    screenshotAnalysisResult: String?,
    generatedGamingPackText: String?,
    onSaveGamingProfile: (String, String, String, String) -> Unit,
    onDeleteGamingProfile: (Int) -> Unit,
    onGenerateGamingPack: (String, String) -> Unit,
    onScreenshotPicked: (Uri?) -> Unit,
    onAnalyzeScreenshot: (String) -> Unit,
    onSaveVaultNote: (String, String, NoteCategory, String) -> Unit
) {
    val photoPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri -> onScreenshotPicked(uri) }

    var gameTitleInput by rememberSaveable { mutableStateOf("BGMI / Mobile FPS") }
    var topicFocusInput by rememberSaveable { mutableStateOf("Zero Lag 60 FPS + 4-Finger Gyro Sensitivity") }
    var sensitivityInput by rememberSaveable { mutableStateOf("Camera: 120% · Red Dot: 55% · 3x: 25% · Gyro: 280%") }
    var checklistInput by rememberSaveable { mutableStateOf("Clear RAM · Gaming DND ON · Ping < 50ms") }
    var screenshotQuestion by rememberSaveable { mutableStateOf("Analyze my HUD button layout and match stats.") }

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .testTag("gaming_companion_list"),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. YouTube Gaming Creator Studio
        item {
            ControlSectionCard(
                title = "Gaming Companion & YouTube Creator Generator",
                subtitle = "Generate video ideas, titles, SEO descriptions & thumbnail concepts. Fair-play tips only."
            ) {
                OutlinedTextField(
                    value = gameTitleInput,
                    onValueChange = { gameTitleInput = it },
                    label = { Text("Favourite Game Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("gaming_title_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = topicFocusInput,
                    onValueChange = { topicFocusInput = it },
                    label = { Text("Video Topic / Strategy Focus") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                Button(
                    onClick = { onGenerateGamingPack(gameTitleInput, topicFocusInput) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("generate_gaming_pack_button")
                ) {
                    Icon(Icons.Default.SportsEsports, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Generate YouTube Titles, Description & Thumbnail Ideas")
                }

                if (!generatedGamingPackText.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = ZiaMidnightSurface,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ZiaAcousticCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = generatedGamingPackText,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZiaTextPrimary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            OutlinedButton(
                                onClick = {
                                    onSaveVaultNote(
                                        "YT Creator Pack: $gameTitleInput",
                                        generatedGamingPackText,
                                        NoteCategory.PROJECT_IDEA,
                                        "Gaming"
                                    )
                                }
                            ) {
                                Text("Save Creator Pack to Knowledge Vault")
                            }
                        }
                    }
                }
            }
        }

        // 2. Manual Gameplay Screenshot Analysis (Zero-Permission Photo Picker)
        item {
            ControlSectionCard(
                title = "Manual Gameplay Screenshot Analysis",
                subtitle = "Select a screenshot via Android Photo Picker. Requires explicit Cloud Upload Consent for vision analysis."
            ) {
                OutlinedButton(
                    onClick = {
                        photoPicker.launch(
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        )
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("pick_gameplay_screenshot_button")
                ) {
                    Icon(Icons.Default.ImageSearch, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Choose Gameplay Screenshot (Photo Picker)")
                }

                if (selectedScreenshotUri != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selected URI: ${selectedScreenshotUri.lastPathSegment ?: "Screenshot"}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZiaAcousticCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = screenshotQuestion,
                        onValueChange = { screenshotQuestion = it },
                        label = { Text("What should ZIA analyze in this screenshot?") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Button(
                        onClick = { onAnalyzeScreenshot(screenshotQuestion) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(Icons.Default.AutoAwesome, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Confirm Cloud Upload & Analyze Screenshot")
                    }
                }

                if (!screenshotAnalysisResult.isNullOrBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = screenshotAnalysisResult,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZiaTextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Fair-Play & Privacy Notice: ZIA background screen capture, auto-gameplay recognition, ya anti-cheat bypass kabhi nahi karti.",
                    style = MaterialTheme.typography.labelSmall,
                    color = ZiaTextMuted
                )
            }
        }

        // 3. Save Sensitivity Profile & Match Checklist
        item {
            ControlSectionCard(
                title = "Save Game Sensitivity & Match Checklist",
                subtitle = "Keep your custom HUD, gyroscope settings, and pre-match routine stored locally."
            ) {
                OutlinedTextField(
                    value = sensitivityInput,
                    onValueChange = { sensitivityInput = it },
                    label = { Text("Sensitivity & Graphic Notes") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = checklistInput,
                    onValueChange = { checklistInput = it },
                    label = { Text("Pre-Match Checklist") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        onSaveGamingProfile(
                            gameTitleInput,
                            sensitivityInput,
                            checklistInput,
                            generatedGamingPackText ?: topicFocusInput
                        )
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Save Gaming Profile Locally")
                }
            }
        }

        items(gamingProfiles, key = { it.id }) { profile ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ZiaMidnightSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ZiaElevatedBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = profile.gameTitle,
                            style = MaterialTheme.typography.titleMedium,
                            color = ZiaAcousticCyan
                        )
                        IconButton(onClick = { onDeleteGamingProfile(profile.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete profile", tint = ZiaCrimsonError)
                        }
                    }
                    Text(
                        text = "Sensitivity: ${profile.sensitivityNotes}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZiaTextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "Checklist:\n${profile.matchChecklist}",
                        style = MaterialTheme.typography.labelSmall,
                        color = ZiaTextSecondary
                    )
                }
            }
        }
    }
}

@Composable
private fun KnowledgeVaultAndSearchSubScreen(
    knowledgeNotes: List<KnowledgeNoteEntity>,
    selectedSafFile: SelectedFileInfo?,
    smartSearchResult: SmartSearchResult?,
    settings: ZiaSettingsState,
    onSafFilePicked: (Uri?) -> Unit,
    onSummarizeSafFile: (Boolean) -> Unit,
    onSaveVaultNote: (String, String, NoteCategory, String) -> Unit,
    onDeleteVaultNote: (Int) -> Unit,
    onRunSmartSearch: (String, Boolean) -> Unit,
    onShowBanner: (String) -> Unit
) {
    val safPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri -> onSafFilePicked(uri) }

    var searchQuery by rememberSaveable { mutableStateOf("") }
    var noteTitle by rememberSaveable { mutableStateOf("") }
    var noteContent by rememberSaveable { mutableStateOf("") }
    var selectedCategory by rememberSaveable { mutableStateOf(NoteCategory.NOTE) }

    LazyColumn(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .testTag("knowledge_vault_list"),
        contentPadding = PaddingValues(bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Smart Search (Offline Local + Optional Cited Web Search)
        item {
            ControlSectionCard(
                title = "Smart Search (Local Vault + Cited Web Search)",
                subtitle = "Clearly distinguishes retrieved facts & sources from ZIA's generated synthesis."
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    label = { Text("Search Notes, Memory, Planner or Web...") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("smart_search_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = { onRunSmartSearch(searchQuery, false) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("offline_vault_search_button")
                    ) {
                        Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Offline Search")
                    }
                    OutlinedButton(
                        onClick = { onRunSmartSearch(searchQuery, true) },
                        modifier = Modifier
                            .weight(1f)
                            .testTag("online_cited_search_button")
                    ) {
                        Icon(Icons.Default.Public, contentDescription = null, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Cited Web Search")
                    }
                }

                if (smartSearchResult != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = ZiaMidnightSurface,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, ZiaSapphireBlue.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(
                                text = "1. ON-DEVICE LOCAL VAULT MATCHES (${smartSearchResult.localMatches.size})",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZiaEmeraldOnline
                            )
                            if (smartSearchResult.localMatches.isEmpty()) {
                                Text(
                                    text = "• No local note matched '${smartSearchResult.query}'.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = ZiaTextSecondary
                                )
                            } else {
                                smartSearchResult.localMatches.forEach { hit ->
                                    Text(
                                        text = "• $hit",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ZiaTextPrimary
                                    )
                                }
                            }

                            if (smartSearchResult.retrievedWebFacts.isNotEmpty()) {
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    text = "2. RETRIEVED WEB FACTS & SOURCES",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZiaAcousticCyan
                                )
                                smartSearchResult.retrievedWebFacts.forEach { fact ->
                                    Text(
                                        text = "• $fact",
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ZiaTextPrimary
                                    )
                                }
                                Text(
                                    text = "Sources: ${smartSearchResult.citedSources.joinToString(", ")}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ZiaTextMuted
                                )
                            }

                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "3. ZIA AI SYNTHESIS (GENERATED RESPONSE)",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZiaNeuralViolet
                            )
                            Text(
                                text = smartSearchResult.aiSynthesis,
                                style = MaterialTheme.typography.bodyMedium,
                                color = ZiaTextPrimary
                            )
                        }
                    }
                }
            }
        }

        // 2. SAF Document Import & Summarizer (Consent-Gated)
        item {
            ControlSectionCard(
                title = "Document Import & Summarizer (SAF)",
                subtitle = "Summarize only files you explicitly select. Cloud upload asks for consent first."
            ) {
                OutlinedButton(
                    onClick = { safPicker.launch(arrayOf("*/*")) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Description, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Import Document via Storage Access Framework")
                }

                if (selectedSafFile != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Selected: ${selectedSafFile.displayName} (${selectedSafFile.sizeBytes} bytes)",
                        style = MaterialTheme.typography.labelLarge,
                        color = ZiaAcousticCyan
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        Button(
                            onClick = { onSummarizeSafFile(false) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Summarize Offline")
                        }
                        OutlinedButton(
                            onClick = { onSummarizeSafFile(true) },
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Cloud AI Summary")
                        }
                    }
                }
            }
        }

        // 3. Add New Knowledge Vault Note
        item {
            ControlSectionCard(
                title = "Add to Personal Knowledge Vault",
                subtitle = "Save notes, project ideas, or selected text 100% offline."
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    NoteCategory.entries.forEach { cat ->
                        FilterChip(
                            selected = selectedCategory == cat,
                            onClick = { selectedCategory = cat },
                            label = { Text(cat.label) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteTitle,
                    onValueChange = { noteTitle = it },
                    label = { Text("Note / Project Idea Title") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_note_title_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = noteContent,
                    onValueChange = { noteContent = it },
                    label = { Text("Content or Summary") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("vault_note_content_input")
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = {
                        if (noteTitle.isNotBlank() && noteContent.isNotBlank()) {
                            onSaveVaultNote(noteTitle, noteContent, selectedCategory, "User")
                            noteTitle = ""
                            noteContent = ""
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("save_vault_note_button")
                ) {
                    Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save to Knowledge Vault")
                }
            }
        }

        items(knowledgeNotes, key = { it.id }) { note ->
            Card(
                colors = CardDefaults.cardColors(containerColor = ZiaMidnightSurface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ZiaElevatedBorder, RoundedCornerShape(16.dp))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = note.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = ZiaAcousticCyan
                            )
                            Text(
                                text = "${note.category} · ${note.languageOrTag}",
                                style = MaterialTheme.typography.labelSmall,
                                color = ZiaTextMuted
                            )
                        }
                        IconButton(onClick = { onDeleteVaultNote(note.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete note", tint = ZiaCrimsonError)
                        }
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyMedium,
                        color = ZiaTextPrimary
                    )
                }
            }
        }
    }
}
