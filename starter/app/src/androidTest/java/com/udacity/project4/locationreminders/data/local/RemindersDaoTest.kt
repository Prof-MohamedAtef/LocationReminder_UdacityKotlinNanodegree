package com.udacity.project4.locationreminders.data.local

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.SmallTest;
import com.udacity.project4.locationreminders.data.dto.ReminderDTO

import org.junit.Before;
import org.junit.Rule;
import org.junit.runner.RunWith;

import kotlinx.coroutines.ExperimentalCoroutinesApi;
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.*
import org.hamcrest.MatcherAssert.assertThat
import org.hamcrest.core.IsEqual
import org.junit.After
import org.junit.Test
import java.util.UUID

/*
//    TODO: Add testing implementation to the RemindersDao.kt
 */

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
//Unit test the DAO
@SmallTest
class RemindersDaoTest {
    private lateinit var reminderDao:RemindersDao
    private lateinit var reminderDatabase: RemindersDatabase

    private val reminder1=ReminderDTO(
        "title 1",
        "Description 1",
        "Location 1",
        30.568923,
        31.569874
    )

    private val reminder2=ReminderDTO(
        "title 2",
        "Description 2",
        "Location 2",
        30.447754,
        31.223366
    )

    private val reminder3=ReminderDTO(
        "title 3",
        "Description 3",
        "Location 3",
        30.884466,
        31.339944
    )

    private val localReminder= listOf(reminder1, reminder2, reminder3).sortedBy { it.id }

    @get:Rule
    var instantExecutorRule=InstantTaskExecutorRule()

    @Before
    fun setupDatabase(){
        reminderDatabase=Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            RemindersDatabase::class.java
        ).build()
        reminderDao=reminderDatabase.reminderDao()
    }


    @After
    fun  clean()= reminderDatabase.close()

    private fun savingData()= runBlockingTest {
        reminderDao.saveReminder(reminder1)
        reminderDao.saveReminder(reminder2)
        reminderDao.saveReminder(reminder3)
    }

    @Test
    fun getDatabaseReminders()= runBlockingTest {
        savingData()

        var reminders=reminderDao.getReminders().sortedBy { it.id }

        assertThat(reminders.count(), `is`(3))
        assertThat(reminders, IsEqual(localReminder))
    }

    @Test
    fun assertDatabaseIsEmpty()= runBlockingTest {
        reminderDao.deleteAllReminders()

        var reminders = reminderDao.getReminders()

        assertThat(reminders.isEmpty(), `is`(true))
    }

    @Test
    fun checkReminderIdInDatabase()= runBlockingTest {
        reminderDao.saveReminder(reminder1)

        val reminder=reminderDao.getReminderById(reminder1.id)

        assertThat(reminder as ReminderDTO, notNullValue())

        assertThat(reminder.id, `is`(reminder1.id))
    }

    @Test
    fun checkReminderNotFound() = runBlockingTest {
        reminderDao.saveReminder(reminder1)
        val reminder=reminderDao.getReminderById(UUID.randomUUID().toString())

        assertThat(reminder, `is`(nullValue()))
    }

    @Test
    fun deleteRemindersFromDatabase() = runBlockingTest {
        savingData()
        reminderDao.deleteAllReminders()

        val reminders = reminderDao.getReminders()
        assertThat(reminders.isEmpty(), `is`(true))
    }
}