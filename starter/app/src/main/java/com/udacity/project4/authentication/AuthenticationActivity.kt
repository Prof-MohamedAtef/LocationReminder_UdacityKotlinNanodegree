package com.udacity.project4.authentication

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import com.firebase.ui.auth.AuthMethodPickerLayout
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.udacity.project4.R
import com.udacity.project4.utils.Config
import com.udacity.project4.utils.Config.SIGN_IN_INTENT_RESULT_CODE
import com.udacity.project4.locationreminders.RemindersActivity
import com.udacity.project4.prefs
import org.koin.android.ext.android.inject

/**
 * This class should be the starting point of the app, It asks the users to sign in / register, and redirects the
 * signed in users to the RemindersActivity.
 */
class AuthenticationActivity : AppCompatActivity() {

    private val authenticationViewModel by inject<AuthenticationViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_authentication)

        if (prefs.UserName!=null){
            goToReminderActivity()
        }else{
            findViewById<Button>(R.id.btnSignIn).setOnClickListener { launchGoogleSignInIntent() }

            authenticationViewModel.authenticationState.observe(this, Observer { state->
                when(state){
                    Config.AuthenticationState.AUTHENTICATED->{
                        goToReminderActivity()
                    }
                    else->{
                        Log.e("FirebaseAuthState", "User not authenticated: $state")
                    }
                }
            })
        }
    }

    private fun goToReminderActivity() {
        val reminderIntent = Intent(this, RemindersActivity::class.java)
        startActivity(reminderIntent)
    }

    private fun launchGoogleSignInIntent() {
        val providersList= arrayListOf(
            AuthUI.IdpConfig.EmailBuilder().build(), AuthUI.IdpConfig.GoogleBuilder().build()
        )
        val customLayout = AuthMethodPickerLayout.Builder(R.layout.signin_layout)
            .setGoogleButtonId(R.id.btnGoogleSignIn)
            .setEmailButtonId(R.id.btnEmailLogin)
            .build()

        val signInIntent=AuthUI.getInstance().createSignInIntentBuilder()
            .setAuthMethodPickerLayout(customLayout)
            .setAvailableProviders(providersList)
            .build()

        startActivityForResult(signInIntent, SIGN_IN_INTENT_RESULT_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == SIGN_IN_INTENT_RESULT_CODE) {
            val response = IdpResponse.fromResultIntent(data)
            if (resultCode == Activity.RESULT_OK) {
                prefs.UserName=response?.email.toString()
                goToReminderActivity()
            } else {
                Log.e("Authentication Error", response?.error.toString())
                val message = "Sign in unsuccessful ${response?.error?.errorCode}"
                Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
            }
        }
    }
}