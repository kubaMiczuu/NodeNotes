package org.jakubmiczek.nodenotes.exception;

import org.jakubmiczek.nodenotes.controller.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(TaskDoesNotExistException.class)
    public ResponseEntity<ApiErrorResponse> handleTaskDoesNotExistException(TaskDoesNotExistException ex) {
        ApiErrorResponse error =  new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), List.of(ex.getMessage()), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(TaskAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleTaskAccessDeniedException(TaskAccessDeniedException ex) {
        ApiErrorResponse error = new ApiErrorResponse(HttpStatus.FORBIDDEN.value(), List.of(ex.getMessage()), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(error);
    }

    @ExceptionHandler(SubItemDoesNotExistException.class)
    public ResponseEntity<ApiErrorResponse> handleSubItemDoesNotExistException(SubItemDoesNotExistException ex) {
        ApiErrorResponse error =  new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), List.of(ex.getMessage()), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<ApiErrorResponse> handleUserAlreadyExistException(UserAlreadyExistException ex) {
        ApiErrorResponse error = new ApiErrorResponse(HttpStatus.CONFLICT.value(), List.of(ex.getMessage()), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(error);
    }

    @ExceptionHandler(UserDoesNotExistException.class)
    public ResponseEntity<ApiErrorResponse> handleUserDoesNotExistException(UserDoesNotExistException ex) {
        ApiErrorResponse error = new ApiErrorResponse(HttpStatus.NOT_FOUND.value(), List.of(ex.getMessage()), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiErrorResponse> handleBadCredentialsException(BadCredentialsException ex) {
        ApiErrorResponse error = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), List.of(ex.getMessage()), LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValidException(MethodArgumentNotValidException ex) {

        List<String> fieldErrors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": "+error.getDefaultMessage())
                .toList();

        ApiErrorResponse error = new ApiErrorResponse(HttpStatus.BAD_REQUEST.value(), fieldErrors, LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }
}
