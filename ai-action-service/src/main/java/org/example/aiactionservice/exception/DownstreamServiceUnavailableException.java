package org.example.aiactionservice.exception;

public class DownstreamServiceUnavailableException
        extends RuntimeException {

    public DownstreamServiceUnavailableException(
            String message) {

        super(message);
    }
}