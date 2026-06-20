package com.ddz.game;

import lombok.Data;
import java.util.ArrayList;
import java.util.List;

@Data
public class Card {

    private Integer suit;
    private Integer rank;
    private String code;
    private String display;

    public Card() {}

    public Card(Integer suit, Integer rank) {
        this.suit = suit;
        this.rank = rank;
        this.code = rank + "_" + suit;
        this.display = generateDisplay(suit, rank);
    }

    private static final String[] SUIT_SYMBOLS = {"♠", "♥", "♣", "♦"};
    private static final String[] RANK_NAMES = {"", "", "", "3", "4", "5", "6", "7", "8", "9", "10", "J", "Q", "K", "A", "2", "小王", "大王"};

    private String generateDisplay(int suit, int rank) {
        if (rank == 14) {
            return "小王";
        }
        if (rank == 15) {
            return "大王";
        }
        return SUIT_SYMBOLS[suit] + RANK_NAMES[rank];
    }

    public static List<Card> generateAllCards() {
        List<Card> cards = new ArrayList<>();
        for (int rank = 3; rank <= 15; rank++) {
            if (rank == 14) {
                cards.add(new Card(4, 14));
            } else if (rank == 15) {
                cards.add(new Card(5, 15));
            } else {
                for (int suit = 0; suit < 4; suit++) {
                    cards.add(new Card(suit, rank));
                }
            }
        }
        return cards;
    }

    public int compareTo(Card other) {
        return Integer.compare(this.rank, other.rank);
    }
}
