package org.example.insurancepolicyservice.exception;

public class DuplicatePolicyApplicationException
        extends RuntimeException {

    public DuplicatePolicyApplicationException(
            String message) {

        super(message);
    }
}