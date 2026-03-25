package com.example.enums;

import lombok.Getter;

@Getter
public enum TaskType {

    TASK(0, "task"),

    BAK(1, "bak");

    private final Integer code;

    private final String exchange;

    TaskType(Integer code, String exchange) {

        this.code = code;

        this.exchange = exchange;

    }

    public static String fromCode(Integer code) {

        if (code == null) {
            return null;
        }
        for (TaskType type : values()) {
            if (type.code.equals(code)) {
                return type.getExchange();
            }
        }
        return null;

    }

    public static Boolean isTask(String taskType) {
        return TASK.getCode() == Integer.parseInt(taskType);
    }

    public static Boolean isBak(String taskType) {
        return BAK.getCode() == Integer.parseInt(taskType);
    }

}