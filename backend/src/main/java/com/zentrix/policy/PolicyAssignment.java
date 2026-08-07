package com.zentrix.policy;

import com.zentrix.device.Device;
import com.zentrix.device.DeviceGroup;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "ASIGNACION_POLITICA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PolicyAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AsignacionPoliticaId")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PoliticaId")
    private Policy policy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "DispositivoId")
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GrupoDispositivoId")
    private DeviceGroup group;

    @Column(name = "FechaAsignacion", nullable = false, updatable = false)
    private LocalDateTime assignedAt;

    @PrePersist
    void onCreate() {
        if (assignedAt == null) {
            assignedAt = LocalDateTime.now();
        }
    }
}
