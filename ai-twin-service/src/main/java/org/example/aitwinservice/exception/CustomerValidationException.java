package org.example.aitwinservice.exception;

public class CustomerValidationException
        extends RuntimeException {

    public CustomerValidationException(String message) {
        super(message);
    }
}