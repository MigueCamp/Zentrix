package com.zentrix.application;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ApplicationRepository extends JpaRepository<Application, Integer> {

    List<Application> findByCompanyId(Integer companyId);

    Optional<Application> findByIdAndCompanyId(Integer id, Integer companyId);

    Optional<Application> findByCompanyIdAndPackageName(Integer companyId, String packageName);
}
