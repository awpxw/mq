package com.example.enums;

import lombok.Getter;

@Getter
public enum RetryStatus {

    UN_RETRIED(0, "未重试"),

    RETRIED(1, "已重试"),

    RETRY_SUCCESS(2, "重试成功"),

    RETRY_FAILED(3, "重试失败");


    private final Integer code;

    private final String desc;

    RetryStatus(Integer code, String desc) {

        this.code = code;

        this.desc = desc;

    }

}