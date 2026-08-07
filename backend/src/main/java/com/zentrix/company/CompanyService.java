package com.zentrix.company;

import com.zentrix.common.DuplicateResourceException;
import com.zentrix.common.ResourceNotFoundException;
import com.zentrix.company.dto.CompanyRequest;
import com.zentrix.company.dto.CompanyResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public CompanyResponse create(CompanyRequest request) {
        if (companyRepository.existsByTaxId(request.taxId())) {
            throw new DuplicateResourceException("Ya existe una empresa con el RUC/NIT " + request.taxId());
        }
        Company company = Company.builder()
                .name(request.name())
                .taxId(request.taxId())
                .status(CompanyStatus.ACTIVA)
                .build();
        return CompanyResponse.from(companyRepository.save(company));
    }

    @Transactional(readOnly = true)
    public List<CompanyResponse> findAll() {
        return companyRepository.findAll().stream()
                .filter(company -> company.getStatus() != CompanyStatus.ELIMINADA)
                .map(CompanyResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public CompanyResponse findById(Integer id) {
        return CompanyResponse.from(getActiveOrSuspended(id));
    }

    public CompanyResponse update(Integer id, CompanyRequest request) {
        Company company = getActiveOrSuspended(id);
        company.setName(request.name());
        company.setTaxId(request.taxId());
        return CompanyResponse.from(company);
    }

    public void delete(Integer id) {
        Company company = getActiveOrSuspended(id);
        company.setStatus(CompanyStatus.ELIMINADA);
    }

    private Company getActiveOrSuspended(Integer id) {
        return companyRepository.findByIdAndStatusNot(id, CompanyStatus.ELIMINADA)
                .orElseThrow(() -> new ResourceNotFoundException("Empresa no encontrada: " + id));
    }
}
