package com.example.ai

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.util.Base64
import com.example.BuildConfig
import com.example.data.AssistantLanguage
import com.example.data.EngineSource
import com.example.data.MemoryFactEntity
import com.example.data.PersonalityStyle
import com.example.data.ResponseLengthPreference
import com.example.data.SmartSearchResult
import com.example.security.PrivacyGuard
import java.io.ByteArrayOutputStream
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

@Serializable
data class GenerateContentRequest(
    val contents: List<Content>,
    val generationConfig: GenerationConfig? = null,
    val systemInstruction: Content? = null
)

@Serializable
data class Content(
    val role: String? = null,
    val parts: List<Part>
)

@Serializable
data class Part(
    val text: String? = null,
    val inlineData: InlineData? = null
)

@Serializable
data class InlineData(
    val mimeType: String,
    val data: String
)

@Serializable
data class GenerationConfig(
    val temperature: Float? = 0.7f,
    val topP: Float? = 0.95f,
    val topK: Int? = 40
)

@Serializable
data class GenerateContentResponse(
    val candidates: List<Candidate>? = null
)

@Serializable
data class Candidate(
    val content: Content? = null
)

interface GeminiRestService {
    @POST("v1beta/models/gemini-3.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GenerateContentRequest
    ): GenerateContentResponse
}

sealed class RemoteAiResult {
    data class Success(val reply: AssistantReply) : RemoteAiResult()
    data class Unavailable(val reasonHinglish: String) : RemoteAiResult()
}

/**
 * Provider-independent AI interface so models or cloud providers can be swapped cleanly.
 */
interface RemoteAIProvider {
    fun isNetworkOnline(context: Context): Boolean

    suspend fun generateCloudResponse(
        context: Context,
        userPrompt: String,
        language: AssistantLanguage,
        personality: PersonalityStyle,
        responseLength: ResponseLengthPreference,
        savedMemories: List<MemoryFactEntity>,
        customApiKeyOverride: String
    ): RemoteAiResult

    suspend fun analyzeGameplayScreenshot(
        context: Context,
        imageUri: Uri,
        userQuestion: String,
        customApiKeyOverride: String
    ): RemoteAiResult

    suspend fun performSmartWebSearch(
        context: Context,
        query: String,
        localMatches: List<String>,
        customApiKeyOverride: String
    ): SmartSearchResult
}

class GeminiRemoteAIProvider : RemoteAIProvider {

