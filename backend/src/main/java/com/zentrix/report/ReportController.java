package com.zentrix.report;

import com.zentrix.common.tenant.TenantResolver;
import com.zentrix.device.dto.DeviceResponse;
import com.zentrix.report.dto.EventReportRow;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Módulo "Reportes" (docs/04_Especificación_de_Módulos.md, sección 7).
 */
@RestController
@RequestMapping("/reports")
@PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN', 'API_CLIENT')")
public class ReportController {

    private final ReportService reportService;
    private final InventoryExporter inventoryExporter;

    public ReportController(ReportService reportService, InventoryExporter inventoryExporter) {
        this.reportService = reportService;
        this.inventoryExporter = inventoryExporter;
    }

    @GetMapping("/inventory")
    public List<DeviceResponse> inventory(@RequestParam(required = false) Integer companyId) {
        return reportService.inventory(TenantResolver.resolve(companyId));
    }

    @GetMapping("/events")
    public Page<EventReportRow> events(@RequestParam(required = false) Integer companyId,
                                        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime from,
                                        @RequestParam @NotNull @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime to,
                                        @RequestParam(required = false) String type,
                                        @RequestParam(defaultValue = "0") int page,
                                        @RequestParam(defaultValue = "20") int size) {
        return reportService.events(TenantResolver.resolve(companyId), from, to, type, PageRequest.of(page, size));
    }

    @GetMapping("/inventory/export")
    public ResponseEntity<byte[]> exportInventory(@RequestParam(required = false) Integer companyId,
                                                    @RequestParam(defaultValue = "pdf") String format) {
        List<DeviceResponse> devices = reportService.inventory(TenantResolver.resolve(companyId));
        boolean xlsx = "xlsx".equalsIgnoreCase(format);
        byte[] body = xlsx ? inventoryExporter.toXlsx(devices) : inventoryExporter.toPdf(devices);
        MediaType mediaType = xlsx
                ? MediaType.valueOf("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
                : MediaType.APPLICATION_PDF;
        String filename = xlsx ? "inventario.xlsx" : "inventario.pdf";
        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(body);
    }
}
