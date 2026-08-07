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
 * Configuración de seguridad por empresa (política de contraseñas, expiración de sesión)
 * — módulo Configuración, docs/04, sección 8.
 */
@Entity
@Table(name = "CONFIGURACION_SEGURIDAD")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SecuritySettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ConfiguracionSeguridadId")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EmpresaId")
    private Company company;

    @Column(name = "PasswordMinLength", nullable = false)
    private int passwordMinLength;

    @Column(name = "RequireUppercase", nullable = false)
    private boolean requireUppercase;

    @Column(name = "RequireDigit", nullable = false)
    private boolean requireDigit;

    @Column(name = "RequireSpecial", nullable = false)
    private boolean requireSpecial;

    @Column(name = "SessionExpirationMinutes", nullable = false)
    private int sessionExpirationMinutes;

    @Column(name = "FechaActualizacion", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void onSave() {
        updatedAt = LocalDateTime.now();
    }

    public static SecuritySettings defaultsFor(Company company) {
        return SecuritySettings.builder()
                .company(company)
                .passwordMinLength(8)
                .requireUppercase(true)
                .requireDigit(true)
                .requireSpecial(false)
                .sessionExpirationMinutes(30)
                .build();
    }
}
