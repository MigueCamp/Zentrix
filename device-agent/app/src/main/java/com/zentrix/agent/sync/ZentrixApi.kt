package com.zentrix.agent.sync

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming
import retrofit2.http.Url

data class EnrollRequest(val enrollmentToken: String, val imei: String, val serialNumber: String?)
data class EnrollResponse(val deviceToken: String)
data class HeartbeatRequest(
    val batteryLevel: Int,
    val storageFreeBytes: Long,
    val memoryUsedBytes: Long? = null,
    val memoryTotalBytes: Long? = null,
    val latitude: Double? = null,
    val longitude: Double? = null
)
data class DeviceCommand(val id: Long, val type: String, val payloadJson: String?, val status: String)
data class CommandAckRequest(val status: String, val detail: String?)

/**
 * Contrato con el backend (com.zentrix.device/com.zentrix.command en Spring Boot) — ver
 * docs/04_Especificación_de_Módulos.md, sección 3, y docs/02, sección 4.2 (Cola de Comandos).
 */
interface ZentrixApi {

    @POST("devices/enroll")
    suspend fun enroll(@Body request: EnrollRequest): Response<EnrollResponse>

    @POST("devices/heartbeat")
    suspend fun heartbeat(@Body request: HeartbeatRequest): Response<Unit>

    @GET("devices/commands/pending")
    suspend fun pendingCommands(): Response<List<DeviceCommand>>

    @POST("devices/commands/{id}/ack")
    suspend fun ackCommand(@Path("id") id: Long, @Body request: CommandAckRequest): Response<Unit>

    @Streaming
    @GET
    suspend fun downloadApk(@Url downloadUrl: String): Response<ResponseBody>
}
