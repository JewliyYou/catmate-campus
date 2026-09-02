package com.catmate.common;

import java.time.LocalDateTime;
import java.util.Map;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(ApiException.class)
    public ResponseEntity<?> handleApi(ApiException e) {
        return ResponseEntity.status(e.getStatus()).body(Map.of("message", e.getMessage(), "time", LocalDateTime.now()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<?> handleValidation(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream().findFirst().map(x -> x.getDefaultMessage()).orElse("请求参数不完整");
        return ResponseEntity.badRequest().body(Map.of("message", message, "time", LocalDateTime.now()));
    }
}
