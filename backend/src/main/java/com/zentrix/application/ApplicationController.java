package com.zentrix.application;

import com.zentrix.application.dto.ApplicationResponse;
import com.zentrix.application.dto.DeviceApplicationResponse;
import com.zentrix.application.dto.InstallRequest;
import com.zentrix.common.tenant.TenantResolver;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * Módulo "Aplicaciones" (docs/04_Especificación_de_Módulos.md, sección 5).
 * /applications/{id}/apk lo consume tanto la consola como el agente Android.
 */
@RestController
@RequestMapping("/applications")
public class ApplicationController {

    private final ApplicationService applicationService;

    public ApplicationController(ApplicationService applicationService) {
        this.applicationService = applicationService;
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    @ResponseStatus(HttpStatus.CREATED)
    public ApplicationResponse upload(@RequestParam(required = false) Integer companyId,
                                       @RequestParam String name, @RequestParam String packageName,
                                       @RequestParam String version, @RequestParam MultipartFile file) {
        return applicationService.upload(TenantResolver.resolve(companyId), name, packageName, version, file);
    }

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public List<ApplicationResponse> findAll(@RequestParam(required = false) Integer companyId) {
        return applicationService.findAll(TenantResolver.resolve(companyId));
    }

    @GetMapping("/{id}/apk")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN', 'DEVICE')")
    public ResponseEntity<byte[]> download(@RequestParam(required = false) Integer companyId, @PathVariable Integer id) {
        byte[] apk = applicationService.readApkBytes(TenantResolver.resolve(companyId), id);
        return ResponseEntity.ok()
                .contentType(MediaType.valueOf("application/vnd.android.package-archive"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"app-" + id + ".apk\"")
                .body(apk);
    }

    @PostMapping("/{id}/install")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public DeviceApplicationResponse install(@RequestParam(required = false) Integer companyId, @PathVariable Integer id,
                                              @RequestBody InstallRequest request) {
        return applicationService.install(TenantResolver.resolve(companyId), id, request);
    }

    @PostMapping("/{id}/uninstall")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void uninstall(@RequestParam(required = false) Integer companyId, @PathVariable Integer id,
                           @RequestBody InstallRequest request) {
        applicationService.uninstall(TenantResolver.resolve(companyId), id, request);
    }

    @GetMapping("/{id}/versions")
    @PreAuthorize("hasAnyAuthority('SUPER_ADMIN', 'EMPRESA_ADMIN')")
    public List<DeviceApplicationResponse> versions(@RequestParam(required = false) Integer companyId, @PathVariable Integer id) {
        return applicationService.versions(TenantResolver.resolve(companyId), id);
    }
}
