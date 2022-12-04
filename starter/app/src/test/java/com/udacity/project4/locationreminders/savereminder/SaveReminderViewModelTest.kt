package com.udacity.project4.locationreminders.savereminder

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PointOfInterest
import com.udacity.project4.R
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.locationreminders.MainCoroutineRule
import com.udacity.project4.locationreminders.data.FakeDataSource
import com.udacity.project4.locationreminders.getOrAwaitValue
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import junit.framework.Assert.assertEquals

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.hamcrest.CoreMatchers.`is`
import org.hamcrest.CoreMatchers.nullValue
import org.hamcrest.MatcherAssert.assertThat
import org.junit.After
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.core.context.stopKoin

/*
//TODO: provide testing to the SaveReminderView and its live data objects
 */

@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class SaveReminderViewModelTest {

    @get:Rule
    var instantExecutorRule = InstantTaskExecutorRule()

    @ExperimentalCoroutinesApi
    @get:Rule
    var mainCoroutineRuleDispatcher = MainCoroutineRule()

    // test double FakeDataSource
    private lateinit var dataSource: FakeDataSource


    // Subject/viewmodel being  tested
    private lateinit var saveReminderViewModel: SaveReminderViewModel

    @Before
    fun setup() {
        stopKoin()
        dataSource = FakeDataSource()

        saveReminderViewModel = SaveReminderViewModel(
            ApplicationProvider.getApplicationContext(),
            dataSource)
    }

    @Test
    fun assertLiveDataIsCleared() {

        /*
        first: fill ViewModel ( LiveData objects )
         */
        saveReminderViewModel.reminderTitle.value = "Home"
        saveReminderViewModel.reminderDescription.value = "Home at Sinai"
        saveReminderViewModel.reminderSelectedLocationStr.value = "Elbarrawy St"
        saveReminderViewModel.latitude.value = 31.964852
        saveReminderViewModel.longitude.value = 30.656523
        saveReminderViewModel.selectedPOI.value =
            PointOfInterest(LatLng(31.989847, 30.698547), "Title", "Description")


        // clear viewmodel
        saveReminderViewModel.onClear()

        // check whether viewmodel is cleared
        assertThat(saveReminderViewModel.reminderTitle.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderDescription.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.reminderSelectedLocationStr.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.selectedPOI.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.latitude.getOrAwaitValue(), `is`(nullValue()))
        assertThat(saveReminderViewModel.longitude.getOrAwaitValue(), `is`(nullValue()))
    }

    @Test
    fun saveNewReminder_assert_newReminderSaved() = mainCoroutineRuleDispatcher.runBlockingTest {
        val reminder = ReminderDataItem("Title", "Description", "Location", 2.5, 4.5)

        saveReminderViewModel.saveReminder(reminder)

        assertThat(saveReminderViewModel.showToast.value, `is`(saveReminderViewModel.app.getString(R.string.reminder_saved)))
        assertEquals(saveReminderViewModel.navigationCommand.getOrAwaitValue(), NavigationCommand.Back)
    }

    @Test
    fun assert_saveNewReminder_titleIsEmpty_returnFalse_showSnackBar() {
        val reminder = ReminderDataItem("", "Description", "Location", 2.5, 4.5)

        val validReminder = saveReminderViewModel.validateEnteredData(reminder)

        assertThat(validReminder, `is`(false))
        assertThat(saveReminderViewModel.showSnackBarInt.getOrAwaitValue(), `is`(R.string.err_enter_title))
    }

    @Test
    fun assert_saveReminder_showLoading() {
        val reminder = ReminderDataItem("", "Description", "Location", 2.5, 4.5)

        mainCoroutineRuleDispatcher.pauseDispatcher()
        saveReminderViewModel.saveReminder(reminder)

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(true))
        mainCoroutineRuleDispatcher.resumeDispatcher()

        assertThat(saveReminderViewModel.showLoading.getOrAwaitValue(), `is`(false))
    }

    @Test
    fun assertDataIsCorrect() {
        // GIVEN - A valid data item
        var dataItem = ReminderDataItem(
            "ReminderDataTitle",
            "ReminderDataDescription",
            "ReminderDataLocation",
            31.658742,
            30.32598,
            "1"
        )
        // WHEN - Try to validate it
        var result = saveReminderViewModel.validateEnteredData(dataItem)

        // THEN - Should return true and showSnackBarInt should be null
        assert(result)
        assert(saveReminderViewModel.showSnackBar.value == null)

        // GIVEN - A data item with invalid/null title
        dataItem = ReminderDataItem(
            null,
            "ReminderDataDescription",
            "ReminderDataLocation",
            31.658742,
            30.32598,
            "1"
        )

        // WHEN - Try to validate it
        result = saveReminderViewModel.validateEnteredData(dataItem)

        // THEN - Should return false and showSnackBarInt should be R.string.err_enter_title
        assert(!result)
        assert(saveReminderViewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_enter_title)


        // GIVEN - A data item with invalid location
        dataItem = ReminderDataItem(
            "Cristo Redentor",
            "Comer aquela feijoada",
            null,
            -22.951944,
            -43.210556,
            "2"
        )

        // WHEN - Try to validate it
        result = saveReminderViewModel.validateEnteredData(dataItem)

        // THEN - Should return false and showSnackBarInt should be R.string.err_select_location
        assert(!result)
        assert(saveReminderViewModel.showSnackBarInt.getOrAwaitValue() == R.string.err_select_location)

    }
}