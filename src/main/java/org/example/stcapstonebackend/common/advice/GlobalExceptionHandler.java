package org.example.stcapstonebackend.common.advice;

import org.example.stcapstonebackend.common.exception.CommentNotFoundException;
import org.example.stcapstonebackend.debate.exception.DebatePostNotFoundException;
import org.example.stcapstonebackend.findTeam.exception.DuplicateAcceptanceException;
import org.example.stcapstonebackend.findTeam.exception.FindTeamPostNotFoundException;
import org.example.stcapstonebackend.findTeam.exception.FindTeamRequestNotFoundException;
import org.example.stcapstonebackend.findTeam.exception.InvalidTagSelectionException;
import org.example.stcapstonebackend.user.exception.DuplicateEmailException;
import org.example.stcapstonebackend.user.exception.InvalidCredentialsException;
import org.example.stcapstonebackend.user.exception.UserNotFoundException;
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

    @ExceptionHandler(CommentNotFoundException.class)
    public ResponseEntity<String> handleCommentNotFoundException(CommentNotFoundException e){
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<String> handleDuplicateEmailException(DuplicateEmailException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<String> handleInvalidCredentialsException(InvalidCredentialsException e) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(e.getMessage());
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<String> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(FindTeamPostNotFoundException.class)
    public ResponseEntity<String> handleFindTeamPostNotFoundException(FindTeamPostNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(FindTeamRequestNotFoundException.class)
    public ResponseEntity<String> handleFindTeamRequestNotFoundException(FindTeamRequestNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(e.getMessage());
    }

    @ExceptionHandler(InvalidTagSelectionException.class)
    public ResponseEntity<String> handleInvalidTagSelectionException(InvalidTagSelectionException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(e.getMessage());
    }

    @ExceptionHandler(DuplicateAcceptanceException.class)
    public ResponseEntity<String> handleDuplicateAcceptanceException(DuplicateAcceptanceException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(e.getMessage());
    }
}
