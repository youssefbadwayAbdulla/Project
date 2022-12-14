package com.udacity.project4

import android.app.Activity
import android.app.Application
import androidx.lifecycle.Transformations.map
import androidx.test.core.app.ActivityScenario
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.RootMatchers.withDecorView
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.reminderslist.RemindersListViewModel
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorActivity
import com.udacity.project4.utils.EspressoIdlingResource
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.koin.test.AutoCloseKoinTest
import org.koin.test.get

@RunWith(AndroidJUnit4::class)
@LargeTest
class RemindersActivityTest :
    AutoCloseKoinTest() {

    private lateinit var dataSource: ReminderDataSource
    private lateinit var context: Application
    private lateinit var saveReminderViewModelTest: SaveReminderViewModel
    private val binding = DataBindingIdlingResource()


    @Before
    fun setup() {
        stopKoin()//stop the original app koin
        context = getApplicationContext()
        val myModule = module {
            viewModel {
                RemindersListViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }
            single {
                SaveReminderViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(context) }
        }
//declare a new koin module
        startKoin {
            modules(listOf(myModule))
        }
//Get our real repository
        dataSource = get()
//clear the data to start fresh
        runBlocking {
            dataSource.deleteAllReminders()
        }
        saveReminderViewModelTest = GlobalContext.get().koin.get()

    }

    @Before
    fun registerIdlingResource() {
        IdlingRegistry.getInstance().register(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().register(binding)
    }

    @After
    fun unregisterIdlingResource() {
        IdlingRegistry.getInstance().unregister(EspressoIdlingResource.countingIdlingResource)
        IdlingRegistry.getInstance().unregister(binding)
    }

    //Toast testing only works on api 29 and below
    @Test
    fun saveReminderScreen_showToastMessage() = runBlockingTest {

        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        binding.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())
        onView(withId(R.id.reminderTitle)).perform(typeText("Title"),closeSoftKeyboard())
        closeSoftKeyboard()
        onView(withId(R.id.reminderDescription)).perform(typeText("Description"),closeSoftKeyboard())


        onView(withId(R.id.select_reminder_location)).perform(click())
        onView(withId(R.id.mapLocation)).perform(click())

        onView(withId(R.id.btn_save)).perform(click())

        onView(withId(R.id.saveReminder)).perform(click())
        onView(withText(R.string.reminder_saved)).inRoot(
            withDecorView(
                not(
                    getActivity(
                        activityScenario
                    )!!.window.decorView
                )
            )
        ).check(matches(isDisplayed()))
        activityScenario.close()
    }



    @Test
    fun saveReminderAnd_checkSnackBar() = runBlockingTest {
        val activityScenario = ActivityScenario.launch(RemindersActivity::class.java)
        binding.monitorActivity(activityScenario)

        onView(withId(R.id.addReminderFAB)).perform(click())

        onView(withId(R.id.reminderTitle)).perform(typeText("Title"))
        Espresso.closeSoftKeyboard()
        onView(withId(R.id.saveReminder)).perform(click())

        onView(
            allOf(
                withId(com.google.android.material.R.id.snackbar_text),
                withText(R.string.err_select_location)
            )
        ).check(matches(isDisplayed()))
        activityScenario.close()
    }


    private fun getActivity(activityScenario: ActivityScenario<RemindersActivity>): Activity? {
        var activity: Activity? = null
        activityScenario.onActivity {
            activity = it
        }
        return activity
    }

}