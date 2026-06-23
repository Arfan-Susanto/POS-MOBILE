package com.possaas

import android.content.Context
import android.content.SharedPreferences

class SessionManager(context: Context) {

    private val preferences: SharedPreferences =
        context.getSharedPreferences(
            "session_prefs",
            Context.MODE_PRIVATE
        )

    companion object {
        private const val KEY_EMAIL = "session_email"
        private const val KEY_PASSWORD = "session_password"
        private const val KEY_ROLE = "session_role"
        private const val KEY_UID = "session_uid"
        private const val KEY_IS_LOGGED_IN = "session_is_logged_in"
    }

    fun saveLoginSession(
        email: String,
        password: String,
        role: String,
        uid: String
    ) {
        preferences.edit().apply {
            putString(KEY_EMAIL, email)
            putString(KEY_PASSWORD, password)
            putString(KEY_ROLE, role)
            putString(KEY_UID, uid)
            putBoolean(KEY_IS_LOGGED_IN, true)
            apply()
        }
    }

    fun getEmail(): String? =
        preferences.getString(KEY_EMAIL, null)

    fun getPassword(): String? =
        preferences.getString(KEY_PASSWORD, null)

    fun getRole(): String? =
        preferences.getString(KEY_ROLE, null)

    fun getUid(): String? =
        preferences.getString(KEY_UID, null)

    fun isLoggedIn(): Boolean =
        preferences.getBoolean(KEY_IS_LOGGED_IN, false)

    fun clearSession() {
        preferences.edit().apply {
            remove(KEY_EMAIL)
            remove(KEY_PASSWORD)
            remove(KEY_ROLE)
            remove(KEY_UID)
            putBoolean(KEY_IS_LOGGED_IN, false)
            apply()
        }
    }
}

