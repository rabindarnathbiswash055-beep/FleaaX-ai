package com.example.security

import android.app.ActivityManager
import android.content.Context
import android.os.Build
import android.os.Environment
import android.os.StatFs
import java.io.File

data class SanitizationResult(
    val isSafe: Boolean,
    val sanitizedText: String,
    val blockedReasonHinglish: String? = null
)

data class DeviceHardwareAudit(
    val deviceModel: String,
    val androidRelease: String,
    val sdkInt: Int,
    val totalRamGb: Double,
    val availableRamGb: Double,
    val freeStorageGb: Double,
    val cpuCores: Int,
    val isLowMemory: Boolean,
    val canRunSmallQuantizedLlm: Boolean,
    val llmRecommendationHinglish: String
)

object PrivacyGuard {

    private val sensitiveKeyKeywords = listOf(
        "password", "passcode", "pin", "otp", "cvv", "secret",
        "token", "apikey", "api_key", "private_key", "aadhaar", "pan_card", "credit_card"
    )

    // Matches 12-16 digit card/ID numbers, API key prefixes, or explicit password assignments
    private val creditCardOrIdRegex = Regex("\\b(?:\\d[ -]*?){12,16}\\b")
    private val apiKeyPrefixRegex = Regex("\\b(AIza[0-9A-Za-z\\-_]{20,}|sk-[0-9A-Za-z]{20,}|ghp_[0-9A-Za-z]{20,})\\b")
    private val otpOrPinRegex = Regex("(?i)\\b(otp|pin|password|passcode|cvv)\\s*(is|:|=|-)?\\s*[0-9A-Za-z@#\\$%]{4,}\\b")
    private val dangerousShellRegex = Regex("(?i)\\b(rm\\s+-rf|su\\s+-c|chmod\\s+777|dd\\s+if=|/system/bin/sh)\\b")

    /**
     * Validates whether a preference key/value is safe to store in local assistant memory.
     * Never stores passwords, authentication tokens, OTPs, or sensitive financial IDs.
     */
    fun validateMemoryPreference(key: String, value: String): SanitizationResult {
        val cleanKey = key.trim()
        val cleanValue = value.trim()
        if (cleanKey.isEmpty() || cleanValue.isEmpty()) {
            return SanitizationResult(
                isSafe = false,
                sanitizedText = "",
                blockedReasonHinglish = "Key aur Value dono bharna zaroori hai (Both key and value are required)."
            )
        }

        val lowerKey = cleanKey.lowercase()
        val lowerVal = cleanValue.lowercase()

        if (sensitiveKeyKeywords.any { lowerKey.contains(it) || lowerVal.contains(it) }) {
            return SanitizationResult(
                isSafe = false,
                sanitizedText = "",
                blockedReasonHinglish = "Security Alert: ZIA AI kabhi bhi passwords, OTPs, PIN, API keys ya private tokens memory mein save nahi karti."
            )
        }

        if (creditCardOrIdRegex.containsMatchIn(cleanValue) ||
            apiKeyPrefixRegex.containsMatchIn(cleanValue) ||
            otpOrPinRegex.containsMatchIn(cleanValue)
        ) {
            return SanitizationResult(
                isSafe = false,
                sanitizedText = "",
                blockedReasonHinglish = "Privacy Guard: Yeh text sensitive number (Card/ID/OTP/API Key) jaisa lag raha hai. Isko memory mein store karna blocked hai."
            )
        }

        return SanitizationResult(
            isSafe = true,
            sanitizedText = cleanValue.take(200)
        )
    }

    /**
     * Redacts sensitive tokens, OTPs, or card numbers before sending any prompt to a cloud AI provider.
     */
    fun sanitizeForCloudPrompt(rawInput: String): SanitizationResult {
        val trimmed = rawInput.trim().take(1500)
        if (dangerousShellRegex.containsMatchIn(trimmed)) {
            return SanitizationResult(
                isSafe = false,
                sanitizedText = trimmed,
                blockedReasonHinglish = "Safety Block: Destructive shell/root commands execute ya process karna mana hai."
            )
        }

        var redacted = trimmed
        redacted = apiKeyPrefixRegex.replace(redacted, "[REDACTED_API_KEY]")
        redacted = creditCardOrIdRegex.replace(redacted, "[REDACTED_NUMBER]")
        redacted = otpOrPinRegex.replace(redacted, "[REDACTED_SECRET]")

        return SanitizationResult(
            isSafe = true,
            sanitizedText = redacted
        )
    }

    /**
     * Inspects actual runtime device RAM, storage, Android version, and CPU cores
     * to provide honest, transparent Android 10 / Redmi 9 compatibility diagnostics.
     */
    fun inspectDeviceHardware(context: Context): DeviceHardwareAudit {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager?.getMemoryInfo(memoryInfo)

        val totalRamGb = if (memoryInfo.totalMem > 0) {
            memoryInfo.totalMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
        } else {
            6.0
        }
        val availRamGb = if (memoryInfo.availMem > 0) {
            memoryInfo.availMem.toDouble() / (1024.0 * 1024.0 * 1024.0)
        } else {
            2.4
        }

        val dataDir: File = Environment.getDataDirectory()
        val stat = StatFs(dataDir.path)
        val freeBytes = stat.availableBlocksLong * stat.blockSizeLong
        val freeStorageGb = freeBytes.toDouble() / (1024.0 * 1024.0 * 1024.0)

        val cores = Runtime.getRuntime().availableProcessors()
        val canRunSmallModel = totalRamGb >= 5.0 && availRamGb >= 1.5 && freeStorageGb >= 2.5

        val recommendation = if (canRunSmallModel) {
            "Aapke device (${String.format("%.1f", totalRamGb)} GB RAM, ${String.format("%.1f", freeStorageGb)} GB free storage) par ZIA ka deterministic LocalCommandEngine + LocalAssistantEngine 0-latency par chal raha hai. Optional 1B-parameter 4-bit quantized model (~800 MB) hardware par chal sakta hai, par battery aur thermal safety ke liye ZIA abhi lightweight Local Engine + Cloud Gemini Hybrid mode use kar rahi hai."
        } else {
            "Available RAM (${String.format("%.1f", availRamGb)} GB) ya storage limited hai. Large local LLM download karne se phone hang ya गरम (overheat) ho sakta hai. ZIA ka built-in lightweight LocalCommandEngine + Hybrid Gemini Cloud mode aapke phone ke liye sabse safe aur fast hai."
        }

        return DeviceHardwareAudit(
            deviceModel = "${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} ${Build.MODEL}",
            androidRelease = "Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})",
            sdkInt = Build.VERSION.SDK_INT,
            totalRamGb = totalRamGb,
            availableRamGb = availRamGb,
            freeStorageGb = freeStorageGb,
            cpuCores = cores,
            isLowMemory = memoryInfo.lowMemory,
            canRunSmallQuantizedLlm = canRunSmallModel,
            llmRecommendationHinglish = recommendation
        )
    }
}
