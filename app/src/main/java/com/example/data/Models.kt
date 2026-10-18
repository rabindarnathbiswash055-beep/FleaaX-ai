package com.example.data

import kotlinx.serialization.Serializable

enum class AvatarState(
    val label: String,
    val hinglishStatus: String
) {
    IDLE("Idle · Ready", "ZIA ready hai — Push-to-Talk dabayein ya type karein"),
    LISTENING("Listening...", "Sun rahi hoon... boliye (Tap Stop anytime)"),
    THINKING("Thinking...", "Samajh rahi hoon..."),
    SPEAKING("Speaking...", "ZIA bol rahi hai..."),
    UNAVAILABLE("Speech Unavailable", "Voice service unavailable — Text mode active hai")
}

enum class AssistantLanguage(
    val code: String,
    val displayName: String,
    val speechLocaleTag: String,
    val ttsLocaleTag: String,
    val samplePrompt: String
) {
    AUTO_MIXED(
        code = "AUTO",
        displayName = "Auto / Mixed (EN + HI + BN)",
        speechLocaleTag = "en-IN",
        ttsLocaleTag = "en-IN",
        samplePrompt = "ZIA, 7:30 AM ka alarm lagao aur Kotlin coroutine samjhao"
    ),
    ENGLISH(
        code = "EN",
        displayName = "English (India)",
        speechLocaleTag = "en-IN",
        ttsLocaleTag = "en-IN",
        samplePrompt = "Open YouTube or set media volume to 60%"
    ),
    HINGLISH(
        code = "HINGLISH",
        displayName = "Hinglish (Hindi + English)",
        speechLocaleTag = "en-IN",
        ttsLocaleTag = "hi-IN",
        samplePrompt = "ZIA, mera naam yaad rakhna aur Wi-Fi settings kholo"
    ),
    BANGLISH(
        code = "BANGLISH",
        displayName = "Banglish (Bengali + English)",
        speechLocaleTag = "en-IN",
        ttsLocaleTag = "bn-IN",
        samplePrompt = "ZIA, kemon acho? Amake 8:00 AM e alarm set kore dao"
    ),
    BENGALI(
        code = "BN",
        displayName = "বাংলা (Bengali)",
        speechLocaleTag = "bn-IN",
        ttsLocaleTag = "bn-IN",
        samplePrompt = "জিয়া, ভলিউম বাড়াও এবং আজকের তারিখ বলো"
    )
}

enum class PersonalityStyle(
    val title: String,
    val badge: String,
    val descriptionHinglish: String,
    val systemPromptDirective: String
) {
    FRIENDLY(
        title = "Friendly",
        badge = "Warm & Helpful",
        descriptionHinglish = "Dostana aur asaan Hinglish/Banglish/English style.",
        systemPromptDirective = "Be warm, encouraging, friendly, and conversational. Explain technical steps clearly in beginner-friendly Hinglish/Banglish/English matching the user's language."
    ),
    PROFESSIONAL(
        title = "Professional",
        badge = "Structured & Clear",
        descriptionHinglish = "Seedha, polite aur professional jawab.",
        systemPromptDirective = "Be structured, polite, accurate, and professional. Provide clear bullet points and well-organized guidance."
    ),
    CONCISE(
        title = "Concise",
        badge = "Short & Fast",
        descriptionHinglish = "Bina extra baaton ke chhota aur fast jawab.",
        systemPromptDirective = "Keep every answer brief, direct, and under 3 short sentences unless code is explicitly requested."
    ),
    PLAYFUL(
        title = "Playful",
        badge = "Witty & Energetic",
        descriptionHinglish = "Halka-phulka, energetic aur creative style.",
        systemPromptDirective = "Be upbeat, witty, energetic, and charming while remaining respectful, accurate, and privacy-conscious."
    )
}

enum class EngineSource(val label: String) {
    LOCAL_COMMAND("Local Command Engine (Offline)"),
    LOCAL_ASSISTANT("On-Device Assistant (Offline)"),
    GEMINI_CLOUD("Gemini Cloud AI (Online)"),
    SYSTEM_SECURITY("Privacy & Security Guard")
}

@Serializable
sealed class PendingAction {
    abstract val actionTitle: String
    abstract val summaryHinglish: String
    abstract val permissionNote: String
    abstract val requiresConfirmation: Boolean

