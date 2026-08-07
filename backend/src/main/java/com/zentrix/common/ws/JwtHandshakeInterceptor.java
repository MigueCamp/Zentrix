package com.zentrix.common.ws;

import com.zentrix.common.security.AuthenticatedUser;
import com.zentrix.common.security.JwtService;
import io.jsonwebtoken.JwtException;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Los WebSocket del navegador no pueden enviar el header Authorization en el handshake,
 * así que el token JWT viaja como query param (`/ws/devices?token=...`) y se valida aquí
 * antes de aceptar la conexión — mismo JWT que usa el resto de la API.
 */
@Component
public class JwtHandshakeInterceptor implements HandshakeInterceptor {

    static final String COMPANY_ID_ATTRIBUTE = "companyId";

    private final JwtService jwtService;

    public JwtHandshakeInterceptor(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                    WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (!(request instanceof ServletServerHttpRequest servletRequest)) {
            return false;
        }
        String token = servletRequest.getServletRequest().getParameter("token");
        if (token == null) {
            return false;
        }
        try {
            AuthenticatedUser user = jwtService.parseToken(token);
            if (user.companyId() == null) {
                return false;
            }
            attributes.put(COMPANY_ID_ATTRIBUTE, user.companyId());
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response,
                                WebSocketHandler wsHandler, Exception exception) {
        // no-op
    }
}
