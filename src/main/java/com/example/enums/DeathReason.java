package com.example.enums;

import lombok.Getter;

@Getter
public enum DeathReason {

    REJECTED("0", "REJECTED"),

    EXPIRED("1", "EXPIRED"),

    DELIVERY_LIMIT("2", "DELIVERY_LIMIT");

    private final String code;

    private final String desc;

    DeathReason(String code, String desc) {

        this.code = code;

        this.desc = desc;

    }

}