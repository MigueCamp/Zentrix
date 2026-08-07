package com.zentrix.application;

import com.zentrix.device.Device;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "DISPOSITIVO_APLICACION")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DispositivoAplicacionId")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "DispositivoId")
    private Device device;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "AplicacionId")
    private Application application;

    @Column(name = "VersionInstalada", length = 30)
    private String installedVersion;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false, length = 20)
    private InstallationStatus status;

    @Column(name = "FechaActualizacion", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    void onSave() {
        updatedAt = LocalDateTime.now();
        if (status == null) {
            status = InstallationStatus.PENDIENTE;
        }
    }
}
