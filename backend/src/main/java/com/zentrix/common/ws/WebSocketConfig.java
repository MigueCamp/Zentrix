package com.zentrix.common.ws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * Endpoint /ws/devices — WebSocket en tiempo real del módulo Monitoreo (docs/04, sección 6).
 * Cada mensaje entrante ya está autenticado por JwtHandshakeInterceptor y difundido solo
 * a sesiones de la misma empresa por DeviceEventBroadcaster.
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final DeviceWebSocketHandler handler;
    private final JwtHandshakeInterceptor handshakeInterceptor;
    private final String[] allowedOrigins;

    public WebSocketConfig(DeviceWebSocketHandler handler, JwtHandshakeInterceptor handshakeInterceptor,
                            @Value("${zentrix.cors.allowed-origins}") String[] allowedOrigins) {
        this.handler = handler;
        this.handshakeInterceptor = handshakeInterceptor;
        this.allowedOrigins = allowedOrigins;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(handler, "/ws/devices")
                .addInterceptors(handshakeInterceptor)
                .setAllowedOrigins(allowedOrigins);
    }
}
