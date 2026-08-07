package com.zentrix.device;

import com.zentrix.company.Company;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "GRUPO_DISPOSITIVO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeviceGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "GrupoDispositivoId")
    private Integer id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "EmpresaId")
    private Company company;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String name;
}
