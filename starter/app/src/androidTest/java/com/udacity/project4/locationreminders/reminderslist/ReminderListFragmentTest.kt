package com.udacity.project4.locationreminders.reminderslist

import android.app.Application
import android.os.Bundle
import androidx.fragment.app.testing.launchFragmentInContainer
import androidx.navigation.NavController
import androidx.navigation.Navigation
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.IdlingRegistry
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.filters.MediumTest
import com.udacity.project4.R
import com.udacity.project4.locationreminders.data.ReminderDataSource
import com.udacity.project4.locationreminders.data.dto.ReminderDTO
import com.udacity.project4.locationreminders.data.local.LocalDB
import com.udacity.project4.locationreminders.data.local.RemindersLocalRepository
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.util.DataBindingIdlingResource
import com.udacity.project4.util.monitorFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.context.stopKoin
import org.koin.dsl.module
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

@RunWith(AndroidJUnit4::class)
@ExperimentalCoroutinesApi
@MediumTest
class ReminderListFragmentTest {
    private lateinit var repository : ReminderDataSource
    private lateinit var context: Application

    // An idling resource that waits for Data Binding to have no pending bindings.
    private val dataBindingIdlingResource = DataBindingIdlingResource()

    @Before
    fun registerIdlingResource(){
        IdlingRegistry.getInstance().register(dataBindingIdlingResource)
    }

    @After
    fun unregisterIdlingResource(){
        IdlingRegistry.getInstance().unregister(dataBindingIdlingResource)
    }

    @Before
    fun initRepo(){
        stopKoin()//stop the original app koin
        context = getApplicationContext()
        val module= module {
            //Declare a ViewModel - be later inject into Fragment with dedicated injector using by viewModel()
            viewModel {
                RemindersListViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }
            //Declare singleton definitions to be later injected using by inject()
            single {
                SaveReminderViewModel(
                    context,
                    get() as ReminderDataSource
                )
            }
            single { RemindersLocalRepository(get()) as ReminderDataSource }
            single { LocalDB.createRemindersDao(context) }

        }

        //declare a new koin module
        startKoin {
            modules(listOf(module))
        }
        //Get our real repository
        repository = GlobalContext.get().koin.get()

        runBlocking { repository.deleteAllReminders() }
    }

    @Test
    fun navigateToAddFragment(){
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)

        val navController = mock(NavController::class.java)
        fragmentScenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        //Espresso
        onView(withId(R.id.addReminderFAB)).perform(click())
        //Mockito
        verify(navController).navigate(ReminderListFragmentDirections.toSaveReminder())
    }

    //    TODO: add testing for the error messages.
    @Test
    fun testErrorReturned(){
        val fragmentScenario = launchFragmentInContainer<ReminderListFragment>(Bundle(), R.style.AppTheme)
        dataBindingIdlingResource.monitorFragment(fragmentScenario)
        onView(withText(R.string.no_data))
            .check(ViewAssertions.matches(isDisplayed()))
    }

    //    TODO: test the displayed data on the UI.
    @Test
    fun validReminder_displaysOnScreen() {
        val myDataObject = ReminderDTO(
            title = "My Home",
            description = "My Home is in Sinai",
            location = "El-Arish",
            latitude = 31.6374,
            longitude = 30.9853
        )

        runBlocking {
            repository.saveReminder(myDataObject)
        }

        val scenario =
            launchFragmentInContainer<ReminderListFragment>(Bundle.EMPTY, R.style.AppTheme)
        val navController = mock(NavController::class.java)
        dataBindingIdlingResource.monitorFragment(scenario)

        scenario.onFragment {
            Navigation.setViewNavController(it.view!!, navController)
        }

        onView(withText(myDataObject.title)).check(matches(isDisplayed()))
        onView(withText(myDataObject.description)).check(matches(isDisplayed()))
        onView(withText(myDataObject.location)).check(matches(isDisplayed()))
    }
}