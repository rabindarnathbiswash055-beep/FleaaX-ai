package com.example

import com.example.ai.DeterministicLocalAssistantEngine
import com.example.data.AssistantLanguage
import com.example.data.PersonalityStyle
import com.example.data.ResponseLengthPreference
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
    fun localAssistantEngine_supportsMultiLanguageCodeGamingAndDocumentSummary() {
        val engine = DeterministicLocalAssistantEngine()
        val mathReply = engine.generateLocalReply(
            input = "What is 25 * 4?",
            language = AssistantLanguage.HINGLISH,
            personality = PersonalityStyle.STUDY_BUDDY,
            savedMemories = emptyList(),
            responseLength = ResponseLengthPreference.BALANCED
        )
        assertTrue(mathReply.replyText.contains("100"))

        val jsReply = engine.generateCodeSnippetByLanguage("JavaScript", "fetch timeout")
        assertNotNull(jsReply.codeSnippet)
        assertTrue(jsReply.codeSnippet!!.contains("AbortController"))

        val htmlReply = engine.generateCodeSnippetByLanguage("HTML/CSS", "card")
        assertNotNull(htmlReply.codeSnippet)
        assertTrue(htmlReply.codeSnippet!!.contains("zia-card"))

        val gamingPack = engine.generateGamingCreatorPack("BGMI", "60 FPS Smooth")
        assertTrue(gamingPack.contains("Thumbnail Concept"))

        val summary = engine.summarizeTextLocally("Study.md", "Line 1 Kotlin\nLine 2 Compose\nLine 3 Room")
        assertTrue(summary.contains("Offline Local Summary"))
    }
}
