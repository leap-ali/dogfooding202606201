package com.ddz.socket;

import com.corundumstudio.socketio.SocketIOClient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SocketSessionManager {

    private static final Map<String, SocketIOClient> clients = new ConcurrentHashMap<>();
    private static final Map<String, String> userRoomMap = new ConcurrentHashMap<>();
    private static final Map<String, Long> clientUserIdMap = new ConcurrentHashMap<>();

    public static void addClient(String sessionId, SocketIOClient client) {
        clients.put(sessionId, client);
    }

    public static void removeClient(String sessionId) {
        clients.remove(sessionId);
        Long userId = clientUserIdMap.remove(sessionId);
        if (userId != null) {
            userRoomMap.entrySet().removeIf(entry -> entry.getKey().equals(sessionId));
        }
    }

    public static SocketIOClient getClient(String sessionId) {
        return clients.get(sessionId);
    }

    public static void setUserRoom(String sessionId, String roomNo) {
        userRoomMap.put(sessionId, roomNo);
    }

    public static String getUserRoom(String sessionId) {
        return userRoomMap.get(sessionId);
    }

    public static void setClientUserId(String sessionId, Long userId) {
        clientUserIdMap.put(sessionId, userId);
    }

    public static Long getClientUserId(String sessionId) {
        return clientUserIdMap.get(sessionId);
    }

    public static Map<String, SocketIOClient> getAllClients() {
        return clients;
    }
}
