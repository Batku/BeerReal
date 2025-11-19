package ee.mips.beerreal

import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performClick
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SettingsNavigationTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun profile_settings_navigation_and_back() {
       
        val profileNodes = composeRule.onAllNodesWithText("Profile")
        val profileNav = profileNodes[0]
        profileNav.assertExists()
        profileNav.performClick()

        composeRule.waitForIdle()

        // Tap settings icon in top app bar
        composeRule.onNodeWithContentDescription("Settings").assertExists()
        composeRule.onNodeWithContentDescription("Settings").performClick()

        // Verify Settings screen title is visible
        composeRule.onNodeWithText("Settings").assertExists()

        // Press back (pop back stack) by clicking the back navigation icon
        composeRule.onNodeWithContentDescription("Back").assertExists()
        composeRule.onNodeWithContentDescription("Back").performClick()

        // Verify we're back on Profile screen by checking at least one node contains "Profile"
        val profileNodesAfter = composeRule.onAllNodesWithText("Profile")
        profileNodesAfter[0].assertExists()
    }
}
