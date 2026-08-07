package com.zentrix.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "ROL_PERMISO")
@IdClass(RolePermissionId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class RolePermission {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RolId")
    private Role role;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "PermisoId")
    private Permission permission;
}
