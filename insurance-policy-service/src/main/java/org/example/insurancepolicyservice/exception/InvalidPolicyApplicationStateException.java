package org.example.insurancepolicyservice.exception;

public class InvalidPolicyApplicationStateException
        extends RuntimeException {

    public InvalidPolicyApplicationStateException(
            String message) {

        super(message);
    }
}