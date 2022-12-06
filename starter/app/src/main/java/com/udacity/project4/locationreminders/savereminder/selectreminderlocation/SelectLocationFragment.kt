package com.udacity.project4.locationreminders.savereminder.selectreminderlocation

import android.annotation.SuppressLint
import android.content.IntentSender
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PointOfInterest
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import com.udacity.project4.R
import com.udacity.project4.base.BaseFragment
import com.udacity.project4.base.NavigationCommand
import com.udacity.project4.databinding.FragmentSelectLocationBinding
import com.udacity.project4.locationreminders.savereminder.SaveReminderViewModel
import com.udacity.project4.utils.*
import org.koin.androidx.viewmodel.ext.android.sharedViewModel

class SelectLocationFragment : BaseFragment(), OnMapReadyCallback {

    //Use Koin to get the view model of the SaveReminder
    override val _viewModel: SaveReminderViewModel by sharedViewModel()
    private lateinit var binding: FragmentSelectLocationBinding

    private var mMap: GoogleMap? = null
    private var selectedPoi: PointOfInterest? = null

//    private val testingPoi:List<PointOfInterest> by inject()

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_select_location, container, false)

        binding.viewModel = _viewModel
        binding.lifecycleOwner = this

        setHasOptionsMenu(true)
        setDisplayHomeAsUpEnabled(true)

        if (allPermissionsGranted(FOREGROUND_LOCATION_PERMISSIONS)) {
            if (allPermissionsGranted(FOREGROUND_LOCATION_PERMISSIONS)) {
                checkLocationSettingsEnabled()
            } else {
                requestForegroundLocationPermission()
            }
        } else {
            requestMissingPermissions(BACKGROUND_LOCATION_PERMISSION)
        }

//        if (!anyPermissionsGranted(FOREGROUND_LOCATION_PERMISSIONS)) {
//            requestMissingPermissions(FOREGROUND_LOCATION_PERMISSIONS)
//        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)


        binding.savePoi.setOnClickListener { view ->
            onLocationSelected()
        }

        // TODO: testing purpose using join
        /*
        for testing, fill selectedPoi with first element in list
         */
//        if (testingPoi.isNotEmpty()){
//            selectedPoi=testingPoi.first()
//        }

        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.M)
    private fun requestForegroundLocationPermission() {
        if (!anyPermissionsGranted(FOREGROUND_LOCATION_PERMISSIONS)) {
            requestMissingPermissions(FOREGROUND_LOCATION_PERMISSIONS)
        }
    }

    private fun checkLocationSettingsEnabled() {
        val builder =
            LocationSettingsRequest.Builder().addLocationRequest(
                LocationRequest.create().apply {
                    priority = LocationRequest.PRIORITY_LOW_POWER
                }
            )

        val client: SettingsClient = LocationServices.getSettingsClient(requireActivity())
        val task: Task<LocationSettingsResponse> = client.checkLocationSettings(builder.build())

        task.addOnFailureListener { exception ->
            if (exception is ResolvableApiException) {
                try {
                    // Show the dialog by calling startResolutionForResult(),
                    // and check the result in onActivityResult().
                    startIntentSenderForResult(
                        exception.resolution.intentSender, Config.REQUEST_CHECK_SETTINGS,
                        null, 0, 0, 0, null
                    )
                } catch (sendEx: IntentSender.SendIntentException) {
                    Snackbar.make(
                        requireView(),
                        R.string.location_required_error,
                        Snackbar.LENGTH_LONG
                    ).setAction(android.R.string.ok) {
                        checkLocationSettingsEnabled()
                    }.show()
                }
            }
        }

        task.addOnCompleteListener {
            if (it.isSuccessful) {
                Log.i("CheckDeviceLocation", "Granted")
                Snackbar.make(
                    requireView(),
                    R.string.location_granted,
                    Snackbar.LENGTH_LONG
                ).show()
            }
        }
    }

    @SuppressLint("MissingPermission")
    @RequiresApi(Build.VERSION_CODES.M)
    fun checkGrantedLocation() {
        if (anyPermissionsGranted(FOREGROUND_LOCATION_PERMISSIONS)) {
            mMap?.isMyLocationEnabled = true

            val fusedLocationProvider =
                LocationServices.getFusedLocationProviderClient(requireActivity())

            val location = fusedLocationProvider.lastLocation

            location.addOnCompleteListener {
                if (it.isSuccessful) it.result?.let { location ->
                    mMap?.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(
                            LatLng(
                                location.latitude,
                                location.longitude
                            ),
                            16f
                        )
                    )
                }
            }
        }
    }

    private fun onLocationSelected() {
        if (selectedPoi != null) {
            _viewModel.selectedPOI.value = selectedPoi
            _viewModel.latitude.value = selectedPoi?.latLng?.latitude
            _viewModel.longitude.value = selectedPoi?.latLng?.longitude
            _viewModel.reminderSelectedLocationStr.value = selectedPoi?.name

            _viewModel.navigationCommand.value =
                NavigationCommand.Back
        } else {
            _viewModel.showSnackBarInt.value = R.string.err_select_location
        }
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.map_options, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.hybrid_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_HYBRID
            true
        }
        R.id.normal_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_NORMAL
            true
        }
        R.id.terrain_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_TERRAIN
            true
        }
        R.id.satellite_map -> {
            mMap?.mapType = GoogleMap.MAP_TYPE_SATELLITE
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(map: GoogleMap) {
        mMap = map

        mMap?.uiSettings?.isZoomControlsEnabled=true
        mMap?.uiSettings?.isScrollGesturesEnabled=true
        mMap?.uiSettings?.isRotateGesturesEnabled=true
        mMap?.setMapStyle(context?.let {
            MapStyleOptions.loadRawResourceStyle(
                it,
                R.raw.gray_map_style
            )
        })

        checkGrantedLocation()

        mMap?.setOnMapClickListener { latLng ->
            mMap?.clear()

            val selectedLocationMarker =
                mMap?.addMarker(MarkerOptions().position(latLng).title(latLng.toString()))

            selectedLocationMarker?.showInfoWindow()

            selectedPoi = PointOfInterest(latLng, latLng.toString(), latLng.toString())
        }

        mMap?.setOnPoiClickListener { poi ->
            mMap?.clear()

            val marker = mMap?.addMarker(MarkerOptions().position(poi.latLng).title(poi.name))

            marker?.showInfoWindow()

            selectedPoi = poi
        }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_PERMISSION_CODE) {
            if (permissions.filter { FOREGROUND_LOCATION_PERMISSIONS.contains(it) }
                    .any { grantResults[permissions.indexOf(it)] == PackageManager.PERMISSION_GRANTED }
            ) {
                checkGrantedLocation()
            } else {
                _viewModel.showSnackBarInt.value = R.string.permission_denied_explanation
            }
        }
    }
}