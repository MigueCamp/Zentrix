package com.zentrix.monitoring;

import com.zentrix.common.ws.DeviceEventBroadcaster;
import com.zentrix.device.Device;
import com.zentrix.device.DeviceRepository;
import com.zentrix.device.DeviceStatus;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * "Un dispositivo sin heartbeat por más de N minutos configurables pasa automáticamente
 * a Offline" — docs/04, sección 3. Corre cada minuto; el heartbeat esperado del agente
 * es cada 15 min, así que 20 min de margen evita falsos positivos por un ciclo perdido.
 */
@Component
public class DeviceOfflineScheduler {

    private static final int OFFLINE_THRESHOLD_MINUTES = 20;

    private final DeviceRepository deviceRepository;
    private final DeviceEventBroadcaster eventBroadcaster;

    public DeviceOfflineScheduler(DeviceRepository deviceRepository, DeviceEventBroadcaster eventBroadcaster) {
        this.deviceRepository = deviceRepository;
        this.eventBroadcaster = eventBroadcaster;
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void flipStaleDevicesOffline() {
        LocalDateTime threshold = LocalDateTime.now().minus(OFFLINE_THRESHOLD_MINUTES, ChronoUnit.MINUTES);
        for (Device device : deviceRepository.findByStatusAndLastSeenAtBefore(DeviceStatus.ONLINE, threshold)) {
            device.setStatus(DeviceStatus.OFFLINE);
            eventBroadcaster.broadcast(device.getCompany().getId(),
                    "{\"event\":\"status\",\"deviceId\":" + device.getId() + ",\"status\":\"OFFLINE\"}");
        }
    }
}
