package com.ddz.game;

public enum CardType {

    PASS(0, "不出"),
    SINGLE(1, "单张"),
    PAIR(2, "对子"),
    TRIPLE(3, "三张"),
    TRIPLE_ONE(4, "三带一"),
    STRAIGHT(5, "顺子"),
    STRAIGHT_PAIR(6, "连对"),
    PLANE(7, "飞机"),
    PLANE_ONE(8, "飞机带单"),
    BOMB(9, "炸弹"),
    ROCKET(10, "王炸");

    private final int priority;
    private final String name;

    CardType(int priority, String name) {
        this.priority = priority;
        this.name = name;
    }

    public int getPriority() {
        return priority;
    }

    public String getName() {
        return name;
    }
}
