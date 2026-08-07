package com.zentrix.common.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Registro en memoria de sesiones WebSocket activas por EmpresaId, y difusión de eventos
 * de telemetría/alertas a la Consola Web — módulo Monitoreo, docs/04, sección 6.
 * En un despliegue multi-instancia esto debería respaldarse en un bus de mensajes
 * (ej. Redis pub/sub); para el alcance actual, una instancia de backend es suficiente.
 */
@Component
public class DeviceEventBroadcaster {

    private final Map<Integer, Set<WebSocketSession>> sessionsByCompany = new ConcurrentHashMap<>();

    public void register(Integer companyId, WebSocketSession session) {
        sessionsByCompany.computeIfAbsent(companyId, key -> ConcurrentHashMap.newKeySet()).add(session);
    }

    public void unregister(WebSocketSession session) {
        sessionsByCompany.values().forEach(sessions -> sessions.remove(session));
    }

    public void broadcast(Integer companyId, String payloadJson) {
        Set<WebSocketSession> sessions = sessionsByCompany.get(companyId);
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        TextMessage message = new TextMessage(payloadJson);
        for (WebSocketSession session : sessions) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(message);
                } catch (IOException ignored) {
                    // Sesión caída: se limpiará en afterConnectionClosed.
                }
            }
        }
    }
}
