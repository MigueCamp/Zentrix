# Device Agent — Agente Android (DPC)

Aplicación Android que actúa como **Device Policy Controller (DPC)** en cada dispositivo administrado. Se ejecuta con privilegios de *Device Owner* y es responsable de aplicar en el dispositivo lo que se configura desde la consola web.

## Responsabilidades

- Enrollment (registro) del dispositivo contra el backend de Zentrix.
- Aplicar políticas recibidas: configuración WiFi, VPN, modo kiosco, restricciones.
- Instalar, actualizar y desinstalar aplicaciones (APK) por orden del backend.
- Reportar telemetría periódica: ubicación, batería, almacenamiento, memoria, estado de conexión.
- Ejecutar acciones remotas (bloqueo, reinicio, borrado, mensajes) enviadas desde la consola.

## Estructura prevista

```
device-agent/
├── app/
│   ├── src/main/java/com/zentrix/agent/
│   │   ├── enrollment/    # Registro e inscripción del dispositivo
│   │   ├── policy/         # Aplicación de políticas (WiFi, VPN, kiosco, restricciones)
│   │   ├── apps/            # Instalación/actualización/desinstalación de aplicaciones
│   │   ├── telemetry/       # Recolección y envío de estado (ubicación, batería, memoria)
│   │   ├── commands/        # Recepción y ejecución de comandos remotos
│   │   └── sync/             # Comunicación con la API del backend
│   └── src/main/res/         # Recursos Android
```

Este módulo se comunica exclusivamente con el `backend` (Spring Boot); no tiene lógica de negocio propia, solo ejecuta lo que la plataforma le indica.

## Estado actual (Fase 3)

- Proyecto Gradle (Kotlin DSL): `com.android.application` 8.7.3, Kotlin 2.0.21, `compileSdk`/`targetSdk` 35, `minSdk` 26.
- **Enrollment real**: `MainActivity` pide token de enrollment + IMEI, llama a `POST /devices/enroll`, guarda el `deviceToken` (`TokenStore`, SharedPreferences) y programa el heartbeat periódico.
- **Heartbeat + telemetría + Cola de Comandos**: `HeartbeatWorker` (WorkManager, cada 15 min) reporta batería/almacenamiento vía `BatteryManager`/`File.freeSpace`, memoria vía `ActivityManager.MemoryInfo` y, si el permiso `ACCESS_FINE_LOCATION` ya fue otorgado, la última ubicación conocida (`TelemetryCollector`, sin solicitar el permiso en background) — todo en un solo `POST /devices/heartbeat`. En el mismo ciclo, `CommandProcessor` hace polling de `GET /devices/commands/pending`, ejecuta cada comando y confirma el resultado con `POST /devices/commands/{id}/ack`.
- **Perfiles y Políticas real**: `PolicyApplier` interpreta el payload de un comando `APPLY_POLICY`. WiFi funciona en cualquier instalación (`WifiNetworkSuggestion`, Android 10+, solo requiere permisos de runtime). VPN/Kiosco/Restricciones usan `DevicePolicyManager` y requieren que el agente esté provisto como **Device Owner** (`adb shell dpm set-device-owner com.zentrix.agent/.policy.ZentrixDeviceAdminReceiver` en un dispositivo/emulador sin cuentas configuradas) — sin eso, el comando se confirma con estado `ERROR` y un detalle explícito en vez de fallar en silencio.
- **Aplicaciones real**: `AppInstaller` descarga el APK (`GET /applications/{id}/apk`, autenticado con el `deviceToken`) y lo instala con `PackageInstaller.Session` (`INSTALL_APP`), o lo desinstala con `PackageInstaller.uninstall` (`UNINSTALL_APP`). Sin Device Owner, Android muestra el diálogo estándar de confirmación al usuario; con Device Owner, es silencioso.
- `ZentrixFcmService` ahora, al recibir un push, encola un `HeartbeatWorker` inmediato (en vez de esperar los 15 min) para acelerar la entrega de comandos — pero el módulo `firebase-messaging-ktx` sigue **no funcional** sin un `google-services.json` real de un proyecto Firebase (no versionado, ver `.gitignore`); mientras tanto, el polling periódico dentro de `HeartbeatWorker` sigue entregando los comandos igual, solo que con hasta 15 min de latencia.
- `BASE_URL` apunta a `http://10.0.2.2:8080/` (alias del emulador Android hacia el backend en Docker); habilitado explícitamente vía `network_security_config.xml` solo para ese host (desarrollo local, no aplica en staging/prod que usan HTTPS).

## Cómo compilarlo en local

Requiere JDK 17 o 21 (no JDK 25+, aún no soportado por Gradle 8.13/AGP 8.7) y Android SDK (`compileSdk`/`build-tools` 35).

```bash
export ANDROID_HOME=<ruta a tu Android SDK>
JAVA_HOME=<ruta a tu JDK 21> ./gradlew assembleDebug
```

El APK de debug queda en `app/build/outputs/apk/debug/app-debug.apk`.
