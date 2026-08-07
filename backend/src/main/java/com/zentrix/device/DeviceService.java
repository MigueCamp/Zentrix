package com.zentrix.device;

import com.zentrix.common.DuplicateResourceException;
import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.common.security.AuthenticatedUser;
import com.zentrix.common.security.CurrentUser;
import com.zentrix.common.security.JwtService;
import com.zentrix.common.ws.DeviceEventBroadcaster;
import com.zentrix.company.Company;
import com.zentrix.company.CompanyRepository;
import com.zentrix.device.dto.*;
import com.zentrix.monitoring.AlertService;
import com.zentrix.monitoring.AlertSeverity;
import com.zentrix.user.AuditLogService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class DeviceService {

    private static final int LOW_BATTERY_THRESHOLD_PERCENT = 15;

    private final DeviceRepository deviceRepository;
    private final DeviceGroupRepository deviceGroupRepository;
    private final DeviceEventRepository deviceEventRepository;
    private final CompanyRepository companyRepository;
    private final JwtService jwtService;
    private final AuditLogService auditLogService;
    private final AlertService alertService;
    private final DeviceEventBroadcaster eventBroadcaster;

    public DeviceService(DeviceRepository deviceRepository, DeviceGroupRepository deviceGroupRepository,
                          DeviceEventRepository deviceEventRepository, CompanyRepository companyRepository,
                          JwtService jwtService, AuditLogService auditLogService, AlertService alertService,
                          DeviceEventBroadcaster eventBroadcaster) {
        this.deviceRepository = deviceRepository;
        this.deviceGroupRepository = deviceGroupRepository;
        this.deviceEventRepository = deviceEventRepository;
        this.companyRepository = companyRepository;
        this.jwtService = jwtService;
        this.auditLogService = auditLogService;
        this.alertService = alertService;
        this.eventBroadcaster = eventBroadcaster;
    }

    public DeviceCreatedResponse register(Integer companyId, DeviceRequest request) {
        if (deviceRepository.existsByImei(request.imei())) {
            throw new DuplicateResourceException("Ya existe un dispositivo con el IMEI " + request.imei());
        }
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));

        Device device = Device.builder()
                .company(company)
                .imei(request.imei())
                .serialNumber(request.serialNumber())
                .model(request.model())
                .androidVersion(request.androidVersion())
                .enrollmentToken(UUID.randomUUID().toString())
                .status(DeviceStatus.OFFLINE)
                .build();
        device = deviceRepository.save(device);

        auditLogService.record("REGISTRAR_DISPOSITIVO", "{\"imei\":\"" + request.imei() + "\"}");
        return DeviceCreatedResponse.from(device);
    }

    @Transactional(readOnly = true)
    public List<DeviceResponse> findAll(Integer companyId) {
        return deviceRepository.findByCompanyIdAndStatusNot(companyId, DeviceStatus.ELIMINADO).stream()
                .map(DeviceResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public DeviceResponse findById(Integer companyId, Integer id) {
        return DeviceResponse.from(getOwnedDevice(companyId, id));
    }

    public DeviceResponse assignGroup(Integer companyId, Integer id, Integer groupId) {
        Device device = getOwnedDevice(companyId, id);
        if (groupId == null) {
            device.setGroup(null);
        } else {
            DeviceGroup group = deviceGroupRepository.findByIdAndCompanyId(groupId, companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado: " + groupId));
            device.setGroup(group);
        }
        return DeviceResponse.from(device);
    }

    public void delete(Integer companyId, Integer id) {
        Device device = getOwnedDevice(companyId, id);
        device.setStatus(DeviceStatus.ELIMINADO);
        device.setEnrollmentToken(null);
        auditLogService.record("ELIMINAR_DISPOSITIVO", "{\"deviceId\":" + id + "}");
    }

    @Transactional(readOnly = true)
    public Page<DeviceEventResponse> history(Integer companyId, Integer id, Pageable pageable) {
        getOwnedDevice(companyId, id);
        return deviceEventRepository.findByDeviceIdOrderByEventDateDesc(id, pageable).map(DeviceEventResponse::from);
    }

    public EnrollResponse enroll(EnrollRequest request) {
        Device device = deviceRepository.findByEnrollmentToken(request.enrollmentToken())
                .orElseThrow(() -> new BadCredentialsException("Token de enrollment inválido"));

        if (!device.getImei().equals(request.imei())) {
            throw new BadCredentialsException("El IMEI no coincide con el token de enrollment");
        }

        device.setEnrollmentToken(null);
        device.setStatus(DeviceStatus.ONLINE);
        device.setLastSeenAt(LocalDateTime.now());
        if (request.serialNumber() != null) {
            device.setSerialNumber(request.serialNumber());
        }

        AuthenticatedUser deviceUser = AuthenticatedUser.forDevice(
                device.getId(), device.getCompany().getId(), device.getImei());
        return new EnrollResponse(jwtService.generateToken(deviceUser));
    }

    public void heartbeat(HeartbeatRequest request) {
        Integer deviceId = CurrentUser.get().deviceId();
        Device device = deviceRepository.findById(deviceId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado: " + deviceId));

        device.setStatus(DeviceStatus.ONLINE);
        device.setLastSeenAt(LocalDateTime.now());

        recordEvent(device, DeviceEventType.CONEXION, "{\"status\":\"ONLINE\"}");
        recordEvent(device, DeviceEventType.BATERIA, "{\"batteryLevel\":" + request.batteryLevel() + "}");
        recordEvent(device, DeviceEventType.ALMACENAMIENTO, "{\"storageFreeBytes\":" + request.storageFreeBytes() + "}");
        if (request.memoryUsedBytes() != null && request.memoryTotalBytes() != null) {
            recordEvent(device, DeviceEventType.MEMORIA,
                    "{\"memoryUsedBytes\":" + request.memoryUsedBytes() + ",\"memoryTotalBytes\":" + request.memoryTotalBytes() + "}");
        }
        if (request.latitude() != null && request.longitude() != null) {
            recordEvent(device, DeviceEventType.UBICACION,
                    "{\"latitude\":" + request.latitude() + ",\"longitude\":" + request.longitude() + "}");
        }

        if (request.batteryLevel() < LOW_BATTERY_THRESHOLD_PERCENT) {
            alertService.raiseIfAbsent(device, "BATERIA_BAJA", AlertSeverity.MEDIA,
                    "Batería al " + request.batteryLevel() + "% en " + device.getImei());
        }

        eventBroadcaster.broadcast(device.getCompany().getId(),
                "{\"event\":\"heartbeat\",\"deviceId\":" + device.getId()
                        + ",\"status\":\"ONLINE\",\"batteryLevel\":" + request.batteryLevel() + "}");
    }

    private void recordEvent(Device device, DeviceEventType type, String valueJson) {
        deviceEventRepository.save(DeviceEvent.builder()
                .device(device)
                .type(type.name())
                .valueJson(valueJson)
                .build());
    }

    private Device getOwnedDevice(Integer companyId, Integer id) {
        return deviceRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado: " + id));
    }
}
