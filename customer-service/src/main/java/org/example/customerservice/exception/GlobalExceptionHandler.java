package org.example.customerservice.exception;

import org.example.customerservice.dto.response.ErrorResponseDTO;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.support.WebExchangeBindException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomerNotFoundException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>>
    handleCustomerNotFound(
            CustomerNotFoundException exception,
            ServerWebExchange exchange) {

        return buildResponse(
                HttpStatus.NOT_FOUND,
                exception.getMessage(),
                exchange
        );
    }

    @ExceptionHandler({
            CustomerProfileAlreadyExistsException.class,
            EmailAlreadyExistsException.class
    })
    public Mono<ResponseEntity<ErrorResponseDTO>>
    handleConflict(
            RuntimeException exception,
            ServerWebExchange exchange) {

        return buildResponse(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                exchange
        );
    }

    @ExceptionHandler(CustomerAccessDeniedException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>>
    handleAccessDenied(
            CustomerAccessDeniedException exception,
            ServerWebExchange exchange) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                exception.getMessage(),
                exchange
        );
    }

    @ExceptionHandler(IdentityServiceException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>>
    handleIdentityServiceFailure(
            IdentityServiceException exception,
            ServerWebExchange exchange) {

        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                exception.getMessage(),
                exchange
        );
    }

    @ExceptionHandler(WebExchangeBindException.class)
    public Mono<ResponseEntity<ErrorResponseDTO>>
    handleValidationFailure(
            WebExchangeBindException exception,
            ServerWebExchange exchange) {

        String message =
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

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                message,
                exchange
        );
    }

    @ExceptionHandler(Exception.class)
    public Mono<ResponseEntity<ErrorResponseDTO>>
    handleUnexpectedFailure(
            Exception exception,
            ServerWebExchange exchange) {

        exception.printStackTrace();

        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred",
                exchange
        );
    }

    private Mono<ResponseEntity<ErrorResponseDTO>>
    buildResponse(
            HttpStatus status,
            String message,
            ServerWebExchange exchange) {

        ErrorResponseDTO errorResponse =
                new ErrorResponseDTO(
                        LocalDateTime.now(),
                        status.value(),
                        status.getReasonPhrase(),
                        message,
                        exchange.getRequest()
                                .getPath()
                                .value()
                );

        return Mono.just(
                ResponseEntity
                        .status(status)
                        .body(errorResponse)
        );
    }
}