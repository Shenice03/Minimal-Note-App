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

@RunWith(AndroidJUnit4.class)
public class AddTaskListActivityTest {

    @Rule
    public ActivityScenarioRule<HomeActivity> scenarioRule =
            new ActivityScenarioRule<>(HomeActivity.class);


    @Test
    public void testCreateTaskList() throws InterruptedException {
        // step 1: click the add button on the home screen
        Espresso.onView(withId(R.id.fabAddNote)).perform(click());
        Thread.sleep(1000);

        // step 2: click the new task button on the home screen
        Espresso.onView(withText("New Task List")).perform(click());
        Thread.sleep(1000);

        // step 3: type a task item
        Espresso.onView(withId(R.id.editTextTaskInput))
                .perform(typeText("Do assignment 3"), closeSoftKeyboard());

        // step 4: click the add button to add the task
        Espresso.onView(withId(R.id.buttonAddTask)).perform(click());

        // step 5: save the task list
        Espresso.onView(withId(R.id.buttonSaveTaskList)).perform(click());

        // step 6: verifies it returns to home and displays the task
        Espresso.onView(withText("Do assignment 3"))
                .check(matches(isDisplayed()));
    }
}