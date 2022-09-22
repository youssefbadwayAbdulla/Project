package com.udacity.project4.locationreminders.savereminder

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.hamcrest.CoreMatchers
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.notNullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config


@Config(maxSdk = Build.VERSION_CODES.P)
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    val instantTaskExecutorRuleTest = InstantTaskExecutorRule()

    @get:Rule
    val mainCoroutineRuleTest = MainCoroutineRule()

    private lateinit var fakeLocalDataSourceForTesting: FakeDataSource
    private lateinit var saveRemindersViewModelTest: SaveReminderViewModel

    @Before
    fun setup() {
        stopKoin()

        fakeLocalDataSourceForTesting = FakeDataSource()
        saveRemindersViewModelTest = SaveReminderViewModel(
            getApplicationContext(),
            fakeLocalDataSourceForTesting
        )
        runBlocking{ fakeLocalDataSourceForTesting.deleteAllReminders()}

    }

    private fun getReminderAndSaveLocation(): ReminderDataItem {
        return ReminderDataItem(
            title = "title",
            description = "desc",
            location = "loc",
            latitude = 47.5456551,
            longitude = 122.0101731)
    }
    @Test
    fun saveRemindersLocation() {
        val reminderDataItem = getReminderAndSaveLocation()
        saveRemindersViewModelTest.saveReminder(reminderDataItem)
        assertThat(saveRemindersViewModelTest.showToast.getOrAwaitValue(), `is`("Reminder Saved !"))
    }

    @Test
    fun `saveRemindersLocation_withoutTitle()`() {

        val reminderDataItem = ReminderDataItem(
            title = "",
            description = "desc",
            location = "loc",
            latitude = 47.5456551,
            longitude = 122.0101731)

        saveRemindersViewModelTest.validateAndSaveReminder(reminderDataItem)
        assertThat(saveRemindersViewModelTest.showSnackBarInt.getOrAwaitValue(), notNullValue())

    }

    @Test
    fun loadSaveReminders_and_showLocationLoading() = runBlocking {

        val reminderDataItem = getReminderAndSaveLocation()

        mainCoroutineRuleTest.pauseDispatcher()
        saveRemindersViewModelTest.validateAndSaveReminder(reminderDataItem)
        assertThat(saveRemindersViewModelTest.showLoading.getOrAwaitValue(), CoreMatchers.`is`(true))

        mainCoroutineRuleTest.resumeDispatcher()
        assertThat(saveRemindersViewModelTest.showLoading.getOrAwaitValue(), CoreMatchers.`is`(false))


    }


    @Test
    fun loadSaveReminders_and_showWithoutLocationLoading() {

        val reminderDataItem = ReminderDataItem(
            title = "hey",
            description = "hey",
            location = "",
            latitude = 47.5456551,
            longitude = 122.0101731)

        saveRemindersViewModelTest.validateAndSaveReminder(reminderDataItem)
        assertThat(saveRemindersViewModelTest.showSnackBarInt.getOrAwaitValue(), notNullValue())

    }
}