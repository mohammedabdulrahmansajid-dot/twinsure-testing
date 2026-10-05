package org.example.claimsservice.exception;

public class ClaimEligibilityException
        extends RuntimeException {

    public ClaimEligibilityException(
            String message) {

        super(message);
    }
}