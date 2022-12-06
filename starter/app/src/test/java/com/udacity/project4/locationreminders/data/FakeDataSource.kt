package com.udacity.project4.locationreminders.data

import androidx.lifecycle.MutableLiveData
import com.udacity.project4.locationreminders.MainCoroutineRule.Companion.errorMessage
import com.udacity.project4.locationreminders.MainCoroutineRule.Companion.notFoundMessage
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.locationreminders.reminderslist.ReminderListFragmentDirections
import kotlinx.coroutines.runBlocking

//Use FakeDataSource that acts as a test double to the LocalDataSource
class FakeDataSource(
    private val remindersList: MutableList<ReminderDTO> = mutableListOf()) : ReminderDataSource {



    private var shouldReturnError = false

    fun setReturnError(value: Boolean) {
        shouldReturnError = value
    }

    override suspend fun getReminders(): Result<List<ReminderDTO>> {
        if (shouldReturnError) {
            return Result.Error(errorMessage)
        }
        try {
            remindersList.let {
                return Result.Success(ArrayList(it))
            }
        }catch (ex:Exception){
            return Result.Error(ex.localizedMessage)
        }
    }

    override suspend fun saveReminder(reminder: ReminderDTO) {
        remindersList.add(reminder)
    }

    override suspend fun getReminder(id: String): Result<ReminderDTO> {
        try {
            if (shouldReturnError) {
                return Result.Error(errorMessage)
            }
            if (remindersList!=null){
                remindersList.let {
                    return Result.Success(it.single { reminder -> reminder.id == id })
                }
            }else{
                return Result.Error(notFoundMessage)
            }
        }catch (e: Exception){
            return Result.Error(e.localizedMessage)
        }
    }

    override suspend fun deleteAllReminders() {
        remindersList.clear()
    }
}