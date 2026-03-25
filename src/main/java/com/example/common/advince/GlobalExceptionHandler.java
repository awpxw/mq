package com.example.common.advince;


import com.example.common.exception.BizException;
import com.example.common.exception.Result;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BizException.class)
    public Result<?> handleBizException(BizException e, HttpServletResponse response) {
        log.warn("业务异常: {} ", e.getMessage());
        response.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());  // 500
        return Result.fail();
    }

}