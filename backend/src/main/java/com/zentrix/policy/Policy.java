package com.zentrix.policy;

import com.zentrix.company.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Perfil de configuración (WiFi, VPN, kiosco, restricciones). Ver
 * docs/04_Especificación_de_Módulos.md, sección 4.
 */
@Entity
@Table(name = "POLITICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Policy {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PoliticaId")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EmpresaId")
    private Company company;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "Tipo", nullable = false, length = 30)
    private PolicyType type;

    @Column(name = "ConfiguracionJson", nullable = false)
    private String configurationJson;

    @Column(name = "Cifrada", nullable = false)
    private boolean encrypted;

    @Column(name = "FechaActualizacion", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void onSave() {
        updatedAt = LocalDateTime.now();
    }
}
