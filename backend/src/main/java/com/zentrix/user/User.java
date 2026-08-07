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
@Table(name = "USUARIO")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "UsuarioId")
    private Integer id;

    /**
     * Null únicamente para usuarios SUPER_ADMIN (acceso cross-tenant).
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "EmpresaId")
    private Company company;

    @Column(name = "Nombre", nullable = false, length = 150)
    private String name;

    @Column(name = "Email", nullable = false, unique = true, length = 200)
    private String email;

    @Column(name = "PasswordHash", nullable = false, length = 255)
    private String passwordHash;

    @Enumerated(EnumType.STRING)
    @Column(name = "Estado", nullable = false, length = 20)
    private UserStatus status;

    @Column(name = "FechaCreacion", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (status == null) {
            status = UserStatus.ACTIVO;
        }
    }
}
