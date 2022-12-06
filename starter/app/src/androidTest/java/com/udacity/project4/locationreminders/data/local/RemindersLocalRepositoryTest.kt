package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.dto.Result
import com.udacity.project4.util.assertReminder
import junit.framework.Assert.assertEquals
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.*

/*
//    TODO: Add testing implementation to the RemindersLocalRepository.kt
 */

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
@MediumTest
class RemindersLocalRepositoryTest {

    private lateinit var remindersRepository:RemindersLocalRepository
    private lateinit var remindersDatabase: RemindersDatabase
    private lateinit var remindersDao: RemindersDao


    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    val data = mutableListOf(
        ReminderDTO(
            "Mohamed Home",
            "Sinai, Arish, Cairo st",
            "ElArish",
            31.890278,
            30.492222,
            "1"
        ),
        ReminderDTO(
            "Mohamed Home",
            "Sinai, Arish, Cairo st",
            "ElArish",
            31.890278,
            30.492222,
            "1"
        ),
        ReminderDTO(
            "Mohamed Home",
            "Sinai, Arish, Cairo st",
            "ElArish",
            31.890278,
            30.492222,
            "1"
        ),
        ReminderDTO(
            "Mohamed Home",
            "Sinai, Arish, Cairo st",
            "ElArish",
            31.890278,
            30.492222,
            "1"
        )

    )


    private val reminder1 = ReminderDTO(
        "Reminder title 1",
        "Reminder description 1",
        "Reminder location 1",
        24.46017677941061,
        54.42401049833613)

    private val reminder2 = ReminderDTO(
        "Reminder title 2",
        "Reminder description 2",
        "Reminder location 2",
        24.46017677941061,
        54.42401049833613)

    private val reminder3 = ReminderDTO(
        "Reminder title 3",
        "Reminder description 3",
        "Reminder location 3",
        24.46017677941061,
        54.42401049833613)

    private val localReminder = listOf(reminder1, reminder2, reminder3).sortedBy { it.id }

    private val reminderId = UUID.randomUUID().toString()
    private val reminderWithId =  ReminderDTO("Reminder with ID",
        "Reminder Description with ID",
        "Reminder Location with ID",
        24.46017677941061,
        54.42401049833613,
        reminderId)

    private fun saveReminders() = runBlockingTest {
        remindersDao.saveReminder(reminder1)
        remindersDao.saveReminder(reminder2)
        remindersDao.saveReminder(reminder3)
    }

    @Before
    fun createRepository() {
        remindersDatabase = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).allowMainThreadQueries().build()
        remindersDao = remindersDatabase.reminderDao()
        remindersRepository = RemindersLocalRepository(remindersDao, Dispatchers.Main)
    }


    @Test
    fun emptyDB() = runBlocking {
        // GIVEN - No reminder was added in DB

        // WHEN - Get the reminder list
        val result = remindersRepository.getReminders()

        // THEN - None should be returned, but with Success
        assert(result is Result.Success)
        result as Result.Success
        assert(result.data.isEmpty())
    }

    @Test
    fun InsertgetAllRemindersFromDB()= runBlocking {
        // GIVEN a list of three reminders
        saveReminders()

        // WHEN reminders are requested from the reminders repository
        val reminders = (remindersRepository.getReminders() as Result.Success).data.sortedBy { it.id }

        // THEN reminders are loaded from local data source
        assertThat(reminders, notNullValue())
        assertThat(reminders, IsEqual(localReminder))
    }


    @Test
    fun assertErrorReturned() = runBlocking {
        // GIVEN - a non inserted reminder id
        val invalidId = "!!!INVALID!!!"

        val errorMessage=remindersRepository.getReminder(invalidId) as Result.Error

        val error=Result.Error(message = "Reminder not found!")

        assertThat(errorMessage.message,`is`(error.message))
    }


    @Test
    fun insertManyAndGetAsMany() = runBlocking {
        // GIVEN - Many reminders are added
        for (reminder in data) remindersRepository.saveReminder(reminder)

        // WHEN - Get all reminder
        val loaded = remindersRepository.getReminders()

        // THEN - All should be returned as is
        assert(loaded is Result.Success)
        loaded as Result.Success
        assert(data.size == loaded.data.size)

        data.forEach { reference ->
            val reminder = loaded.data.find { it.id == reference.id }
            assertReminder(reference, reminder)
        }
    }



    @After
    fun cleanUp() = remindersDatabase.close()
}