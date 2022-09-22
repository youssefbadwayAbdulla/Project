package com.udacity.project4.locationreminders.data.local


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result


import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.intellij.lang.annotations.Identifier
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test

import org.junit.runner.RunWith

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)

@MediumTest
class RemindersLocalRepositoryTest {

    @get:Rule
    var instantExecutorRuleTest = InstantTaskExecutorRule()

    private lateinit var remindersDatabaseTest: RemindersDatabase

    private lateinit var remindersLocalRepository: RemindersLocalRepository

    @Before
    fun setup() {
        remindersDatabaseTest = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()

        remindersLocalRepository = RemindersLocalRepository(
            remindersDatabaseTest.reminderDao()
        )
    }

    @Test
    fun getAndSaveReminder() = runBlocking {
        val insertAndSaveReminders =
            ReminderDTO("title", "description", "location", 30.043457431, 31.2765762)
        remindersDatabaseTest.reminderDao().saveReminder(insertAndSaveReminders)

        val saveResult = remindersLocalRepository.getReminder(insertAndSaveReminders.id)
        saveResult as Result.Success
        assertThat(saveResult.data.title, `is`("title"))
        assertThat(saveResult.data.description, `is`("description"))
        assertThat(saveResult.data.latitude, `is`(30.043457431))
        assertThat(saveResult.data.longitude, `is`(31.2765762))
    }



    @Test
    fun getAndDeleteReminder() = runBlocking {
        val insertAndSaveReminders =
            ReminderDTO("title", "description", "location", 30.043457431, 31.2765762)
        remindersDatabaseTest.reminderDao().saveReminder(insertAndSaveReminders)
        remindersDatabaseTest.reminderDao().deleteAllReminders()

        val saveResult = remindersLocalRepository.getReminders()
        assertThat(saveResult is Result.Success, `is`(true))
        saveResult as Result.Success
        assertThat(saveResult.data, `is`(emptyList()))

    }


    @After
    fun closeRemindersDatabase() {
        remindersDatabaseTest.close()
    }

}