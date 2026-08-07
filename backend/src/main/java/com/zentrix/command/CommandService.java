package com.zentrix.command;

import com.zentrix.command.dto.CommandAckRequest;
import com.zentrix.command.dto.DeviceCommandResponse;
import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.common.security.CurrentUser;
import com.zentrix.device.Device;
import com.zentrix.device.DeviceRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Cola de Comandos: encolar (desde policy/application), entregar al agente vía polling
 * en el heartbeat y registrar el resultado (ack). Ver docs/02, sección 4.2.
 */
@Service
@Transactional
public class CommandService {

    private final DeviceCommandRepository commandRepository;
    private final DeviceRepository deviceRepository;

    public CommandService(DeviceCommandRepository commandRepository, DeviceRepository deviceRepository) {
        this.commandRepository = commandRepository;
        this.deviceRepository = deviceRepository;
    }

    public DeviceCommand enqueue(Device device, CommandType type, String payloadJson) {
        return commandRepository.save(DeviceCommand.builder()
                .device(device)
                .type(type)
                .payloadJson(payloadJson)
                .status(CommandStatus.PENDIENTE)
                .build());
    }

    public List<DeviceCommand> enqueueForDevices(List<Device> devices, CommandType type, String payloadJson) {
        return devices.stream().map(device -> enqueue(device, type, payloadJson)).toList();
    }

    public List<DeviceCommandResponse> pollPendingForCurrentDevice() {
        Integer deviceId = CurrentUser.get().deviceId();
        List<DeviceCommand> pending = commandRepository.findByDeviceIdAndStatus(deviceId, CommandStatus.PENDIENTE);
        pending.forEach(command -> command.setStatus(CommandStatus.ENVIADO));
        return pending.stream().map(DeviceCommandResponse::from).toList();
    }

    public DeviceCommand ack(Long commandId, CommandAckRequest request) {
        Integer deviceId = CurrentUser.get().deviceId();
        DeviceCommand command = commandRepository.findById(commandId)
                .orElseThrow(() -> new ResourceNotFoundException("Comando no encontrado: " + commandId));
        if (!command.getDevice().getId().equals(deviceId)) {
            throw new AccessDeniedException("El comando no pertenece a este dispositivo");
        }
        command.setStatus(request.status() == CommandAckRequest.CommandAckStatus.COMPLETADO
                ? CommandStatus.COMPLETADO : CommandStatus.ERROR);
        command.setResultDetail(request.detail());
        return command;
    }

    @Transactional(readOnly = true)
    public Page<DeviceCommandResponse> history(Integer companyId, Integer deviceId, Pageable pageable) {
        deviceRepository.findByIdAndCompanyId(deviceId, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado: " + deviceId));
        return commandRepository.findByDeviceIdOrderByCreatedAtDesc(deviceId, pageable).map(DeviceCommandResponse::from);
    }
}
