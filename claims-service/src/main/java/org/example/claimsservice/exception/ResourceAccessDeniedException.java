package org.example.claimsservice.exception;

public class ResourceAccessDeniedException
        extends RuntimeException {

    public ResourceAccessDeniedException(
            String message) {

        super(message);
    }
}