package com.zentrix.command;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import com.zentrix.application.ApplicationService;
import com.zentrix.application.InstallationStatus;
import com.zentrix.command.dto.CommandAckRequest;
import com.zentrix.command.dto.DeviceCommandResponse;
import com.zentrix.common.security.CurrentUser;
import com.zentrix.common.tenant.TenantResolver;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cola de Comandos: el agente hace polling de pendientes en cada heartbeat y confirma
 * el resultado; la consola consulta el historial por dispositivo. Ver docs/02, sección 4.2.
 */
@RestController
@RequestMapping("/devices")
public class CommandController {

    private final CommandService commandService;
    private final ApplicationService applicationService;
    private final ObjectMapper objectMapper;

    public CommandController(CommandService commandService, ApplicationService applicationService, ObjectMapper objectMapper) {
        this.commandService = commandService;
        this.applicationService = applicationService;
        this.objectMapper = objectMapper;
    }

    @GetMapping("/commands/pending")
    @PreAuthorize("hasAuthority('DEVICE')")
    public List<DeviceCommandResponse> pollPending() {
        return commandService.pollPendingForCurrentDevice();
    }

    @PostMapping("/commands/{id}/ack")
    @PreAuthorize("hasAuthority('DEVICE')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void ack(@PathVariable Long id, @Valid @RequestBody CommandAckRequest request) {
        DeviceCommand command = commandService.ack(id, request);
        applyInstallationSideEffect(command, request);
    }

    @GetMapping("/{deviceId}/commands")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public Page<DeviceCommandResponse> history(@RequestParam(required = false) Integer companyId,
                                                @PathVariable Integer deviceId,
                                                @RequestParam(defaultValue = "0") int page,
                                                @RequestParam(defaultValue = "20") int size) {
        return commandService.history(TenantResolver.resolve(companyId), deviceId, PageRequest.of(page, size));
    }

    private void applyInstallationSideEffect(DeviceCommand command, CommandAckRequest request) {
        if (command.getType() != CommandType.INSTALL_APP && command.getType() != CommandType.UNINSTALL_APP) {
            return;
        }
        JsonNode payload = readPayload(command.getPayloadJson());
        if (payload == null || !payload.hasNonNull("applicationId")) {
            return;
        }
        Integer applicationId = payload.get("applicationId").asInt();
        Integer deviceId = CurrentUser.get().deviceId();
        boolean ok = request.status() == CommandAckRequest.CommandAckStatus.COMPLETADO;
        InstallationStatus status;
        if (command.getType() == CommandType.INSTALL_APP) {
            status = ok ? InstallationStatus.INSTALADA : InstallationStatus.ERROR;
        } else {
            status = ok ? InstallationStatus.DESINSTALADA : InstallationStatus.ERROR;
        }
        String version = payload.hasNonNull("version") ? payload.get("version").asText() : null;
        applicationService.updateInstallStatus(deviceId, applicationId, status, version);
    }

    private JsonNode readPayload(String payloadJson) {
        if (payloadJson == null) {
            return null;
        }
        try {
            return objectMapper.readTree(payloadJson);
        } catch (Exception e) {
            return null;
        }
    }
}
