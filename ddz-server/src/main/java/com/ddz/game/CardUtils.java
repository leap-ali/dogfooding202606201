package com.ddz.game;

import java.util.*;
import java.util.stream.Collectors;

public class CardUtils {

    public static void shuffle(List<Card> cards) {
        Collections.shuffle(cards, new Random());
    }

    public static void sortCards(List<Card> cards) {
        cards.sort((a, b) -> Integer.compare(b.getRank(), a.getRank()));
    }

    public static CardType analyzeType(List<Card> cards) {
        if (cards == null || cards.isEmpty()) {
            return null;
        }
        int size = cards.size();
        Map<Integer, List<Card>> rankMap = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank, LinkedHashMap::new, Collectors.toList()));
        List<Map.Entry<Integer, List<Card>>> sortedEntries = new ArrayList<>(rankMap.entrySet());
        sortedEntries.sort((a, b) -> {
            int sizeCompare = Integer.compare(b.getValue().size(), a.getValue().size());
            if (sizeCompare != 0) return sizeCompare;
            return Integer.compare(b.getKey(), a.getKey());
        });

        if (size == 2 && cards.stream().allMatch(c -> c.getRank() >= 14)) {
            return CardType.ROCKET;
        }
        if (size == 1) {
            return CardType.SINGLE;
        }
        if (size == 2 && rankMap.size() == 1) {
            return CardType.PAIR;
        }
        if (size == 3 && rankMap.size() == 1 && rankMap.values().iterator().next().size() == 3) {
            return CardType.TRIPLE;
        }
        if (size == 4 && rankMap.size() == 1 && rankMap.values().iterator().next().size() == 4) {
            return CardType.BOMB;
        }
        if (size == 4 && rankMap.size() == 2) {
            for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
                if (entry.getValue().size() == 3) {
                    return CardType.TRIPLE_ONE;
                }
            }
        }
        if (size >= 5 && rankMap.size() == size && !containsJoker(cards) && isConsecutive(rankMap.keySet())) {
            return CardType.STRAIGHT;
        }
        if (size >= 6 && size % 2 == 0 && rankMap.size() == size / 2
                && rankMap.values().stream().allMatch(list -> list.size() == 2)
                && !containsJoker(cards) && isConsecutive(rankMap.keySet())) {
            return CardType.STRAIGHT_PAIR;
        }
        List<Integer> tripleRanks = rankMap.entrySet().stream()
                .filter(e -> e.getValue().size() >= 3)
                .map(Map.Entry::getKey)
                .sorted()
                .collect(Collectors.toList());
        int tripleCount = tripleRanks.size();
        if (tripleCount >= 2 && isConsecutiveInt(tripleRanks)) {
            int planeMain = tripleCount * 3;
            if (size == planeMain) {
                return CardType.PLANE;
            }
            int remaining = size - planeMain;
            if (remaining == tripleCount) {
                return CardType.PLANE_ONE;
            }
        }
        return null;
    }

    private static boolean containsJoker(List<Card> cards) {
        return cards.stream().anyMatch(c -> c.getRank() >= 14);
    }

    private static boolean isConsecutive(Set<Integer> ranks) {
        if (ranks.isEmpty()) return false;
        List<Integer> list = new ArrayList<>(ranks);
        Collections.sort(list);
        if (list.get(0) < 3 || list.get(list.size() - 1) > 13) {
            return false;
        }
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) - list.get(i - 1) != 1) {
                return false;
            }
        }
        return true;
    }

    private static boolean isConsecutiveInt(List<Integer> list) {
        if (list.isEmpty()) return false;
        Collections.sort(list);
        if (list.get(0) < 3 || list.get(list.size() - 1) > 13) {
            return false;
        }
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i) - list.get(i - 1) != 1) {
                return false;
            }
        }
        return true;
    }

    public static boolean canBeat(List<Card> current, List<Card> last, CardType lastType) {
        if (current == null || current.isEmpty()) {
            return false;
        }
        CardType currentType = analyzeType(current);
        if (currentType == null) {
            return false;
        }
        if (currentType == CardType.ROCKET) {
            return true;
        }
        if (last == null || last.isEmpty() || lastType == CardType.PASS) {
            return true;
        }
        if (currentType == CardType.BOMB && lastType != CardType.BOMB && lastType != CardType.ROCKET) {
            return true;
        }
        if (currentType != lastType) {
            return false;
        }
        return getMainRank(current, currentType) > getMainRank(last, lastType);
    }

    public static int getMainRank(List<Card> cards, CardType type) {
        if (cards == null || cards.isEmpty()) return 0;
        Map<Integer, List<Card>> rankMap = cards.stream()
                .collect(Collectors.groupingBy(Card::getRank));
        switch (type) {
            case SINGLE:
            case PAIR:
            case TRIPLE:
            case BOMB:
                return cards.get(0).getRank();
            case TRIPLE_ONE:
                for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
                    if (entry.getValue().size() == 3) {
                    return entry.getKey();
                }
                }
                return 0;
            case STRAIGHT:
            case STRAIGHT_PAIR:
                return cards.stream().mapToInt(Card::getRank).min().orElse(0);
            case PLANE:
            case PLANE_ONE:
                return rankMap.entrySet().stream()
                        .filter(e -> e.getValue().size() >= 3)
                        .mapToInt(Map.Entry::getKey)
                        .min().orElse(0);
            default:
                return 0;
        }
    }

    public static List<Card> findHint(List<Card> hand, List<Card> lastPlay, CardType lastType) {
        if (lastPlay == null || lastPlay.isEmpty() || lastType == CardType.PASS) {
            List<Card> single = hand.stream()
                    .filter(c -> c.getRank() < 14)
                    .min(Comparator.comparingInt(Card::getRank))
                    .map(Collections::singletonList)
                    .orElse(null);
            return single != null ? new ArrayList<>(single) : null;
        }
        List<List<Card>> candidates = new ArrayList<>();
        findHintsRecursive(hand, lastPlay, lastType, 0, new ArrayList<>(), candidates);
        if (!candidates.isEmpty()) {
            return candidates.get(0);
        }
        List<Card> bomb = findBomb(hand, lastPlay, lastType);
        if (bomb != null) {
            return bomb;
        }
        List<Card> rocket = findRocket(hand);
        return rocket;
    }

    private static void findHintsRecursive(List<Card> hand, List<Card> lastPlay, CardType lastType,
                                            int start, List<Card> selected, List<List<Card>> result) {
        if (!result.isEmpty()) return;
        if (selected.size() == lastPlay.size()) {
            CardType selectedType = analyzeType(selected);
            if (selectedType != null && selectedType == lastType && canBeat(selected, lastPlay, lastType)) {
                result.add(new ArrayList<>(selected));
            }
            return;
        }
        for (int i = start; i < hand.size(); i++) {
            selected.add(hand.get(i));
            findHintsRecursive(hand, lastPlay, lastType, i + 1, selected, result);
            selected.remove(selected.size() - 1);
            if (!result.isEmpty()) break;
        }
    }

    private static List<Card> findBomb(List<Card> hand, List<Card> lastPlay, CardType lastType) {
        Map<Integer, List<Card>> rankMap = hand.stream()
                .collect(Collectors.groupingBy(Card::getRank));
        int lastMainRank = getMainRank(lastPlay, lastType);
        for (Map.Entry<Integer, List<Card>> entry : rankMap.entrySet()) {
            if (entry.getValue().size() == 4) {
                if (lastType != CardType.BOMB || entry.getKey() > lastMainRank) {
                    return new ArrayList<>(entry.getValue());
                }
            }
        }
        return null;
    }

    private static List<Card> findRocket(List<Card> hand) {
        List<Card> jokers = hand.stream().filter(c -> c.getRank() >= 14).collect(Collectors.toList());
        if (jokers.size() == 2) {
            return jokers;
        }
        return null;
    }
}
