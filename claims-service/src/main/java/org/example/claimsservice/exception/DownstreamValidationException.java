package org.example.claimsservice.exception;

public class DownstreamValidationException
        extends RuntimeException {

    public DownstreamValidationException(
            String message) {

        super(message);
    }
}