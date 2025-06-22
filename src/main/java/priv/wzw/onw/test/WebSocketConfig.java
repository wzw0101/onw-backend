package priv.wzw.onw.test;

import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

//@EnableWebSocket
//@Configuration
public class WebSocketConfig implements WebSocketConfigurer {

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(testWebSocketHandler(), "/test/websocket").setAllowedOrigins("*");
    }

    @Bean
    public TestWebSocketHandler testWebSocketHandler() {
        return new TestWebSocketHandler();
    }
}
