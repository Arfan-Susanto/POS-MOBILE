package com.possaas

import android.content.Context
import android.content.SharedPreferences

object StoreState {
    private const val PREFS = "store_prefs"
    private const val KEY_CLOSED = "is_closed"
    private var prefs: SharedPreferences? = null

    fun init(context: Context) {
        if (prefs == null) {
            prefs = context.applicationContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        }
    }

    var isClosed: Boolean
        get() = prefs?.getBoolean(KEY_CLOSED, false) ?: false
        set(value) { prefs?.edit()?.putBoolean(KEY_CLOSED, value)?.apply() }

    fun registerListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs?.registerOnSharedPreferenceChangeListener(listener)
    }

    fun unregisterListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        prefs?.unregisterOnSharedPreferenceChangeListener(listener)
    }
}