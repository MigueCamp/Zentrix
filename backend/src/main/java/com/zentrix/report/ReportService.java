package com.zentrix.report;

import com.zentrix.device.DeviceEventRepository;
import com.zentrix.device.DeviceRepository;
import com.zentrix.device.DeviceStatus;
import com.zentrix.device.dto.DeviceResponse;
import com.zentrix.report.dto.EventReportRow;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Módulo "Reportes" (docs/04, sección 7): consultas de solo lectura sobre DISPOSITIVO
 * y EVENTO_DISPOSITIVO, siempre acotadas al EmpresaId del solicitante.
 */
@Service
@Transactional(readOnly = true)
public class ReportService {

    private final DeviceRepository deviceRepository;
    private final DeviceEventRepository deviceEventRepository;

    public ReportService(DeviceRepository deviceRepository, DeviceEventRepository deviceEventRepository) {
        this.deviceRepository = deviceRepository;
        this.deviceEventRepository = deviceEventRepository;
    }

    public List<DeviceResponse> inventory(Integer companyId) {
        return deviceRepository.findByCompanyIdAndStatusNot(companyId, DeviceStatus.ELIMINADO).stream()
                .map(DeviceResponse::from)
                .toList();
    }

    public Page<EventReportRow> events(Integer companyId, LocalDateTime from, LocalDateTime to, String type, Pageable pageable) {
        Page<com.zentrix.device.DeviceEvent> page = type == null
                ? deviceEventRepository.findByDeviceCompanyIdAndEventDateBetweenOrderByEventDateDesc(companyId, from, to, pageable)
                : deviceEventRepository.findByDeviceCompanyIdAndTypeAndEventDateBetweenOrderByEventDateDesc(companyId, type, from, to, pageable);
        return page.map(EventReportRow::from);
    }
}
