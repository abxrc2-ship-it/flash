package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.example.data.SettingsRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("FlashLight Pro", appName)
    }

    @Test
    fun `test settings repository persistence`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val repo = SettingsRepository(context)

        repo.setHapticEnabled(false)
        assertFalse(repo.settings.value.hapticEnabled)

        repo.setHapticEnabled(true)
        assertTrue(repo.settings.value.hapticEnabled)

        repo.setAutoOffMinutes(5)
        assertEquals(5, repo.settings.value.autoOffMinutes)
    }
}
