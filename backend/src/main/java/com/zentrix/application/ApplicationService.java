package com.zentrix.application;

import com.zentrix.command.CommandService;
import com.zentrix.command.CommandType;
import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.company.Company;
import com.zentrix.company.CompanyRepository;
import com.zentrix.device.Device;
import com.zentrix.device.DeviceGroup;
import com.zentrix.device.DeviceGroupRepository;
import com.zentrix.device.DeviceRepository;
import com.zentrix.application.dto.ApplicationResponse;
import com.zentrix.application.dto.DeviceApplicationResponse;
import com.zentrix.application.dto.InstallRequest;
import com.zentrix.user.AuditLogService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Módulo "Aplicaciones" (docs/04, sección 5): catálogo de APKs y distribución
 * remota (instalar/desinstalar) vía Cola de Comandos.
 */
@Service
@Transactional
public class ApplicationService {

    private final ApplicationRepository applicationRepository;
    private final DeviceApplicationRepository deviceApplicationRepository;
    private final CompanyRepository companyRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceGroupRepository deviceGroupRepository;
    private final CommandService commandService;
    private final AuditLogService auditLogService;
    private final Path storageDir;

    public ApplicationService(ApplicationRepository applicationRepository,
                               DeviceApplicationRepository deviceApplicationRepository,
                               CompanyRepository companyRepository, DeviceRepository deviceRepository,
                               DeviceGroupRepository deviceGroupRepository, CommandService commandService,
                               AuditLogService auditLogService, @Value("${zentrix.storage.apk-dir}") String apkDir) {
        this.applicationRepository = applicationRepository;
        this.deviceApplicationRepository = deviceApplicationRepository;
        this.companyRepository = companyRepository;
        this.deviceRepository = deviceRepository;
        this.deviceGroupRepository = deviceGroupRepository;
        this.commandService = commandService;
        this.auditLogService = auditLogService;
        this.storageDir = Path.of(apkDir);
    }

    public ApplicationResponse upload(Integer companyId, String name, String packageName, String version,
                                       MultipartFile file) {
        Company company = companyRepository.findById(companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + companyId));

        String fileUrl = storeFile(companyId, packageName, version, file);
        Application application = applicationRepository.findByCompanyIdAndPackageName(companyId, packageName)
                .orElseGet(() -> Application.builder().company(company).packageName(packageName).build());
        application.setName(name);
        application.setCurrentVersion(version);
        application.setFileUrl(fileUrl);
        application = applicationRepository.save(application);

        auditLogService.record("SUBIR_APLICACION",
                "{\"applicationId\":" + application.getId() + ",\"version\":\"" + version + "\"}");
        return ApplicationResponse.from(application);
    }

    @Transactional(readOnly = true)
    public List<ApplicationResponse> findAll(Integer companyId) {
        return applicationRepository.findByCompanyId(companyId).stream().map(ApplicationResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public byte[] readApkBytes(Integer companyId, Integer id) {
        Application application = getOwnedApplication(companyId, id);
        try {
            return Files.readAllBytes(Path.of(application.getFileUrl()));
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    public DeviceApplicationResponse install(Integer companyId, Integer applicationId, InstallRequest request) {
        Application application = getOwnedApplication(companyId, applicationId);
        List<Device> targetDevices = resolveTargets(companyId, request);

        DeviceApplicationResponse response = null;
        for (Device device : targetDevices) {
            DeviceApplication deviceApplication = deviceApplicationRepository
                    .findByDeviceIdAndApplicationId(device.getId(), application.getId())
                    .orElseGet(() -> DeviceApplication.builder().device(device).application(application).build());
            deviceApplication.setStatus(InstallationStatus.PENDIENTE);
            deviceApplication = deviceApplicationRepository.save(deviceApplication);
            response = DeviceApplicationResponse.from(deviceApplication);

            commandService.enqueue(device, CommandType.INSTALL_APP, installPayload(application));
        }

        auditLogService.record("INSTALAR_APLICACION", "{\"applicationId\":" + application.getId() + "}");
        return response;
    }

    public void uninstall(Integer companyId, Integer applicationId, InstallRequest request) {
        Application application = getOwnedApplication(companyId, applicationId);
        List<Device> targetDevices = resolveTargets(companyId, request);

        for (Device device : targetDevices) {
            deviceApplicationRepository.findByDeviceIdAndApplicationId(device.getId(), application.getId())
                    .ifPresent(deviceApplicationRepository::delete);
            commandService.enqueue(device, CommandType.UNINSTALL_APP,
                    "{\"applicationId\":" + application.getId() + ",\"packageName\":\"" + application.getPackageName() + "\"}");
        }

        auditLogService.record("DESINSTALAR_APLICACION", "{\"applicationId\":" + application.getId() + "}");
    }

    @Transactional(readOnly = true)
    public List<DeviceApplicationResponse> versions(Integer companyId, Integer applicationId) {
        getOwnedApplication(companyId, applicationId);
        return deviceApplicationRepository.findByApplicationId(applicationId).stream()
                .map(DeviceApplicationResponse::from)
                .toList();
    }

    public void updateInstallStatus(Integer deviceId, Integer applicationId, InstallationStatus status, String version) {
        deviceApplicationRepository.findByDeviceIdAndApplicationId(deviceId, applicationId).ifPresent(deviceApplication -> {
            deviceApplication.setStatus(status);
            if (version != null) {
                deviceApplication.setInstalledVersion(version);
            }
        });
    }

    private List<Device> resolveTargets(Integer companyId, InstallRequest request) {
        if ((request.deviceId() == null) == (request.groupId() == null)) {
            throw new IllegalArgumentException("Debe indicar exactamente un dispositivo o un grupo destino");
        }
        if (request.deviceId() != null) {
            Device device = deviceRepository.findByIdAndCompanyId(request.deviceId(), companyId)
                    .orElseThrow(() -> new ResourceNotFoundException("Dispositivo no encontrado: " + request.deviceId()));
            return List.of(device);
        }
        DeviceGroup group = deviceGroupRepository.findByIdAndCompanyId(request.groupId(), companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Grupo no encontrado: " + request.groupId()));
        return deviceRepository.findByGroupId(group.getId());
    }

    private String installPayload(Application application) {
        return "{\"applicationId\":" + application.getId()
                + ",\"packageName\":\"" + application.getPackageName() + "\""
                + ",\"version\":\"" + application.getCurrentVersion() + "\""
                + ",\"downloadUrl\":\"/applications/" + application.getId() + "/apk\"}";
    }

    private String storeFile(Integer companyId, String packageName, String version, MultipartFile file) {
        try {
            Path companyDir = storageDir.resolve(String.valueOf(companyId));
            Files.createDirectories(companyDir);
            Path target = companyDir.resolve(packageName + "-" + version + ".apk");
            file.transferTo(target);
            return target.toString();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private Application getOwnedApplication(Integer companyId, Integer id) {
        return applicationRepository.findByIdAndCompanyId(id, companyId)
                .orElseThrow(() -> new ResourceNotFoundException("Aplicación no encontrada: " + id));
    }
}
