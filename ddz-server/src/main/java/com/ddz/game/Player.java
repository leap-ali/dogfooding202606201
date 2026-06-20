package com.ddz.game;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Player {

    private Long userId;
    private String username;
    private String nickname;
    private String avatar;
    private Integer score;
    private List<Card> cards;
    private Integer identity;
    private Boolean isOnline;
    private Integer seatIndex;

    public Player() {
        this.cards = new ArrayList<>();
        this.identity = 0;
        this.isOnline = true;
    }

    public Player(Long userId, String nickname, Integer score) {
        this();
        this.userId = userId;
        this.nickname = nickname;
        this.score = score;
    }

    public int getCardCount() {
        return cards != null ? cards.size() : 0;
    }

    public void removeCards(List<Card> toRemove) {
        if (cards == null || toRemove == null) return;
        for (Card card : toRemove) {
            cards.removeIf(c -> c.getCode().equals(card.getCode()));
        }
    }

    public boolean hasCards(List<Card> toCheck) {
        if (cards == null || toCheck == null) return false;
        for (Card card : toCheck) {
            boolean found = cards.stream().anyMatch(c -> c.getCode().equals(card.getCode()));
            if (!found) return false;
        }
        return true;
    }
}
