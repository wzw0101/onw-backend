package priv.wzw.onw.test;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.HashMap;
import java.util.Map;

@Slf4j
public class TestWebSocketHandler extends TextWebSocketHandler {
    private Map<String, WebSocketSession> connectedSession = new HashMap<>();


    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        connectedSession.put(session.getId(), session);
        super.afterConnectionEstablished(session);
    }

    @Override
    protected void handleTextMessage(WebSocketSession messageSession, TextMessage message) throws Exception {
        log.info("message session id {}", messageSession);
        for (WebSocketSession session : connectedSession.values()) {
            session.sendMessage(new TextMessage(message.getPayload() + " joined the group"));
        }
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        connectedSession.remove(session.getId());
        session.close();
        super.afterConnectionClosed(session, status);
    }
}
