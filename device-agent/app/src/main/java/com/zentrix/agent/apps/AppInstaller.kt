package com.zentrix.agent.apps

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageInstaller
import android.os.Build
import android.util.Log
import com.zentrix.agent.sync.ApiClient
import okhttp3.ResponseBody

/**
 * Instalación/desinstalación remota de APKs vía PackageInstaller — módulo Aplicaciones,
 * docs/04_Especificación_de_Módulos.md, sección 5. Si el agente es Device Owner, el sistema
 * instala/desinstala sin diálogo de confirmación; si no, el usuario ve el diálogo estándar
 * de Android (mismo flujo que cualquier instalador de terceros).
 */
object AppInstaller {

    private const val TAG = "AppInstaller"
    private const val INSTALL_ACTION = "com.zentrix.agent.INSTALL_RESULT"
    private const val UNINSTALL_ACTION = "com.zentrix.agent.UNINSTALL_RESULT"

    suspend fun install(context: Context, downloadUrl: String, packageName: String): Result<Unit> {
        return try {
            val response = ApiClient.instance.downloadApk(downloadUrl)
            val body = response.body()
            if (!response.isSuccessful || body == null) {
                return Result.failure(IllegalStateException("No se pudo descargar el APK: HTTP ${response.code()}"))
            }
            writeAndCommitSession(context, body, packageName)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error instalando $packageName", e)
            Result.failure(e)
        }
    }

    fun uninstall(context: Context, packageName: String): Result<Unit> {
        return try {
            val packageInstaller = context.packageManager.packageInstaller
            val pendingIntent = PendingIntent.getBroadcast(
                context, packageName.hashCode(), Intent(UNINSTALL_ACTION),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            packageInstaller.uninstall(packageName, pendingIntent.intentSender)
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e(TAG, "Error desinstalando $packageName", e)
            Result.failure(e)
        }
    }

    private fun writeAndCommitSession(context: Context, apkBody: ResponseBody, packageName: String) {
        val packageInstaller = context.packageManager.packageInstaller
        val params = PackageInstaller.SessionParams(PackageInstaller.SessionParams.MODE_FULL_INSTALL).apply {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) setAppPackageName(packageName)
        }
        val sessionId = packageInstaller.createSession(params)
        val session = packageInstaller.openSession(sessionId)

        session.use { openSession ->
            openSession.openWrite(packageName, 0, apkBody.contentLength()).use { out ->
                apkBody.byteStream().use { input -> input.copyTo(out) }
                openSession.fsync(out)
            }
            val pendingIntent = PendingIntent.getBroadcast(
                context, sessionId, Intent(INSTALL_ACTION),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            openSession.commit(pendingIntent.intentSender)
        }
    }
}
