package org.example.aiactionservice.exception;

public class ResourceAccessDeniedException
        extends RuntimeException {

    public ResourceAccessDeniedException(
            String message) {

        super(message);
    }
}