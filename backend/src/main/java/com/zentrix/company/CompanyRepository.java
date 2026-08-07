package com.zentrix.company;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, Integer> {

    boolean existsByTaxId(String taxId);

    Optional<Company> findByIdAndStatusNot(Integer id, CompanyStatus status);
}
