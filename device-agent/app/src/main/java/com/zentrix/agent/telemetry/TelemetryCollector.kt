package com.zentrix.agent.telemetry

import android.Manifest
import android.app.ActivityManager
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat

/** Memoria y ubicación reportadas en el heartbeat — módulo Monitoreo, docs/04, sección 6. */
object TelemetryCollector {

    data class Memory(val usedBytes: Long, val totalBytes: Long)

    fun memory(context: Context): Memory {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val info = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(info)
        return Memory(usedBytes = info.totalMem - info.availMem, totalBytes = info.totalMem)
    }

    /** Devuelve null si el permiso de ubicación no fue otorgado; no lo solicita (se pide en enrollment). */
    fun lastKnownLocation(context: Context): Location? {
        val granted = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) ==
            PackageManager.PERMISSION_GRANTED
        if (!granted) return null

        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
            .filter { locationManager.isProviderEnabled(it) }
            .mapNotNull { runCatching { locationManager.getLastKnownLocation(it) }.getOrNull() }
            .maxByOrNull { it.time }
    }
}
