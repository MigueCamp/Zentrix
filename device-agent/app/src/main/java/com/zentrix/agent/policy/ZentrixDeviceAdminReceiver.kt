package com.zentrix.agent.policy

import android.app.admin.DeviceAdminReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.util.Log

/**
 * Punto de entrada de las políticas del dispositivo (Device Owner / Device Admin).
 * La aplicación de perfiles concretos (WiFi, VPN, kiosco, restricciones) vive en
 * PolicyApplier, que usa `componentName` para las llamadas a DevicePolicyManager
 * que requieren Device Owner.
 */
class ZentrixDeviceAdminReceiver : DeviceAdminReceiver() {

    override fun onEnabled(context: Context, intent: Intent) {
        super.onEnabled(context, intent)
        Log.i(TAG, "Zentrix Agent habilitado como administrador del dispositivo")
    }

    override fun onDisabled(context: Context, intent: Intent) {
        super.onDisabled(context, intent)
        Log.w(TAG, "Zentrix Agent deshabilitado como administrador del dispositivo")
    }

    companion object {
        private const val TAG = "ZentrixDeviceAdmin"

        val componentName: ComponentName
            get() = ComponentName(
                "com.zentrix.agent",
                ZentrixDeviceAdminReceiver::class.java.name
            )
    }
}
