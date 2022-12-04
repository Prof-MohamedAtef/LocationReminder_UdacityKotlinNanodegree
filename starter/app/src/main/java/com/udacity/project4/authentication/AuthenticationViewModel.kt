package com.udacity.project4.authentication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.map
import com.udacity.project4.utils.Config

open class AuthenticationViewModel:ViewModel() {
    private val logged:Boolean=false
    private val _logged: MutableLiveData<Boolean> =
        MutableLiveData(logged)
    val authenticationState=FirebaseLiveData().map { firebaseUser ->
        if (firebaseUser!=null){
            Config.AuthenticationState.AUTHENTICATED
        }else{
            Config.AuthenticationState.UNAUTHENTICATED
        }
    }

    open fun signIn(context: Context) {}
    open fun signOut() {}
    open fun getLoginState(): LiveData<Boolean> {
        return _logged
    }
}