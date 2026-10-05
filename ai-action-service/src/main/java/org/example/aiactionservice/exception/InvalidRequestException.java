package org.example.aiactionservice.exception;

public class InvalidRequestException
        extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}