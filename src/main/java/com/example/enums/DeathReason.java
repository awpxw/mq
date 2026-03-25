package com.example.enums;

import lombok.Getter;

@Getter
public enum DeathReason {

    REJECTED("REJECTED", "消费者拒绝"),

    EXPIRED("EXPIRED", "消息过期"),

    DELIVERY_LIMIT("DELIVERY_LIMIT", "超过投递次数限制");

    private final String code;

    private final String desc;

    DeathReason(String code, String desc) {

        this.code = code;

        this.desc = desc;

    }

}