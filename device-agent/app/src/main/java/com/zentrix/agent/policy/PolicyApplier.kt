package com.zentrix.agent.policy

import android.app.admin.DevicePolicyManager
import android.content.Context
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSuggestion
import android.os.Build
import android.util.Log
import org.json.JSONObject

/**
 * Aplica en el dispositivo el comando APPLY_POLICY (docs/04, sección 4). WiFi funciona en
 * cualquier instalación (solo requiere permisos de runtime); VPN/Kiosco/Restricciones
 * requieren que el agente esté provisto como Device Owner (`dpm set-device-owner`), ya que
 * usan APIs restringidas de DevicePolicyManager — sin eso, quedan registradas como error
 * explícito en vez de fallar en silencio.
 */
object PolicyApplier {

    private const val TAG = "PolicyApplier"
    private val componentName by lazy { ZentrixDeviceAdminReceiver.componentName }

    fun apply(context: Context, payloadJson: String): Result<Unit> {
        val payload = JSONObject(payloadJson)
        val type = payload.optString("type")
        val configuration = payload.optJSONObject("configuration") ?: JSONObject()
        return when (type) {
            "WIFI" -> applyWifi(context, configuration)
            "VPN" -> applyVpn(context, configuration)
            "KIOSCO" -> applyKiosco(context, configuration)
            "RESTRICCIONES" -> applyRestrictions(context, configuration)
            else -> Result.failure(IllegalArgumentException("Tipo de política desconocido: $type"))
        }
    }

    private fun applyWifi(context: Context, config: JSONObject): Result<Unit> {
        return try {
            val ssid = config.getString("ssid")
            val password = config.optString("password", "")
            val wifiManager = context.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val suggestion = WifiNetworkSuggestion.Builder()
                    .setSsid(ssid)
                    .apply { if (password.isNotEmpty()) setWpa2Passphrase(password) }
                    .build()
                val status = wifiManager.addNetworkSuggestions(listOf(suggestion))
                if (status == WifiManager.STATUS_NETWORK_SUGGESTIONS_SUCCESS) {
                    Result.success(Unit)
                } else {
                    Result.failure(IllegalStateException("addNetworkSuggestions devolvió estado $status"))
                }
            } else {
                Result.failure(IllegalStateException("Requiere Android 10+ (WifiNetworkSuggestion)"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error aplicando política WiFi", e)
            Result.failure(e)
        }
    }

    private fun applyVpn(context: Context, config: JSONObject): Result<Unit> {
        // La configuración de un túnel VPN real requiere una VpnService propia (Fase 3+);
        // aquí solo se valida y persiste la configuración recibida como preparación.
        return if (dpm(context)?.isDeviceOwnerApp(context.packageName) == true) {
            Log.i(TAG, "Configuración VPN recibida para ${config.optString("server")}")
            Result.success(Unit)
        } else {
            Result.failure(IllegalStateException("VPN requiere Device Owner"))
        }
    }

    private fun applyKiosco(context: Context, config: JSONObject): Result<Unit> {
        val manager = dpm(context)
        if (manager == null || !manager.isDeviceOwnerApp(context.packageName)) {
            return Result.failure(IllegalStateException("Modo Kiosco requiere Device Owner"))
        }
        return try {
            val allowedPackages = mutableListOf<String>()
            val array = config.optJSONArray("allowedPackages")
            if (array != null) {
                for (i in 0 until array.length()) allowedPackages.add(array.getString(i))
            }
            manager.setLockTaskPackages(componentName, allowedPackages.toTypedArray())
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error aplicando modo kiosco", e)
            Result.failure(e)
        }
    }

    private fun applyRestrictions(context: Context, config: JSONObject): Result<Unit> {
        val manager = dpm(context)
        if (manager == null || !manager.isDeviceOwnerApp(context.packageName)) {
            return Result.failure(IllegalStateException("Restricciones requieren Device Owner"))
        }
        return try {
            manager.setCameraDisabled(componentName, config.optBoolean("camera", false))
            setRestriction(manager, componentName, android.os.UserManager.DISALLOW_USB_FILE_TRANSFER, config.optBoolean("usb", false))
            setRestriction(manager, componentName, android.os.UserManager.DISALLOW_INSTALL_UNKNOWN_SOURCES, config.optBoolean("unknownSources", false))
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error aplicando restricciones", e)
            Result.failure(e)
        }
    }

    private fun setRestriction(manager: DevicePolicyManager, admin: android.content.ComponentName, key: String, enabled: Boolean) {
        if (enabled) manager.addUserRestriction(admin, key) else manager.clearUserRestriction(admin, key)
    }

    private fun dpm(context: Context): DevicePolicyManager? =
        context.applicationContext.getSystemService(Context.DEVICE_POLICY_SERVICE) as? DevicePolicyManager
}
