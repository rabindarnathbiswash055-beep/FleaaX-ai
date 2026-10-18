package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.commands.DeterministicLocalCommandEngine
import com.example.data.PendingAction
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read ZIA AI app_name string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("ZIA AI", appName)
    }

    @Test
    fun `localCommandEngine parses Hinglish and Bengali commands with confirmation gate`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val engine = DeterministicLocalCommandEngine()

        val alarmCmd = engine.evaluateCommand("Hey ZIA, 7:30 ka alarm lagao", context)
        assertTrue(alarmCmd.matched)
        assertTrue(alarmCmd.pendingAction is PendingAction.SetAlarm)
        assertTrue(alarmCmd.pendingAction?.requiresConfirmation == true)

        val volumeCmd = engine.evaluateCommand("জিয়া, ভলিউম 70% বাড়াও", context)
        assertTrue(volumeCmd.matched)
        assertTrue(volumeCmd.pendingAction is PendingAction.AdjustVolume)

        val callCmd = engine.evaluateCommand("Call 9876543210", context)
        assertTrue(callCmd.matched)
        assertTrue(callCmd.pendingAction is PendingAction.DialPhone)
        assertTrue(callCmd.pendingAction?.requiresConfirmation == true)
    }
}
