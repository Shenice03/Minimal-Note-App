package ca.yorku.eecs.mack.notetakingapp;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import androidx.test.espresso.Espresso;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import androidx.test.espresso.contrib.DrawerActions;
import androidx.test.espresso.contrib.NavigationViewActions;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

@RunWith(AndroidJUnit4.class)
public class HomeActivityTest {

    @Rule
    public ActivityScenarioRule<HomeActivity> scenarioRule =
            new ActivityScenarioRule<>(HomeActivity.class);

    @Test
    public void testExportNoteFromNavigationDrawer() throws InterruptedException {
        // step 1: open menu drawer
        Espresso.onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open());

        Thread.sleep(500); // optional delay

        // step 2: click the export button
        Espresso.onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_export));

    }

    @Test
    public void testAddNewNoteFromHome() throws InterruptedException {
        Espresso.onView(withId(R.id.fabAddNote)).perform(click());
        Thread.sleep(500);

        Espresso.onView(withId(R.id.buttonNewNote)).perform(click());
        Thread.sleep(500);

        String title = "Test Title";
        String content = "Test Content";

        Espresso.onView(withId(R.id.editTextInput))
                .perform(typeText(title + "\n" + content), closeSoftKeyboard());
        Thread.sleep(500);

        Espresso.onView(withId(R.id.buttonSaveNote)).perform(click());
        Thread.sleep(1500);

        Espresso.onView(withText(title)).check(matches(isDisplayed()));
    }

    @Test
    public void testToggleDarkMode() throws InterruptedException {
        Espresso.onView(withId(R.id.drawer_layout))
                .perform(DrawerActions.open());
        Thread.sleep(500);

        Espresso.onView(withId(R.id.nav_view))
                .perform(NavigationViewActions.navigateTo(R.id.nav_dark_mode));
        Thread.sleep(1000);
    }

    @Test
    public void testLongClickToDeleteNote() throws InterruptedException {
        Espresso.onView(withId(R.id.fabAddNote)).perform(click());
        Thread.sleep(500);

        Espresso.onView(withId(R.id.buttonNewNote)).perform(click());
        Thread.sleep(500);

        String title = "NoteToDelete";

        Espresso.onView(withId(R.id.editTextInput))
                .perform(typeText(title + "\nSome content"), closeSoftKeyboard());
        Thread.sleep(500);

        Espresso.onView(withId(R.id.buttonSaveNote)).perform(click());
        Thread.sleep(1500);

        Espresso.onView(withText(title)).check(matches(isDisplayed()));

        Espresso.onView(withText(title)).perform(longClick());
        Thread.sleep(500);

        Espresso.onView(withText("Delete")).perform(click());
        Thread.sleep(1000);

        Espresso.onView(withText(title)).check(doesNotExist());
    }

}