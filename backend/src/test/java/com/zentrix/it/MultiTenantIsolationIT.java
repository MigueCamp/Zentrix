package com.zentrix.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Aislamiento multi-tenant a nivel HTTP (docs/07 §3, "no negociable"): con JWT reales y contra
 * SQL Server real, un administrador de la Empresa A no puede leer ni operar recursos de la
 * Empresa B. Complementa a {@code TenantResolverTest} (unitario) probando el cableado completo:
 * JWT → TenantContext → filtro por EmpresaId en el repositorio → código HTTP.
 */
class MultiTenantIsolationIT extends AbstractIntegrationTest {

    private int createCompany(String superToken, String suffix) {
        String body = "{\"name\":\"Empresa-" + suffix + "\",\"taxId\":\"TAX-" + suffix + "\"}";
        return mapper.readTree(post("/companies", body, superToken).getResponseBody()).get("id").asInt();
    }

    private void createCompanyAdmin(String superToken, int companyId, String suffix) {
        String body = "{\"name\":\"Admin " + suffix + "\",\"email\":\"admin-" + suffix
                + "@zentrix.local\",\"password\":\"AdminPass123\"}";
        post("/users?companyId=" + companyId, body, superToken);
    }

    @Test
    @DisplayName("Un EMPRESA_ADMIN de A no puede ver un dispositivo de B ni operar sobre B")
    void adminCannotCrossTenant() {
        String s = UUID.randomUUID().toString().substring(0, 8);
        String superToken = login(BOOTSTRAP_EMAIL, BOOTSTRAP_PASSWORD);

        int companyA = createCompany(superToken, "A" + s);
        int companyB = createCompany(superToken, "B" + s);
        createCompanyAdmin(superToken, companyA, "A" + s);
        createCompanyAdmin(superToken, companyB, "B" + s);

        // Un usuario con empresa recibe autoridad EMPRESA_ADMIN automáticamente al iniciar sesión.
        String tokenA = login("admin-A" + s + "@zentrix.local", "AdminPass123");

        // El super admin registra un dispositivo en la Empresa B.
        String deviceBody = "{\"imei\":\"IMEI-" + s + "\",\"serialNumber\":\"SN-" + s + "\"}";
        int deviceBId = mapper.readTree(
                post("/devices?companyId=" + companyB, deviceBody, superToken).getResponseBody()).get("id").asInt();

        // 1) A no puede LEER el dispositivo de B (aunque conozca su ID) -> 404 en su propio tenant.
        assertThat(get("/devices/" + deviceBId, tokenA).getStatus()).isEqualTo(HttpStatus.NOT_FOUND);

        // 2) A no puede OPERAR sobre la Empresa B pasando companyId=B -> 403 (TenantResolver).
        assertThat(get("/users?companyId=" + companyB, tokenA).getStatus()).isEqualTo(HttpStatus.FORBIDDEN);

        // 3) A sí puede listar sus propios dispositivos (200) y ahí NO aparece el de B.
        var ownDevices = get("/devices", tokenA);
        assertThat(ownDevices.getStatus()).isEqualTo(HttpStatus.OK);
        assertThat(ownDevices.getResponseBody()).doesNotContain("\"id\":" + deviceBId + ",");
    }
}
