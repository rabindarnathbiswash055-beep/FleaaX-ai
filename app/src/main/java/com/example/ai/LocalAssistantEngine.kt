package com.example.ai

import com.example.data.AssistantLanguage
import com.example.data.EngineSource
import com.example.data.MemoryFactEntity
import com.example.data.PersonalityStyle
import java.util.Locale

data class AssistantReply(
    val replyText: String,
    val codeSnippet: String? = null,
    val sourceEngine: EngineSource = EngineSource.LOCAL_ASSISTANT
)

interface LocalAssistantEngine {
    fun generateLocalReply(
        input: String,
        language: AssistantLanguage,
        personality: PersonalityStyle,
        savedMemories: List<MemoryFactEntity>,
        offlineReasonNote: String? = null
    ): AssistantReply
}

class DeterministicLocalAssistantEngine : LocalAssistantEngine {

    override fun generateLocalReply(
        input: String,
        language: AssistantLanguage,
        personality: PersonalityStyle,
        savedMemories: List<MemoryFactEntity>,
        offlineReasonNote: String?
    ): AssistantReply {
        val lower = input.trim().lowercase(Locale.ROOT)
        val userName = savedMemories.firstOrNull {
            it.key.equals("User Name", ignoreCase = true) || it.key.contains("name", ignoreCase = true)
        }?.value

        val namePrefix = if (!userName.isNullOrBlank()) "$userName, " else ""

        // 1. Multilingual Greetings & Identity
        if (lower in listOf("hi", "hello", "hey", "namaste", "salam", "nomoshkar", "hlw") ||
            lower.contains("kemon acho") ||
            lower.contains("kaisi ho") ||
            lower.contains("how are you") ||
            lower.contains("কেমন আছো") ||
            lower.contains("तुम कौन हो") ||
            lower.contains("who are you")
        ) {
            val baseGreeting = when (language) {
                AssistantLanguage.BENGALI ->
                    "নমস্কার ${userName ?: ""}! আমি জিয়া (ZIA AI) — আপনার ব্যক্তিগত ভয়েস অ্যাসিস্ট্যান্ট। আমি অফলাইনে ফোনের কমান্ড (অ্যালার্ম, ভলিউম, অ্যাপ খোলা) এবং অনলাইনে কোডিং ও প্রশ্নের উত্তর দিতে পারি।"
                AssistantLanguage.BANGLISH ->
                    "Hello $namePrefix! Ami bhalo achi! Ami ZIA AI — tomar personal multilingual voice assistant. Tumi amake Banglish, Hinglish, বাংলা ba English-e phone commands ba coding প্রশ্ন korte paro."
                AssistantLanguage.ENGLISH ->
                    "Hello $namePrefix! I'm ZIA AI — Your Voice, Your World, Your ZIA. I'm ready for offline phone commands, reminders, volume control, and hybrid AI coding help."
                else ->
                    "Namaste $namePrefix! Main bilkul theek hoon! Main ZIA AI hoon — aapki friendly multilingual assistant. Aap mujhse offline phone commands ('7:30 ka alarm lagao', 'Volume 60% karo', 'Open YouTube') ya coding questions puch sakte hain."
            }
            return AssistantReply(replyText = applyPersonalityTone(baseGreeting, personality, offlineReasonNote))
        }

        // 2. Simple Arithmetic / Math Evaluation Offline
        val mathMatch = Regex("(\\d+(?:\\.\\d+)?)\\s*([+\\-*/x×÷])\\s*(\\d+(?:\\.\\d+)?)").find(lower)
        if (mathMatch != null) {
            val a = mathMatch.groupValues[1].toDoubleOrNull()
            val op = mathMatch.groupValues[2]
            val b = mathMatch.groupValues[3].toDoubleOrNull()
            if (a != null && b != null) {
                val res = when (op) {
                    "+" -> a + b
                    "-" -> a - b
                    "*", "x", "×" -> a * b
                    "/", "÷" -> if (b != 0.0) a / b else Double.NaN
                    else -> Double.NaN
                }
                val formatted = if (res % 1.0 == 0.0) res.toLong().toString() else String.format(Locale.US, "%.4f", res)
                return AssistantReply(
                    replyText = "${namePrefix}Local Math Engine result: $a $op $b = $formatted (Calculated 100% offline)."
                )
            }
        }

        // 3. Offline Coding Helper Snippets (Kotlin, Compose, Coroutines, Room, Python)
        if (lower.contains("compose") || lower.contains("button") || lower.contains("ui code")) {
            val snippet = """
                @Composable
                fun ZiaActionCard(
                    title: String,
                    onActionClick: () -> Unit
                ) {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        Row(
                            modifier = Modifier.padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(text = title, modifier = Modifier.weight(1f))
                            Button(onClick = onActionClick) {
                                Text("Run")
                            }
                        }
                    }
                }
            """.trimIndent()
            return AssistantReply(
                replyText = "${namePrefix}Yeh raha Jetpack Compose Material 3 card aur button ka clean Kotlin code snippet. Isme state hoisting use kiya gaya hai taaki UI reusable rahe:",
                codeSnippet = snippet
            )
        }

        if (lower.contains("coroutine") || lower.contains("viewmodel") || lower.contains("stateflow")) {
            val snippet = """
                class AssistantViewModel(
                    private val repository: DataRepository
                ) : ViewModel() {

                    private val _uiState = MutableStateFlow("Idle")
                    val uiState: StateFlow<String> = _uiState.asStateFlow()

                    fun loadSafely() {
                        viewModelScope.launch(Dispatchers.IO) {
                            _uiState.value = "Loading..."
                            val result = repository.fetchLocalData()
                            _uiState.value = result
                        }
                    }
                }
            """.trimIndent()
            return AssistantReply(
                replyText = "${namePrefix}Kotlin Coroutines aur `StateFlow` ke saath Android `ViewModel` likhne ka best-practice pattern niche diya gaya hai (`Dispatchers.IO` background kaam ke liye):",
                codeSnippet = snippet
            )
        }

        if (lower.contains("acode") || lower.contains("build apk") || lower.contains("mobile build") || lower.contains("redmi")) {
            return AssistantReply(
                replyText = "Phone-Only Build & Redmi 9 Guidance:\n" +
                    "1. Acode ek lightweight code editor hai, lekin usme full Android SDK, JDK 17, aur Gradle daemon built-in nahi hota.\n" +
                    "2. Isliye ZIA AI ko is Cloud Native Android Build Environment mein full Kotlin + Jetpack Compose + Room + Retrofit ke saath compile kiya gaya hai.\n" +
                    "3. Aap seedha settings menu se compiled installable APK export/install kar sakte hain—kisi PC ki zaroorat nahi hai!"
            )
        }

        // 4. Memory Inquiry ("What do you remember about me?", "Meri memory dikhao")
        if (lower.contains("what do you remember") ||
            lower.contains("kya yaad hai") ||
            lower.contains("my memory") ||
            lower.contains("saved preferences")
        ) {
            val summary = if (savedMemories.isEmpty()) {
                "Abhi aapki local memory khaali hai. Aap bol sakte hain: 'Remember my name is Aarav' ya Memory tab mein add kar sakte hain."
            } else {
                savedMemories.joinToString("\n") { "• ${it.key}: ${it.value}" }
            }
            return AssistantReply(
                replyText = "${namePrefix}Aapke phone par locally saved preferences yeh hain:\n$summary"
            )
        }

        // 5. Transparent Offline Fallback Explanation
        val note = offlineReasonNote
            ?: "Abhi ZIA Local Assistant Engine use kar rahi hai."
        val fallbackReply = when (language) {
            AssistantLanguage.BENGALI ->
                "$note\n\nঅফলাইন মোডে আমি এই কাজগুলো तुरंत করতে পারি:\n• \"ভলিউম ৭০% করো\" বা \"মিউট করো\"\n• \"৭:৩০ এ অ্যালার্ম লাগাও\"\n• \"ইউটিউব খোলো\" বা \"ওয়াইফাই সেটিংস খোলো\"\n• \"রিমাইন্ডার সেট করো\"\n\nজটিল প্রশ্ন বা কোডিং এর জন্য ইন্টারনেট চালু করে Cloud AI Consent অন করুন।"
            AssistantLanguage.BANGLISH ->
                "$note\n\nOffline mode-e ami tomar phone commands instantly run korte pari:\n• \"Volume 70% koro\" ba \"Mute volume\"\n• \"7:30 ka alarm lagao\"\n• \"Open YouTube\" ba \"Wi-Fi settings kholo\"\n• \"Remind me to revise Kotlin\"\n• \"Write Compose button code\"\n\nAdvanced AI reasoning er jonno internet on rekhe Cloud AI enable koro."
            else ->
                "$note\n\nOffline rehte hue bhi main yeh kaam turant kar sakti hoon:\n" +
                    "• Volume Control: \"Volume 70% karo\" ya \"Ring volume kam karo\"\n" +
                    "• System Alarms & Reminders: \"7:30 AM ka alarm lagao\" ya \"Remind me to call Riya\"\n" +
                    "• App Launcher & Panels: \"Open YouTube\", \"Wi-Fi settings kholo\", \"Bluetooth kholo\"\n" +
                    "• Calls & SMS Preview: \"Call 9876543210\" ya \"Send SMS to 9876543210 Reaching soon\"\n" +
                    "• Offline Coding Snippets: \"Show Kotlin Coroutine code\" ya \"Show Compose UI code\""
        }

        return AssistantReply(replyText = applyPersonalityTone(fallbackReply, personality, null))
    }

    private fun applyPersonalityTone(
        base: String,
        personality: PersonalityStyle,
        extraNote: String?
    ): String {
        val prefix = when (personality) {
            PersonalityStyle.FRIENDLY -> ""
            PersonalityStyle.PROFESSIONAL -> "[Professional Mode] "
            PersonalityStyle.CONCISE -> ""
            PersonalityStyle.PLAYFUL -> "✨ "
        }
        val suffix = if (!extraNote.isNullOrBlank()) "\n\n($extraNote)" else ""
        return "$prefix$base$suffix"
    }
}
