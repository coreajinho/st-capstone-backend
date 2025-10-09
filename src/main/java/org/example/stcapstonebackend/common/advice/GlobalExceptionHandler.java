package org.example.stcapstonebackend.common.advice;

import org.example.stcapstonebackend.debate.exception.DebatePostNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            String fieldName = error.getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        // 400 Bad Request 상태 코드와 함께 에러 메시지를 JSON 형태로 반환
        return ResponseEntity.badRequest().body(errors);
    }

    @ExceptionHandler(DebatePostNotFoundException.class)
    public ResponseEntity<String> handlePostNotFoundException(DebatePostNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }
}
