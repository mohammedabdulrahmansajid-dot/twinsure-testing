package org.example.notificationservice.utility;

// Runs Jakarta validation for DTOs received by functional handlers.
// Router and Handler endpoints use this utility because automatic
// Controller-style validation is not available.

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validator;
import org.example.notificationservice.exception.InvalidRequestException;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.Set;
import java.util.stream.Collectors;

@Component
public class RequestValidator {

    private final Validator validator;

    public RequestValidator(
            Validator validator) {

        this.validator = validator;
    }

    public <T> Mono<T> validate(
            T request) {

        Set<ConstraintViolation<T>> violations =
                validator.validate(request);

        if (violations.isEmpty()) {
            return Mono.just(request);
        }

        String message =
                violations.stream()
                        .map(violation ->
                                violation.getPropertyPath()
                                        + ": "
                                        + violation.getMessage()
                        )
                        .sorted()
                        .collect(
                                Collectors.joining(", ")
                        );

        return Mono.error(
                new InvalidRequestException(
                        message
                )
        );
    }
}