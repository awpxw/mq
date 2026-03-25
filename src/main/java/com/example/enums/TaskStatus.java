package com.example.enums;

import lombok.Getter;

@Getter
public enum TaskStatus {

    PENDING(0, "待处理"),

    PROCESSING(1, "处理中"),

    SUCCESS(2, "成功"),

    FAILED(3, "失败"),

    CANCELLED(4, "已取消"),

    DEAD_LETTER(5, "死信");

    private final Integer code;

    private final String desc;

    TaskStatus(Integer code, String desc) {

        this.code = code;

        this.desc = desc;

    }

    public static TaskStatus fromCode(Integer code) {

        for (TaskStatus status : values()) {
            if (status.code.equals(code)) {
                return status;
            }
        }
        return null;

    }

}