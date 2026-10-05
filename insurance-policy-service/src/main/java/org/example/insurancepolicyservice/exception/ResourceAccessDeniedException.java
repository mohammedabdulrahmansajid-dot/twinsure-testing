package org.example.insurancepolicyservice.exception;

public class ResourceAccessDeniedException
        extends RuntimeException {

    public ResourceAccessDeniedException(
            String message) {

        super(message);
    }
}