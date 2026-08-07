package com.zentrix.device;

import com.zentrix.company.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "DISPOSITIVO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Device {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "DispositivoId")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EmpresaId")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "GrupoDispositivoId")
    private DeviceGroup group;

    @Column(name = "IMEI", nullable = false, unique = true, length = 50)
    private String imei;

    @Column(name = "NumeroSerie", length = 100)
    private String serialNumber;

    @Column(name = "Modelo", length = 100)
    private String model;

    @Column(name = "VersionAndroid", length = 20)
    private String androidVersion;

    @Column(name = "TokenEnrollment", length = 255)
    private String enrollmentToken;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false, length = 20)
    private DeviceStatus status;

    @Column(name = "UltimaConexion")
    private LocalDateTime lastSeenAt;

    @Column(name = "FechaRegistro", nullable = false, updatable = false)
    private LocalDateTime registeredAt;

    @PrePersist
    void onCreate() {
        if (registeredAt == null) {
            registeredAt = LocalDateTime.now();
        }
        if (status == null) {
            status = DeviceStatus.OFFLINE;
        }
    }
}
