package com.zentrix.config;

import com.zentrix.company.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * API key de integración, asociada a una empresa (hereda su aislamiento multi-tenant)
 * — módulo Configuración, docs/04, sección 8. Solo se guarda el hash SHA-256 de la clave;
 * el valor en claro se muestra una única vez al crearla.
 */
@Entity
@Table(name = "API_KEY")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiKey {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ApiKeyId")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EmpresaId")
    private Company company;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "Prefijo", nullable = false, length = 16)
    private String prefix;

    @Column(name = "KeyHash", nullable = false, length = 128)
    private String keyHash;

    @Column(name = "Activa", nullable = false)
    private boolean active;

    @Column(name = "FechaCreacion", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "FechaRevocacion")
    private LocalDateTime revokedAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
