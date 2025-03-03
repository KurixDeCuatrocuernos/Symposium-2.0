package com.sympos2.securities;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

import com.sympos2.services.ChatWebSocketHandler;

/**
 * This class contains Beans to configure the WebSocket for ChatWebSocketHandler. 
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 * @see WebSocketConfigurer
 */
@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer{
    
	/**
     * This function set the configuration of WebSocket Handler (URL, origin and interceptors).
     * @param registry WebSocketHandlerRegistry which collects the default WebSocketHandler configuratio to edit it. 
     */
	@Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        // Registra un manejador de WebSocket en una URL específica
        registry.addHandler(chatWebSocketHandler(), "/chat")
                .setAllowedOrigins("*")  // Permitir cualquier origen (debe ser ajustado para producción)
                .addInterceptors(new HttpSessionHandshakeInterceptor());
    }

	/**
	 * This method creates the new WebSocket handler (with the specified configuration.
	 * @return returns the new ChatWebSocketHandler with the configuration changed.
	 */
    public WebSocketHandler chatWebSocketHandler() {
        return new ChatWebSocketHandler();
    }
}
