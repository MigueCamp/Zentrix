package com.zentrix;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.security.autoconfigure.UserDetailsServiceAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * UserDetailsServiceAutoConfiguration se excluye porque Zentrix no usa el
 * UserDetailsService de Spring: la autenticación es JWT + BCrypt manual
 * en AuthService (ver docs/05_Seguridad_y_Cumplimiento.md).
 * EnableScheduling habilita DeviceOfflineScheduler (módulo Monitoreo).
 */
@SpringBootApplication(exclude = UserDetailsServiceAutoConfiguration.class)
@EnableScheduling
public class ZentrixBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZentrixBackendApplication.class, args);
	}

}
