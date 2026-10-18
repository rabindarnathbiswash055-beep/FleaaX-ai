package com.example.commands

import android.content.Context
import android.os.BatteryManager
import com.example.data.EngineSource
import com.example.data.PendingAction
import com.example.security.PrivacyGuard
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class LocalCommandParseResult(
    val matched: Boolean,
    val replyText: String = "",
    val pendingAction: PendingAction? = null,
    val reminderToSave: Pair<String, String>? = null, // title to timeText
    val memoryToSave: Pair<String, String>? = null,   // key to value
    val engineSource: EngineSource = EngineSource.LOCAL_COMMAND
)

interface LocalCommandEngine {
    fun evaluateCommand(rawInput: String, context: Context): LocalCommandParseResult
}

class DeterministicLocalCommandEngine : LocalCommandEngine {

    private val commonAppKeywords = mapOf(
        "youtube" to "com.google.android.youtube",
        "whatsapp" to "com.whatsapp",
        "chrome" to "com.android.chrome",
        "browser" to "com.android.chrome",
        "gmail" to "com.google.android.gm",
        "maps" to "com.google.android.apps.maps",
        "calculator" to "com.google.android.calculator",
        "calendar" to "com.google.android.calendar",
        "clock" to "com.google.android.deskclock",
        "contacts" to "com.google.android.contacts",
        "files" to "com.google.android.documentsui",
        "settings" to "com.android.settings",
        "spotify" to "com.spotify.music",
        "acode" to "com.foxdebug.acode"
    )

    override fun evaluateCommand(rawInput: String, context: Context): LocalCommandParseResult {
        val text = rawInput.trim()
        val lower = text.lowercase(Locale.ROOT)

        // Strip optional "hey zia" or "zia" wake prefix
        val normalized = lower
            .removePrefix("hey zia")
            .removePrefix("hi zia")
            .removePrefix("hello zia")
            .removePrefix("zia")
            .trim(',', ' ', '.', '!')

        if (normalized.isEmpty()) {
            return LocalCommandParseResult(
                matched = true,
                replyText = "Ji boliye! Main ZIA hoon — aap English, Hinglish, Banglish, ya বাংলা mein command de sakte hain (jaise 'Volume 60% karo', '7:30 ka alarm lagao', ya 'Open YouTube')."
            )
        }

        // 1. Time & Date Queries (English, Hinglish, Banglish, Bengali)
        if (normalized.contains("time kya") ||
            normalized.contains("what time") ||
            normalized.contains("current time") ||
            normalized.contains("baje hain") ||
            normalized.contains("somoy koto") ||
            normalized.contains("কটা বাজে") ||
            normalized.contains("সময় কত")
        ) {
            val timeStr = SimpleDateFormat("hh:mm a", Locale.getDefault()).format(Date())
            return LocalCommandParseResult(
                matched = true,
                replyText = "Abhi time $timeStr ho raha hai. (Current local time: $timeStr)"
            )
        }

        if (normalized.contains("aaj ki date") ||
            normalized.contains("today's date") ||
            normalized.contains("what date") ||
            normalized.contains("aaj konsa din") ||
            normalized.contains("ajker tarikh") ||
            normalized.contains("আজকের তারিখ")
        ) {
            val dateStr = SimpleDateFormat("EEEE, dd MMMM yyyy", Locale.getDefault()).format(Date())
            return LocalCommandParseResult(
                matched = true,
                replyText = "Aaj $dateStr hai."
            )
        }

        // 2. Battery & Hardware Status
        if (normalized.contains("battery") ||
            normalized.contains("charge kitna") ||
            normalized.contains("ব্যাটারি")
        ) {
            val bm = context.getSystemService(Context.BATTERY_SERVICE) as? BatteryManager
            val level = bm?.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY) ?: -1
            val reply = if (level in 0..100) {
                "Aapke phone ki battery abhi $level% charged hai."
            } else {
                "Battery level read karne ke liye system status check kiya gaya."
            }
            return LocalCommandParseResult(matched = true, replyText = reply)
        }

