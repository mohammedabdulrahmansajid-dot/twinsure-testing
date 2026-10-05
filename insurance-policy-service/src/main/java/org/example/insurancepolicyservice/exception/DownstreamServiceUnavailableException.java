package org.example.insurancepolicyservice.exception;

public class DownstreamServiceUnavailableException
        extends RuntimeException {

    public DownstreamServiceUnavailableException(
            String message) {

        super(message);
    }
}