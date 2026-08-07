package com.zentrix.company.dto;

import com.zentrix.company.Company;

import java.time.LocalDateTime;

public record CompanyResponse(Integer id, String name, String taxId, String status, LocalDateTime createdAt) {

    public static CompanyResponse from(Company company) {
        return new CompanyResponse(
                company.getId(),
                company.getName(),
                company.getTaxId(),
                company.getStatus().name(),
                company.getCreatedAt()
        );
    }
}