        if (normalized.contains("ram status") ||
            normalized.contains("device status") ||
            normalized.contains("phone memory") ||
            normalized.contains("hardware check")
        ) {
            val audit = PrivacyGuard.inspectDeviceHardware(context)
            return LocalCommandParseResult(
                matched = true,
                replyText = "Device Audit (${audit.deviceModel} · ${audit.androidRelease}):\n" +
                    "• Available RAM: ${String.format("%.2f", audit.availableRamGb)} GB / ${String.format("%.1f", audit.totalRamGb)} GB\n" +
                    "• Free Storage: ${String.format("%.2f", audit.freeStorageGb)} GB\n" +
                    "• CPU Cores: ${audit.cpuCores}\n" +
                    "${audit.llmRecommendationHinglish}"
            )
        }

        // 3. Save Memory Preference Commands ("Remember my name is ...", "Mera naam ... yaad rakhna")
        val rememberNameRegex = Regex("(?i)(?:remember my name is|my name is|mera naam|amar naam)\\s+([a-zA-Z0-9_ ]{2,30})(?:\\s+hai|\\s+yaad rakhna)?")
        rememberNameRegex.find(text)?.let { match ->
            val nameVal = match.groupValues[1].trim()
            return LocalCommandParseResult(
                matched = true,
                replyText = "Done! Maine local memory mein aapka naam '$nameVal' save kar liya hai. Aap ise Memory screen mein kabhi bhi edit ya delete kar sakte hain.",
                memoryToSave = "User Name" to nameVal
            )
        }

        val genericRememberRegex = Regex("(?i)(?:remember that|yaad rakhna ki|mone rekho)\\s+(.+)")
        genericRememberRegex.find(text)?.let { match ->
            val noteVal = match.groupValues[1].trim()
            return LocalCommandParseResult(
                matched = true,
                replyText = "Maine yeh note aapki local Preference Memory mein check karke add kiya hai: \"$noteVal\"",
                memoryToSave = "Custom Preference" to noteVal
            )
        }

        // 4. Volume Controls (Media / Ring / Alarm)
        if (normalized.contains("volume") ||
            normalized.contains("aawaz") ||
            normalized.contains("awaz") ||
            normalized.contains("आवाज़") ||
            normalized.contains("সাউন্ড") ||
            normalized.contains("ভলিউম") ||
            normalized.contains("mute")
        ) {
            val stream = when {
                normalized.contains("ring") || normalized.contains("call") -> "RING"
                normalized.contains("alarm") -> "ALARM"
                normalized.contains("notification") -> "NOTIFICATION"
                else -> "MEDIA"
            }

            val percentMatch = Regex("(\\d{1,3})\\s*%?").find(normalized)
            val targetPercent = when {
                normalized.contains("mute") || normalized.contains("silent") || normalized.contains("band") -> 0
                normalized.contains("full") || normalized.contains("max") -> 100
                percentMatch != null -> percentMatch.groupValues[1].toIntOrNull()?.coerceIn(0, 100) ?: 50
                normalized.contains("badha") || normalized.contains("up") || normalized.contains("increase") ||
                    normalized.contains("zyada") || normalized.contains("barao") || normalized.contains("বাড়াও") -> -1
                normalized.contains("kam") || normalized.contains("down") || normalized.contains("decrease") ||
                    normalized.contains("komao") || normalized.contains("কমাও") -> -2
                else -> 60
            }

            val action = PendingAction.AdjustVolume(
                streamType = stream,
                targetPercent = targetPercent,
                summaryHinglish = when (targetPercent) {
                    -1 -> "$stream volume ek step badhaya gaya."
                    -2 -> "$stream volume ek step kam kiya gaya."
                    else -> "$stream volume $targetPercent% par set kiya gaya."
                }
            )
            return LocalCommandParseResult(
                matched = true,
                replyText = action.summaryHinglish,
                pendingAction = action
            )
        }

