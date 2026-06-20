package com.ddz.socket;

import com.ddz.game.GameRoom;
import com.ddz.game.Player;
import com.ddz.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class GameRoomManager {

    private final Map<String, GameRoom> rooms = new ConcurrentHashMap<>();

    @Autowired
    private UserService userService;

    public GameRoom getRoom(String roomNo) {
        return rooms.get(roomNo);
    }

    public GameRoom createRoom(String roomNo, int baseScore) {
        GameRoom room = new GameRoom();
        room.setRoomNo(roomNo);
        room.setBaseScore(baseScore);
        rooms.put(roomNo, room);
        return room;
    }

    public void removeRoom(String roomNo) {
        rooms.remove(roomNo);
    }

    public Player getPlayerInRoom(String roomNo, Long userId) {
        GameRoom room = rooms.get(roomNo);
        if (room == null) return null;
        return room.getPlayerByUserId(userId);
    }

    public boolean addPlayerToRoom(String roomNo, Player player) {
        GameRoom room = rooms.get(roomNo);
        if (room == null) return false;
        if (room.isFull()) return false;
        room.addPlayer(player);
        return true;
    }

    public void removePlayerFromRoom(String roomNo, Long userId) {
        GameRoom room = rooms.get(roomNo);
        if (room == null) return;
        room.removePlayer(userId);
        if (room.getPlayers().isEmpty()) {
            removeRoom(roomNo);
        }
    }

    public int getRoomPlayerCount(String roomNo) {
        GameRoom room = rooms.get(roomNo);
        if (room == null) return 0;
        return room.getPlayers().size();
    }
}
