package com.zentrix.command;

import com.zentrix.command.dto.CommandAckRequest;
import com.zentrix.command.dto.DeviceCommandResponse;
import com.zentrix.common.security.AuthenticatedUser;
import com.zentrix.device.Device;
import com.zentrix.device.DeviceRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Cola de Comandos (docs/02, sección 4.2 y docs/07, sección 4): encolado, entrega por polling
 * (PENDIENTE → ENVIADO) y confirmación, incluyendo que un dispositivo no pueda confirmar el
 * comando de otro (aislamiento a nivel de comando).
 */
@ExtendWith(MockitoExtension.class)
class CommandServiceTest {

    @Mock DeviceCommandRepository commandRepository;
    @Mock DeviceRepository deviceRepository;
    @InjectMocks CommandService service;

    @AfterEach
    void clearContext() {
        SecurityContextHolder.clearContext();
    }

    private void authenticateAsDevice(int deviceId) {
        AuthenticatedUser principal = AuthenticatedUser.forDevice(deviceId, 1, "imei-" + deviceId);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(principal, null, List.of()));
    }

    private Device device(int id) {
        return Device.builder().id(id).build();
    }

    @Test
    @DisplayName("enqueueForDevices crea un comando PENDIENTE por cada dispositivo")
    void enqueuesOnePerDevice() {
        when(commandRepository.save(any(DeviceCommand.class))).thenAnswer(inv -> inv.getArgument(0));

        List<DeviceCommand> created = service.enqueueForDevices(
                List.of(device(1), device(2), device(3)), CommandType.APPLY_POLICY, "{}");

        verify(commandRepository, times(3)).save(any(DeviceCommand.class));
        assertThat(created).hasSize(3).allMatch(c -> c.getStatus() == CommandStatus.PENDIENTE);
    }

    @Test
    @DisplayName("pollPendingForCurrentDevice marca los comandos entregados como ENVIADO")
    void pollFlipsPendingToSent() {
        authenticateAsDevice(5);
        DeviceCommand a = DeviceCommand.builder().device(device(5)).type(CommandType.INSTALL_APP)
                .status(CommandStatus.PENDIENTE).payloadJson("{}").build();
        DeviceCommand b = DeviceCommand.builder().device(device(5)).type(CommandType.APPLY_POLICY)
                .status(CommandStatus.PENDIENTE).payloadJson("{}").build();
        when(commandRepository.findByDeviceIdAndStatus(5, CommandStatus.PENDIENTE)).thenReturn(List.of(a, b));

        List<DeviceCommandResponse> delivered = service.pollPendingForCurrentDevice();

        assertThat(delivered).hasSize(2);
        assertThat(a.getStatus()).isEqualTo(CommandStatus.ENVIADO);
        assertThat(b.getStatus()).isEqualTo(CommandStatus.ENVIADO);
    }

    @Test
    @DisplayName("ack COMPLETADO cierra el comando con su detalle de resultado")
    void ackCompletesCommand() {
        authenticateAsDevice(5);
        DeviceCommand command = DeviceCommand.builder().device(device(5)).type(CommandType.INSTALL_APP)
                .status(CommandStatus.ENVIADO).payloadJson("{}").build();
        when(commandRepository.findById(100L)).thenReturn(Optional.of(command));

        service.ack(100L, new CommandAckRequest(CommandAckRequest.CommandAckStatus.COMPLETADO, "instalada ok"));

        assertThat(command.getStatus()).isEqualTo(CommandStatus.COMPLETADO);
        assertThat(command.getResultDetail()).isEqualTo("instalada ok");
    }

    @Test
    @DisplayName("un dispositivo NO puede confirmar un comando que pertenece a otro dispositivo")
    void ackRejectsCommandOfAnotherDevice() {
        authenticateAsDevice(5);
        DeviceCommand foreign = DeviceCommand.builder().device(device(9)).type(CommandType.APPLY_POLICY)
                .status(CommandStatus.ENVIADO).payloadJson("{}").build();
        when(commandRepository.findById(200L)).thenReturn(Optional.of(foreign));

        assertThatThrownBy(() -> service.ack(200L,
                new CommandAckRequest(CommandAckRequest.CommandAckStatus.COMPLETADO, "x")))
                .isInstanceOf(AccessDeniedException.class);
    }
}
