package org.example.claimsservice.exception;

public class DuplicateIncidentException
        extends RuntimeException {

    public DuplicateIncidentException(
            String message) {

        super(message);
    }
}