package org.example.identityservice.exception;

import org.example.identityservice.dto.response.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import org.springframework.security.core.AuthenticationException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;
import org.example.identityservice.exception.InvalidStaffRoleException;
import org.example.identityservice.exception.UserNotFoundException;
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            UsernameAlreadyExistsException.class
    )
    public Mono<ResponseEntity<ErrorResponseDTO>>
    handleUsernameAlreadyExists(
            UsernameAlreadyExistsException exception,
            ServerWebExchange exchange) {

        ErrorResponseDTO errorResponse =
                new ErrorResponseDTO(
                        LocalDateTime.now(),
                        HttpStatus.CONFLICT.value(),
                        HttpStatus.CONFLICT
                                .getReasonPhrase(),
                        exception.getMessage(),
                        exchange.getRequest()
                                .getPath()
                                .value()
                );

        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.CONFLICT)
                        .body(errorResponse)
        );
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>>
    handleValidationException(
            WebExchangeBindException exception,
            ServerWebExchange exchange) {

        String validationMessage =
                exception.getFieldErrors()
                        .stream()
                        .map(fieldError ->
                                fieldError.getField()
                                        + ": "
                                        + fieldError
                                        .getDefaultMessage()
                        )
                        .collect(
                                Collectors.joining(", ")
                        );

        ErrorResponseDTO errorResponse =
                new ErrorResponseDTO(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST
                                .getReasonPhrase(),
                        validationMessage,
                        exchange.getRequest()
                                .getPath()
                                .value()
                );

        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(errorResponse)
        );
    }



    @ExceptionHandler(AuthenticationException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleAuthenticationException(
            AuthenticationException exception,
            ServerWebExchange exchange) {

        ErrorResponseDTO errorResponse =
                new ErrorResponseDTO(
                        LocalDateTime.now(),
                        HttpStatus.UNAUTHORIZED.value(),
                        HttpStatus.UNAUTHORIZED.getReasonPhrase(),
                        "Invalid username or password",
                        exchange.getRequest()
                                .getPath()
                                .value()
                );

        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.UNAUTHORIZED)
                        .body(errorResponse)
        );
    }



    @ExceptionHandler(UserNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleUserNotFound(
            UserNotFoundException exception,
            ServerWebExchange exchange) {

        ErrorResponseDTO errorResponse =
                new ErrorResponseDTO(
                        LocalDateTime.now(),
                        HttpStatus.NOT_FOUND.value(),
                        HttpStatus.NOT_FOUND.getReasonPhrase(),
                        exception.getMessage(),
                        exchange.getRequest()
                                .getPath()
                                .value()
                );

        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.NOT_FOUND)
                        .body(errorResponse)
        );
    }

    @ExceptionHandler(InvalidStaffRoleException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>> handleInvalidStaffRole(
            InvalidStaffRoleException exception,
            ServerWebExchange exchange) {

        ErrorResponseDTO errorResponse =
                new ErrorResponseDTO(
                        LocalDateTime.now(),
                        HttpStatus.BAD_REQUEST.value(),
                        HttpStatus.BAD_REQUEST.getReasonPhrase(),
                        exception.getMessage(),
                        exchange.getRequest()
                                .getPath()
                                .value()
                );

        return Mono.just(
                ResponseEntity
                        .status(HttpStatus.BAD_REQUEST)
                        .body(errorResponse)
        );
    }


    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponseDTO>>
    handleUnexpectedException(
            Exception exception,
            ServerWebExchange exchange) {

        ErrorResponseDTO errorResponse =
                new ErrorResponseDTO(
                        LocalDateTime.now(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                                .value(),
                        HttpStatus.INTERNAL_SERVER_ERROR
                                .getReasonPhrase(),
                        "An unexpected error occurred",
                        exchange.getRequest()
                                .getPath()
                                .value()
                );

        return Mono.just(
                ResponseEntity
                        .status(
                                HttpStatus.INTERNAL_SERVER_ERROR
                        )
                        .body(errorResponse)
        );
    }
}