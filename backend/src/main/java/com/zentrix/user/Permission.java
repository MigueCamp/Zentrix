package com.zentrix.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "PERMISO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Permission {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "PermisoId")
    private Integer id;

    @Column(name = "Codigo", nullable = false, unique = true, length = 100)
    private String code;

    @Column(name = "Descripcion", nullable = false, length = 255)
    private String description;
}
