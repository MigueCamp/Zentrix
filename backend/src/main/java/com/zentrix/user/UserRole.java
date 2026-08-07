package com.zentrix.user;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "USUARIO_ROL")
@IdClass(UserRoleId.class)
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UserRole {

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "UsuarioId")
    private User user;

    @Id
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "RolId")
    private Role role;
}
