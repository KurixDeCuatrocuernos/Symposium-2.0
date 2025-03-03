package com.sympos2.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * This class manages the chat between two users.
 * @author KurixDeCuatroCuernos
 * @version 0.1.0
 * @see WebSocketConfig
 * @see TextWebSocketHandler
 */
public class ChatWebSocketHandler extends TextWebSocketHandler{
	
    private static final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private static final Map<WebSocketSession, String> userNames = new HashMap<>();
    
    /**
     * This method handles the text messages in chat.
     * @param session WebSocketSession with the current session of an user in chat.
     * @param message TextMessage with the message that an user publics in chat.
     */
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        // Verificar si ya hay 2 clientes conectados
        if (sessions.size() > 2) {
            session.sendMessage(new TextMessage("No se pueden conectar más de 2 clientes."));
            session.close();
            return;
        }

        // Si el mensaje recibido es el nombre, lo almacenamos
        String userName = message.getPayload();
        if (userNames.get(session) == null && !userName.isEmpty()) {
            setUserName(session, userName);
            session.sendMessage(new TextMessage("¡Bienvenido, " + userName + "!"));

            // Notificar a los demás usuarios que se ha unido un nuevo usuario
            for (WebSocketSession s : sessions) {
                if (s.isOpen() && !s.getId().equals(session.getId())) {
                    String otherUserName = userNames.get(s);
                    s.sendMessage(new TextMessage(userName + " se ha unido al chat."));
                }
            }

            return; // No procesar más el mensaje, ya que es el nombre del usuario
        }

        // Obtener el nombre del usuario que está enviando el mensaje
        userName = userNames.get(session);

        // Enviar el mensaje a todos los clientes conectados
        for (WebSocketSession s : sessions) {
            if (s.isOpen() && !s.getId().equals(session.getId())) {
                String otherUserName = userNames.get(s);  // Nombre del usuario contrario
                s.sendMessage(new TextMessage(userName + " dijo: \n" + message.getPayload()));
            }
        }
    }
    
    /**
     * This method handles the behavior of the chat socket when an User connects to the chat. 
     * @param session WebSocketSession with the current session of an user in chat.
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        super.afterConnectionEstablished(session);
        if (sessions.size() <= 2) {
            sessions.add(session);
            System.out.println("Cliente conectado: " + session.getId());

            // Enviar un mensaje al cliente pidiendo el nombre de usuario
        } else {
            session.sendMessage(new TextMessage("El número máximo de conexiones (2) ha sido alcanzado."));
            session.close();
        }
    }
    
    /**
     * This method handles the behavior of the chat socket when an User disconnects from the chat.
     * @param session WebSocketSession with the current session of an user in chat.
     * @param status the status of the chat server.
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, org.springframework.web.socket.CloseStatus status) throws Exception {
        super.afterConnectionClosed(session, status);

        // Obtener el nombre del usuario desconectado
        String userName = userNames.get(session);

        // Eliminar la sesión y su nombre de usuario
        sessions.remove(session);
        userNames.remove(session);
        System.out.println("Cliente desconectado: " + session.getId());

        // Notificar a los demás usuarios que el usuario se ha desconectado
        if (userName != null) {
            for (WebSocketSession s : sessions) {
                if (s.isOpen()) {
                    s.sendMessage(new TextMessage(userName + " ha abandonado el chat."));
                }
            }
        }
    }

    /**
     * This method links a UserName to a session object.
     * @param session WebSocketSession with the session of an user in chat.
     * @param userName String with the user name to link it to a session.
     */
    public void setUserName(WebSocketSession session, String userName) {
        userNames.put(session, userName);
    }
}
