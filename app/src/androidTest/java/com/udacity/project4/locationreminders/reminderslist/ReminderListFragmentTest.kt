package com.udacity.project4.locationreminders.reminderslist


import android.os.Bundle
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.FakeDataSource
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify


@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
//UI Testing
@MediumTest
class ReminderListFragmentTest {
    @get:Rule
    var instantExecutorRuleTest = InstantTaskExecutorRule()
    private lateinit var fakeLocalDataSourceForTesting: ReminderDataSource
    private lateinit var loadRemindersViewModelTest: RemindersListViewModel
    private val binding = DataBindingIdlingResource()

    @Before
    fun setupTest() {
        fakeLocalDataSourceForTesting = FakeDataSource()
        loadRemindersViewModelTest =
            RemindersListViewModel(getApplicationContext(), fakeLocalDataSourceForTesting as ReminderDataSource)
        stopKoin()
        modules()
    }

    private fun modules() {
        val myModule = module {
            single {
                loadRemindersViewModelTest
            }
        }
        startKoin {
            modules(listOf(myModule))
        }
    }


    //    TODO: test the navigation of the fragments.
    @Test
    fun addNavigationReminders() = runBlockingTest {
        val fragment = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        val navController = mock(NavController::class.java)
        fragment.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())

        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

//    TODO: test the displayed data on the UI.

    @Test
    fun displayData_and_DisplayUITest() = runBlockingTest {
        val reminderLocationTest =
            ReminderDTO("title", "description", "dhaka", 37.819927, 39.852927)
        fakeLocalDataSourceForTesting.saveReminder(reminderLocationTest)
        launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        onView(withId(R.id.noDataTextView)).check(matches(CoreMatchers.not(isDisplayed())))
        onView(withText(reminderLocationTest.title)).check(matches(isDisplayed()))
        onView(withText(reminderLocationTest.description)).check(matches(isDisplayed()))
        onView(withText(reminderLocationTest.location)).check(matches(isDisplayed()))

    }

    //    TODO: add testing for the error messages.
    @Test
    fun errorShowSnackAndBackShown()  {
        val fragmentScenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        binding.monitorFragment(fragmentScenario)
        val controller = mock(NavController::class.java)

        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, controller)
        }

        onView(withId(R.id.addReminderFAB)).perform(click())
        verify(controller).navigate(ReminderListFragmentDirections.toSaveReminder())
    }
    }
