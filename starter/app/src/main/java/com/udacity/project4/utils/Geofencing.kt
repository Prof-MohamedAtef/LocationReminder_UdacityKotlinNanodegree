package com.udacity.project4.utils

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import android.widget.Toast
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationServices
import com.udacity.project4.R
import com.udacity.project4.locationreminders.geofence.GeofenceBroadcastReceiver
import com.udacity.project4.locationreminders.reminderslist.ReminderDataItem

const val EXTRA_REMINDER = "extra_id_reminder"
const val ACTION_GEOFENCING = "action.geofencing"
const val GEOFENCE_RADIUS_IN_METERS = 200f
const val GEOFENCE_EXPIRATION = Geofence.NEVER_EXPIRE

fun geofencingPendingIntent(context: Context, reminderId: String): PendingIntent {
    val intent = Intent(context, GeofenceBroadcastReceiver::class.java)
    intent.action = ACTION_GEOFENCING
    intent.putExtra(EXTRA_REMINDER, reminderId)
    // TODO: App crashes when adding a Geofence (Nexus 7, API 32):
    /*
    todo solution: added PendingIntent.FLAG_MUTABLE.
     */
    return PendingIntent.getBroadcast(context, 0, intent,PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_ONE_SHOT)
}

@SuppressLint("MissingPermission")
fun addGeofencingRequest(context: Context, reminder: ReminderDataItem): Boolean {

    if (reminder.latitude == null || reminder.longitude == null)
        return false

    val geofencingClient = LocationServices.getGeofencingClient(context)

    val geofence = Geofence.Builder()
        .setRequestId(reminder.id)
        .setCircularRegion(
            reminder.latitude!!,
            reminder.longitude!!,
            GEOFENCE_RADIUS_IN_METERS
        )
        .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
        .setExpirationDuration(GEOFENCE_EXPIRATION)
        .build()

    val geofencingRequest = GeofencingRequest.Builder()
        .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
        .addGeofence(geofence)
        .build()

    val pendingIntent = geofencingPendingIntent(context, reminder.id)

    geofencingClient.addGeofences(geofencingRequest, pendingIntent)?.run {
        addOnSuccessListener {
            Toast.makeText(context, R.string.geofence_added,Toast.LENGTH_LONG).show()
            Log.d("SaveReminderGeofencing","Add Geofence no. "+ geofence.requestId)
        }
        addOnFailureListener {
            Toast.makeText(context, R.string.geofences_not_added,Toast.LENGTH_LONG).show()
            if ((it.message!=null)){
                Log.d("SaveReminderGeofencing",it.message!!)
            }
        }
    }
    return true
}

fun removeGeofences(context: Context, geofences: List<Geofence>){
    val geofencingClient=LocationServices.getGeofencingClient(context)

    geofencingClient.removeGeofences(geofences.map { it.requestId })
}