package org.example.claimsservice.exception;

public class DuplicateClaimException
        extends RuntimeException {

    public DuplicateClaimException(
            String message) {

        super(message);
    }
}