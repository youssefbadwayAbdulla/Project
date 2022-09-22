package com.udacity.project4.locationreminders.data

import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val listReminderDTO: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {
    private var shouldReturnErrorTest = false

    fun setReturnError(shouldReturnError: Boolean) {
        shouldReturnErrorTest = shouldReturnError
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnErrorTest) {
            return Result.Error(
                "Error  Can not get reminders"
            )
        }
        listReminderDTO?.let { return Result.Success(it) }
        return Result.Error("Reminders not founded here")
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        listReminderDTO?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        listReminderDTO?.firstOrNull { reminder ->
            reminder.id == id
        }?.let { reminderSuccess ->
            return Result.Success(reminderSuccess)
        }
        return Result.Error("Location reminder  not founded")
    }

    override suspend fun deleteAllReminders() {
        listReminderDTO?.clear()
    }


}