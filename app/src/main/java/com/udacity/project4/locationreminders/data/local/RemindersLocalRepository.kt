package com.udacity.project4.locationreminders.data.local

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.utils.wrapEspressoIdlingResource
import kotlinx.coroutines.*


class RemindersLocalRepository(
    private val remindersDao: RemindersDao,

    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ReminderDataSource {

    override suspend fun getReminders(): Result<List<ReminderDTO>> = wrapEspressoIdlingResource {
        withContext(ioDispatcher) {
            return@withContext try {
                Result.Success(remindersDao.getReminders())
            } catch (ex: Exception) {
                Result.Error(ex.localizedMessage)
            }
        }
    }


    override suspend fun saveReminder(reminder: ReminderDTO) = wrapEspressoIdlingResource {
        withContext(ioDispatcher) {
            remindersDao.saveReminder(reminder)
        }
    }


    override suspend fun getReminder(id: String): Result<ReminderDTO> = wrapEspressoIdlingResource {
        withContext(ioDispatcher) {
            try {
                val reminder = remindersDao.getReminderById(id)
                if (reminder != null) {
                    return@withContext Result.Success(reminder)
                } else {
                    return@withContext Result.Error("Reminder not found!")
                }
            } catch (e: Exception) {
                return@withContext Result.Error(e.localizedMessage)
            }
        }
    }

    override suspend fun deleteAllReminders() = wrapEspressoIdlingResource {
        withContext(ioDispatcher) {
            remindersDao.deleteAllReminders()
        }
    }

}
