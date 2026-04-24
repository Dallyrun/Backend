package com.inseong.dallyrun.backend.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

/**
 * 회원 연령대.
 *
 * <p>와이어 포맷은 숫자(20/30/40/50/60) 이고 DB 는 enum 이름(TWENTIES 등)을 저장한다.
 * 60 은 "60대 이상" 을 의미한다.
 */
public enum AgeBracket {
    TWENTIES(20),
    THIRTIES(30),
    FORTIES(40),
    FIFTIES(50),
    SIXTIES_OR_ABOVE(60);

    private final int value;

    AgeBracket(int value) {
        this.value = value;
    }

    @JsonValue
    public int getValue() {
        return value;
    }

    @JsonCreator
    public static AgeBracket fromValue(int value) {
        for (AgeBracket bracket : values()) {
            if (bracket.value == value) {
                return bracket;
            }
        }
        throw new IllegalArgumentException("연령대는 20, 30, 40, 50, 60 중 하나여야 합니다.");
    }
}
