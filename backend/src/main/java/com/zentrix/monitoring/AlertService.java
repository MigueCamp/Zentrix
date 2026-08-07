package com.zentrix.monitoring;

import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.device.Device;
import com.zentrix.monitoring.dto.AlertResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AlertService {

    private final AlertRepository alertRepository;

    public AlertService(AlertRepository alertRepository) {
        this.alertRepository = alertRepository;
    }

    /** Evita spam de alertas: solo crea una nueva si no hay una sin atender del mismo tipo. */
    public void raiseIfAbsent(Device device, String type, AlertSeverity severity, String message) {
        boolean alreadyOpen = alertRepository.findFirstByDeviceIdAndTypeAndAcknowledgedFalse(device.getId(), type).isPresent();
        if (alreadyOpen) {
            return;
        }
        alertRepository.save(Alert.builder()
                .device(device)
                .type(type)
                .severity(severity)
                .message(message)
                .acknowledged(false)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<AlertResponse> findForCompany(Integer companyId, Boolean acknowledged, Pageable pageable) {
        Page<Alert> page = acknowledged == null
                ? alertRepository.findByDeviceCompanyIdOrderByCreatedAtDesc(companyId, pageable)
                : alertRepository.findByDeviceCompanyIdAndAcknowledgedOrderByCreatedAtDesc(companyId, acknowledged, pageable);
        return page.map(AlertResponse::from);
    }

    public AlertResponse acknowledge(Integer companyId, Integer alertId) {
        Alert alert = alertRepository.findByIdAndDeviceCompanyId(alertId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Alerta no encontrada: " + alertId));
        alert.setAcknowledged(true);
        return AlertResponse.from(alert);
    }
}
