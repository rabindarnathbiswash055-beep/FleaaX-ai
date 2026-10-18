package com.example.commands

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.provider.AlarmClock
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.provider.Settings
import android.view.KeyEvent
import com.example.data.InstalledAppItem
import com.example.data.PendingAction
import com.example.data.SelectedFileInfo
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

data class ActionExecutionOutcome(
    val success: Boolean,
    val messageHinglish: String
)

class ActionExecutor(private val context: Context) {

    fun getLaunchableInstalledApps(): List<InstalledAppItem> {
        return try {
            val pm = context.packageManager
            val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
                addCategory(Intent.CATEGORY_LAUNCHER)
            }
            val resolveInfos = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                pm.queryIntentActivities(mainIntent, PackageManager.ResolveInfoFlags.of(0L))
            } else {
                @Suppress("DEPRECATION")
                pm.queryIntentActivities(mainIntent, 0)
            }
            resolveInfos
                .mapNotNull { info ->
                    val pkg = info.activityInfo?.packageName ?: return@mapNotNull null
                    val label = info.loadLabel(pm)?.toString()?.trim().orEmpty()
                    if (label.isNotEmpty() && pkg != context.packageName) {
                        InstalledAppItem(label = label, packageName = pkg)
                    } else null
                }
                .distinctBy { it.packageName }
                .sortedBy { it.label.lowercase(Locale.ROOT) }
        } catch (e: Exception) {
            emptyList()
        }
    }

    fun getCurrentVolumePercentages(): Map<String, Int> {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return mapOf("MEDIA" to 50, "RING" to 50, "ALARM" to 70, "NOTIFICATION" to 50)

        fun streamPercent(stream: Int): Int {
            val max = audioManager.getStreamMaxVolume(stream).coerceAtLeast(1)
            val current = audioManager.getStreamVolume(stream)
            return ((current.toFloat() / max.toFloat()) * 100f).toInt().coerceIn(0, 100)
        }

        return mapOf(
            "MEDIA" to streamPercent(AudioManager.STREAM_MUSIC),
            "RING" to streamPercent(AudioManager.STREAM_RING),
            "ALARM" to streamPercent(AudioManager.STREAM_ALARM),
            "NOTIFICATION" to streamPercent(AudioManager.STREAM_NOTIFICATION)
        )
    }

    fun execute(action: PendingAction): ActionExecutionOutcome {
        return try {
            when (action) {
                is PendingAction.LaunchApp -> launchApp(action)
                is PendingAction.DialPhone -> openDialer(action)
                is PendingAction.ComposeSms -> openSmsComposer(action)
                is PendingAction.SetAlarm -> setSystemAlarm(action)
                is PendingAction.AdjustVolume -> adjustVolume(action)
                is PendingAction.OpenSystemSettings -> openSettingsPanel(action.panelType)
                is PendingAction.LaunchCamera -> launchCameraIntent()
                is PendingAction.MediaTransport -> dispatchMediaKey(action.command)
                is PendingAction.ClearAllMemory -> ActionExecutionOutcome(
                    success = true,
                    messageHinglish = "Saari local memory, reminders aur chat history safely delete kar di gayi hai."
                )
            }
        } catch (e: Exception) {
            ActionExecutionOutcome(
                success = false,
                messageHinglish = "Action execute nahi ho paya (${e.javaClass.simpleName}): Yeh app ya system intent is device par available nahi hai."
            )
        }
    }

    private fun launchApp(action: PendingAction.LaunchApp): ActionExecutionOutcome {
        val pm = context.packageManager
        val queryLower = action.appNameQuery.trim().lowercase(Locale.ROOT)

        // 1. Try resolved package name if known
        if (!action.resolvedPackageName.isNullOrBlank()) {
            val directIntent = pm.getLaunchIntentForPackage(action.resolvedPackageName)
            if (directIntent != null) {
                directIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(directIntent)
                return ActionExecutionOutcome(
                    success = true,
                    messageHinglish = "'${action.resolvedLabel ?: action.appNameQuery}' successfully open ho gaya!"
                )
            }
        }

        // 2. Search installed launcher apps by label or package substring
        val installed = getLaunchableInstalledApps()
        val matchedApp = installed.firstOrNull {
            it.label.lowercase(Locale.ROOT).contains(queryLower) ||
                it.packageName.lowercase(Locale.ROOT).contains(queryLower)
        }

        if (matchedApp != null) {
            val intent = pm.getLaunchIntentForPackage(matchedApp.packageName)
            if (intent != null) {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return ActionExecutionOutcome(
                    success = true,
                    messageHinglish = "'${matchedApp.label}' (${matchedApp.packageName}) launch kar diya gaya hai."
                )
            }
        }

        // 3. Special fallback for system settings / clock / calculator intents
        if (queryLower.contains("setting")) {
            return openSettingsPanel("MAIN")
        }

        return ActionExecutionOutcome(
            success = false,
            messageHinglish = "'${action.appNameQuery}' aapke phone par installed nahi mila. Aap Quick Actions tab mein Installed Apps list dekh sakte hain."
        )
    }

    private fun openDialer(action: PendingAction.DialPhone): ActionExecutionOutcome {
        val sanitizedNumber = action.phoneNumber.filter { it.isDigit() || it == '+' || it == '*' || it == '#' }
        val intent = Intent(Intent.ACTION_DIAL).apply {
            data = Uri.parse("tel:$sanitizedNumber")
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionExecutionOutcome(
            success = true,
            messageHinglish = "Phone Dialer '$sanitizedNumber' ke saath open kar diya gaya hai. Call lagane ke liye Dialer mein green button dabayein."
        )
    }

    private fun openSmsComposer(action: PendingAction.ComposeSms): ActionExecutionOutcome {
        val sanitizedNumber = action.phoneNumber.filter { it.isDigit() || it == '+' }
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$sanitizedNumber")
            putExtra("sms_body", action.messageBody)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
        return ActionExecutionOutcome(
            success = true,
            messageHinglish = "SMS Composer open ho gaya hai. Message review karke Send dabayein."
        )
    }

    private fun setSystemAlarm(action: PendingAction.SetAlarm): ActionExecutionOutcome {
        val intent = Intent(AlarmClock.ACTION_SET_ALARM).apply {
            putExtra(AlarmClock.EXTRA_HOUR, action.hour24)
            putExtra(AlarmClock.EXTRA_MINUTES, action.minute)
            putExtra(AlarmClock.EXTRA_MESSAGE, action.label)
            putExtra(AlarmClock.EXTRA_SKIP_UI, false)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
            ActionExecutionOutcome(
                success = true,
                messageHinglish = String.format(
                    Locale.US,
                    "Clock app mein %02d:%02d ka alarm ('%s') set karne ke liye bhej diya gaya hai.",
                    action.hour24,
                    action.minute,
                    action.label
                )
            )
        } else {
            ActionExecutionOutcome(
                success = false,
                messageHinglish = "System Clock app alarm intent support nahi kar rahi, par aapka reminder ZIA ke local database mein safe hai."
            )
        }
    }

    private fun adjustVolume(action: PendingAction.AdjustVolume): ActionExecutionOutcome {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return ActionExecutionOutcome(false, "AudioManager service unavailable hai.")

        val stream = when (action.streamType.uppercase(Locale.ROOT)) {
            "RING" -> AudioManager.STREAM_RING
            "ALARM" -> AudioManager.STREAM_ALARM
            "NOTIFICATION" -> AudioManager.STREAM_NOTIFICATION
            else -> AudioManager.STREAM_MUSIC
        }

        return try {
            when (action.targetPercent) {
                -1 -> audioManager.adjustStreamVolume(
                    stream,
                    AudioManager.ADJUST_RAISE,
                    AudioManager.FLAG_SHOW_UI
                )
                -2 -> audioManager.adjustStreamVolume(
                    stream,
                    AudioManager.ADJUST_LOWER,
                    AudioManager.FLAG_SHOW_UI
                )
                else -> {
                    val max = audioManager.getStreamMaxVolume(stream).coerceAtLeast(1)
                    val targetIndex = ((action.targetPercent.coerceIn(0, 100) / 100f) * max).toInt()
                    audioManager.setStreamVolume(stream, targetIndex, AudioManager.FLAG_SHOW_UI)
                }
            }
            val updatedMap = getCurrentVolumePercentages()
            val updatedVal = updatedMap[action.streamType.uppercase(Locale.ROOT)] ?: action.targetPercent
            ActionExecutionOutcome(
                success = true,
                messageHinglish = "${action.streamType} volume ab $updatedVal% par set hai."
            )
        } catch (se: SecurityException) {
            ActionExecutionOutcome(
                success = false,
                messageHinglish = "Do Not Disturb (DND) active hone ki wajah se Ring/Notification volume change restricted hai. Sound settings panel check karein."
            )
        }
    }

    fun openSettingsPanel(panelType: String): ActionExecutionOutcome {
        val intent = when (panelType.uppercase(Locale.ROOT)) {
            "WIFI" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_WIFI)
            } else {
                Intent(Settings.ACTION_WIFI_SETTINGS)
            }
            "VOLUME_PANEL" -> if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                Intent(Settings.Panel.ACTION_VOLUME)
            } else {
                Intent(Settings.ACTION_SOUND_SETTINGS)
            }
            "BLUETOOTH" -> Intent(Settings.ACTION_BLUETOOTH_SETTINGS)
            "SOUND" -> Intent(Settings.ACTION_SOUND_SETTINGS)
            "DISPLAY" -> Intent(Settings.ACTION_DISPLAY_SETTINGS)
            "NOTIFICATION_LISTENER" -> Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS)
            "APP_DETAILS" -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                data = Uri.fromParts("package", context.packageName, null)
            }
            else -> Intent(Settings.ACTION_SETTINGS)
        }.apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        return try {
            context.startActivity(intent)
            ActionExecutionOutcome(
                success = true,
                messageHinglish = "$panelType system settings panel open kar diya gaya hai."
            )
        } catch (e: Exception) {
            val fallback = Intent(Settings.ACTION_SETTINGS).apply {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(fallback)
            ActionExecutionOutcome(
                success = true,
                messageHinglish = "System Settings open kar diya gaya hai."
            )
        }
    }

    private fun launchCameraIntent(): ActionExecutionOutcome {
        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        return try {
            context.startActivity(intent)
            ActionExecutionOutcome(
                success = true,
                messageHinglish = "Camera app launch ho gayi hai."
            )
        } catch (e: Exception) {
            ActionExecutionOutcome(
                success = false,
                messageHinglish = "Camera app launch nahi ho saki. Kripya check karein ki Camera app enabled hai."
            )
        }
    }

    private fun dispatchMediaKey(command: String): ActionExecutionOutcome {
        val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            ?: return ActionExecutionOutcome(false, "AudioManager available nahi hai.")

        val keyCode = when (command.uppercase(Locale.ROOT)) {
            "NEXT" -> KeyEvent.KEYCODE_MEDIA_NEXT
            "PREVIOUS" -> KeyEvent.KEYCODE_MEDIA_PREVIOUS
            "STOP" -> KeyEvent.KEYCODE_MEDIA_STOP
            else -> KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE
        }

        val downEvent = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val upEvent = KeyEvent(KeyEvent.ACTION_UP, keyCode)
        audioManager.dispatchMediaKeyEvent(downEvent)
        audioManager.dispatchMediaKeyEvent(upEvent)

        return ActionExecutionOutcome(
            success = true,
            messageHinglish = "Media control '$command' active audio session ko bhej diya gaya hai."
        )
    }

    /**
     * Reads metadata and safe text preview from a user-selected file via Storage Access Framework (SAF).
     * Never scans private files outside the single URI explicitly picked by the user.
     */
    fun inspectSafFile(uri: Uri): SelectedFileInfo? {
        return try {
            val resolver = context.contentResolver
            val mimeType = resolver.getType(uri) ?: "application/octet-stream"
            var displayName = uri.lastPathSegment ?: "selected_file"
            var sizeBytes = 0L

            resolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (nameIdx >= 0) displayName = cursor.getString(nameIdx) ?: displayName
                    if (sizeIdx >= 0 && !cursor.isNull(sizeIdx)) sizeBytes = cursor.getLong(sizeIdx)
                }
            }

            val isTextOrCode = mimeType.startsWith("text/") ||
                displayName.endsWith(".kt") ||
                displayName.endsWith(".java") ||
                displayName.endsWith(".xml") ||
                displayName.endsWith(".json") ||
                displayName.endsWith(".md") ||
                displayName.endsWith(".py") ||
                displayName.endsWith(".js") ||
                displayName.endsWith(".txt") ||
                displayName.endsWith(".gradle")

            val preview = if (isTextOrCode) {
                resolver.openInputStream(uri)?.use { inputStream ->
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val sb = StringBuilder()
                    var lineCount = 0
                    var line: String?
                    while (reader.readLine().also { line = it } != null && lineCount < 35) {
                        sb.append(line).append("\n")
                        lineCount++
                    }
                    sb.toString().take(1800).ifBlank { "(Empty text/code file)" }
                } ?: "(Unable to read file stream)"
            } else {
                "Binary/Media file selected via Storage Access Framework. ZIA only reads text/code content when explicitly requested."
            }

            SelectedFileInfo(
                displayName = displayName,
                mimeType = mimeType,
                sizeBytes = sizeBytes,
                previewContent = preview
            )
        } catch (e: Exception) {
            null
        }
    }
}
