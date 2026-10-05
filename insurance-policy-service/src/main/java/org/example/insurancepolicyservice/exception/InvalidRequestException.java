package org.example.insurancepolicyservice.exception;

public class InvalidRequestException
        extends RuntimeException {

    public InvalidRequestException(String message) {
        super(message);
    }
}