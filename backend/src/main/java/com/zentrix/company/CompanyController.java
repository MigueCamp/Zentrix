package com.zentrix.company;

import com.zentrix.company.dto.CompanyRequest;
import com.zentrix.company.dto.CompanyResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Módulo "Administración de Empresas" — solo accesible al rol SUPER_ADMIN,
 * ver docs/04_Especificación_de_Módulos.md, sección 1.
 */
@RestController
@RequestMapping("/companies")
@PreAuthorize("hasAuthority('SUPER_ADMIN')")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CompanyResponse create(@Valid @RequestBody CompanyRequest request) {
        return companyService.create(request);
    }

    @GetMapping
    public List<CompanyResponse> findAll() {
        return companyService.findAll();
    }

    @GetMapping("/{id}")
    public CompanyResponse findById(@PathVariable Integer id) {
        return companyService.findById(id);
    }

    @PutMapping("/{id}")
    public CompanyResponse update(@PathVariable Integer id, @Valid @RequestBody CompanyRequest request) {
        return companyService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable Integer id) {
        companyService.delete(id);
    }
}
