package org.example.claimsservice.exception;

public class InvalidClaimStateException
        extends RuntimeException {

    public InvalidClaimStateException(
            String message) {

        super(message);
    }
}