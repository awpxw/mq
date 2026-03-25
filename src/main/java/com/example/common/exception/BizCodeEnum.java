package com.example.common.exception;

import lombok.Getter;

@Getter
public enum BizCodeEnum {

    SUCCESS(200, "成功"),

    FAILED(500, "失败");

    private final int code;

    private final String message;

    BizCodeEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

}