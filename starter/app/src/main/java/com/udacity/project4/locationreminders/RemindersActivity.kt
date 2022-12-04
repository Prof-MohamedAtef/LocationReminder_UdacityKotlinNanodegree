package com.udacity.project4.locationreminders

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.NavigationUI
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.udacity.project4.R
import com.udacity.project4.authentication.AuthenticationActivity
import com.udacity.project4.authentication.AuthenticationViewModel
import com.udacity.project4.databinding.ActivityRemindersBinding
import com.udacity.project4.prefs
import com.udacity.project4.utils.Config
import kotlinx.android.synthetic.main.activity_reminders.*
import org.koin.android.ext.android.inject

/**
 * The RemindersActivity that holds the reminders fragments
 */
class RemindersActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding:ActivityRemindersBinding

    private val authenticationViewModel by inject<AuthenticationViewModel>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding= DataBindingUtil.setContentView(this,R.layout.activity_reminders)


        authenticationViewModel.authenticationState.observe(this, Observer { authState->
            if (authState==Config.AuthenticationState.UNAUTHENTICATED){
                goToAuthenticationActivity()
            }
        })

        val navHostFragment=supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController=navHostFragment.navController
        appBarConfiguration= AppBarConfiguration(
            setOf(
                R.id.reminderListFragment,
                R.id.saveReminderFragment,
                R.id.selectLocationFragment
            )
        )
        setupActionBarWithNavController(navController,
            appBarConfiguration)
    }

    private fun goToAuthenticationActivity() {
        val authIntent = Intent(this, AuthenticationActivity::class.java)
        startActivity(authIntent)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.logout-> {
                prefs.clearPrefs()
                FirebaseAuth.getInstance().signOut()
                goToAuthenticationActivity()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onSupportNavigateUp(): Boolean {
        val navController = this.findNavController(R.id.nav_host_fragment)
        return NavigationUI.navigateUp(navController, appBarConfiguration)
    }
}