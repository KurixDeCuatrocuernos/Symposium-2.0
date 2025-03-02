package com.sympos2.services;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;

import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

public class ChatWebSocketHandler extends TextWebSocketHandler{
	
    private static final Set<WebSocketSession> sessions = new CopyOnWriteArraySet<>();
    private static final Map<WebSocketSession, String> userNames = new HashMap<>();

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

    // Método para manejar el nombre de usuario del cliente
    public void setUserName(WebSocketSession session, String userName) {
        userNames.put(session, userName);
    }
}
