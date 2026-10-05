package org.example.notificationservice.exception;

public class ResourceAccessDeniedException
        extends RuntimeException {

    public ResourceAccessDeniedException(
            String message) {

        super(message);
    }
}