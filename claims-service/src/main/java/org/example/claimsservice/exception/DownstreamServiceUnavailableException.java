package org.example.claimsservice.exception;

public class DownstreamServiceUnavailableException
        extends RuntimeException {

    public DownstreamServiceUnavailableException(
            String message) {

        super(message);
    }
}