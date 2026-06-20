package com.ddz.game;

import lombok.Data;
import java.util.*;

@Data
public class GameRoom {

    private String roomNo;
    private Integer baseScore;
    private List<Player> players;
    private List<Card> bottomCards;
    private Integer landlordIndex;
    private Integer currentPlayerIndex;
    private List<Card> lastPlayCards;
    private CardType lastPlayType;
    private Integer lastPlayIndex;
    private Integer passCount;
    private String gameStatus;
    private List<Integer> grabOrder;
    private Integer currentGrabIndex;
    private Integer grabCount;
    private Long startTime;
    private Long endTime;
    private Integer winnerIndex;
    private Integer winnerType;

    public GameRoom() {
        this.players = new ArrayList<>();
        this.bottomCards = new ArrayList<>();
        this.landlordIndex = -1;
        this.currentPlayerIndex = 0;
        this.lastPlayCards = new ArrayList<>();
        this.lastPlayType = null;
        this.lastPlayIndex = -1;
        this.passCount = 0;
        this.gameStatus = "WAITING";
        this.grabOrder = new ArrayList<>();
        this.currentGrabIndex = 0;
        this.grabCount = 0;
        this.winnerIndex = -1;
        this.winnerType = 0;
    }

    public void addPlayer(Player player) {
        if (players.size() < 3) {
            player.setSeatIndex(players.size());
            players.add(player);
        }
    }

    public void removePlayer(Long userId) {
        players.removeIf(p -> p.getUserId().equals(userId));
    }

    public Player getPlayerByUserId(Long userId) {
        for (Player player : players) {
            if (player.getUserId().equals(userId)) {
                return player;
            }
        }
        return null;
    }

    public int getPlayerIndex(Long userId) {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getUserId().equals(userId)) {
                return i;
            }
        }
        return -1;
    }

    public boolean isFull() {
        return players.size() >= 3;
    }

    public void initGame() {
        this.gameStatus = "GRABBING";
        this.landlordIndex = -1;
        this.lastPlayCards = new ArrayList<>();
        this.lastPlayType = null;
        this.lastPlayIndex = -1;
        this.passCount = 0;
        this.bottomCards = new ArrayList<>();
        this.winnerIndex = -1;
        this.grabCount = 0;
        this.startTime = System.currentTimeMillis();

        for (Player player : players) {
            player.getCards().clear();
            player.setIdentity(0);
        }

        List<Card> allCards = Card.generateAllCards();
        CardUtils.shuffle(allCards);

        for (int i = 0; i < 51; i++) {
            players.get(i % 3).getCards().add(allCards.get(i));
        }
        for (int i = 51; i < 54; i++) {
            bottomCards.add(allCards.get(i));
        }

        for (Player player : players) {
            CardUtils.sortCards(player.getCards());
        }

        Random random = new Random();
        int startIndex = random.nextInt(3);
        grabOrder = new ArrayList<>();
        for (int i = 0; i < 3; i++) {
            grabOrder.add((startIndex + i) % 3);
        }
        currentGrabIndex = 0;
    }

    public boolean grabLandlord(int playerIndex, boolean grab) {
        if (!"GRABBING".equals(gameStatus)) {
            return false;
        }
        if (grabOrder.get(currentGrabIndex) != playerIndex) {
            return false;
        }
        if (grab) {
            landlordIndex = playerIndex;
            grabCount++;
        }
        currentGrabIndex++;

        if (currentGrabIndex >= 3) {
            if (grabCount > 0) {
                setLandlord(landlordIndex);
                return true;
            } else {
                initGame();
                return false;
            }
        }
        return false;
    }

    public void setLandlord(int index) {
        this.landlordIndex = index;
        players.get(index).setIdentity(1);
        for (Card card : bottomCards) {
            players.get(index).getCards().add(card);
        }
        CardUtils.sortCards(players.get(index).getCards());
        this.gameStatus = "PLAYING";
        this.currentPlayerIndex = index;
        this.lastPlayIndex = -1;
        this.passCount = 0;
    }

    public boolean playCards(int playerIndex, List<Card> cards) {
        if (!"PLAYING".equals(gameStatus)) {
            return false;
        }
        if (currentPlayerIndex != playerIndex) {
            return false;
        }
        Player player = players.get(playerIndex);
        if (!player.hasCards(cards)) {
            return false;
        }
        CardType cardType = CardUtils.analyzeType(cards);
        if (cardType == null) {
            return false;
        }
        if (lastPlayCards != null && !lastPlayCards.isEmpty() && lastPlayIndex != playerIndex) {
            if (!CardUtils.canBeat(cards, lastPlayCards, lastPlayType)) {
                return false;
            }
        }

        player.removeCards(cards);
        lastPlayCards = new ArrayList<>(cards);
        lastPlayType = cardType;
        lastPlayIndex = playerIndex;
        passCount = 0;

        if (player.getCards().isEmpty()) {
            gameStatus = "FINISHED";
            winnerIndex = playerIndex;
            endTime = System.currentTimeMillis();
            winnerType = player.getIdentity();
        } else {
            nextPlayer();
        }
        return true;
    }

    public boolean pass(int playerIndex) {
        if (!"PLAYING".equals(gameStatus)) {
            return false;
        }
        if (currentPlayerIndex != playerIndex) {
            return false;
        }
        if (lastPlayCards == null || lastPlayCards.isEmpty() || lastPlayIndex == playerIndex) {
            return false;
        }
        passCount++;
        lastPlayType = CardType.PASS;

        if (passCount >= 2) {
            lastPlayCards = new ArrayList<>();
            lastPlayType = null;
            lastPlayIndex = playerIndex;
            passCount = 0;
        }

        nextPlayer();
        return true;
    }

    public void nextPlayer() {
        currentPlayerIndex = (currentPlayerIndex + 1) % 3;
    }

    public int checkWin() {
        for (int i = 0; i < players.size(); i++) {
            if (players.get(i).getCards().isEmpty()) {
                return i;
            }
        }
        return -1;
    }

    public boolean isLandlordWin() {
        if (winnerIndex < 0) return false;
        return players.get(winnerIndex).getIdentity() == 1;
    }

    public int calculateScoreChange(int playerIndex) {
        if (winnerIndex < 0) return 0;
        boolean isLandlord = players.get(playerIndex).getIdentity() == 1;
        boolean isWin = (isLandlord && isLandlordWin()) || (!isLandlord && !isLandlordWin());
        int multiplier = isLandlord ? 3 : 1;
        return isWin ? baseScore * multiplier : -baseScore * multiplier;
    }

    public List<Player> getPlayers() {
        return players;
    }
}
