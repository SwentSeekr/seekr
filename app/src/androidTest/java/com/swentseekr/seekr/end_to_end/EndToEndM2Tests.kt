package com.swentseekr.seekr.end_to_end;

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.firebase.FirebaseApp
import kotlinx.coroutines.runBlocking
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)

class EndToEndM2Tests {

    // Compose test rule: launches a real activity and manages the Compose lifecycle.
    @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    /**
     * Setup step run before each test. Initializes Firebase only if it hasn’t been initialized
     * already. (Prevents crashes when ViewModels internally reference Firebase.)
     */
    @Before
    fun setupFirebase() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        if (FirebaseApp.getApps(context).isEmpty()) {
            FirebaseApp.initializeApp(context)
        }
    }
    /**
     * First, Tests according to user stories.
     * I want to pick a hunt from the overview screen and start it, so that I can begin the hunt.
     * Then, I want to review it, and it should be in my finished hunts list.
     */

    /**
     * I want to create a hunt, log out, log in, edit the hunt, and modify my profile.
     */

    /**
     * I want to like a hunt in overview and the start it from profile.
     */

}
