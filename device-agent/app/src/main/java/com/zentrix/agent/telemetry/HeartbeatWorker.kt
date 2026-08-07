package com.zentrix.agent.telemetry

import android.content.Context
import android.os.BatteryManager
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.zentrix.agent.commands.CommandProcessor
import com.zentrix.agent.sync.ApiClient
import com.zentrix.agent.sync.HeartbeatRequest

/**
 * Reporta batería/almacenamiento periódicamente (módulo Monitoreo,
 * docs/04_Especificación_de_Módulos.md sección 6) y, en el mismo ciclo, hace polling
 * de la Cola de Comandos (docs/02, sección 4.2) — aplica políticas e instala/desinstala
 * apps pendientes. Se programa desde enrollment una vez completada la inscripción.
 */
class HeartbeatWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return try {
            val batteryManager = applicationContext.getSystemService(Context.BATTERY_SERVICE) as BatteryManager
            val batteryLevel = batteryManager.getIntProperty(BatteryManager.BATTERY_PROPERTY_CAPACITY)
            val storageFreeBytes = applicationContext.filesDir.freeSpace
            val memory = TelemetryCollector.memory(applicationContext)
            val location = TelemetryCollector.lastKnownLocation(applicationContext)

            val response = ApiClient.instance.heartbeat(
                HeartbeatRequest(
                    batteryLevel = batteryLevel,
                    storageFreeBytes = storageFreeBytes,
                    memoryUsedBytes = memory.usedBytes,
                    memoryTotalBytes = memory.totalBytes,
                    latitude = location?.latitude,
                    longitude = location?.longitude
                )
            )
            if (!response.isSuccessful) return Result.retry()

            CommandProcessor.syncPendingCommands(applicationContext)
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val WORK_NAME = "zentrix-heartbeat"
    }
}
