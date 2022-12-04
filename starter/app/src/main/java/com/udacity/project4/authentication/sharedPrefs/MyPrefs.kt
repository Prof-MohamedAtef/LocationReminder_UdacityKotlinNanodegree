package com.udacity.project4.authentication.sharedPrefs

import android.content.Context
import android.content.SharedPreferences
import com.udacity.project4.utils.Config

class MyPrefs(context: Context) {
    private val UserNameHandler:String? = "UserNameHandler"
    private val preferences: SharedPreferences = context.getSharedPreferences(Config.AuthPref,Context.MODE_PRIVATE)
    var UserName: String?
        get() = preferences.getString(UserNameHandler, null)
        set(value) = preferences.edit().putString(UserNameHandler, value).apply()

    fun clearPrefs(){
        preferences.edit().clear().commit()
    }
}