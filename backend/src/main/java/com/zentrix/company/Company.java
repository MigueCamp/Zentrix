package com.zentrix.company;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "EMPRESA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "EmpresaId")
    private Integer id;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "RUC_NIT", nullable = false, unique = true, length = 50)
    private String taxId;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false, length = 20)
    private CompanyStatus status;

    @Column(name = "FechaCreacion", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = CompanyStatus.ACTIVA;
        }
    }
}
