package org.example.notificationservice.exception;

public class InvalidRequestException
        extends RuntimeException {

    public InvalidRequestException(
            String message) {

        super(message);
    }
}