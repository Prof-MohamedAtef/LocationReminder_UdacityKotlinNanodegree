package com.udacity.project4.locationreminders.reminderslist

import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.getOrAwaitValue
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.MatcherAssert.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin
import com.udacity.project4.locationreminders.MainCoroutineRule.Companion.errorMessage

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
/*
TODO: provide testing to the RemindersListViewModel and its live data objects
 */
class RemindersListViewModelTest {
    // testing with architecture components
    @get:Rule
    var instanceExecutorRule=InstantTaskExecutorRule()

    // main coroutine Dispatcher for Unit Testing
    @get:Rule
    var mainCoroutineDispatcherRule = MainCoroutineRule()


    // use test double/Fake data source to be injected into the viewmodel
    private lateinit var dataSource: FakeDataSource

    // viewmodel being tested - subject under test


    // Subject under test
    private lateinit var remindersListViewModel: RemindersListViewModel

    @Before
    fun setupViewModel(){
        stopKoin()
        dataSource= FakeDataSource()
        remindersListViewModel= RemindersListViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource
        )
    }

    @Test
    fun loadReminders_assert_listNotEmpty()=mainCoroutineDispatcherRule.runBlockingTest {
        dataSource.deleteAllReminders()
        val reminder=ReminderDTO("Title", "Description", "Location", 1.5, 3.0)
        dataSource.saveReminder(reminder)

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isNotEmpty(), `is`(true))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun loadReminders_assert_returnEmptyList() = mainCoroutineDispatcherRule.runBlockingTest {
        dataSource.deleteAllReminders()
        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.remindersList.getOrAwaitValue().isEmpty(), `is`(true))
        assertThat(remindersListViewModel.showNoData.getOrAwaitValue(), `is`(true))
    }

    @Test
    fun loadReminders_assert_returnError() = mainCoroutineDispatcherRule.runBlockingTest {
        dataSource.setReturnError(true)

        remindersListViewModel.loadReminders()

        assertThat(remindersListViewModel.showSnackBar.getOrAwaitValue(), `is`(errorMessage))
        Log.e("Test Pass", "Test Returned this error Message$errorMessage")
    }


    @Test
    fun check_loading() {
        // GIVEN - set showLoading to null
        assert(remindersListViewModel.showLoading.value == null)

        // WHEN - Load reminders
        mainCoroutineDispatcherRule.pauseDispatcher()
        remindersListViewModel.loadReminders()

        // THEN - show Loading indicator
        assert(remindersListViewModel.showLoading.getOrAwaitValue())

        // WHEN - Finished loading
        mainCoroutineDispatcherRule.resumeDispatcher()

        // THEN - Loading indication should be hide
        assert(!remindersListViewModel.showLoading.getOrAwaitValue())
    }
}