package com.udacity.project4.locationreminders

import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(private val listReminderDTO: MutableList<ReminderDTO>? = mutableListOf()) :
    ReminderDataSource {
    private var shouldReturnErrorTest = false

    fun setReturnError(shouldReturnError: Boolean) {
        this.shouldReturnErrorTest = shouldReturnError
    }
    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        return if (listReminderDTO != null) {
            return Result.Success(listReminderDTO)
        } else {
            Result.Error("No reminder Location Founded")
        }
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