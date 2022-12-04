package com.udacity.project4.locationreminders.savereminder

import android.app.Activity
import android.content.Intent
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSaveReminderBinding
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem
import com.udacity.project4.utils.*
import com.udacity.project4.utils.Config.REQUEST_CHECK_SETTINGS
import kotlinx.android.synthetic.main.fragment_save_reminder.*
import org.koin.android.ext.android.inject

class SaveReminderFragment : BaseFragment(){
    //Get the view model this time as a single to be shared with the another fragment
    override val _viewModel: SaveReminderViewModel by inject()
    private lateinit var binding: FragmentSaveReminderBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_save_reminder, container, false)

        setDisplayHomeAsUpEnabled(true)
        setTitle(getString(R.string.save_reminder))

        binding.viewModel = _viewModel

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lifecycleOwner = this
        binding.selectLocation.setOnClickListener {
            //            Navigate to another fragment to get the user location
            _viewModel.navigationCommand.value =
                NavigationCommand.To(SaveReminderFragmentDirections.actionSaveReminderFragmentToSelectLocationFragment())
        }

        binding.saveReminder.setOnClickListener {
            saveReminder()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun saveReminder() {
        val title = _viewModel.reminderTitle.value
        val description = _viewModel.reminderDescription.value
        val location = _viewModel.reminderSelectedLocationStr.value
        val latitude = _viewModel.latitude.value
        val longitude = _viewModel.longitude.value

        val reminder = ReminderDataItem(
            title, description, location, latitude, longitude
        )

        if (allPermissionsGranted(BACKGROUND_LOCATION_PERMISSION) &&
            anyPermissionsGranted(FOREGROUND_LOCATION_PERMISSIONS)
        ) {
            checkLocationSettingsEnabled {
                addGeofencingRequest(requireContext(), reminder)
                _viewModel.validateAndSaveReminder(reminder)
            }
        } else {
            val permission = BACKGROUND_LOCATION_PERMISSION + FOREGROUND_LOCATION_PERMISSIONS
            requestMissingPermissions(permission)
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onStart() {
        super.onStart()
        if (allPermissionsGranted(BACKGROUND_LOCATION_PERMISSION)&&
                anyPermissionsGranted(FOREGROUND_LOCATION_PERMISSIONS)){
            checkLocationSettingsEnabled { null }
        }else{
            requestForegroundAndBackgroundLocationPermissions()
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestForegroundAndBackgroundLocationPermissions() {
        if (!anyPermissionsGranted(FOREGROUND_LOCATION_PERMISSIONS)) {
            requestMissingPermissions(FOREGROUND_LOCATION_PERMISSIONS)
        }
    }

    private fun checkLocationSettingsEnabled(function: () -> Unit) {
        val builder=
            LocationSettingsRequest.Builder().addLocationRequest(
                LocationRequest.create().apply {
                    priority=LocationRequest.PRIORITY_LOW_POWER
                }
            )

        val client: SettingsClient =LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener{ exception ->
            if (exception is ResolvableApiException){
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    startIntentSenderForResult(
                        exception.resolution.intentSender, REQUEST_CHECK_SETTINGS,
                        null, 0, 0, 0, null
                    )
                }catch (sendEx: IntentSender.SendIntentException){
                    Snackbar.make(
                        requireView(),
                        R.string.location_required_error,
                        Snackbar.LENGTH_LONG
                    ).setAction(android.R.string.ok) {
                        checkLocationSettingsEnabled(function)
                    }.show()
                }
            }
        }

        task.addOnCompleteListener{
            if (it.isSuccessful){
                Log.i("CheckDeviceLocation", "Granted")
                Snackbar.make(
                    requireView(),
                    R.string.location_granted,
                    Snackbar.LENGTH_LONG
                ).show()
                function()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode== REQUEST_PERMISSION_CODE){
            val foreground=permissions.filter { FOREGROUND_LOCATION_PERMISSIONS.contains(it) }
            val anyForeground =
                foreground.isEmpty() || foreground.any{ grantResults[permissions.indexOf(it)] == PackageManager.PERMISSION_GRANTED }

            val background = permissions.filter { BACKGROUND_LOCATION_PERMISSION.contains(it) }
            val allBackground =
                background.isEmpty() || background.all { grantResults[permissions.indexOf(it)] == PackageManager.PERMISSION_GRANTED }

            if (anyForeground && allBackground){
                /*
                save reminders
                 */
                saveReminder()
            }else{
                _viewModel.showSnackBarInt.value=R.string.permission_denied_explanation
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode== REQUEST_CHECK_SETTINGS){
            if (resultCode== Activity.RESULT_OK){
                saveReminder()
            }else{
                _viewModel.showSnackBarInt.value=R.string.location_required_error
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        //make sure to clear the view model after destroy, as it's a single view model.
        _viewModel.onClear()
    }
}