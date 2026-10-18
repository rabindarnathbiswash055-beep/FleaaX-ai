package com.example.ai

import com.example.data.AssistantLanguage
import com.example.data.EngineSource
import com.example.data.MemoryFactEntity
import com.example.data.PersonalityStyle
import com.example.data.ResponseLengthPreference
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
        offlineReasonNote: String? = null,
        responseLength: ResponseLengthPreference = ResponseLengthPreference.BALANCED,
        customGreeting: String? = null
    ): AssistantReply

    fun generateCodeSnippetByLanguage(
        codingLanguage: String,
        topicOrError: String
    ): AssistantReply

    fun generateGamingCreatorPack(
        gameName: String,
        topicFocus: String
    ): String

    fun summarizeTextLocally(
        title: String,
        rawText: String
    ): String
}

class DeterministicLocalAssistantEngine : LocalAssistantEngine {

    override fun generateLocalReply(
        input: String,
        language: AssistantLanguage,
        personality: PersonalityStyle,
        savedMemories: List<MemoryFactEntity>,
        offlineReasonNote: String?,
        responseLength: ResponseLengthPreference,
        customGreeting: String?
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
            val customHeader = if (!customGreeting.isNullOrBlank()) "$customGreeting\n\n" else ""
            val baseGreeting = when (language) {
                AssistantLanguage.BENGALI ->
                    "${customHeader}নমস্কার ${userName ?: ""}! আমি জিয়া (ZIA AI) — আপনার ব্যক্তিগত ভয়েস, স্টাডি, কোডিং এবং গেমিং অ্যাসিস্ট্যান্ট।"
                AssistantLanguage.BANGLISH ->
                    "${customHeader}Hello $namePrefix! Ami ZIA AI — tomar personal multilingual voice, study, coding aur gaming assistant. Tumi Banglish, Hinglish, বাংলা ba English-e command dite paro."
                AssistantLanguage.ENGLISH ->
                    "${customHeader}Hello $namePrefix! I'm ZIA AI — Your Voice, Your World, Your ZIA. Ready for offline phone commands, study planning, coding snippets, gaming profiles, and smart search."
                else ->
                    "${customHeader}Namaste $namePrefix! Main ZIA AI hoon — aapki friendly multilingual assistant (Voice, Study Planner, Coding Lab, Gaming Hub, aur Knowledge Vault)."
            }
            return AssistantReply(
                replyText = applyPersonalityTone(baseGreeting, personality, offlineReasonNote, responseLength)
            )
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

        // 3. Multi-Language Coding Assistant (Kotlin, JavaScript, HTML, CSS, Python)
        if (lower.contains("javascript") || lower.contains("js code") || lower.contains("fetch")) {
            return generateCodeSnippetByLanguage("JavaScript", input)
        }
        if (lower.contains("html") || lower.contains("css") || lower.contains("webpage")) {
            return generateCodeSnippetByLanguage("HTML/CSS", input)
        }
        if (lower.contains("python")) {
            return generateCodeSnippetByLanguage("Python", input)
        }
        if (lower.contains("compose") || lower.contains("button") || lower.contains("ui code")) {
            return generateCodeSnippetByLanguage("Kotlin Compose", input)
        }
        if (lower.contains("coroutine") || lower.contains("viewmodel") || lower.contains("stateflow") || lower.contains("kotlin")) {
            return generateCodeSnippetByLanguage("Kotlin Coroutines", input)
        }

        // 4. Gaming Companion Queries
        if (lower.contains("gaming") || lower.contains("sensitivity") || lower.contains("bgmi") ||
            lower.contains("free fire") || lower.contains("youtube idea")
        ) {
            val pack = generateGamingCreatorPack("Mobile Tactical FPS", input)
            return AssistantReply(
                replyText = applyPersonalityTone(pack, personality, offlineReasonNote, responseLength)
            )
        }

        // 5. Study & Focus Queries
        if (lower.contains("study") || lower.contains("pomodoro") || lower.contains("focus") || lower.contains("exam")) {
            val studyTip = "Study Buddy Tip (Hinglish):\n" +
                "1. 25-Minute Pomodoro Focus blocks use karein aur har block ke baad 5 minute aankhon ko rest dein.\n" +
                "2. Active Recall: Topic padhne ke baad bina dekhe 3 key points Knowledge Vault mein likhein.\n" +
                "3. Workspace -> Planner & Focus tab mein apna daily schedule aur timer start karein!"
            return AssistantReply(
                replyText = applyPersonalityTone(studyTip, personality, offlineReasonNote, responseLength)
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

        // 6. Memory Inquiry
        if (lower.contains("what do you remember") ||
            lower.contains("kya yaad hai") ||
            lower.contains("my memory") ||
            lower.contains("saved preferences")
        ) {
            val summary = if (savedMemories.isEmpty()) {
                "Abhi aapki local memory khaali hai. Aap bol sakte hain: 'Remember my name is Aarav' ya Workspace -> Smart Memory mein add kar sakte hain."
            } else {
                savedMemories.joinToString("\n") { "• [${it.category}] ${it.key}: ${it.value}" }
            }
            return AssistantReply(
                replyText = "${namePrefix}Aapke phone par locally saved preferences yeh hain:\n$summary"
            )
        }

        // 7. Transparent Offline Fallback Explanation
        val note = offlineReasonNote ?: "Abhi ZIA Local Assistant Engine use kar rahi hai."
        val fallbackReply = when (language) {
            AssistantLanguage.BENGALI ->
                "$note\n\nঅফলাইন মোডে আমি এই কাজগুলো तुरंत করতে পারি:\n• ফোনের কমান্ড: \"ভলিউম ৭০% করো\", \"৭:৩০ এ অ্যালার্ম লাগাও\", \"ইউটিউব খোলো\"\n• স্টাডি প্ল্যানার ও ফোকাস টাইমার (Workspace ট্যাব)\n• কোডিং ল্যাব (Kotlin, JS, HTML, CSS স্নিপেট)\n• গেমিং প্রোফাইল ও নলেজ ভল্ট সার্চ"
            AssistantLanguage.BANGLISH ->
                "$note\n\nOffline mode-e ami tomar phone commands aur Workspace features instantly run korte pari:\n• \"Volume 70% koro\", \"7:30 ka alarm lagao\", \"Open YouTube\"\n• Workspace -> Planner & Focus Timer, Coding Lab (Kotlin/JS/HTML/CSS), Gaming Hub, aur Knowledge Vault."
            else ->
                "$note\n\nOffline rehte hue bhi main yeh kaam turant kar sakti hoon:\n" +
                    "• Phone Controls: \"Volume 70% karo\", \"7:30 AM ka alarm lagao\", \"Open YouTube\", \"Wi-Fi settings kholo\"\n" +
                    "• Workspace Hub: Daily Study Planner & Focus Timer, Coding Lab (Kotlin, JS, HTML, CSS), Gaming Companion, aur Knowledge Vault Search."
        }

        return AssistantReply(
            replyText = applyPersonalityTone(fallbackReply, personality, null, responseLength)
        )
    }

    override fun generateCodeSnippetByLanguage(
        codingLanguage: String,
        topicOrError: String
    ): AssistantReply {
        val langUpper = codingLanguage.uppercase(Locale.ROOT)
        return when {
            langUpper.contains("JS") || langUpper.contains("JAVASCRIPT") -> {
                val snippet = """
                    // Safe Async Fetch with Timeout & Error Handling (JavaScript)
                    async function fetchWithTimeout(url, timeoutMs = 8000) {
                      const controller = new AbortController();
                      const timer = setTimeout(() => controller.abort(), timeoutMs);
                      try {
                        const response = await fetch(url, { signal: controller.signal });
                        if (!response.ok) throw new Error(`HTTP Status: ${'$'}{response.status}`);
                        return await response.json();
                      } catch (err) {
                        console.error("Network or Timeout Error:", err.message);
                        return null;
                      } finally {
                        clearTimeout(timer);
                      }
                    }
                """.trimIndent()
                AssistantReply(
                    replyText = "JavaScript (`async/await` + `AbortController`) ka clean snippet jo network failures aur timeouts ko safely handle karta hai:",
                    codeSnippet = snippet
                )
            }
            langUpper.contains("HTML") || langUpper.contains("CSS") -> {
                val snippet = """
                    <!-- Responsive Futuristic Card (HTML5 + CSS3) -->
                    <style>
                      .zia-card {
                        background: linear-gradient(145deg, #121726, #1A2136);
                        border: 1px solid #283252;
                        border-radius: 16px;
                        padding: 1.25rem;
                        color: #F3F6FF;
                        font-family: system-ui, sans-serif;
                        max-width: 420px;
                      }
                      .zia-badge {
                        color: #22D3EE;
                        font-size: 0.75rem;
                        letter-spacing: 0.08em;
                        text-transform: uppercase;
                      }
                    </style>
                    <article class="zia-card">
                      <span class="zia-badge">ZIA AI Component</span>
                      <h3>Responsive Dark UI</h3>
                      <p>Clean mobile-first layout ready for Acode or web preview.</p>
                    </article>
                """.trimIndent()
                AssistantReply(
                    replyText = "Yeh raha responsive HTML5 + CSS3 dark glassmorphic card template jo aap seedha copy ya Code Library mein save kar sakte hain:",
                    codeSnippet = snippet
                )
            }
            langUpper.contains("PYTHON") -> {
                val snippet = """
                    # Clean Data Filter & Summary Helper (Python 3)
                    from typing import List, Dict

                    def summarize_tasks(tasks: List[Dict[str, object]]) -> str:
                        completed = [t["title"] for t in tasks if t.get("done")]
                        pending = [t["title"] for t in tasks if not t.get("done")]
                        return f"Completed: {len(completed)} | Pending: {len(pending)}"

                    if __name__ == "__main__":
                        sample = [{"title": "Study Kotlin", "done": True}, {"title": "Revise DSA", "done": False}]
                        print(summarize_tasks(sample))
                """.trimIndent()
                AssistantReply(
                    replyText = "Python 3 type-annotated function snippet niche diya gaya hai:",
                    codeSnippet = snippet
                )
            }
            langUpper.contains("COROUTINE") -> {
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
                AssistantReply(
                    replyText = "Kotlin Coroutines aur `StateFlow` ke saath Android `ViewModel` likhne ka best-practice pattern (`Dispatchers.IO` background safety ke saath):",
                    codeSnippet = snippet
                )
            }
            else -> {
                val snippet = """
                    @Composable
                    fun ZiaActionCard(
                        title: String,
                        subtitle: String,
                        onActionClick: () -> Unit
                    ) {
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.padding(16.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(text = title, style = MaterialTheme.typography.titleMedium)
                                    Text(text = subtitle, style = MaterialTheme.typography.bodySmall)
                                }
                                Button(onClick = onActionClick) {
                                    Text("Run")
                                }
                            }
                        }
                    }
                """.trimIndent()
                AssistantReply(
                    replyText = "Jetpack Compose Material 3 card aur state-hoisted button ka clean Kotlin code snippet:",
                    codeSnippet = snippet
                )
            }
        }
    }

    override fun generateGamingCreatorPack(
        gameName: String,
        topicFocus: String
    ): String {
        val cleanGame = gameName.ifBlank { "BGMI / Mobile Gaming" }
        val cleanTopic = topicFocus.ifBlank { "Zero Recoil Sensitivity & 60 FPS Smooth Guide" }
        return """
            🎮 Gaming Companion & YouTube Creator Pack ($cleanGame)
            
            1. Video Title Ideas:
               • "$cleanGame on Redmi 9 (6GB RAM): $cleanTopic [2026 Tested]"
               • "Best 4-Finger Claw + Gyro Sensitivity for $cleanGame (No Lag Setup)"
               • "Top 5 Clutch Mistakes to Avoid in Final Zone — $cleanGame Guide"
            
            2. SEO Video Description Template:
               "Is video mein humne $cleanGame ke liye $cleanTopic test kiya hai on Android 10 (6GB RAM).
               Timestamps:
               00:00 - Device & Graphic Settings
               01:45 - Camera & ADS Sensitivity Breakdown
               03:30 - Gyroscope Drills & Pre-Match Checklist"
            
            3. Thumbnail Concept:
               • Left side: High-contrast character close-up with cyan/violet rim glow.
               • Right side: Large bold yellow/white text "ZERO LAG + NEW SENSI" with a green "60 FPS STABLE" badge.
            
            4. Fair-Play Note:
               ZIA sirf legitimate sensitivity, hardware optimization, aur tactical tips deti hai (No auto-aim or anti-cheat bypasses).
        """.trimIndent()
    }

    override fun summarizeTextLocally(title: String, rawText: String): String {
        val cleaned = rawText.trim()
        if (cleaned.isEmpty()) return "Empty document content."
        val lines = cleaned.lines().map { it.trim() }.filter { it.isNotEmpty() }
        val wordCount = cleaned.split(Regex("\\s+")).size
        val keyPoints = lines.take(5).joinToString("\n") { "• ${it.take(140)}" }
        return "Offline Local Summary for '$title' ($wordCount words, ${lines.size} non-empty lines):\n$keyPoints"
    }

    private fun applyPersonalityTone(
        base: String,
        personality: PersonalityStyle,
        extraNote: String?,
        responseLength: ResponseLengthPreference
    ): String {
        val prefix = when (personality) {
            PersonalityStyle.FRIENDLY -> ""
            PersonalityStyle.PROFESSIONAL -> "[Professional] "
            PersonalityStyle.FUNNY -> "[Witty Mode 😄] "
            PersonalityStyle.STUDY_BUDDY -> "[Study Buddy 📚] "
            PersonalityStyle.GAMING_ASSISTANT -> "[Gaming Coach 🎮] "
            PersonalityStyle.CONCISE -> ""
            PersonalityStyle.PLAYFUL -> "✨ "
        }
        val trimmedBase = if (responseLength == ResponseLengthPreference.SHORT || personality == PersonalityStyle.CONCISE) {
            base.lines().take(4).joinToString("\n")
        } else {
            base
        }
        val suffix = if (!extraNote.isNullOrBlank()) "\n\n($extraNote)" else ""
        return "$prefix$trimmedBase$suffix"
    }
}
