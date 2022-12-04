package com.udacity.project4.authentication

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.udacity.project4.utils.Config

class FakeAuthController (private val logged: Boolean = true):AuthenticationViewModel() {
    private val _logged: MutableLiveData<Boolean> =
        MutableLiveData(logged)

    override fun signIn(context: Context) {
        _logged.value = true
    }

    override fun signOut() {
        _logged.value = false
    }

    override fun getLoginState(): LiveData<Boolean> {
        return _logged
    }
}