package com.fy.weblog.handler;

import com.fy.weblog.dto.Result;
import com.fy.weblog.exception.BusinessException;
import com.fy.weblog.utils.ErrorCode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

//全局异常处理器
@RestControllerAdvice
public class GlobalExceptionHandler{

    // 业务异常（自定义）
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<Result<?>> handleBusinessException(BusinessException e) {
        return ResponseEntity
                .status(e.getHttpStatus())  // 设置 HTTP 状态码
                .body(Result.fail(e.getCode(), e.getMessage()));
    }

    // 系统异常（兜底）
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Result<?>> handleException(Exception e) {
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Result.fail(ErrorCode.SYSTEM_ERROR, "系统错误: " + e.getMessage()));
    }
}