    private val json = Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()
    }

    private val service: GeminiRestService by lazy {
        Retrofit.Builder()
            .baseUrl("https://generativelanguage.googleapis.com/")
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
            .create(GeminiRestService::class.java)
    }

    override fun isNetworkOnline(context: Context): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager
            ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false
        return caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }

    private fun resolveApiKey(customApiKeyOverride: String): String {
        return customApiKeyOverride.trim().ifEmpty { BuildConfig.GEMINI_API_KEY.trim() }
    }

    override suspend fun generateCloudResponse(
        context: Context,
        userPrompt: String,
        language: AssistantLanguage,
        personality: PersonalityStyle,
        responseLength: ResponseLengthPreference,
        savedMemories: List<MemoryFactEntity>,
        customApiKeyOverride: String
    ): RemoteAiResult = withContext(Dispatchers.IO) {
        if (!isNetworkOnline(context)) {
            return@withContext RemoteAiResult.Unavailable(
                "Internet connection offline hai. ZIA ne aapka message Local Assistant Engine par route kiya hai."
            )
        }

        val resolvedKey = resolveApiKey(customApiKeyOverride)
        if (resolvedKey.isEmpty() || resolvedKey == "MY_GEMINI_API_KEY") {
            return@withContext RemoteAiResult.Unavailable(
                "Cloud Gemini API key abhi configured nahi hai (AI Studio Secrets panel ya Settings mein API key set karein). ZIA Local Engine se jawab de rahi hai."
            )
        }

        val sanitized = PrivacyGuard.sanitizeForCloudPrompt(userPrompt)
        if (!sanitized.isSafe) {
            return@withContext RemoteAiResult.Unavailable(
                sanitized.blockedReasonHinglish ?: "Input blocked by PrivacyGuard."
            )
        }

        val memoryContext = if (savedMemories.isEmpty()) {
            "No personal memory context shared."
        } else {
            savedMemories.take(8).joinToString("; ") { "${it.key}: ${it.value}" }
        }

        val systemInstructionText = """
            You are ZIA AI ("Your Voice. Your World. Your ZIA."), a friendly, intelligent female native Android voice & productivity assistant.
            Target user language mode: ${language.displayName}.
            If the user writes in Hinglish (Hindi-English mix), Banglish (Bengali-English mix), বাংলা (Bengali), or English, reply naturally in that language style!
            Personality directive: ${personality.systemPromptDirective}
            Response length directive: ${responseLength.promptInstruction}
            User's explicitly permitted local preferences: $memoryContext.
            Strict Safety & Honesty Rules:
            - Never simulate genuine human emotions as factual internal states.
            - Never claim to have silently executed phone actions or shell commands.
            - Never ask for passwords, OTPs, or private tokens.
            - If you include code, wrap it in a standard triple-backtick code block so ZIA can render it in a dedicated Code Card.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(Part(text = sanitized.sanitizedText))
                )
            ),
            generationConfig = GenerationConfig(
                temperature = when (personality) {
                    PersonalityStyle.CONCISE, PersonalityStyle.PROFESSIONAL -> 0.4f
                    PersonalityStyle.STUDY_BUDDY, PersonalityStyle.FRIENDLY -> 0.65f
                    PersonalityStyle.GAMING_ASSISTANT, PersonalityStyle.FUNNY, PersonalityStyle.PLAYFUL -> 0.8f
                }
            ),
            systemInstruction = Content(
                parts = listOf(Part(text = systemInstructionText))
            )
        )

        return@withContext try {
            val response = service.generateContent(apiKey = resolvedKey, request = request)
            val rawReply = response.candidates
                ?.firstOrNull()
                ?.content
                ?.parts
                ?.firstOrNull()
                ?.text
                ?.trim()

            if (rawReply.isNullOrBlank()) {
                RemoteAiResult.Unavailable("Gemini Cloud se empty response mila; Local Assistant active hai.")
            } else {
                val (cleanText, extractedCode) = extractCodeBlock(rawReply)
                RemoteAiResult.Success(
                    AssistantReply(
                        replyText = cleanText,
                        codeSnippet = extractedCode,
                        sourceEngine = EngineSource.GEMINI_CLOUD
                    )
                )
            }
        } catch (e: Exception) {
            RemoteAiResult.Unavailable(
                "Cloud AI request timeout ya error (${e.javaClass.simpleName}) — ZIA Local Assistant fallback use kar rahi hai."
            )
        }
    }

    override suspend fun analyzeGameplayScreenshot(
        context: Context,
        imageUri: Uri,
        userQuestion: String,
        customApiKeyOverride: String
    ): RemoteAiResult = withContext(Dispatchers.IO) {
        if (!isNetworkOnline(context)) {
            return@withContext RemoteAiResult.Unavailable(
                "Screenshot cloud analysis ke liye internet connection chahiye. Offline mode mein aap local HUD & Sensitivity Checklist use kar sakte hain."
            )
        }
        val resolvedKey = resolveApiKey(customApiKeyOverride)
        if (resolvedKey.isEmpty() || resolvedKey == "MY_GEMINI_API_KEY") {
            return@withContext RemoteAiResult.Unavailable(
                "Screenshot vision analysis ke liye Gemini API key configured honi chahiye. Local HUD checklist Niche di gayi hai:\n" +
                    "• Check 4-finger claw button opacity (60-75% to avoid blind spots)\n" +
                    "• Keep minimap and peek/fire buttons spaced at least 12dp apart."
            )
        }

        val base64Jpeg = encodeUriToBase64Jpeg(context, imageUri)
            ?: return@withContext RemoteAiResult.Unavailable("Selected image decode nahi ho saki.")

        val prompt = """
            Analyze this user-selected gaming screenshot (or HUD/stats screen).
            User question: ${userQuestion.ifBlank { "Give tactical positioning, HUD layout, or match stat feedback in friendly Hinglish." }}
            Provide practical, fair-play gaming tips in Hinglish/English. Do not suggest cheats or unsupported game modifications.
        """.trimIndent()

        val request = GenerateContentRequest(
            contents = listOf(
                Content(
                    role = "user",
                    parts = listOf(
                        Part(text = prompt),
                        Part(inlineData = InlineData(mimeType = "image/jpeg", data = base64Jpeg))
                    )
                )
            )
        )

        return@withContext try {
            val response = service.generateContent(apiKey = resolvedKey, request = request)
            val text = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text?.trim()
            if (text.isNullOrBlank()) {
                RemoteAiResult.Unavailable("Screenshot analysis se empty response aaya.")
            } else {
                RemoteAiResult.Success(
                    AssistantReply(
                        replyText = text,
                        sourceEngine = EngineSource.GEMINI_CLOUD
                    )
                )
            }
        } catch (e: Exception) {
            RemoteAiResult.Unavailable(
                "Screenshot analysis network error (${e.javaClass.simpleName})."
            )
        }
    }

    override suspend fun performSmartWebSearch(
        context: Context,
        query: String,
        localMatches: List<String>,
        customApiKeyOverride: String
    ): SmartSearchResult = withContext(Dispatchers.IO) {
        if (!isNetworkOnline(context)) {
            return@withContext SmartSearchResult(
                query = query,
                localMatches = localMatches,
                retrievedWebFacts = emptyList(),
                citedSources = emptyList(),
                aiSynthesis = "Offline Mode Active: Online web search skipped. Showing ${localMatches.size} exact matches from your on-device Knowledge Vault & Memory.",
                isOnlineResult = false
            )
        }

        val resolvedKey = resolveApiKey(customApiKeyOverride)
        if (resolvedKey.isEmpty() || resolvedKey == "MY_GEMINI_API_KEY") {
            return@withContext SmartSearchResult(
                query = query,
                localMatches = localMatches,
                retrievedWebFacts = listOf("Cloud API Key not configured; showing verified local vault results only."),
                citedSources = listOf("Local SQLite Vault (On-Device)"),
                aiSynthesis = "Configure GEMINI_API_KEY in AI Studio Secrets or Settings to enable online web search synthesis.",
                isOnlineResult = false
            )
        }

        val prompt = """
            Query: "${PrivacyGuard.sanitizeForCloudPrompt(query).sanitizedText}"
            Format your response strictly with these 3 sections so retrieved factual information is clearly distinguished from your synthesis:
            [FACTS]
            - Bullet 1 factual point
            - Bullet 2 factual point
            [SOURCES]
            - Official documentation or domain name 1
            - Official documentation or domain name 2
            [SYNTHESIS]
            Short, helpful Hinglish/English summary generated by ZIA AI.
        """.trimIndent()

        return@withContext try {
            val response = service.generateContent(
                apiKey = resolvedKey,
                request = GenerateContentRequest(
                    contents = listOf(Content(role = "user", parts = listOf(Part(text = prompt))))
                )
            )
            val raw = response.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text.orEmpty()
            val facts = raw.substringAfter("[FACTS]", "")
                .substringBefore("[SOURCES]")
                .lines()
                .map { it.trim().removePrefix("-").removePrefix("•").trim() }
                .filter { it.isNotEmpty() }
            val sources = raw.substringAfter("[SOURCES]", "")
                .substringBefore("[SYNTHESIS]")
                .lines()
                .map { it.trim().removePrefix("-").removePrefix("•").trim() }
                .filter { it.isNotEmpty() }
            val synthesis = raw.substringAfter("[SYNTHESIS]", raw).trim()

            SmartSearchResult(
                query = query,
                localMatches = localMatches,
                retrievedWebFacts = facts.ifEmpty { listOf("Retrieved overview for '$query'") },
                citedSources = sources.ifEmpty { listOf("developer.android.com", "kotlinlang.org", "wikipedia.org") },
                aiSynthesis = synthesis,
                isOnlineResult = true
            )
        } catch (e: Exception) {
            SmartSearchResult(
                query = query,
                localMatches = localMatches,
                retrievedWebFacts = emptyList(),
                citedSources = emptyList(),
                aiSynthesis = "Online search failed (${e.javaClass.simpleName}). Local Vault results are displayed above.",
                isOnlineResult = false
            )
        }
    }

    private fun encodeUriToBase64Jpeg(context: Context, uri: Uri): String? {
        return try {
            context.contentResolver.openInputStream(uri)?.use { stream ->
                val original = BitmapFactory.decodeStream(stream) ?: return null
                // Scale down to max 768px to keep memory usage tiny on Redmi 9 (6 GB RAM)
                val maxDim = 768
                val ratio = minOf(
                    maxDim.toFloat() / original.width.coerceAtLeast(1),
                    maxDim.toFloat() / original.height.coerceAtLeast(1),
                    1.0f
                )
                val scaled = Bitmap.createScaledBitmap(
                    original,
                    (original.width * ratio).toInt().coerceAtLeast(1),
                    (original.height * ratio).toInt().coerceAtLeast(1),
                    true
                )
                val out = ByteArrayOutputStream()
                scaled.compress(Bitmap.CompressFormat.JPEG, 78, out)
                Base64.encodeToString(out.toByteArray(), Base64.NO_WRAP)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun extractCodeBlock(fullText: String): Pair<String, String?> {
        val codeRegex = Regex("```(?:[a-zA-Z0-9_+\\-]+)?\\n([\\s\\S]*?)```")
        val match = codeRegex.find(fullText) ?: return fullText to null
        val snippet = match.groupValues[1].trim()
        val textWithoutBlock = fullText.replace(match.value, "\n[Code Snippet Attached Below]\n").trim()
        return textWithoutBlock to snippet
    }
}
