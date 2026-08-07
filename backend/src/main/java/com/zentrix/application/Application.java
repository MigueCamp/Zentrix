package com.zentrix.application;

import com.zentrix.company.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Catálogo de APKs distribuibles a la flota. Ver docs/04, sección 5.
 */
@Entity
@Table(name = "APLICACION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Application {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AplicacionId")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EmpresaId")
    private Company company;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "PackageName", nullable = false, length = 150)
    private String packageName;

    @Column(name = "VersionActual", nullable = false, length = 30)
    private String currentVersion;

    @Column(name = "UrlArchivo", nullable = false, length = 500)
    private String fileUrl;

    @Column(name = "FechaSubida", nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    @PrePersist
    void onCreate() {
        if (uploadedAt == null) {
            uploadedAt = LocalDateTime.now();
        }
    }
}
