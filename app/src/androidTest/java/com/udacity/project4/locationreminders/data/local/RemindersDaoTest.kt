package com.udacity.project4.locationreminders.data.local


import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test


@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@SmallTest
class RemindersDaoTest {
    @get:Rule
    var instantExecutorRuleTest = InstantTaskExecutorRule()
    private lateinit var remindersDatabaseTest: RemindersDatabase

    @Before
    fun setup() {
        remindersDatabaseTest = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(), RemindersDatabase::class.java
        ).build()
    }

    @Test
    fun getRemindersDao_and_getRemindersTDO() = runBlockingTest {
        //given....(insert and update Reminders
        val insertAndSaveReminders =
            ReminderDTO("title", "description", "location", 30.043457431, 31.2765762)
        remindersDatabaseTest.reminderDao().saveReminder(insertAndSaveReminders)
        //when get insert and update Reminders from ReminderDatabase
        val getRemindersLocation = remindersDatabaseTest.reminderDao().getReminders()
        //then
        assertThat(getRemindersLocation.size, `is`(1))
        assertThat(getRemindersLocation[0].title, `is`(insertAndSaveReminders.title))
        assertThat(getRemindersLocation[0].description, `is`(insertAndSaveReminders.description))
        assertThat(getRemindersLocation[0].location, `is`(insertAndSaveReminders.location))
        assertThat(getRemindersLocation[0].latitude, `is`(insertAndSaveReminders.latitude))
        assertThat(getRemindersLocation[0].longitude, `is`(insertAndSaveReminders.longitude))
    }

    @Test
    fun getRemindersDao_and_getRemindersTDO_ById() = runBlockingTest {
        //given....(insert and update Reminders
        val insertAndSaveReminders =
            ReminderDTO("title", "description", "location", 30.043457431, 31.2765762)
        remindersDatabaseTest.reminderDao().saveReminder(insertAndSaveReminders)
        val reminderTestById =
            remindersDatabaseTest.reminderDao().getReminderById(insertAndSaveReminders.id)
        //when get insert and update Reminders from ReminderDatabase

        //then
        assertThat(reminderTestById?.id, `is`(insertAndSaveReminders.id))
        assertThat(reminderTestById?.title, `is`(insertAndSaveReminders.title))
        assertThat(reminderTestById?.description, `is`(insertAndSaveReminders.description))
        assertThat(reminderTestById?.location, `is`(insertAndSaveReminders.location))
        assertThat(reminderTestById?.latitude, `is`(insertAndSaveReminders.latitude))
        assertThat(reminderTestById?.longitude, `is`(insertAndSaveReminders.longitude))
    }

    @Test
    fun deleteAllRemindersDao() = runBlockingTest {
        //given....(insert and update Reminders
        val listRemindersTest = listOf(
            ReminderDTO("title1", "description1", "location1", 30.043457431, 31.2765762),
            ReminderDTO("title2", "description2", "location2", 30.043457431, 31.2765762),
            ReminderDTO("title3", "description3", "location3", 30.043457431, 31.2765762)
        )
        listRemindersTest.forEach { saveList ->
            remindersDatabaseTest.reminderDao().saveReminder(saveList)

        }

        remindersDatabaseTest.reminderDao().deleteAllReminders()


        //then
        val getReminderListTest = remindersDatabaseTest.reminderDao().getReminders()
        assertThat(getReminderListTest.isEmpty(), `is`(true))
    }



    @Test
    fun returnNoReminders_dataError()= runBlockingTest{
        val reminder = ReminderDTO("titleError", "descriptionError", "locationError", 37.819927, 39.8145927)
        remindersDatabaseTest.reminderDao().saveReminder(reminder)
        val randomId="55415254324fs711"
        val loadReminderLocations = remindersDatabaseTest.reminderDao().getReminderById(randomId)

        assertNull(loadReminderLocations)

    }
    @After
    fun closeRemindersDatabase() {
        remindersDatabaseTest.close()
    }


}