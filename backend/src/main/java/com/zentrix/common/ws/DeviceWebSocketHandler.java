package com.zentrix.common.ws;

import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

@Component
public class DeviceWebSocketHandler extends TextWebSocketHandler {

    private final DeviceEventBroadcaster broadcaster;

    public DeviceWebSocketHandler(DeviceEventBroadcaster broadcaster) {
        this.broadcaster = broadcaster;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        Integer companyId = (Integer) session.getAttributes().get(JwtHandshakeInterceptor.COMPANY_ID_ATTRIBUTE);
        if (companyId != null) {
            broadcaster.register(companyId, session);
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        broadcaster.unregister(session);
    }
}
