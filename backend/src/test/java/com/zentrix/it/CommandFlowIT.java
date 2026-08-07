package com.zentrix.it;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import tools.jackson.databind.JsonNode;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Flujo de comandos asíncronos de punta a punta (docs/07 §4) contra SQL Server real:
 * asignar una política encola un comando, el dispositivo lo recibe por polling
 * (PENDIENTE → ENVIADO), lo confirma (ack) y no se le vuelve a entregar (entrega idempotente).
 */
class CommandFlowIT extends AbstractIntegrationTest {

    @Test
    @DisplayName("Asignar política → encola comando → dispositivo hace polling, ack y no se re-entrega")
    void policyAssignmentReachesDeviceOnce() {
        String s = UUID.randomUUID().toString().substring(0, 8);
        String superToken = login(BOOTSTRAP_EMAIL, BOOTSTRAP_PASSWORD);

        // Empresa + dispositivo pre-registrado.
        int companyId = mapper.readTree(post("/companies",
                "{\"name\":\"Cmd-" + s + "\",\"taxId\":\"TAX-" + s + "\"}", superToken).getResponseBody())
                .get("id").asInt();

        JsonNode registered = mapper.readTree(post("/devices?companyId=" + companyId,
                "{\"imei\":\"IMEI-" + s + "\",\"serialNumber\":\"SN-" + s + "\"}", superToken).getResponseBody());
        int deviceId = registered.get("id").asInt();
        String enrollmentToken = registered.get("enrollmentToken").asText();
        String imei = registered.get("imei").asText();

        // El agente se inscribe y obtiene su token de dispositivo (/devices/enroll es público).
        String deviceToken = mapper.readTree(post("/devices/enroll",
                "{\"enrollmentToken\":\"" + enrollmentToken + "\",\"imei\":\"" + imei
                        + "\",\"serialNumber\":\"SN-" + s + "\"}", null).getResponseBody())
                .get("deviceToken").asText();

        // Crear una política de kiosco y asignarla al dispositivo (encola APPLY_POLICY).
        int policyId = mapper.readTree(post("/policies?companyId=" + companyId,
                "{\"name\":\"Kiosco-" + s + "\",\"type\":\"KIOSCO\",\"configurationJson\":\"{\\\"lockTask\\\":true}\"}",
                superToken).getResponseBody()).get("id").asInt();
        var assign = post("/policies/" + policyId + "/assign?companyId=" + companyId,
                "{\"deviceId\":" + deviceId + ",\"groupId\":null}", superToken);
        assertThat(assign.getStatus().is2xxSuccessful()).isTrue();

        // El dispositivo hace polling: recibe exactamente 1 comando APPLY_POLICY.
        var pending = get("/devices/commands/pending", deviceToken);
        assertThat(pending.getStatus()).isEqualTo(HttpStatus.OK);
        JsonNode commands = mapper.readTree(pending.getResponseBody());
        assertThat(commands.isArray()).isTrue();
        assertThat(commands.size()).isEqualTo(1);
        assertThat(commands.get(0).get("type").asText()).isEqualTo("APPLY_POLICY");
        long commandId = commands.get(0).get("id").asLong();

        // Confirma la ejecución (ack COMPLETADO).
        var ack = post("/devices/commands/" + commandId + "/ack",
                "{\"status\":\"COMPLETADO\",\"detail\":\"aplicada\"}", deviceToken);
        assertThat(ack.getStatus().is2xxSuccessful()).isTrue();

        // Un segundo polling ya NO devuelve el comando (fue entregado: ENVIADO/COMPLETADO, no PENDIENTE).
        var pendingAgain = get("/devices/commands/pending", deviceToken);
        assertThat(mapper.readTree(pendingAgain.getResponseBody()).size()).isZero();
    }
}
