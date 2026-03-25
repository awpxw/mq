package com.example.enums;

import lombok.Getter;

@Getter
public enum IdempotentStatus {

    PROCESSING(0, "处理中"),

    PROCESSED(1, "已处理"),

    FAILED(2, "处理失败");

    private final Integer code;

    private final String desc;

    IdempotentStatus(Integer code, String desc) {

        this.code = code;

        this.desc = desc;

    }

}