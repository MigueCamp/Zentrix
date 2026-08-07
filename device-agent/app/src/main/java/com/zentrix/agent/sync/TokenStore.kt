package com.zentrix.agent.sync

import android.content.Context
import android.content.SharedPreferences

/**
 * Persiste el token de dispositivo emitido en el enroll (ver EnrollResponse).
 * Se usa para autenticar el heartbeat y futuros comandos (Fase 2).
 */
object TokenStore {

    private const val PREFS_NAME = "zentrix_agent"
    private const val KEY_DEVICE_TOKEN = "device_token"

    private lateinit var prefs: SharedPreferences

    fun init(context: Context) {
        prefs = context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    fun saveDeviceToken(token: String) {
        prefs.edit().putString(KEY_DEVICE_TOKEN, token).apply()
    }

    fun getDeviceToken(): String? = prefs.getString(KEY_DEVICE_TOKEN, null)

    fun isEnrolled(): Boolean = getDeviceToken() != null

    fun clear() {
        prefs.edit().remove(KEY_DEVICE_TOKEN).apply()
    }
}
