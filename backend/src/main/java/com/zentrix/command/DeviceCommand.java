package com.zentrix.command;

import com.zentrix.device.Device;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * Cola de Comandos (docs/02_Arquitectura_del_Sistema.md, sección 4.2): toda orden asíncrona
 * hacia el agente Android (aplicar política, instalar/desinstalar app) pasa por aquí.
 */
@Entity
@Table(name = "COMANDO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceCommand {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ComandoId")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DispositivoId")
    private Device device;

    @Enumerated(EnumType.STRING)
    @Column(name = "Tipo", nullable = false, length = 30)
    private CommandType type;

    @Column(name = "PayloadJson")
    private String payloadJson;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false, length = 20)
    private CommandStatus status;

    @Column(name = "DetalleResultado", length = 500)
    private String resultDetail;

    @Column(name = "FechaCreacion", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "FechaActualizacion", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (createdAt == null) {
            createdAt = now;
        }
        updatedAt = now;
        if (status == null) {
            status = CommandStatus.PENDIENTE;
        }
    }

    @PreUpdate
    void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
