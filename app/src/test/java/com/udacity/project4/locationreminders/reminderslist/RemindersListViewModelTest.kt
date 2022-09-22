package com.udacity.project4.locationreminders.reminderslist

import android.os.Build
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.android.architecture.blueprints.todoapp.getOrAwaitValue
import com.udacity.project4.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.Is
import org.hamcrest.core.IsNot
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import org.robolectric.annotation.Config

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@Config(sdk = [Build.VERSION_CODES.O_MR1])
class RemindersListViewModelTest {
    @get:Rule
    var instantExecutorRuleTest = InstantTaskExecutorRule()
    @get:Rule
    var mainCoroutineRuleTest = MainCoroutineRule()

    private lateinit var fakeLocalDataSourceForTesting: FakeDataSource
    private lateinit var loadRemindersViewModelTest: RemindersListViewModel
    private lateinit var remindersList: List<ReminderDTO>

    @Before
    fun setup() {
        fakeLocalDataSourceForTesting = FakeDataSource()
        loadRemindersViewModelTest =
            RemindersListViewModel(
                ApplicationProvider.getApplicationContext(),
                fakeLocalDataSourceForTesting
            )
    }

    @Test
    fun addLoadReminders_setLiveDataHasValue() {
        remindersList = listOf(
            ReminderDTO("title 1", "description 1", "Cairo ", 30.043457431, 31.2765762),
            ReminderDTO("title 2", "description 2", "Cairo ", 30.043457431, 31.2765762),
            ReminderDTO("title 3", "description 3", "Cairo ", 30.043457431, 31.2765762),
            ReminderDTO("title 4", "description 4", "Cairo ", 30.043457431, 31.2765762),
            ReminderDTO("title 5", "description 5", "Cairo ", 30.043457431, 31.2765762),
            ReminderDTO("title 6", "description 6", "Cairo ", 30.043457431, 31.2765762)
        )
        fakeLocalDataSourceForTesting = FakeDataSource(remindersList.toMutableList())

        val loadRemindersViewModelTest =
            RemindersListViewModel(
                ApplicationProvider.getApplicationContext(),
                fakeLocalDataSourceForTesting
            )
        loadRemindersViewModelTest.loadReminders()
        assertThat(
            loadRemindersViewModelTest.remindersList.getOrAwaitValue(), (IsNot.not(emptyList()))
        )
        assertThat(
            loadRemindersViewModelTest.remindersList.getOrAwaitValue().size,
            Is.`is`(remindersList.size)
        )

    }

    @Test
    fun getLoadReminders_CheckOfRemindersTasksLoading(){
        mainCoroutineRuleTest.pauseDispatcher()
        loadRemindersViewModelTest.loadReminders()
        assertThat(loadRemindersViewModelTest.showLoading.getOrAwaitValue(), Is.`is`(true))
        mainCoroutineRuleTest.resumeDispatcher()
        assertThat(loadRemindersViewModelTest.showLoading.getOrAwaitValue(), Is.`is`(false))
    }

    @Test
    fun shouldReturnError(){
        fakeLocalDataSourceForTesting = FakeDataSource(null)
        loadRemindersViewModelTest = RemindersListViewModel(ApplicationProvider.getApplicationContext(), fakeLocalDataSourceForTesting)
        fakeLocalDataSourceForTesting.setReturnError(true)
        loadRemindersViewModelTest.loadReminders()
        assertThat(loadRemindersViewModelTest.showSnackBar.getOrAwaitValue(),
            Is.`is`("Error  Can not get reminders")
        )
    }

    @After
    fun koinStopWork() {
        stopKoin()
    }

}