package com.zentrix.user;

import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.common.security.CurrentUser;
import com.zentrix.company.Company;
import com.zentrix.company.CompanyRepository;
import com.zentrix.user.dto.AuditLogResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    public AuditLogService(AuditLogRepository auditLogRepository, UserRepository userRepository,
                            CompanyRepository companyRepository) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    public void record(String action, String detailJson) {
        var actingUser = CurrentUser.get();
        User user = userRepository.findById(actingUser.userId())
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + actingUser.userId()));
        Company company = actingUser.companyId() != null
                ? companyRepository.findById(actingUser.companyId()).orElse(null)
                : null;

        auditLogRepository.save(AuditLog.builder()
                .company(company)
                .user(user)
                .action(action)
                .detailJson(detailJson)
                .build());
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findForCurrentTenant(Integer companyId, Pageable pageable) {
        Page<AuditLog> logs = companyId != null
                ? auditLogRepository.findByCompanyIdOrderByActionDateDesc(companyId, pageable)
                : auditLogRepository.findAllByOrderByActionDateDesc(pageable);
        return logs.map(AuditLogResponse::from);
    }
}
