package com.udacity.project4.locationreminders.data

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    private val remindersList: MutableList<ReminderDTO> = mutableListOf(
        ReminderDTO(
            "AtefHome",
            "Sinai",
            "El-Arish",
            31.565,
            30.565,
            "1"
        ),
        ReminderDTO(
            "AtefHome",
            "Sinai",
            "El-Arish",
            31.565,
            30.565,
            "2"
        ),
        ReminderDTO(
            "AtefHome",
            "Sinai",
            "El-Arish",
            31.565,
            30.565,
            "3"
        )
    )
) : ReminderDataSource {

    private var  notFoundMessage= "not found!"
    private var errorMessage = "Reminder Id not found"

    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error(errorMessage)
        }
        remindersList?.let {
            return Result.Success(ArrayList(it))
        }
        return Result.Error(notFoundMessage)
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList?.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        try {
            if (shouldReturnError) {
                return Result.Error(errorMessage)
            }
            remindersList?.let {
                return Result.Success(it.single { reminder -> reminder.id == id })
            }
            return Result.Error(notFoundMessage)
        }catch (e: Exception){
            return Result.Error(e.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        remindersList?.clear()
    }
}