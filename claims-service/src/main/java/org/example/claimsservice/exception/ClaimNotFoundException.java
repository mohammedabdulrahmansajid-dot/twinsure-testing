package org.example.claimsservice.exception;

public class ClaimNotFoundException
        extends RuntimeException {

    public ClaimNotFoundException(
            String message) {

        super(message);
    }
}