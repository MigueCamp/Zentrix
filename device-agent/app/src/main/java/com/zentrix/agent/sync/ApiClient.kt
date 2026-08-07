package com.zentrix.agent.sync

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Cliente HTTP hacia el backend. 10.0.2.2 es el alias del emulador Android
 * hacia el localhost del host (backend corriendo vía infra/docker/docker-compose.yml).
 * En Fase 2 esto se mueve a variantes de build (dev/staging/prod).
 */
object ApiClient {

    private const val BASE_URL = "http://10.0.2.2:8080/"

    private val authInterceptor = Interceptor { chain ->
        val token = TokenStore.getDeviceToken()
        val request = if (token != null) {
            chain.request().newBuilder().addHeader("Authorization", "Bearer $token").build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }

    val instance: ZentrixApi by lazy {
        val client = OkHttpClient.Builder()
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .addInterceptor(authInterceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ZentrixApi::class.java)
    }
}