        // 5. Alarms ("7:30 ka alarm lagao", "set alarm for 06:45", "8 baje alarm")
        if (normalized.contains("alarm") || normalized.contains("अलार्म") || normalized.contains("অ্যালার্ম")) {
            val timeColonMatch = Regex("(\\d{1,2}):(\\d{2})\\s*(am|pm)?").find(normalized)
            val hourOnlyMatch = Regex("(\\d{1,2})\\s*(?:baje|am|pm|tar)?").find(normalized)

            var hour = 7
            var minute = 0
            if (timeColonMatch != null) {
                hour = timeColonMatch.groupValues[1].toIntOrNull() ?: 7
                minute = timeColonMatch.groupValues[2].toIntOrNull() ?: 0
                val ampm = timeColonMatch.groupValues[3]
                if (ampm == "pm" && hour < 12) hour += 12
                if (ampm == "am" && hour == 12) hour = 0
            } else if (hourOnlyMatch != null) {
                hour = hourOnlyMatch.groupValues[1].toIntOrNull() ?: 7
                if (normalized.contains("pm") || normalized.contains("shaam") || normalized.contains("raat")) {
                    if (hour < 12) hour += 12
                }
            }
            hour = hour.coerceIn(0, 23)
            minute = minute.coerceIn(0, 59)

            val formatted = String.format(Locale.US, "%02d:%02d", hour, minute)
            val action = PendingAction.SetAlarm(
                hour24 = hour,
                minute = minute,
                label = "ZIA AI Alarm"
            )
            return LocalCommandParseResult(
                matched = true,
                replyText = "Maine $formatted ka System Alarm tayyar kar diya hai. Niche Confirmation Card par 'Confirm & Execute' dabayein taaki Android Clock app mein alarm set ho jaye.",
                pendingAction = action
            )
        }

        // 6. Reminders ("Remind me to ...", "Reminder lagao ...")
        if (normalized.contains("remind") ||
            normalized.contains("reminder") ||
            normalized.contains("yaad dila") ||
            normalized.contains("মনে করিয়ে") ||
            normalized.contains("রিমাইন্ডার")
        ) {
            val cleanReminder = text
                .replace(Regex("(?i)^(hey zia|zia)?[, ]*(remind me to|set a reminder to|set reminder|reminder lagao|mujhe yaad dilana|amake mone koriye dio)\\s*"), "")
                .ifBlank { text }
            return LocalCommandParseResult(
                matched = true,
                replyText = "In-app Reminder save ho gaya hai: \"$cleanReminder\". Aap ise Quick Actions ya Memory tab mein dekh sakte hain.",
                reminderToSave = cleanReminder to "Scheduled via Voice/Chat"
            )
        }

        // 7. Phone Calls ("Call 9876543210", "9876543210 par call lagao")
        if (normalized.startsWith("call ") ||
            normalized.contains("call lagao") ||
            normalized.contains("phone lagao") ||
            normalized.contains("কল করো")
        ) {
            val digits = Regex("[+0-9][0-9\\- ]{3,15}").find(text)?.value?.replace(" ", "") ?: ""
            val targetNumber = digits.ifBlank { "112" }
            val contactName = text.replace(Regex("(?i)(call|lagao|phone|par|ko|করো)"), "").trim()
            val action = PendingAction.DialPhone(
                phoneNumber = targetNumber,
                contactLabel = contactName
            )
            return LocalCommandParseResult(
                matched = true,
                replyText = "Safety Gate: Call karne se pehle Confirmation Preview zaroori hai. Confirm karne par Dialer '$targetNumber' ke saath khulega.",
                pendingAction = action
            )
        }

        // 8. SMS / Message Composer ("Send SMS to 9876543210 Hello", "Message bhejo ...")
        if (normalized.contains("send sms") ||
            normalized.contains("sms bhejo") ||
            normalized.contains("message bhejo") ||
            normalized.contains("text message") ||
            normalized.contains("মেসেজ পাঠাও")
        ) {
            val numberMatch = Regex("([+0-9]{4,15})").find(text)
            val phone = numberMatch?.groupValues?.get(1) ?: ""
            val body = text
                .replace(Regex("(?i)(send sms to|sms bhejo|message bhejo|text message to)"), "")
                .replace(phone, "")
                .trim()
                .ifBlank { "Hello from ZIA AI!" }
            val action = PendingAction.ComposeSms(
                phoneNumber = phone.ifBlank { "Enter Recipient in SMS App" },
                messageBody = body
            )
            return LocalCommandParseResult(
                matched = true,
                replyText = "SMS Composer tayyar hai. Confirm karne par Android SMS app prefilled message ke saath open hogi (bina aapke Send dabaye SMS nahi jayega).",
                pendingAction = action
            )
        }

