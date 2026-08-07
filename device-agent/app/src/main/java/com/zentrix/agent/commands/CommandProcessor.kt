package com.zentrix.agent.commands

import android.content.Context
import android.util.Log
import com.zentrix.agent.apps.AppInstaller
import com.zentrix.agent.policy.PolicyApplier
import com.zentrix.agent.sync.ApiClient
import com.zentrix.agent.sync.CommandAckRequest
import com.zentrix.agent.sync.DeviceCommand
import org.json.JSONObject

/**
 * Ejecuta en el dispositivo los comandos entregados por /devices/commands/pending
 * y reporta el resultado con /devices/commands/{id}/ack — Cola de Comandos,
 * docs/02_Arquitectura_del_Sistema.md, sección 4.2.
 */
object CommandProcessor {

    private const val TAG = "CommandProcessor"

    suspend fun syncPendingCommands(context: Context) {
        val response = ApiClient.instance.pendingCommands()
        val commands = response.body().orEmpty()
        for (command in commands) {
            val result = execute(context, command)
            ack(command, result)
        }
    }

    private suspend fun execute(context: Context, command: DeviceCommand): Result<Unit> {
        val payload = command.payloadJson
        if (payload == null) {
            return Result.failure(IllegalArgumentException("Comando sin payload"))
        }
        return try {
            when (command.type) {
                "APPLY_POLICY" -> PolicyApplier.apply(context, payload)
                "INSTALL_APP" -> {
                    val json = JSONObject(payload)
                    AppInstaller.install(context, json.getString("downloadUrl"), json.getString("packageName"))
                }
                "UNINSTALL_APP" -> {
                    val json = JSONObject(payload)
                    AppInstaller.uninstall(context, json.getString("packageName"))
                }
                else -> Result.failure(IllegalArgumentException("Tipo de comando desconocido: ${command.type}"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error ejecutando comando ${command.id}", e)
            Result.failure(e)
        }
    }

    private suspend fun ack(command: DeviceCommand, result: Result<Unit>) {
        val request = if (result.isSuccess) {
            CommandAckRequest(status = "COMPLETADO", detail = null)
        } else {
            CommandAckRequest(status = "ERROR", detail = result.exceptionOrNull()?.message?.take(500))
        }
        try {
            ApiClient.instance.ackCommand(command.id, request)
        } catch (e: Exception) {
            Log.e(TAG, "No se pudo confirmar el comando ${command.id}", e)
        }
    }
}
