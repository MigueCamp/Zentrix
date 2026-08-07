package com.zentrix.monitoring;

import com.zentrix.device.Device;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Alerta generada a partir de telemetría (batería baja, pérdida de conexión, etc.)
 * — módulo Monitoreo, docs/04, sección 6.
 */
@Entity
@Table(name = "ALERTA")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "AlertaId")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DispositivoId")
    private Device device;

    @Column(name = "Tipo", nullable = false, length = 30)
    private String type;

    @Enumerated(EnumType.STRING)
    @Column(name = "Severidad", nullable = false, length = 20)
    private AlertSeverity severity;

    @Column(name = "Mensaje", nullable = false, length = 500)
    private String message;

    @Column(name = "Atendida", nullable = false)
    private boolean acknowledged;

    @Column(name = "FechaCreacion", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }
}
