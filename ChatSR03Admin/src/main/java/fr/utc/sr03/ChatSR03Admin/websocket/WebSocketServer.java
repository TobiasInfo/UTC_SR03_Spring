package fr.utc.sr03.ChatSR03Admin.websocket;

import fr.utc.sr03.ChatSR03Admin.repository.UserRepository;
import jakarta.websocket.*;
import jakarta.websocket.server.PathParam;
import jakarta.websocket.server.ServerEndpoint;
import jakarta.websocket.server.ServerEndpointConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * WebSocket server endpoint for handling chat sessions.
 */
@Component
@ServerEndpoint(value = "/WebSocketServer/{idChat}/{idUser}", configurator = WebSocketServer.EndpointConfigurator.class)
public class WebSocketServer {
    private static final Logger LOGGER = LoggerFactory.getLogger(WebSocketServer.class);

    private static WebSocketServer singleton;

    private static UserRepository userRepository;

    private final Hashtable<Integer, Set<Session>> chatSessions = new Hashtable<>();

    /**
     * Private constructor to enforce singleton pattern.
     * @param userRepository the user repository
     */
    private WebSocketServer(UserRepository userRepository) {
        WebSocketServer.userRepository = userRepository;
    }

    public static class EndpointConfigurator extends ServerEndpointConfig.Configurator {
        @Override
        @SuppressWarnings("unchecked")
        public <T> T getEndpointInstance(Class<T> endpointClass) {
            return (T) WebSocketServer.getInstance();
        }
    }

    public static WebSocketServer getInstance() {
        if (WebSocketServer.singleton == null) {
            WebSocketServer.singleton = new WebSocketServer(userRepository);
        }
        return WebSocketServer.singleton;
    }

    /**
     * Called when a new WebSocket connection is opened.
     * @param session the WebSocket session
     * @param idChat the chat ID
     * @param idUser the user ID
     */
    @OnOpen
    public void open(Session session,
                     @PathParam("idChat") Integer idChat,
                     @PathParam("idUser") Integer idUser
    ) {
        LOGGER.info("Session ouverte pour [{}] dans le chat [{}]", idUser, idChat);

        session.getUserProperties().put("idChat", idChat);
        session.getUserProperties().put("idUser", idUser);

        //chatSessions.computeIfAbsent(idChat, k -> new HashSet<>()).add(session);
        if(chatSessions.get(idChat) == null){
            chatSessions.put(idChat, new HashSet<>());
        }
        chatSessions.get(idChat).add(session);
        sendConnectedUsers(idChat);
        sendMessageToAll(idChat, "User [" + idUser + "] has joined the chat.");
    }

    /**
     * Called when a WebSocket connection is closed.
     * @param session the WebSocket session
     */
    @OnClose
    public void close(Session session) {
        Integer idChat = (Integer) session.getUserProperties().get("idChat");
        Integer idUser = (Integer) session.getUserProperties().get("idUser");

        LOGGER.info("Session fermée pour [{}] dans le chat [{}]", idUser, idChat);

        if (chatSessions.get(idChat) != null) {
            chatSessions.get(idChat).remove(session);
            if (chatSessions.get(idChat).isEmpty()) {
                chatSessions.remove(idChat);
                return;
            }
        }
        sendMessageToAll(idChat, "User [" + idUser + "] has left the chat.");
        sendConnectedUsers(idChat);
        try {
            session.close(); // Fermer la session WebSocket de l'utilisateur
        } catch (IOException e) {
            LOGGER.error("Erreur lors de la fermeture de la session WebSocket pour l'utilisateur [{}] : {}", idUser, e.getMessage());
        }
    }

    /**
     * Called when an error occurs in the WebSocket connection.
     * @param session the WebSocket session
     * @param error the error that occurred
     */
    @OnError
    public void onError(Session session, Throwable error) {
        LOGGER.error("Error in session [{}]: {}", session.getId(), error.getMessage());
    }

    /**
     * Called when a message is received from a WebSocket client.
     * @param message the message received
     * @param session the WebSocket session
     */
    @OnMessage
    public void onMessage(String message, Session session) {
        Integer idChat = (Integer) session.getUserProperties().get("idChat");
        Integer idUser = (Integer) session.getUserProperties().get("idUser");

        LOGGER.info("Message reçu de [{}] dans le chat [{}] : [{}]", idUser, idChat, message);
        sendMessageToAll(idChat, "User [" + idUser + "]: " + message);
    }

    /**
     * Sends a message to a specific WebSocket session.
     * @param session the WebSocket session
     * @param message the message to send
     */
    private void sendMessage(Session session, String message) {
        try {
            session.getBasicRemote().sendText(message);
            LOGGER.info("J'ai bien envoyer le message : {}", message);
        } catch (IOException e) {
            LOGGER.error("Erreur lors de l'envoi du message à la session [{}] : {}", session.getId(), e.getMessage());
        }
    }

    /**
     * Sends a message to all WebSocket clients in a specific chat.
     * @param idChat the chat ID
     * @param message the message to send
     */
    private void sendMessageToAll(Integer idChat, String message) {
        // Extract the idUser from the message JSON string
        Integer senderIdUser = extractIdUserFromMessage(message);
        if (senderIdUser == null) {
            LOGGER.error("Failed to extract idUser from message: {}", message);
            return;
        }

        Set<Session> sessions = chatSessions.get(idChat);
        if (sessions != null) {
            for (Session s : sessions) {
                Integer sessionIdUser = (Integer) s.getUserProperties().get("idUser");
                // Check if the session's user ID is not equal to the sender's user ID
                if (!senderIdUser.equals(sessionIdUser)) {
                    LOGGER.info("Envoi du message [{}] à [{}]", message, sessionIdUser);
                    sendMessage(s, message);
                }
            }
        }
    }

    /**
     * Sends the list of connected users to all WebSocket clients in a specific chat.
     * @param idChat the chat ID
     */
    private void sendConnectedUsers(Integer idChat) {
        Set<Session> sessions = chatSessions.get(idChat);
        if (sessions != null) {
            Set<Integer> connectedUsers = sessions.stream()
                    .map(s -> (Integer) s.getUserProperties().get("idUser"))
                    .collect(Collectors.toSet());
            String message = "{\"type\": \"userStatusUpdate\", \"connectedUsers\": " + connectedUsers.toString() + "}";
            for (Session s : sessions) {
                sendMessage(s, message);
            }
        }
    }

    /**
     * Extracts the user ID from a JSON message.
     * @param message the JSON message
     * @return the user ID, or null if extraction fails
     */
    private Integer extractIdUserFromMessage(String message) {
        try {
            // Simple JSON parsing without dependencies
            int idUserIndex = message.indexOf("\"idUser\":");
            if (idUserIndex == -1) {
                return null;
            }

            int startIndex = idUserIndex + 9;
            int endIndex = message.indexOf(",", startIndex);
            if (endIndex == -1) {
                endIndex = message.indexOf("}", startIndex);
            }

            if (endIndex == -1) {
                return null;
            }

            String idUserStr = message.substring(startIndex, endIndex).trim();
            return Integer.parseInt(idUserStr);
        } catch (Exception e) {
            LOGGER.error("Error parsing idUser from message", e);
            return null;
        }
    }

    /**
     * Retrieves the sessions for a specific chat ID.
     * @param idChat the chat ID
     * @return the set of sessions
     */
    public Set<Session> getSessions(Integer idChat) {
        return chatSessions.get(idChat);
    }
}
