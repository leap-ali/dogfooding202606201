package com.ddz.socket;

import com.alibaba.fastjson.JSON;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnConnect;
import com.corundumstudio.socketio.annotation.OnDisconnect;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.ddz.entity.GameRecord;
import com.ddz.entity.User;
import com.ddz.game.Card;
import com.ddz.game.CardType;
import com.ddz.game.GameRoom;
import com.ddz.game.Player;
import com.ddz.service.GameRecordService;
import com.ddz.service.UserService;
import com.ddz.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.util.*;
import java.util.stream.Collectors;

@Component
public class SocketEventHandler {

    @Autowired
    private SocketIOServer server;

    @Autowired
    private GameRoomManager roomManager;

    @Autowired
    private UserService userService;

    @Autowired
    private GameRecordService gameRecordService;

    @Autowired
    private JwtUtil jwtUtil;

    @Autowired
    private com.ddz.service.RoomService roomService;

    @PostConstruct
    public void start() {
        server.addConnectListener(client -> onConnect(client));
        server.addDisconnectListener(client -> onDisconnect(client));

        server.addEventListener("join_room", Map.class, (client, data, ackSender) -> {
            handleJoinRoom(client, data);
        });

        server.addEventListener("leave_room", Map.class, (client, data, ackSender) -> {
            handleLeaveRoom(client, data);
        });

        server.addEventListener("ready_game", Map.class, (client, data, ackSender) -> {
            handleReadyGame(client, data);
        });

        server.addEventListener("grab_landlord", Map.class, (client, data, ackSender) -> {
            handleGrabLandlord(client, data);
        });

        server.addEventListener("play_cards", Map.class, (client, data, ackSender) -> {
            handlePlayCards(client, data);
        });

        server.addEventListener("pass_cards", Map.class, (client, data, ackSender) -> {
            handlePassCards(client, data);
        });

        server.addEventListener("hint_cards", Map.class, (client, data, ackSender) -> {
            handleHintCards(client, data, ackSender);
        });

        server.addEventListener("chat_message", Map.class, (client, data, ackSender) -> {
            handleChatMessage(client, data);
        });

        server.start();
        System.out.println("Socket.IO server started on port 9092");
    }

    @PreDestroy
    public void stop() {
        server.stop();
    }

    @OnConnect
    public void onConnect(SocketIOClient client) {
        String token = client.getHandshakeData().getSingleUrlParam("token");
        if (token == null || token.isEmpty()) {
            client.disconnect();
            return;
        }
        Long userId = jwtUtil.getUserId(token);
        if (userId == null) {
            client.disconnect();
            return;
        }
        SocketSessionManager.addClient(client.getSessionId().toString(), client);
        SocketSessionManager.setClientUserId(client.getSessionId().toString(), userId);
        System.out.println("Client connected: " + client.getSessionId() + ", userId: " + userId);
    }

    @OnDisconnect
    public void onDisconnect(SocketIOClient client) {
        String sessionId = client.getSessionId().toString();
        String roomNo = SocketSessionManager.getUserRoom(sessionId);
        Long userId = SocketSessionManager.getClientUserId(sessionId);
        if (roomNo != null && userId != null) {
            GameRoom room = roomManager.getRoom(roomNo);
            if (room != null) {
                Player player = room.getPlayerByUserId(userId);
                if (player != null) {
                    player.setIsOnline(false);
                    broadcastRoomUpdate(roomNo);
                }
            }
        }
        SocketSessionManager.removeClient(sessionId);
        System.out.println("Client disconnected: " + sessionId);
    }

    private void handleJoinRoom(SocketIOClient client, Map<String, Object> data) {
        String sessionId = client.getSessionId().toString();
        Long userId = SocketSessionManager.getClientUserId(sessionId);
        if (userId == null) return;

        String roomNo = (String) data.get("roomNo");
        if (roomNo == null || roomNo.isEmpty()) return;

        String oldRoom = SocketSessionManager.getUserRoom(sessionId);
        if (oldRoom != null && !oldRoom.equals(roomNo)) {
            roomManager.removePlayerFromRoom(oldRoom, userId);
            broadcastRoomUpdate(oldRoom);
        }

        User user = userService.getUserById(userId);
        if (user == null) {
            client.sendEvent("error", "用户不存在");
            return;
        }

        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null) {
            com.ddz.entity.Room dbRoom = roomService.getRoomByNo(roomNo);
            if (dbRoom == null) {
                client.sendEvent("error", "房间不存在");
                return;
            }
            room = roomManager.createRoom(roomNo, dbRoom.getBaseScore());
        }

