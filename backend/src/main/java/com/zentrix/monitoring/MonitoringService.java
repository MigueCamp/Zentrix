package com.zentrix.monitoring;

import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.device.Device;
import com.zentrix.device.DeviceEvent;
import com.zentrix.device.DeviceEventRepository;
import com.zentrix.device.DeviceEventType;
import com.zentrix.device.DeviceRepository;
import com.zentrix.monitoring.dto.DeviceLocationResponse;
import com.zentrix.monitoring.dto.DeviceStatusResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Optional;

/**
 * Proyección de lectura rápida del estado "actual" de un dispositivo, construida a partir
 * del último EVENTO_DISPOSITIVO de cada tipo (sin recorrer todo el historial) — docs/04,
 * sección 6.
 */
@Service
@Transactional(readOnly = true)
public class MonitoringService {

    private static final int ONLINE_THRESHOLD_MINUTES = 5;

    private final DeviceRepository deviceRepository;
    private final DeviceEventRepository deviceEventRepository;
    private final ObjectMapper objectMapper;

    public MonitoringService(DeviceRepository deviceRepository, DeviceEventRepository deviceEventRepository,
                              ObjectMapper objectMapper) {
        this.deviceRepository = deviceRepository;
        this.deviceEventRepository = deviceEventRepository;
        this.objectMapper = objectMapper;
    }

    public DeviceStatusResponse status(Integer companyId, Integer deviceId) {
        Device device = getOwnedDevice(companyId, deviceId);

        Integer batteryLevel = latestValue(deviceId, DeviceEventType.BATERIA, "batteryLevel")
                .map(JsonNode::asInt).orElse(null);
        Long storageFree = latestValue(deviceId, DeviceEventType.ALMACENAMIENTO, "storageFreeBytes")
                .map(JsonNode::asLong).orElse(null);
        Long memoryUsed = latestValue(deviceId, DeviceEventType.MEMORIA, "memoryUsedBytes")
                .map(JsonNode::asLong).orElse(null);
        Long memoryTotal = latestValue(deviceId, DeviceEventType.MEMORIA, "memoryTotalBytes")
                .map(JsonNode::asLong).orElse(null);

        boolean online = device.getLastSeenAt() != null
                && device.getLastSeenAt().isAfter(LocalDateTime.now().minus(ONLINE_THRESHOLD_MINUTES, ChronoUnit.MINUTES));

        return new DeviceStatusResponse(device.getId(), online, batteryLevel, storageFree,
                memoryUsed, memoryTotal, device.getLastSeenAt());
    }

    public DeviceLocationResponse location(Integer companyId, Integer deviceId) {
        getOwnedDevice(companyId, deviceId);
        return deviceEventRepository.findFirstByDeviceIdAndTypeOrderByEventDateDesc(deviceId, DeviceEventType.UBICACION.name())
                .map(event -> {
                    JsonNode node = objectMapper.readTree(event.getValueJson());
                    return new DeviceLocationResponse(deviceId, node.get("latitude").asDouble(),
                            node.get("longitude").asDouble(), event.getEventDate());
                })
                .orElse(new DeviceLocationResponse(deviceId, null, null, null));
    }

    private Optional<JsonNode> latestValue(Integer deviceId, DeviceEventType type, String field) {
        return deviceEventRepository.findFirstByDeviceIdAndTypeOrderByEventDateDesc(deviceId, type.name())
                .map(DeviceEvent::getValueJson)
                .map(objectMapper::readTree)
                .map(node -> node.get(field))
                .filter(node -> !node.isNull());
    }

    private Device getOwnedDevice(Integer companyId, Integer deviceId) {
        return deviceRepository.findByIdAndCompanyId(deviceId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado: " + deviceId));
    }
}
