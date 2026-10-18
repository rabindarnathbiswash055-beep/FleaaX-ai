package com.example

import com.example.ai.DeterministicLocalAssistantEngine
import com.example.data.AssistantLanguage
import com.example.data.PersonalityStyle
import com.example.security.PrivacyGuard
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {

    @Test
    fun privacyGuard_blocksPasswordsAndOtpsInMemory() {
        val unsafePassword = PrivacyGuard.validateMemoryPreference("My Bank Password", "Secret12345")
        assertFalse(unsafePassword.isSafe)

        val unsafeOtp = PrivacyGuard.validateMemoryPreference("Note", "My OTP is 483920")
        assertFalse(unsafeOtp.isSafe)

        val safePreference = PrivacyGuard.validateMemoryPreference("Preferred Language", "Hinglish & Banglish")
        assertTrue(safePreference.isSafe)
    }

    @Test
    fun localAssistantEngine_calculatesMathAndGeneratesCodeOffline() {
        val engine = DeterministicLocalAssistantEngine()
        val mathReply = engine.generateLocalReply(
            input = "What is 25 * 4?",
            language = AssistantLanguage.HINGLISH,
            personality = PersonalityStyle.FRIENDLY,
            savedMemories = emptyList()
        )
        assertTrue(mathReply.replyText.contains("100"))

        val codeReply = engine.generateLocalReply(
            input = "Show Kotlin coroutine viewmodel code",
            language = AssistantLanguage.HINGLISH,
            personality = PersonalityStyle.PROFESSIONAL,
            savedMemories = emptyList()
        )
        assertNotNull(codeReply.codeSnippet)
    }
}
