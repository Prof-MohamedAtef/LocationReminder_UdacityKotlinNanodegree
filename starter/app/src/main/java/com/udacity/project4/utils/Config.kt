package com.udacity.project4.utils

object Config {
    val AuthPref: String?="AuthenticationPreferences"
    const val SIGN_IN_INTENT_RESULT_CODE=1000
    const val REQUEST_CHECK_SETTINGS: Int = 0x8a9

    public enum class AuthenticationState {
        AUTHENTICATED, UNAUTHENTICATED
    }
}