package com.zentrix.common.security;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Cifrado en reposo de configuración sensible WiFi/VPN (docs/05). AES-256-GCM con IV aleatorio
 * por valor: ida y vuelta correcta, no determinista, y detección de manipulación.
 */
class PolicyCipherTest {

    // Clave AES-256 de prueba (32 bytes en Base64) — no es la de producción.
    private final PolicyCipher cipher = new PolicyCipher("5wYYAh1IGo8WzWpCyjxwWM5/LmMsR7FFF328IRYYwJI=");

    @Test
    @DisplayName("encrypt seguido de decrypt recupera el texto original")
    void roundTrip() {
        String plain = "{\"ssid\":\"Corp\",\"password\":\"S3cr3t-Wifi!\"}";
        String encoded = cipher.encrypt(plain);

        assertThat(encoded).isNotEqualTo(plain);
        assertThat(cipher.decrypt(encoded)).isEqualTo(plain);
    }

    @Test
    @DisplayName("cifrar el mismo valor dos veces produce ciphertext distinto (IV aleatorio)")
    void nonDeterministic() {
        String plain = "clave-vpn-psk";
        assertThat(cipher.encrypt(plain)).isNotEqualTo(cipher.encrypt(plain));
    }

    @Test
    @DisplayName("un ciphertext manipulado no se puede descifrar (autenticación GCM)")
    void tamperedCiphertextFails() {
        String encoded = cipher.encrypt("dato-sensible");
        // Alterar el último carácter Base64 invalida el tag GCM.
        char last = encoded.charAt(encoded.length() - 1);
        String tampered = encoded.substring(0, encoded.length() - 1) + (last == 'A' ? 'B' : 'A');

        assertThatThrownBy(() -> cipher.decrypt(tampered))
                .isInstanceOf(IllegalStateException.class);
    }
}
