package com.zentrix.user;

import com.zentrix.company.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "LOG_AUDITORIA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "LogAuditoriaId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmpresaId")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UsuarioId")
    private User user;

    @Column(name = "Accion", nullable = false, length = 100)
    private String action;

    @Column(name = "DetalleJson", columnDefinition = "NVARCHAR(MAX)")
    private String detailJson;

    @Column(name = "FechaAccion", nullable = false, updatable = false)
    private LocalDateTime actionDate;

    @PrePersist
    void onCreate() {
        if (actionDate == null) {
            actionDate = LocalDateTime.now();
        }
    }
}
