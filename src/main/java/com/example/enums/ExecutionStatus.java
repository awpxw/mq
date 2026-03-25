package com.example.enums;

import lombok.Getter;

@Getter
public enum ExecutionStatus {

    START(0, "已开始"),

    SUCCESS(1, "成功"),

    FAILED(2, "失败");

    private final Integer code;

    private final String desc;

    ExecutionStatus(Integer code, String desc) {

        this.code = code;

        this.desc = desc;

    }

}