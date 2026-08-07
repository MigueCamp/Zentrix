package com.zentrix.auth;

import com.zentrix.user.User;
import com.zentrix.user.UserRepository;
import com.zentrix.user.UserStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

/**
 * Crea el primer usuario SUPER_ADMIN si zentrix.bootstrap.admin-email/admin-password
 * están configurados y aún no existe. Pensado para el arranque inicial de un ambiente
 * nuevo (ver docs/08_Plan_de_Implementación.md, Fase 0); no crea nada si ya hay datos.
 */
@Component
public class SuperAdminBootstrap implements ApplicationRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String bootstrapEmail;
    private final String bootstrapPassword;

    public SuperAdminBootstrap(UserRepository userRepository,
                                PasswordEncoder passwordEncoder,
                                @Value("${zentrix.bootstrap.admin-email:}") String bootstrapEmail,
                                @Value("${zentrix.bootstrap.admin-password:}") String bootstrapPassword) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.bootstrapEmail = bootstrapEmail;
        this.bootstrapPassword = bootstrapPassword;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(bootstrapEmail) || !StringUtils.hasText(bootstrapPassword)) {
            return;
        }
        if (userRepository.findByEmailAndStatus(bootstrapEmail, UserStatus.ACTIVO).isPresent()) {
            return;
        }

        User admin = User.builder()
                .company(null)
                .name("Super Administrador")
                .email(bootstrapEmail)
                .passwordHash(passwordEncoder.encode(bootstrapPassword))
                .status(UserStatus.ACTIVO)
                .build();
        userRepository.save(admin);
    }
}
