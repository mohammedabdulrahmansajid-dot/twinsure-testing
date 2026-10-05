package org.example.claimsservice.exception;

public class InvalidRequestException
        extends RuntimeException {

    public InvalidRequestException(
            String message) {

        super(message);
    }
}