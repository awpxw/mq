package com.example.common.exception;

import lombok.Getter;

@Getter
public class BizException extends RuntimeException {

    private final int code;

    private final String msg;

    public BizException(int code, String msg) {
        this.code = code;
        this.msg = msg;
    }

}