    @Serializable
    data class LaunchApp(
        val appNameQuery: String,
        val resolvedPackageName: String? = null,
        val resolvedLabel: String? = null,
        override val actionTitle: String = "Open Installed App",
        override val summaryHinglish: String = "Launch '$appNameQuery' via Android PackageManager intent.",
        override val permissionNote: String = "Uses standard launcher intent; no private app data is accessed.",
        override val requiresConfirmation: Boolean = false
    ) : PendingAction()

    @Serializable
    data class DialPhone(
        val phoneNumber: String,
        val contactLabel: String = "",
        override val actionTitle: String = "Open Phone Dialer",
        override val summaryHinglish: String = "Dialer mein number '$phoneNumber' prefill karega. Call tabhi lagegi jab aap Dialer mein Call button dabayenge.",
        override val permissionNote: String = "Uses ACTION_DIAL (Zero direct call permission needed; requires your confirmation).",
        override val requiresConfirmation: Boolean = true
    ) : PendingAction()

    @Serializable
    data class ComposeSms(
        val phoneNumber: String,
        val messageBody: String,
        override val actionTitle: String = "Compose SMS Message",
        override val summaryHinglish: String = "SMS app khol kar '$phoneNumber' ke liye message prefill karega: \"$messageBody\"",
        override val permissionNote: String = "Uses ACTION_SENDTO (smsto:); message is not sent silently.",
        override val requiresConfirmation: Boolean = true
    ) : PendingAction()

    @Serializable
    data class SetAlarm(
        val hour24: Int,
        val minute: Int,
        val label: String,
        override val actionTitle: String = "Set System Alarm",
        override val summaryHinglish: String = "System Clock app mein ${String.format("%02d:%02d", hour24, minute)} ka alarm ('$label') set karega.",
        override val permissionNote: String = "Uses AlarmClock.ACTION_SET_ALARM permission.",
        override val requiresConfirmation: Boolean = true
    ) : PendingAction()

    @Serializable
    data class AdjustVolume(
        val streamType: String, // MEDIA, RING, ALARM, NOTIFICATION
        val targetPercent: Int, // 0..100 or -1 for step up, -2 for step down
        override val actionTitle: String = "Adjust Device Volume",
        override val summaryHinglish: String = "$streamType volume ko update karega.",
        override val permissionNote: String = "Uses Android AudioManager stream controls.",
        override val requiresConfirmation: Boolean = false
    ) : PendingAction()

    @Serializable
    data class OpenSystemSettings(
        val panelType: String, // WIFI, BLUETOOTH, SOUND, DISPLAY, NOTIFICATION_LISTENER, APP_DETAILS
        override val actionTitle: String = "Open System Settings Panel",
        override val summaryHinglish: String = "Android $panelType settings panel open karega (Android 10+ par silent background Wi-Fi/BT toggle restricted hai).",
        override val permissionNote: String = "Opens official Android Settings panel for user control.",
        override val requiresConfirmation: Boolean = false
    ) : PendingAction()

    @Serializable
    data class LaunchCamera(
        override val actionTitle: String = "Open Camera",
        override val summaryHinglish: String = "Android Camera app launch karega. ZIA bina aapke action ke photo capture nahi karti.",
        override val permissionNote: String = "Uses MediaStore.ACTION_IMAGE_CAPTURE intent with visible UI.",
        override val requiresConfirmation: Boolean = true
    ) : PendingAction()

    @Serializable
    data class MediaTransport(
        val command: String, // PLAY_PAUSE, NEXT, PREVIOUS, STOP
        override val actionTitle: String = "Media Playback Control",
        override val summaryHinglish: String = "Active music player ko '$command' media key bhejega.",
        override val permissionNote: String = "Uses AudioManager dispatchMediaKeyEvent.",
        override val requiresConfirmation: Boolean = false
    ) : PendingAction()

    @Serializable
    data class ClearAllMemory(
        override val actionTitle: String = "Wipe All Local Memory & History",
        override val summaryHinglish: String = "ZIA ke saare saved preferences, reminders aur chat history phone se permanently delete ho jayenge.",
        override val permissionNote: String = "Irreversible local database reset.",
        override val requiresConfirmation: Boolean = true
    ) : PendingAction()
}

data class InstalledAppItem(
    val label: String,
    val packageName: String
)

data class SelectedFileInfo(
    val displayName: String,
    val mimeType: String,
    val sizeBytes: Long,
    val previewContent: String
)

data class NotificationSummary(
    val id: String,
    val packageName: String,
    val appTitle: String,
    val contentPreview: String,
    val timestamp: Long
)
