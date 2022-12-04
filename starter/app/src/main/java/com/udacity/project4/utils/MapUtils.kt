package com.udacity.project4.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment

val REQUEST_PERMISSION_CODE = 1010;

val FOREGROUND_LOCATION_PERMISSIONS = listOf(
    Manifest.permission.ACCESS_COARSE_LOCATION,
    Manifest.permission.ACCESS_FINE_LOCATION
)

val BACKGROUND_LOCATION_PERMISSION =
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)
        listOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
    else emptyList()


@RequiresApi(Build.VERSION_CODES.M)
fun Activity.requestMissingPermissions(
    permissions: List<String>,
    requestCode: Int = REQUEST_PERMISSION_CODE
) {
    requestPermissions(
        (permissions.filter {
            checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }).toTypedArray(),
        requestCode
    )
}

@RequiresApi(Build.VERSION_CODES.M)
fun Fragment.requestMissingPermissions(
    permissions: List<String>,
    requestCode: Int = REQUEST_PERMISSION_CODE
) {
    requestPermissions(
        (permissions.filter {
            requireActivity().checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED
        }).toTypedArray(),
        requestCode
    )
}

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.allPermissionsGranted(permissions: List<String>): Boolean {
    return permissions.none { checkSelfPermission(it) != PackageManager.PERMISSION_GRANTED }
}

@RequiresApi(Build.VERSION_CODES.M)
fun Fragment.allPermissionsGranted(permissions: List<String>): Boolean {
    return requireActivity().allPermissionsGranted(permissions)
}

@RequiresApi(Build.VERSION_CODES.M)
fun Activity.anyPermissionsGranted(permissions: List<String>): Boolean {
    return permissions.any { checkSelfPermission(it) == PackageManager.PERMISSION_GRANTED }
}

@RequiresApi(Build.VERSION_CODES.M)
fun Fragment.anyPermissionsGranted(permissions: List<String>): Boolean {
    return requireActivity().anyPermissionsGranted(permissions)
}