package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.google.firebase.FirebaseApp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Archer AI", appName)
    }

    @Test
    fun `mainActivity launches without crashing when Firebase is uninitialized`() {
        FirebaseApp.clearInstancesForTest()
        val controller = Robolectric.buildActivity(MainActivity::class.java).create().start().resume()
        assertNotNull(controller.get())
    }
}