        // 9. System Settings Panels (Wi-Fi, Bluetooth, Sound, Display, Notifications)
        if (normalized.contains("wifi") || normalized.contains("wi-fi") || normalized.contains("ওয়াইফাই")) {
            return LocalCommandParseResult(
                matched = true,
                replyText = "Android 10+ security rules ke hisaab se apps background mein Wi-Fi silently toggle nahi kar sakti. Main aapke liye official Wi-Fi Settings Panel khol rahi hoon.",
                pendingAction = PendingAction.OpenSystemSettings("WIFI")
            )
        }

        if (normalized.contains("bluetooth") || normalized.contains("ब्लूटूथ") || normalized.contains("ব্লুটুথ")) {
            return LocalCommandParseResult(
                matched = true,
                replyText = "Bluetooth Settings panel open kar rahi hoon taaki aap safely device connect ya toggle kar sakein.",
                pendingAction = PendingAction.OpenSystemSettings("BLUETOOTH")
            )
        }

        // 10. Camera Launch
        if (normalized.contains("open camera") ||
            normalized.contains("camera kholo") ||
            normalized.contains("take photo") ||
            normalized.contains("photo khicho") ||
            normalized.contains("ক্যামেরা খোলো")
        ) {
            return LocalCommandParseResult(
                matched = true,
                replyText = "Camera launch karne ke liye Confirmation Card par tap karein. ZIA kabhi bhi background mein chhup kar photo ya video capture nahi karti.",
                pendingAction = PendingAction.LaunchCamera()
            )
        }

        // 11. Media Playback Controls
        if (normalized.contains("play music") ||
            normalized.contains("pause music") ||
            normalized.contains("resume music") ||
            normalized.contains("gaana chala") ||
            normalized.contains("gaana roko") ||
            normalized.contains("গান চালাও")
        ) {
            return LocalCommandParseResult(
                matched = true,
                replyText = "Active music session ko Play/Pause signal bhej diya gaya hai. (Note: Kuch third-party apps background media keys restrict kar sakti hain).",
                pendingAction = PendingAction.MediaTransport("PLAY_PAUSE")
            )
        }

        if (normalized.contains("next song") || normalized.contains("next track") || normalized.contains("agla gaana")) {
            return LocalCommandParseResult(
                matched = true,
                replyText = "Next track media command bhej diya gaya hai.",
                pendingAction = PendingAction.MediaTransport("NEXT")
            )
        }

        if (normalized.contains("previous song") || normalized.contains("pichla gaana")) {
            return LocalCommandParseResult(
                matched = true,
                replyText = "Previous track media command bhej diya gaya hai.",
                pendingAction = PendingAction.MediaTransport("PREVIOUS")
            )
        }

        // 12. Open Installed Apps ("Open YouTube", "WhatsApp kholo", "Launch Calculator")
        val openAppRegex = Regex("(?i)^(?:open|launch|start|kholo|chalu karo|chalu koro)\\s+([a-zA-Z0-9 ._-]{2,30})$")
        val openSuffixRegex = Regex("(?i)^([a-zA-Z0-9 ._-]{2,30})\\s+(?:kholo|open karo|chalu karo|kholun|খোলো)$")

        val requestedAppName = openAppRegex.find(normalized)?.groupValues?.get(1)?.trim()
            ?: openSuffixRegex.find(normalized)?.groupValues?.get(1)?.trim()

        if (requestedAppName != null && !requestedAppName.contains("setting")) {
            val cleanQuery = requestedAppName.lowercase(Locale.ROOT)
            val knownPkg = commonAppKeywords.entries.firstOrNull { cleanQuery.contains(it.key) }?.value
            return LocalCommandParseResult(
                matched = true,
                replyText = "'$requestedAppName' ko installed apps mein check karke launch kar rahi hoon...",
                pendingAction = PendingAction.LaunchApp(
                    appNameQuery = requestedAppName,
                    resolvedPackageName = knownPkg,
                    resolvedLabel = requestedAppName.replaceFirstChar { it.uppercase() }
                )
            )
        }

        return LocalCommandParseResult(matched = false)
    }
}
