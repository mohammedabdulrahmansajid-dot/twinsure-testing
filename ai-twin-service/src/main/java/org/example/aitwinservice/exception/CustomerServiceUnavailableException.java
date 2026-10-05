package org.example.aitwinservice.exception;

public class CustomerServiceUnavailableException
        extends RuntimeException {

    public CustomerServiceUnavailableException(
            String message) {

        super(message);
    }
}