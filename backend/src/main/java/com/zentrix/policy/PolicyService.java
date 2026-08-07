package com.zentrix.policy;

import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ObjectNode;
import com.zentrix.command.CommandService;
import com.zentrix.command.CommandType;
import com.zentrix.common.DuplicateResourceException;
import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.common.security.PolicyCipher;
import com.zentrix.company.Company;
import com.zentrix.company.CompanyRepository;
import com.zentrix.device.Device;
import com.zentrix.device.DeviceGroup;
import com.zentrix.device.DeviceGroupRepository;
import com.zentrix.device.DeviceRepository;
import com.zentrix.policy.dto.*;
import com.zentrix.user.AuditLogService;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Set;

/**
 * Módulo "Perfiles y Políticas" (docs/04, sección 4): creación, cifrado de configuración
 * sensible (WiFi/VPN) y propagación asíncrona vía Cola de Comandos.
 */
@Service
@Transactional
public class PolicyService {

    private static final Set<PolicyType> SENSITIVE_TYPES = Set.of(PolicyType.WIFI, PolicyType.VPN);

    private final PolicyRepository policyRepository;
    private final PolicyAssignmentRepository assignmentRepository;
    private final CompanyRepository companyRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceGroupRepository deviceGroupRepository;
    private final PolicyCipher policyCipher;
    private final CommandService commandService;
    private final AuditLogService auditLogService;
    private final ObjectMapper objectMapper;

    public PolicyService(PolicyRepository policyRepository, PolicyAssignmentRepository assignmentRepository,
                          CompanyRepository companyRepository, DeviceRepository deviceRepository,
                          DeviceGroupRepository deviceGroupRepository, PolicyCipher policyCipher,
                          CommandService commandService, AuditLogService auditLogService, ObjectMapper objectMapper) {
        this.policyRepository = policyRepository;
        this.assignmentRepository = assignmentRepository;
        this.companyRepository = companyRepository;
        this.deviceRepository = deviceRepository;
        this.deviceGroupRepository = deviceGroupRepository;
        this.policyCipher = policyCipher;
        this.commandService = commandService;
        this.auditLogService = auditLogService;
        this.objectMapper = objectMapper;
    }

    public PolicyResponse create(Integer companyId, PolicyRequest request) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));

        boolean sensitive = SENSITIVE_TYPES.contains(request.type());
        Policy policy = Policy.builder()
                .company(company)
                .name(request.name())
                .type(request.type())
                .configurationJson(sensitive ? policyCipher.encrypt(request.configurationJson()) : request.configurationJson())
                .encrypted(sensitive)
                .build();
        try {
            policy = policyRepository.save(policy);
        } catch (DataIntegrityViolationException e) {
            throw new DuplicateResourceException("Ya existe una política con el nombre " + request.name());
        }

        auditLogService.record("CREAR_POLITICA", "{\"policyId\":" + policy.getId() + ",\"tipo\":\"" + policy.getType() + "\"}");
        return PolicyResponse.from(policy, request.configurationJson());
    }

    @Transactional(readOnly = true)
    public List<PolicyResponse> findAll(Integer companyId) {
        return policyRepository.findByCompanyId(companyId).stream()
                .map(policy -> PolicyResponse.from(policy, decrypt(policy)))
                .toList();
    }

    @Transactional(readOnly = true)
    public PolicyResponse findById(Integer companyId, Integer id) {
        Policy policy = getOwnedPolicy(companyId, id);
        return PolicyResponse.from(policy, decrypt(policy));
    }

    public PolicyResponse update(Integer companyId, Integer id, PolicyRequest request) {
        Policy policy = getOwnedPolicy(companyId, id);
        boolean sensitive = SENSITIVE_TYPES.contains(request.type());

        policy.setName(request.name());
        policy.setType(request.type());
        policy.setConfigurationJson(sensitive ? policyCipher.encrypt(request.configurationJson()) : request.configurationJson());
        policy.setEncrypted(sensitive);

        propagateToAssignedDevices(policy, request.configurationJson());
        auditLogService.record("ACTUALIZAR_POLITICA", "{\"policyId\":" + policy.getId() + "}");
        return PolicyResponse.from(policy, request.configurationJson());
    }

    public PolicyAssignmentResponse assign(Integer companyId, Integer policyId, PolicyAssignRequest request) {
        Policy policy = getOwnedPolicy(companyId, policyId);

        if ((request.deviceId() == null) == (request.groupId() == null)) {
            throw new IllegalArgumentException("Debe indicar exactamente un dispositivo o un grupo destino");
        }

        List<Device> targetDevices;
        PolicyAssignment assignment;
        if (request.deviceId() != null) {
            Device device = deviceRepository.findByIdAndCompanyId(request.deviceId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado: " + request.deviceId()));
            assignmentRepository.findByDeviceIdAndPolicyType(device.getId(), policy.getType())
                    .forEach(assignmentRepository::delete);
            assignment = assignmentRepository.save(PolicyAssignment.builder().policy(policy).device(device).build());
            targetDevices = List.of(device);
        } else {
            DeviceGroup group = deviceGroupRepository.findByIdAndCompanyId(request.groupId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado: " + request.groupId()));
            assignmentRepository.findByGroupIdAndPolicyType(group.getId(), policy.getType())
                    .forEach(assignmentRepository::delete);
            assignment = assignmentRepository.save(PolicyAssignment.builder().policy(policy).group(group).build());
            targetDevices = deviceRepository.findByGroupId(group.getId());
        }

        commandService.enqueueForDevices(targetDevices, CommandType.APPLY_POLICY, buildPayload(policy, decrypt(policy)));
        auditLogService.record("ASIGNAR_POLITICA",
                "{\"policyId\":" + policy.getId() + ",\"deviceId\":" + request.deviceId() + ",\"groupId\":" + request.groupId() + "}");
        return PolicyAssignmentResponse.from(assignment);
    }

    @Transactional(readOnly = true)
    public List<PolicyAssignmentResponse> findAssignments(Integer companyId) {
        return assignmentRepository.findByPolicyCompanyId(companyId).stream()
                .map(PolicyAssignmentResponse::from)
                .toList();
    }

    private void propagateToAssignedDevices(Policy policy, String plainConfigurationJson) {
        List<PolicyAssignment> assignments = assignmentRepository.findByPolicyId(policy.getId());
        List<Device> targetDevices = assignments.stream()
                .flatMap(a -> a.getDevice() != null
                        ? List.of(a.getDevice()).stream()
                        : deviceRepository.findByGroupId(a.getGroup().getId()).stream())
                .distinct()
                .toList();
        if (!targetDevices.isEmpty()) {
            commandService.enqueueForDevices(targetDevices, CommandType.APPLY_POLICY, buildPayload(policy, plainConfigurationJson));
        }
    }

    private String buildPayload(Policy policy, String plainConfigurationJson) {
        ObjectNode envelope = objectMapper.createObjectNode();
        envelope.put("policyId", policy.getId());
        envelope.put("name", policy.getName());
        envelope.put("type", policy.getType().name());
        try {
            JsonNode configuration = objectMapper.readTree(plainConfigurationJson);
            envelope.set("configuration", configuration);
        } catch (Exception e) {
            envelope.put("configuration", plainConfigurationJson);
        }
        return envelope.toString();
    }

    private String decrypt(Policy policy) {
        return policy.isEncrypted() ? policyCipher.decrypt(policy.getConfigurationJson()) : policy.getConfigurationJson();
    }

    private Policy getOwnedPolicy(Integer companyId, Integer id) {
        return policyRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Política no encontrada: " + id));
    }
}
