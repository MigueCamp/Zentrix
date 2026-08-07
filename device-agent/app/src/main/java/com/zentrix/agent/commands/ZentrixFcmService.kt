package com.zentrix.agent.commands

import android.util.Log
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.zentrix.agent.telemetry.HeartbeatWorker

/**
 * Recibe el push de FCM que despierta al agente para consultar comandos pendientes en la
 * API sin esperar al próximo heartbeat periódico — flujo descrito en
 * docs/02_Arquitectura_del_Sistema.md, sección 4.2. Requiere un `google-services.json`
 * real (ver device-agent/README.md); sin él este servicio nunca se invoca y el agente
 * sigue funcionando igual vía el polling de comandos dentro de HeartbeatWorker.
 */
class ZentrixFcmService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.i(TAG, "Comando pendiente notificado por FCM: ${message.data}")
        val request = OneTimeWorkRequestBuilder<HeartbeatWorker>().build()
        WorkManager.getInstance(applicationContext).enqueue(request)
    }

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // TODO(Fase 1): enviar el token al backend junto con el token de enrollment
        Log.i(TAG, "Nuevo token FCM generado")
    }

    companion object {
        private const val TAG = "ZentrixFcmService"
    }
}
