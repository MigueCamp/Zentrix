package com.zentrix.user;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {

    Page<AuditLog> findByCompanyIdOrderByActionDateDesc(Integer companyId, Pageable pageable);

    Page<AuditLog> findAllByOrderByActionDateDesc(Pageable pageable);
}
