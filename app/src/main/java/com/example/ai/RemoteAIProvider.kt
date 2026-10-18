package com.example.ai

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.example.BuildConfig
import com.example.data.AssistantLanguage
import com.example.data.EngineSource
import com.example.data.MemoryFactEntity
import com.example.data.PersonalityStyle
import com.example.security.PrivacyGuard
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
    val text: String? = null
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

interface RemoteAIProvider {
    fun isNetworkOnline(context: Context): Boolean
    suspend fun generateCloudResponse(
        context: Context,
        userPrompt: String,
        language: AssistantLanguage,
        personality: PersonalityStyle,
        savedMemories: List<MemoryFactEntity>,
        customApiKeyOverride: String
    ): RemoteAiResult
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

    override suspend fun generateCloudResponse(
        context: Context,
        userPrompt: String,
        language: AssistantLanguage,
        personality: PersonalityStyle,
        savedMemories: List<MemoryFactEntity>,
        customApiKeyOverride: String
    ): RemoteAiResult = withContext(Dispatchers.IO) {
        if (!isNetworkOnline(context)) {
            return@withContext RemoteAiResult.Unavailable(
                "Internet connection offline hai. ZIA ne aapka message Local Assistant Engine par route kiya hai."
            )
        }

        val resolvedKey = customApiKeyOverride.trim().ifEmpty { BuildConfig.GEMINI_API_KEY.trim() }
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
            "No saved user preferences."
        } else {
            savedMemories.take(8).joinToString("; ") { "${it.key}: ${it.value}" }
        }

        val systemInstructionText = """
            You are ZIA AI ("Your Voice. Your World. Your ZIA."), a friendly, intelligent female native Android voice & coding assistant.
            Target user language mode: ${language.displayName}.
            If the user writes in Hinglish (Hindi-English mix), Banglish (Bengali-English mix), বাংলা (Bengali), or English, reply naturally in that language style!
            Personality directive: ${personality.systemPromptDirective}
            User's opt-in local memory preferences: $memoryContext.
            Security rules:
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
                    PersonalityStyle.FRIENDLY -> 0.7f
                    PersonalityStyle.PLAYFUL -> 0.85f
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

    private fun extractCodeBlock(fullText: String): Pair<String, String?> {
        val codeRegex = Regex("```(?:[a-zA-Z0-9_+\\-]+)?\\n([\\s\\S]*?)```")
        val match = codeRegex.find(fullText) ?: return fullText to null
        val snippet = match.groupValues[1].trim()
        val textWithoutBlock = fullText.replace(match.value, "\n[Code Snippet Attached Below]\n").trim()
        return textWithoutBlock to snippet
    }
}
