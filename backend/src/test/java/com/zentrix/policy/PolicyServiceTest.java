package com.zentrix.policy;

import com.zentrix.command.CommandService;
import com.zentrix.command.CommandType;
import com.zentrix.common.security.PolicyCipher;
import com.zentrix.company.CompanyRepository;
import com.zentrix.device.Device;
import com.zentrix.device.DeviceRepository;
import com.zentrix.device.DeviceGroupRepository;
import com.zentrix.policy.dto.PolicyAssignRequest;
import com.zentrix.user.AuditLogService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import tools.jackson.databind.json.JsonMapper;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Módulo Perfiles y Políticas (docs/04, sección 4). Cubre las reglas de negocio explícitas:
 * exactamente un destino (dispositivo XOR grupo) y "una sola política activa por Tipo" — al
 * asignar, la asignación previa del mismo tipo se reemplaza y se encola el comando de aplicación.
 */
@ExtendWith(MockitoExtension.class)
class PolicyServiceTest {

    @Mock PolicyRepository policyRepository;
    @Mock PolicyAssignmentRepository assignmentRepository;
    @Mock CompanyRepository companyRepository;
    @Mock DeviceRepository deviceRepository;
    @Mock DeviceGroupRepository deviceGroupRepository;
    @Mock PolicyCipher policyCipher;
    @Mock CommandService commandService;
    @Mock AuditLogService auditLogService;

    private PolicyService service;

    private static final int COMPANY_ID = 1;

    @BeforeEach
    void setUp() {
        service = new PolicyService(policyRepository, assignmentRepository, companyRepository,
                deviceRepository, deviceGroupRepository, policyCipher, commandService, auditLogService,
                JsonMapper.builder().build());
    }

    private Policy kioscoPolicy() {
        return Policy.builder().id(50).name("Kiosco corp").type(PolicyType.KIOSCO)
                .configurationJson("{}").encrypted(false).build();
    }

    @Test
    @DisplayName("Asignar sin dispositivo ni grupo es rechazado")
    void assignWithoutTargetRejected() {
        when(policyRepository.findByIdAndCompanyId(50, COMPANY_ID)).thenReturn(Optional.of(kioscoPolicy()));
        assertThatThrownBy(() -> service.assign(COMPANY_ID, 50, new PolicyAssignRequest(null, null)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Asignar con dispositivo Y grupo a la vez es rechazado")
    void assignWithBothTargetsRejected() {
        when(policyRepository.findByIdAndCompanyId(50, COMPANY_ID)).thenReturn(Optional.of(kioscoPolicy()));
        assertThatThrownBy(() -> service.assign(COMPANY_ID, 50, new PolicyAssignRequest(2, 3)))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Asignar a un dispositivo reemplaza la política previa del mismo tipo y encola el comando")
    void assignToDeviceReplacesSameTypeAndEnqueues() {
        Policy policy = kioscoPolicy();
        Device device = Device.builder().id(2).build();
        PolicyAssignment previous = PolicyAssignment.builder().id(9).policy(policy).device(device).build();

        when(policyRepository.findByIdAndCompanyId(50, COMPANY_ID)).thenReturn(Optional.of(policy));
        when(deviceRepository.findByIdAndCompanyId(2, COMPANY_ID)).thenReturn(Optional.of(device));
        when(assignmentRepository.findByDeviceIdAndPolicyType(2, PolicyType.KIOSCO)).thenReturn(List.of(previous));
        when(assignmentRepository.save(any(PolicyAssignment.class)))
                .thenReturn(PolicyAssignment.builder().id(10).policy(policy).device(device).build());

        service.assign(COMPANY_ID, 50, new PolicyAssignRequest(2, null));

        // La asignación previa del mismo tipo se borra (una sola activa por Tipo).
        verify(assignmentRepository).delete(previous);
        // Y se encola el comando APPLY_POLICY para el dispositivo destino.
        verify(commandService).enqueueForDevices(eq(List.of(device)), eq(CommandType.APPLY_POLICY), any());
    }
}
