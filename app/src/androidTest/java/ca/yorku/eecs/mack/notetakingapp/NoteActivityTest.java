package ca.yorku.eecs.mack.notetakingapp;

import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.hamcrest.Matchers.containsString;

@RunWith(AndroidJUnit4.class)
public class NoteActivityTest {

    @Rule
    public ActivityScenarioRule<HomeActivity> scenarioRule =
            new ActivityScenarioRule<>(HomeActivity.class);

    @Test
    public void testCreateAndSaveNote() throws InterruptedException {
        // step 1: click the add button
        Espresso.onView(withId(R.id.fabAddNote)).perform(click());
        Thread.sleep(1000);

        // step 2: click new note button
        Espresso.onView(withText("New Note")).perform(click());
        Thread.sleep(1000);

        // step 3: click keyboard icon
        Espresso.onView(withId(R.id.buttonToggleKeyboard)).perform(click());
        Thread.sleep(1000);

        // step 4: type into note
        String noteText = "Testing";
        Espresso.onView(withId(R.id.editTextInput))
                .perform(typeText(noteText), closeSoftKeyboard());

        Thread.sleep(1000);

        // step 5: click save button
        Espresso.onView(withId(R.id.buttonSaveNote)).perform(click());
        Thread.sleep(1500); // Wait for return to HomeActivity

        // step 6: checks to see if the new note is displayed in the list
        Espresso.onView(withText(noteText))
                .check(matches(isDisplayed()));
    }

    @Test
    public void testSwitchToDrawingAndSaveNote() throws InterruptedException {
        // Step 1: click the add button on home page
        Espresso.onView(withId(R.id.fabAddNote)).perform(click());
        Thread.sleep(1000);

        // Step 2: select "New Note"
        Espresso.onView(withText("New Note")).perform(click());
        Thread.sleep(1000);

        // Step 3: click keyboard icon to enable typing first
        Espresso.onView(withId(R.id.buttonToggleKeyboard)).perform(click());
        Thread.sleep(500);

        // Step 4: input text
        String drawingText = "Text before drawing";
        Espresso.onView(withId(R.id.editTextInput))
                .perform(typeText(drawingText), closeSoftKeyboard());
        Thread.sleep(500);

        // Step 5: switch to drawing mode
        Espresso.onView(withId(R.id.buttonEnableDrawing)).perform(click());
        Thread.sleep(1000); // UI transition

        // Step 6: save the note
        Espresso.onView(withId(R.id.buttonSaveNote)).perform(click());
        Thread.sleep(1500); // wait to return

        // Step 7: confirm the note content is visible on home
        Espresso.onView(withText(drawingText)).check(matches(isDisplayed()));
    }

    @Test
    public void testInsertWeeklyTemplate() throws InterruptedException {
        // Step 1: click add note
        Espresso.onView(withId(R.id.fabAddNote)).perform(click());
        Thread.sleep(1000);

        // Step 2: select "New Note"
        Espresso.onView(withText("New Note")).perform(click());
        Thread.sleep(1000);

        // Step 3: open overflow menu
        Espresso.openActionBarOverflowOrOptionsMenu(
                androidx.test.core.app.ApplicationProvider.getApplicationContext());
        Espresso.onView(withText("Insert Template")).perform(click());
        Thread.sleep(1000);

        // Step 4: click Weekly Planner
        Espresso.onView(withText("Weekly Planner")).perform(click());
        Thread.sleep(500);

        // Step 5: Verify that the corresponding content has been inserted into the input box
        Espresso.onView(withId(R.id.editTextInput))
                .check(matches(withText(containsString("Monday:"))));
    }

}
