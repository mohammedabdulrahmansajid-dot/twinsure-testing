package org.example.insurancepolicyservice.exception;

public class PolicyApplicationNotFoundException
        extends RuntimeException {

    public PolicyApplicationNotFoundException(
            String message) {

        super(message);
    }
}