        Player existingPlayer = room.getPlayerByUserId(userId);
        if (existingPlayer != null) {
            existingPlayer.setIsOnline(true);
            SocketSessionManager.setUserRoom(sessionId, roomNo);
            client.joinRoom(roomNo);
            client.sendEvent("room_state", buildRoomState(room, userId));
            broadcastRoomUpdate(roomNo);
            return;
        }

        if (room.isFull()) {
            client.sendEvent("error", "房间已满");
            return;
        }

        if (!"WAITING".equals(room.getGameStatus())) {
            client.sendEvent("error", "游戏已开始，无法加入");
            return;
        }

        if (user.getScore() < 0) {
            client.sendEvent("error", "积分为负，无法开局");
            return;
        }

        Player player = new Player(userId, user.getNickname(), user.getScore());
        player.setAvatar(user.getAvatar());
        player.setUsername(user.getUsername());
        roomManager.addPlayerToRoom(roomNo, player);
        SocketSessionManager.setUserRoom(sessionId, roomNo);
        client.joinRoom(roomNo);

        client.sendEvent("room_state", buildRoomState(room, userId));
        broadcastRoomUpdate(roomNo);

        if (room.isFull() && "WAITING".equals(room.getGameStatus())) {
            room.initGame();
            broadcastGameStart(roomNo);
        }
    }

    private void handleLeaveRoom(SocketIOClient client, Map<String, Object> data) {
        String sessionId = client.getSessionId().toString();
        Long userId = SocketSessionManager.getClientUserId(sessionId);
        if (userId == null) return;

        String roomNo = (String) data.get("roomNo");
        if (roomNo == null) return;

        GameRoom room = roomManager.getRoom(roomNo);
        if (room != null && "PLAYING".equals(room.getGameStatus())) {
            int playerIndex = room.getPlayerIndex(userId);
            if (playerIndex >= 0) {
                Player player = room.getPlayers().get(playerIndex);
                if (player.getCards() != null && !player.getCards().isEmpty()) {
                    int scoreChange = -room.getBaseScore() * (player.getIdentity() == 1 ? 6 : 2);
                    userService.updateScore(userId, scoreChange, false);
                    GameRecord record = new GameRecord();
                    record.setRoomNo(roomNo);
                    record.setPlayerId(userId);
                    record.setIdentity(player.getIdentity());
                    record.setIsWin(0);
                    record.setScoreChange(scoreChange);
                    record.setBaseScore(room.getBaseScore());
                    gameRecordService.saveRecord(record);
                }
            }
        }

        roomManager.removePlayerFromRoom(roomNo, userId);
        client.leaveRoom(roomNo);
        SocketSessionManager.setUserRoom(sessionId, null);
        client.sendEvent("left_room", roomNo);

        GameRoom updatedRoom = roomManager.getRoom(roomNo);
        if (updatedRoom != null) {
            broadcastRoomUpdate(roomNo);
        }
    }

    private void handleReadyGame(SocketIOClient client, Map<String, Object> data) {
        String sessionId = client.getSessionId().toString();
        Long userId = SocketSessionManager.getClientUserId(sessionId);
        if (userId == null) return;

        String roomNo = (String) data.get("roomNo");
        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null) return;

        if (room.isFull() && "WAITING".equals(room.getGameStatus())) {
            room.initGame();
            broadcastGameStart(roomNo);
        }
    }

    private void handleGrabLandlord(SocketIOClient client, Map<String, Object> data) {
        String sessionId = client.getSessionId().toString();
        Long userId = SocketSessionManager.getClientUserId(sessionId);
        if (userId == null) return;

        String roomNo = (String) data.get("roomNo");
        boolean grab = Boolean.TRUE.equals(data.get("grab"));
        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null || !"GRABBING".equals(room.getGameStatus())) return;

        int playerIndex = room.getPlayerIndex(userId);
        if (playerIndex < 0) return;

        boolean finished = room.grabLandlord(playerIndex, grab);
        broadcastGrabState(roomNo, playerIndex, grab);

        if (finished && room.getLandlordIndex() >= 0) {
            broadcastLandlordDecided(roomNo);
        }
    }

    private void handlePlayCards(SocketIOClient client, Map<String, Object> data) {
        String sessionId = client.getSessionId().toString();
        Long userId = SocketSessionManager.getClientUserId(sessionId);
        if (userId == null) return;

        String roomNo = (String) data.get("roomNo");
        @SuppressWarnings("unchecked")
        List<Map<String, Object>> cardsData = (List<Map<String, Object>>) data.get("cards");
        if (cardsData == null) return;

        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null || !"PLAYING".equals(room.getGameStatus())) return;

        int playerIndex = room.getPlayerIndex(userId);
        if (playerIndex < 0) return;

        List<Card> cards = cardsData.stream().map(cd -> {
            Card c = new Card();
            c.setSuit((Integer) cd.get("suit"));
            c.setRank((Integer) cd.get("rank"));
            c.setCode((String) cd.get("code"));
            c.setDisplay((String) cd.get("display"));
            return c;
        }).collect(Collectors.toList());

        boolean success = room.playCards(playerIndex, cards);
        if (!success) {
            client.sendEvent("play_error", "出牌不合法");
            return;
        }

        broadcastPlayCards(roomNo, playerIndex, cards);

        if ("FINISHED".equals(room.getGameStatus())) {
            handleGameOver(roomNo);
        }
    }

    private void handlePassCards(SocketIOClient client, Map<String, Object> data) {
        String sessionId = client.getSessionId().toString();
        Long userId = SocketSessionManager.getClientUserId(sessionId);
        if (userId == null) return;

        String roomNo = (String) data.get("roomNo");
        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null || !"PLAYING".equals(room.getGameStatus())) return;

        int playerIndex = room.getPlayerIndex(userId);
        if (playerIndex < 0) return;

        boolean success = room.pass(playerIndex);
        if (!success) {
            client.sendEvent("play_error", "无法不出");
            return;
        }

        broadcastPassCards(roomNo, playerIndex);
    }

    private void handleHintCards(SocketIOClient client, Map<String, Object> data, com.corundumstudio.socketio.AckRequest ackSender) {
        String sessionId = client.getSessionId().toString();
        Long userId = SocketSessionManager.getClientUserId(sessionId);
        if (userId == null) return;

        String roomNo = (String) data.get("roomNo");
        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null) return;

        int playerIndex = room.getPlayerIndex(userId);
        if (playerIndex < 0) return;

        Player player = room.getPlayers().get(playerIndex);
        List<Card> hintCards = com.ddz.game.CardUtils.findHint(
                player.getCards(),
                room.getLastPlayCards(),
                room.getLastPlayType()
        );

        client.sendEvent("hint_cards_result", hintCards);
    }

    private void handleChatMessage(SocketIOClient client, Map<String, Object> data) {
        String sessionId = client.getSessionId().toString();
        Long userId = SocketSessionManager.getClientUserId(sessionId);
        if (userId == null) return;

        String roomNo = (String) data.get("roomNo");
        String message = (String) data.get("message");
        User user = userService.getUserById(userId);
        if (user == null) return;

        Map<String, Object> msgData = new HashMap<>();
        msgData.put("userId", userId);
        msgData.put("nickname", user.getNickname());
        msgData.put("message", message);
        msgData.put("time", System.currentTimeMillis());

        server.getRoomOperations(roomNo).sendEvent("chat_message", msgData);
    }

    private void handleGameOver(String roomNo) {
        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null) return;

        boolean landlordWin = room.isLandlordWin();
        for (int i = 0; i < room.getPlayers().size(); i++) {
            Player player = room.getPlayers().get(i);
            int scoreChange = room.calculateScoreChange(i);
            boolean isWin = (player.getIdentity() == 1 && landlordWin)
                    || (player.getIdentity() == 0 && !landlordWin);

            userService.updateScore(player.getUserId(), scoreChange, isWin);
            player.setScore(player.getScore() + scoreChange);

            GameRecord record = new GameRecord();
            record.setRoomNo(roomNo);
            record.setPlayerId(player.getUserId());
            record.setIdentity(player.getIdentity());
            record.setIsWin(isWin ? 1 : 0);
            record.setScoreChange(scoreChange);
            record.setBaseScore(room.getBaseScore());
            gameRecordService.saveRecord(record);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("landlordWin", landlordWin);
        result.put("winnerIndex", room.getWinnerIndex());
        result.put("players", room.getPlayers().stream().map(p -> {
            Map<String, Object> pmap = new HashMap<>();
            pmap.put("userId", p.getUserId());
            pmap.put("nickname", p.getNickname());
            pmap.put("identity", p.getIdentity());
            pmap.put("scoreChange", room.calculateScoreChange(room.getPlayers().indexOf(p)));
            pmap.put("score", p.getScore());
            return pmap;
        }).collect(Collectors.toList()));
        result.put("bottomCards", room.getBottomCards());

        server.getRoomOperations(roomNo).sendEvent("game_over", result);
    }

    private void broadcastRoomUpdate(String roomNo) {
        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null) return;
        for (SocketIOClient client : server.getRoomOperations(roomNo).getClients()) {
            Long userId = SocketSessionManager.getClientUserId(client.getSessionId().toString());
            if (userId != null) {
                client.sendEvent("room_state", buildRoomState(room, userId));
            }
        }
    }

    private Map<String, Object> buildRoomState(GameRoom room, Long userId) {
        Map<String, Object> state = new HashMap<>();
        state.put("roomNo", room.getRoomNo());
        state.put("baseScore", room.getBaseScore());
        state.put("gameStatus", room.getGameStatus());
        state.put("landlordIndex", room.getLandlordIndex());
        state.put("currentPlayerIndex", room.getCurrentPlayerIndex());
        state.put("bottomCards", room.getBottomCards());
        state.put("lastPlayCards", room.getLastPlayCards());
        state.put("lastPlayIndex", room.getLastPlayIndex());
        state.put("lastPlayType", room.getLastPlayType() != null ? room.getLastPlayType().name() : null);

        int myIndex = room.getPlayerIndex(userId);
        state.put("myIndex", myIndex);

        List<Map<String, Object>> playerStates = new ArrayList<>();
        for (int i = 0; i < room.getPlayers().size(); i++) {
            Player p = room.getPlayers().get(i);
            Map<String, Object> ps = new HashMap<>();
            ps.put("userId", p.getUserId());
            ps.put("nickname", p.getNickname());
            ps.put("avatar", p.getAvatar());
            ps.put("score", p.getScore());
            ps.put("identity", p.getIdentity());
            ps.put("isOnline", p.getIsOnline());
            ps.put("seatIndex", i);
            ps.put("cardCount", p.getCardCount());
            if (i == myIndex) {
                ps.put("cards", p.getCards());
            } else {
                ps.put("cards", new ArrayList<>());
            }
            playerStates.add(ps);
        }
        state.put("players", playerStates);

        if ("GRABBING".equals(room.getGameStatus())) {
            state.put("grabOrder", room.getGrabOrder());
            state.put("currentGrabIndex", room.getCurrentGrabIndex());
        }

        return state;
    }

    private void broadcastGameStart(String roomNo) {
        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null) return;
        broadcastRoomUpdate(roomNo);
    }

    private void broadcastGrabState(String roomNo, int playerIndex, boolean grab) {
        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("playerIndex", playerIndex);
        data.put("grab", grab);
        data.put("currentGrabIndex", room.getCurrentGrabIndex());
        server.getRoomOperations(roomNo).sendEvent("grab_update", data);
        broadcastRoomUpdate(roomNo);
    }

    private void broadcastLandlordDecided(String roomNo) {
        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("landlordIndex", room.getLandlordIndex());
        data.put("bottomCards", room.getBottomCards());
        server.getRoomOperations(roomNo).sendEvent("landlord_decided", data);
        broadcastRoomUpdate(roomNo);
    }

    private void broadcastPlayCards(String roomNo, int playerIndex, List<Card> cards) {
        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("playerIndex", playerIndex);
        data.put("cards", cards);
        data.put("cardType", room.getLastPlayType() != null ? room.getLastPlayType().name() : null);
        data.put("currentPlayerIndex", room.getCurrentPlayerIndex());
        data.put("passCount", room.getPassCount());
        server.getRoomOperations(roomNo).sendEvent("play_cards", data);
        broadcastRoomUpdate(roomNo);
    }

    private void broadcastPassCards(String roomNo, int playerIndex) {
        GameRoom room = roomManager.getRoom(roomNo);
        if (room == null) return;
        Map<String, Object> data = new HashMap<>();
        data.put("playerIndex", playerIndex);
        data.put("currentPlayerIndex", room.getCurrentPlayerIndex());
        data.put("passCount", room.getPassCount());
        server.getRoomOperations(roomNo).sendEvent("pass_cards", data);
        broadcastRoomUpdate(roomNo);
    }
}
