package com.udacity.project4.locationreminders

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.udacity.project4.R
import com.udacity.project4.databinding.FragmentReminderDescriptionMapBinding
import com.udacity.project4.utils.FOREGROUND_LOCATION_PERMISSIONS
import com.udacity.project4.utils.anyPermissionsGranted
import com.udacity.project4.utils.requestMissingPermissions

private const val ARG_PARAM1_Latitude = "Latitude"
private const val ARG_PARAM2_Longitude = "Longitude"
private const val ARG_PARAM3_Title = "Title"

class ReminderDescriptionMapFragment : Fragment(), OnMapReadyCallback {

    private var title: String?=null
    private var longitude: String?=null
    private var latitude: String?=null
    private var mMap: GoogleMap?=null
    private lateinit var binding: FragmentReminderDescriptionMapBinding


    @RequiresApi(Build.VERSION_CODES.M)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_reminder_description_map, container, false)
        if (!anyPermissionsGranted(FOREGROUND_LOCATION_PERMISSIONS)) {
            requestMissingPermissions(FOREGROUND_LOCATION_PERMISSIONS)
        }

        val mapFragment = childFragmentManager
            .findFragmentById(R.id.map_fragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        latitude= arguments?.get(ARG_PARAM1_Latitude) as String?
        longitude= arguments?.get(ARG_PARAM2_Longitude) as String?
        title=arguments?.get(ARG_PARAM3_Title) as String?

        return binding.root
    }

    companion object {
        @JvmStatic
        fun newInstance(latitude: String, longitude: String, title: String) =
            ReminderDescriptionMapFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1_Latitude, latitude)
                    putString(ARG_PARAM2_Longitude, longitude)
                    putString(ARG_PARAM3_Title, title)
                }
            }
    }

    @RequiresApi(Build.VERSION_CODES.M)
    override fun onMapReady(map: GoogleMap?) {
        mMap=map
        mMap?.clear()
        mMap?.setMapStyle(context?.let { MapStyleOptions.loadRawResourceStyle(it, R.raw.gray_map_style) })


        if (latitude!=null&&longitude!=null){
            mMap?.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    latLng(),
                    16f
                )
            )
        }


        val selectedLocationMarker=mMap?.addMarker(MarkerOptions().position(latLng()).title(title))
        selectedLocationMarker?.showInfoWindow()
    }

    fun latLng(): LatLng {
        return LatLng(
            latitude!!.toDouble(),
            longitude!!.toDouble()
        )
    }
}