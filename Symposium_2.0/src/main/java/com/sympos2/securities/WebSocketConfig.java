package com.sympos2.securities;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.sympos2.services.ChatWebSocketHandler;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer{
    
	@Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Registra un manejador de WebSocket en una URL específica
        registry.addHandler(chatWebSocketHandler(), "/chat")
                .setAllowedOrigins("*")  // Permitir cualquier origen (debe ser ajustado para producción)
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }

    public WebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketHandler();
    }
}
