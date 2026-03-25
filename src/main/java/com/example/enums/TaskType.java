package com.example.enums;

import lombok.Getter;

@Getter
public enum TaskType {

    EMAIL("EMAIL", "邮件发送任务"),

    SMS("SMS", "短信发送任务"),

    CALC("CALC", "计算任务"),

    REPORT("REPORT", "报表生成任务"),

    BACKUP("BACKUP", "备份任务");

    private final String code;

    private final String desc;

    TaskType(String code, String desc) {

        this.code = code;

        this.desc = desc;

    }

    public static TaskType fromCode(String code) {

        if (code == null) {
            return null;
        }
        for (TaskType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;

    }

}